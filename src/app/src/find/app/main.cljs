(ns find.app.main
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.core.async.impl.protocols :refer [closed?]]
   [clojure.string]
   [cljs.core.async.interop :refer-macros [<p!]]
   [goog.string.format :as format]
   [goog.string :refer [format]]
   [goog.object]
   [cljs.reader :refer [read-string]]
   [find.app.http :as app.http]
   [find.app.ipfs :as app.ipfs]
   [find.app.electron :as app.electron]))

(defonce fs (js/require "fs"))
(defonce path (js/require "path"))
(defonce axios (.-default (js/require "axios")))

(declare)

(defn main [& args]
  (println ::main)
  (go
    (<! (app.http/start))
    (<! (app.electron/start))
    (<! (app.ipfs/start))))

(def exports #js {:main main})

(when (exists? js/module)
  (set! js/module.exports exports))