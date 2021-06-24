(ns notepad.seesaw1
  (:gen-class)
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.string :as str]
   [seesaw.core]))

(defn render
  []
  (seesaw.core/invoke-later
   (-> (seesaw.core/frame
        :title "Hello"
        :content "Hello, Seesaw"
        :on-close :exit)
       seesaw.core/pack!
       seesaw.core/show!)))

