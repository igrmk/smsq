package main

import (
	"bytes"
	"crypto/sha256"
	"database/sql"
	"encoding/base64"
	"encoding/json"
	"errors"
	"fmt"
	"html"
	"net/http"
	"os"
	"os/signal"
	"path"
	"strconv"
	"strings"
	"syscall"
	"time"
	"unicode/utf8"

	tg "github.com/go-telegram-bot-api/telegram-bot-api"
	"github.com/google/tink/go/hybrid"
	"github.com/google/tink/go/tink"
	_ "github.com/mattn/go-sqlite3"
)

var errBlockedByUser = errors.New("blocked by user")

// checkErr panics on an error
func checkErr(err error) {
	if err != nil {
		panic(err)
	}
}

type smsRequest struct {
	Payload string
	Version int
}

type sms struct {
	Key       string `json:"key"`
	ID        int64  `json:"id"`
	Text      string `json:"text"`
	SIM       string `json:"sim"`
	Carrier   string `json:"carrier"`
	Sender    string `json:"sender"`
	Timestamp int64  `json:"timestamp"`
	Offset    int    `json:"offset"`
}

type deliveryResult int

//go:generate jsonenums -type=deliveryResult
const (
	delivered deliveryResult = iota
	networkError
	blocked
	badRequest
	userNotFound
	apiRetired
)

type smsResponse struct {
	Error  *string         `json:"error"`
	Result *deliveryResult `json:"result"`
}

type deliverCommand struct {
	sms    sms
	result chan deliveryResult
}

type worker struct {
	bot         *tg.BotAPI
	db          *sql.DB
	cfg         *config
	client      *http.Client
	deliverChan chan deliverCommand
	decryptor   tink.HybridDecrypt
}

func newWorker() *worker {
	if len(os.Args) != 2 {
		panic("usage: smsq <config>")
	}
	cfg := readConfig(os.Args[1])
	client := &http.Client{Timeout: time.Second * time.Duration(cfg.TimeoutSeconds)}
	bot, err := tg.NewBotAPIWithClient(cfg.BotToken, tg.APIEndpoint, client)
	checkErr(err)
	db, err := sql.Open("sqlite3", cfg.DBPath)
	checkErr(err)
	decryptor, err := hybrid.NewHybridDecrypt(cfg.privateKey)
	checkErr(err)
	w := &worker{
		bot:         bot,
		db:          db,
		cfg:         cfg,
		client:      client,
		deliverChan: make(chan deliverCommand),
		decryptor:   decryptor,
	}

	return w
}

func (r deliveryResult) String() string {
	switch r {
	case delivered:
		return "delivered"
	case networkError:
		return "network_error"
	case blocked:
		return "blocked"
	case badRequest:
		return "bad_request"
	case userNotFound:
		return "user_not_found"
	case apiRetired:
		return "api_retired"
	default:
		return "undefined"
	}
}

func (w *worker) logConfig() {
	cfgString, err := json.MarshalIndent(w.cfg, "", "    ")
	checkErr(err)
	linf("config: " + string(cfgString))
}

func (w *worker) setWebhook() {
	linf("setting webhook...")
	_, err := w.bot.SetWebhook(tg.NewWebhook(path.Join(w.cfg.WebhookDomain, w.cfg.BotToken)))
	checkErr(err)
	info, err := w.bot.GetWebhookInfo()
	checkErr(err)
	if info.LastErrorDate != 0 {
		linf("last webhook error time: %v", time.Unix(int64(info.LastErrorDate), 0))
	}
	if info.LastErrorMessage != "" {
		linf("last webhook error message: %s", info.LastErrorMessage)
	}
	linf("OK")
}

func (w *worker) removeWebhook() {
	linf("removing webhook...")
	_, err := w.bot.RemoveWebhook()
	checkErr(err)
	linf("OK")
}

func (w *worker) mustExec(query string, args ...interface{}) {
	stmt, err := w.db.Prepare(query)
	checkErr(err)
	_, err = stmt.Exec(args...)
	checkErr(err)
	stmt.Close()
}

func (w *worker) createDatabase() {
	linf("creating database if needed...")
	w.mustExec(`
		create table if not exists feedback (
			chat_id integer,
			text text);`)
	w.mustExec(`
		create table if not exists users (
			chat_id integer primary key,
			key text not null default '',
			delivered integer not null default 0,
			deleted integer not null default 0);`)
}

