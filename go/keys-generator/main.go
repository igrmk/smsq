package main

import (
	"bytes"
	"fmt"
	"log"

	"github.com/google/tink/go/hybrid"
	"github.com/google/tink/go/insecurecleartextkeyset"
	"github.com/google/tink/go/keyset"
)

func main() {
	khPriv, err := keyset.NewHandle(hybrid.ECIESHKDFAES128CTRHMACSHA256KeyTemplate())
	if err != nil {
		log.Fatal(err)
	}

	var buf bytes.Buffer

	khPub, err := khPriv.Public()
	if err != nil {
		log.Fatal(err)
	}

	exportedPub := keyset.NewJSONWriter(&buf)
	if err = insecurecleartextkeyset.Write(khPub, exportedPub); err != nil {
		log.Fatal(err)
	}

	fmt.Println("public key:")
	fmt.Println(string(buf.Bytes()))

	buf = bytes.Buffer{}

	exportedPriv := keyset.NewJSONWriter(&buf)
	if err := insecurecleartextkeyset.Write(khPriv, exportedPriv); err != nil {
		log.Fatal(err)
	}

	fmt.Println("private key:")
	fmt.Println(string(buf.Bytes()))
}
