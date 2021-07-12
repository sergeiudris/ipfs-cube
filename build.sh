#!/bin/bash

repl(){
  clj -M:repl
}

main(){
  clojure -M:main
}

compile-java(){
  lein javac
}

gen-proto(){
  OUT=target/proto
  mkdir -p $OUT
  SRC=$(cd ../ && pwd)/cljctools/ipfs-jvm/src
	protoc --java_out=${OUT} --proto_path ${SRC}/cljctools/ipfs/runtime dht_proto.proto
}

depstar-uberjar(){
  clojure -X:depstar uberjar \
    :aot true \
    :jar target/find.standalone.jar \
    :verbose false \
    :main-class ipfs-shipyard.find.main
}

lein-uberjar(){
  lein with-profiles +prod uberjar
  #  java -Dclojure.core.async.pool-size=1 -jar target/find-standalone.jar
}

"$@"