func (w *worker) keyForChat(chatID int64) *string {
	query, err := w.db.Query("select key from users where chat_id=? and deleted=0", chatID)
	checkErr(err)
	defer query.Close()
	if !query.Next() {
		return nil
	}
	var chatKey string
	checkErr(query.Scan(&chatKey))
	return &chatKey
}

func (w *worker) chatForKey(chatKey string) *int64 {
	query, err := w.db.Query("select chat_id from users where key=? and deleted=0", chatKey)
	checkErr(err)
	defer query.Close()
	if !query.Next() {
		return nil
	}
	var chatID int64
	checkErr(query.Scan(&chatID))
	return &chatID
}

func checkKey(key string) bool {
	hash := sha256.Sum256([]byte(key))
	hash = sha256.Sum256(hash[:])
	return hash[0] == 0 && hash[1]&0xf0 == 0
}

func (w *worker) userExists(chatID int64) bool {
	return singleInt(w.db.QueryRow("select count(*) from users where chat_id=? and deleted=0", chatID)) != 0
}

func (w *worker) stop(chatID int64) {
	w.mustExec("update users set deleted=1 where chat_id=?", chatID)
	_ = w.sendText(chatID, false, parseRaw, "Access revoked")
}

func (w *worker) start(chatID int64, key string) {
	if key == "" && w.userExists(chatID) {
		_ = w.sendText(chatID, false, parseRaw, "You are already set up!")
		return
	}
	if key == "" || !checkKey(key) {
		_ = w.sendText(chatID, false, parseRaw, "Install smsQ application on your phone https://smsq.me")
		return
	}
	chatKey := w.keyForChat(chatID)
	if chatKey != nil && *chatKey == key {
		_ = w.sendText(chatID, false, parseRaw, "You are already set up!")
		return
	}
	if chatKey != nil {
		_ = w.sendText(chatID, false, parseRaw, "Your previous subscription is revoked")
	}

	existingChatID := w.chatForKey(key)
	if existingChatID != nil && *existingChatID != chatID {
		_ = w.sendText(chatID, false, parseRaw, "Subscription on other Telegram account has been revoked")
		_ = w.sendText(*existingChatID, false, parseRaw, "Your subscription has been revoked from other Telegram account")
	}

	w.mustExec(`
		insert or replace into users (chat_id, key) values (?, ?)
		on conflict(chat_id) do update set key=excluded.key, deleted=0`,
		chatID,
		key)

	_ = w.sendText(chatID, false, parseRaw, "Congratulations! You should see here new SMS messages")
}

func (w *worker) broadcastChats() (chats []int64) {
	chatsQuery, err := w.db.Query(`select chat_id from users where deleted=0`)
	checkErr(err)
	defer chatsQuery.Close()
	for chatsQuery.Next() {
		var chatID int64
		checkErr(chatsQuery.Scan(&chatID))
		chats = append(chats, chatID)
	}
	return
}

func (w *worker) broadcast(text string) {
	if text == "" {
		return
	}
	chats := w.broadcastChats()
	for _, chatID := range chats {
		_ = w.sendText(chatID, true, parseRaw, text)
	}
	_ = w.sendText(w.cfg.AdminID, false, parseRaw, "OK")
}

func (w *worker) direct(arguments string) {
	parts := strings.SplitN(arguments, " ", 2)
	if len(parts) < 2 {
		_ = w.sendText(w.cfg.AdminID, false, parseRaw, "Usage: /direct chatID text")
		return
	}
	whom, err := strconv.ParseInt(parts[0], 10, 64)
	if err != nil {
		_ = w.sendText(w.cfg.AdminID, false, parseRaw, "First argument is invalid")
		return
	}
	text := parts[1]
	if text == "" {
		return
	}
	_ = w.sendText(whom, true, parseRaw, text)
	_ = w.sendText(w.cfg.AdminID, false, parseRaw, "OK")
}

func (w *worker) processAdminMessage(chatID int64, command, arguments string) bool {
	switch command {
	case "stat":
		w.stat()
		return true
	case "broadcast":
		w.broadcast(arguments)
		return true
	case "direct":
		w.direct(arguments)
		return true
	}
	return false
}

