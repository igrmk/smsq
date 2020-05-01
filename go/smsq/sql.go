package main

import "database/sql"

func singleInt(row *sql.Row) (result int) {
	checkErr(row.Scan(&result))
	return result
}

func singleInt64(row *sql.Row) (result int64) {
	checkErr(row.Scan(&result))
	return result
}
