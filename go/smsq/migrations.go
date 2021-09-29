package main

import (
	"database/sql"
)

var migrations = []func(w *worker){
	func(w *worker) {
		w.mustExec(`
			create table if not exists feedback (
				chat_id integer,
				text text);`)
		w.mustExec(`
			create table if not exists users (
				chat_id integer primary key,
				key text not null default '',
				delivered integer not null default 0,
				delivered_today integer not null default 0,
				received_today integer not null default 0,
				deleted integer not null default 0);`)
		w.mustExec(`
			create table if not exists midnight (
				unix_time integer not null default 0);`)
	},
	func(w *worker) {
		w.mustExec("alter table users add daily_limit integer not null default 0;")
		w.mustExec("update users set daily_limit=?", w.cfg.DeliveredLimit)
	},
}

func (w *worker) applyMigrations() {
	row := w.db.QueryRow("select version from schema_version")
	var version int
	err := row.Scan(&version)
	if err == sql.ErrNoRows {
		version = 0
		w.mustExec("insert into schema_version(version) values (0)")
	} else {
		checkErr(err)
	}
	for i, m := range migrations[version+1:] {
		n := i + version + 1
		linf("applying migration %d", n)
		m(w)
		w.mustExec("update schema_version set version=?", n)
	}
}

func (w *worker) createDatabase() {
	linf("creating database if needed...")
	w.mustExec(`create table if not exists schema_version (version integer);`)
	w.applyMigrations()
}