func (w *worker) processIncomingCommand(chatID int64, command, arguments string) {
	command = strings.ToLower(command)
	linf("chat: %d, command: %s %s", chatID, command, arguments)
	if chatID == w.cfg.AdminID && w.processAdminMessage(chatID, command, arguments) {
		return
	}
	switch command {
	case "stop":
		w.stop(chatID)
	case "feedback":
		w.feedback(chatID, arguments)
	case "start":
		w.start(chatID, arguments)
	case "help":
		_ = w.sendText(chatID, false, parseHTML,
			""+
				"smsq: Receive SMS messages in Telegram\n"+
				"1. Install Android app\n"+
				"2. Open app, start forwarding, connect Telegram\n"+
				"3. Now you receive your SMS messages in this bot!\n"+
				"Project page: https://smsq.me\n"+
				"Source code: https://github.com/igrmk/smsq\n"+
				"\n"+
				"Bot commands:\n"+
				"<b>/help</b> — Help\n"+
				"<b>/stop</b> — Revoke access\n"+
				"<b>/feedback</b> — Send feedback")
	default:
		_ = w.sendText(chatID, false, parseRaw, "Unknown command")
	}
}

func (w *worker) ourID() int64 {
	if idx := strings.Index(w.cfg.BotToken, ":"); idx != -1 {
		id, err := strconv.ParseInt(w.cfg.BotToken[:idx], 10, 64)
		checkErr(err)
		return id
	}
	checkErr(errors.New("cannot get our ID"))
	return 0
}

func (w *worker) processTGUpdate(u tg.Update) {
	if u.Message != nil && u.Message.Chat != nil {
		if newMembers := u.Message.NewChatMembers; newMembers != nil && len(*newMembers) > 0 {
			ourID := w.ourID()
			for _, m := range *newMembers {
				if int64(m.ID) == ourID {
					_ = w.sendText(u.Message.Chat.ID, false, parseRaw, "This bot should not work in group")
					break
				}
			}
		} else if u.Message.IsCommand() {
			w.processIncomingCommand(u.Message.Chat.ID, u.Message.Command(), u.Message.CommandArguments())
		} else {
			if u.Message.Text == "" {
				return
			}
			parts := strings.SplitN(u.Message.Text, " ", 2)
			for len(parts) < 2 {
				parts = append(parts, "")
			}
			w.processIncomingCommand(u.Message.Chat.ID, parts[0], parts[1])
		}
	}
}

func (w *worker) feedback(chatID int64, text string) {
	if text == "" {
		_ = w.sendText(chatID, false, parseRaw, "Command format: /feedback <text>")
		return
	}
	w.mustExec("insert into feedback (chat_id, text) values (?, ?)", chatID, text)
	_ = w.sendText(chatID, false, parseRaw, "Thank you for your feedback")
	_ = w.sendText(w.cfg.AdminID, true, parseRaw, fmt.Sprintf("Feedback: %s", text))
}

func (w *worker) userCount() int {
	query := w.db.QueryRow("select count(*) from users where deleted=0")
	return singleInt(query)
}

func (w *worker) activeUserCount() int {
	query := w.db.QueryRow("select count(*) from users where delivered > 0 and deleted=0")
	return singleInt(query)
}

func (w *worker) smsCount() int {
	query := w.db.QueryRow("select coalesce(sum(delivered), 0) from users")
	return singleInt(query)
}

func (w *worker) stat() {
	lines := []string{}
	lines = append(lines, fmt.Sprintf("users: %d", w.userCount()))
	lines = append(lines, fmt.Sprintf("active users: %d", w.activeUserCount()))
	lines = append(lines, fmt.Sprintf("smses: %d", w.smsCount()))
	_ = w.sendText(w.cfg.AdminID, false, parseRaw, strings.Join(lines, "\n"))
}

func (w *worker) sendText(chatID int64, notify bool, parse parseKind, text string) error {
	msg := tg.NewMessage(chatID, text)
	msg.DisableNotification = !notify
	switch parse {
	case parseHTML, parseMarkdown:
		msg.ParseMode = parse.String()
	}
	return w.send(&messageConfig{msg})
}

func (w *worker) send(msg baseChattable) error {
	if _, err := w.bot.Send(msg); err != nil {
		switch err := err.(type) {
		case tg.Error:
			if err.Code == 403 {
				linf("bot is blocked by the user %d, %v", msg.baseChat().ChatID, err)
				return errBlockedByUser
			}
			lerr("cannot send a message to %d, code %d, %v", msg.baseChat().ChatID, err.Code, err)
		default:
			lerr("unexpected error type while sending a message to %d, %v", msg.baseChat().ChatID, err)
		}
		return err
	}
	return nil
}

func (w *worker) handleRetired(writer http.ResponseWriter, r *http.Request) {
	if r.Method != "POST" {
		http.Error(writer, "404 not found", http.StatusNotFound)
		return
	}

	w.ldbg("got retired API call")
	writer.Header().Set("Content-Type", "application/json")
	w.apiReply(writer, apiRetired)
}

