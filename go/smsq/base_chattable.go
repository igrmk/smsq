package main

import tg "github.com/go-telegram-bot-api/telegram-bot-api"

type baseChattable interface {
	tg.Chattable
	baseChat() *tg.BaseChat
}

type messageConfig struct{ tg.MessageConfig }

func (m *messageConfig) baseChat() *tg.BaseChat {
	return &m.BaseChat
}
