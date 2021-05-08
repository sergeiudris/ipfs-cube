(ns find.app.core
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.core.async.impl.protocols :refer [closed?]]
   [cljs.core.async.interop :refer-macros [<p!]]
   [clojure.string]
   [goog.string.format :as format]
   [goog.string :refer [format]]
   [goog.object]
   [cljs.reader :refer [read-string]]
   [cognitect.transit :as transit]))

(def transit-write
  (let [handlers {}
        writer (transit/writer
                :json-verbose
                {:handlers handlers})]
    (fn [data]
      (transit/write writer data))))

(def transit-read
  (let [handlers {}
        reader (transit/reader
                :json-verbose
                {:handlers handlers})]
    (fn [data]
      (transit/read reader data))))