func (w *worker) handleV1SMS(writer http.ResponseWriter, r *http.Request) {
	if r.Method != "POST" {
		http.Error(writer, "404 not found", http.StatusNotFound)
		return
	}

	w.ldbg("got new SMS")

	var request smsRequest
	err := json.NewDecoder(r.Body).Decode(&request)
	if err != nil {
		w.ldbg("cannot decode v1 request")
		http.Error(writer, err.Error(), http.StatusBadRequest)
		return
	}

	if request.Version != 1 {
		lerr("version is not 1")
		http.Error(writer, err.Error(), http.StatusBadRequest)
		return
	}

	decrypted, err := w.decrypt(request.Payload)
	if err != nil {
		lerr("decryption error")
		http.Error(writer, err.Error(), http.StatusBadRequest)
		return
	}

	var sms sms
	err = json.NewDecoder(bytes.NewReader(decrypted)).Decode(&sms)
	if err != nil {
		lerr("cannot decode SMS")
		http.Error(writer, err.Error(), http.StatusBadRequest)
		return
	}

	writer.Header().Set("Content-Type", "application/json")
	if false ||
		!utf8.ValidString(sms.Text) ||
		!utf8.ValidString(sms.Carrier) ||
		!utf8.ValidString(sms.SIM) ||
		!utf8.ValidString(sms.Sender) {
		w.apiReply(writer, badRequest)
		lerr("invalid text")
		return
	}

	deliver := deliverCommand{sms: sms, result: make(chan deliveryResult)}
	defer close(deliver.result)
	w.deliverChan <- deliver
	result := <-deliver.result
	w.apiReply(writer, result)
}

func (w *worker) apiReply(writer http.ResponseWriter, result deliveryResult) {
	writer.WriteHeader(http.StatusOK)
	res := smsResponse{Result: &result}
	resString, err := json.Marshal(res)
	checkErr(err)
	_, err = writer.Write(resString)
	checkErr(err)
}

func (w *worker) handleEndpoints() {
	http.HandleFunc(path.Join(w.cfg.APIDomain, "/v0/sms"), w.handleRetired)
	http.HandleFunc(path.Join(w.cfg.APIDomain, "/v1/sms"), w.handleV1SMS)
}

func (w *worker) deliver(sms sms) deliveryResult {
	chatID := w.chatForKey(sms.Key)
	if chatID == nil {
		w.ldbg("cannot found user")
		return userNotFound
	}

	var lines []string
	loc := time.FixedZone("", sms.Offset)
	tm := time.Unix(sms.Timestamp, 0).In(loc)
	lines = append(lines, tm.Format("2006-01-02 15:04:05"))
	var sender = html.EscapeString(sms.Sender)
	var sim = html.EscapeString(sms.SIM)
	if sim == "" {
		sim = html.EscapeString(sms.Carrier)
	}
	if sim != "" {
		sender = strings.Join([]string{sender, sim}, " ")
	}
	if sender != "" {
		lines = append(lines, sender)
	}

	for i, l := range lines {
		lines[i] = "<i>" + l + "</i>"
	}

	lines = append(lines, html.EscapeString(sms.Text))
	text := strings.Join(lines, "\n")

	if err := w.sendText(*chatID, true, parseHTML, text); err != nil {
		switch err {
		case errBlockedByUser:
			return blocked
		default:
			return networkError
		}
	}
	w.mustExec("update users set delivered=delivered+1 where chat_id=?", chatID)
	return delivered
}

func (w *worker) decrypt(str string) ([]byte, error) {
	data, err := base64.StdEncoding.DecodeString(str)
	if err != nil {
		return nil, err
	}
	result, err := w.decryptor.Decrypt(data, nil)
	if err != nil {
		return nil, err
	}
	return result, nil
}

func main() {
	w := newWorker()
	w.logConfig()
	w.setWebhook()
	w.createDatabase()

	incoming := w.bot.ListenForWebhook(path.Join(w.cfg.WebhookDomain, w.cfg.BotToken))
	w.handleEndpoints()

	go func() {
		checkErr(http.ListenAndServe(w.cfg.ListenAddress, nil))
	}()

	signals := make(chan os.Signal, 16)
	signal.Notify(signals, syscall.SIGINT, syscall.SIGTERM, syscall.SIGABRT)
	for {
		select {
		case s := <-w.deliverChan:
			s.result <- w.deliver(s.sms)
		case m := <-incoming:
			w.processTGUpdate(m)
		case s := <-signals:
			linf("got signal %v", s)
			w.removeWebhook()
			return
		}
	}
}
