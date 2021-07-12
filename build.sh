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

uberjar(){

  # clojure -X:depstar uberjar \
  #   :aot true \
  #   :jar target/find.standalone.jar \
  #   :verbose false \
  #   :main-class ipfs-shipyard.find.main


  lein with-profiles +prod uberjar
  #  java -Dclojure.core.async.pool-size=1 -jar target/find-standalone.jar
}

j-package(){
  OS=${1:?"Need OS type (windows/linux/mac)"}

  echo "Starting build..."

  if [ "$OS" == "windows" ]; then
    J_ARG="--win-menu --win-dir-chooser --win-shortcut --icon resources/icon.ico"
          
  elif [ "$OS" == "linux" ]; then
      J_ARG="--linux-shortcut --icon resources/icon256x256.png"
  else
      J_ARG="--icon resources/icon.icns"
  fi

  APP_VERSION=0.1.0

  jpackage \
    --input target \
    --dest target \
    --main-jar find.standalone.jar \
    --name "find" \
    --main-class clojure.main \
    --arguments -m \
    --arguments ipfs-shipyard.find.main \
    --resource-dir resources \
    --java-options -Xmx2048m \
    --app-version ${APP_VERSION} \
    $J_ARG
  
}


"$@"