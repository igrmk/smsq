package main

import "database/sql"

func singleInt(row *sql.Row) (result int) {
	checkErr(row.Scan(&result))
	return result
}
