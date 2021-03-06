package main

import "log"

// lerr logs an error
func lerr(format string, v ...interface{}) { log.Printf("[ERROR] "+format, v...) }

// linf logs an info message
func linf(format string, v ...interface{}) { log.Printf("[INFO] "+format, v...) }

// ldbg logs a debug message
func (w *worker) ldbg(format string, v ...interface{}) {
	if w.cfg.Debug {
		log.Printf("[DEBUG] "+format, v...)
	}
}
