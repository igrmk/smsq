package main

import (
	"bytes"
	"encoding/binary"
	"io/ioutil"
	"os"

	"github.com/google/tink/go/hybrid"
	"github.com/google/tink/go/insecurecleartextkeyset"
	"github.com/google/tink/go/keyset"
)

func checkErr(err error) {
	if err != nil {
		panic(err)
	}
}

func parseKey(file string) (*keyset.Handle, error) {
	data, err := ioutil.ReadFile(file)
	if err != nil {
		return nil, err
	}
	kh, err := insecurecleartextkeyset.Read(keyset.NewJSONReader(bytes.NewReader(data)))
	if err != nil {
		return nil, err
	}
	return kh, nil
}

func main() {
	key, err := parseKey(os.Args[1])
	checkErr(err)
	encryptor, err := hybrid.NewHybridEncrypt(key)
	checkErr(err)
	bytes, err := ioutil.ReadAll(os.Stdin)
	checkErr(err)
	result, err := encryptor.Encrypt(bytes, nil)
	checkErr(err)
	checkErr(binary.Write(os.Stdout, binary.LittleEndian, result))
}
