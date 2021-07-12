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

j-package(){
  OS=${1:?"Need OS type (windows/linux/mac)"}

  echo "Starting build..."

  if [ "$OS" == "windows" ]; then
    J_ARG="--win-menu --win-dir-chooser --win-shortcut"
          
  elif [ "$OS" == "linux" ]; then
      J_ARG="--linux-shortcut"
  else
      J_ARG=""
  fi

  jpackage \
    --input target \
    --dest target \
    --main-jar find.standalone.jar \
    --name "find" \
    --main-class clojure.main \
    --arguments -m \
    --arguments ipfs-shipyard.find.main \
    --resource-dir resources \
    $J_ARG
  
}

"$@"