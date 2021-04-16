package ipfsfind_ipfs_core

import (
	"fmt"
	"net/http"
)

//Main _
func Main() {
	fmt.Println("foo")

	server := http.NewServeMux()

	server.HandleFunc("/", func(writer http.ResponseWriter, request *http.Request) {
		fmt.Fprintf(writer, "foo")
	})

	http.ListenAndServe("0.0.0.0:8000", server)

}
