package main

import (
	"encoding/json"
	"errors"
	"io"
	"os"
	"path/filepath"
)

type config struct {
	ListenPath     string `json:"listen_path"`     // the path excluding domain to listen to, the good choice is "/your-telegram-bot-token"
	ListenAddress  string `json:"listen_address"`  // the address to listen to incoming telegram messages
	Host           string `json:"host"`            // the host name for the email addresses and the webhook
	BotToken       string `json:"bot_token"`       // your telegram bot token
	TimeoutSeconds int    `json:"timeout_seconds"` // HTTP timeout
	AdminID        int64  `json:"admin_id"`        // admin telegram ID
	DBPath         string `json:"db_path"`         // path to the database
	Debug          bool   `json:"debug"`           // debug mode
}

func readConfig(path string) *config {
	file, err := os.Open(filepath.Clean(path))
	checkErr(err)
	defer func() { checkErr(file.Close()) }()
	return parseConfig(file)
}

func parseConfig(r io.Reader) *config {
	decoder := json.NewDecoder(r)
	decoder.DisallowUnknownFields()
	cfg := &config{}
	err := decoder.Decode(cfg)
	checkErr(err)
	checkErr(checkConfig(cfg))
	return cfg
}

func checkConfig(cfg *config) error {
	if cfg.ListenAddress == "" {
		return errors.New("configure listen_address")
	}
	if cfg.ListenPath == "" {
		return errors.New("configure listen_path")
	}
	if cfg.BotToken == "" {
		return errors.New("configure bot_token")
	}
	if cfg.TimeoutSeconds == 0 {
		return errors.New("configure timeout_seconds")
	}
	if cfg.AdminID == 0 {
		return errors.New("configure admin_id")
	}
	if cfg.DBPath == "" {
		return errors.New("configure db_path")
	}
	if cfg.Host == "" {
		return errors.New("configure host")
	}
	return nil
}
