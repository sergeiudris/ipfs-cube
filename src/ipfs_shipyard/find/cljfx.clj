(ns ipfs-shipyard.find.cljfx
  (:gen-class)
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >! <!! >!!  take! put! offer! poll! alt! alts! close! onto-chan!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.string]
   [clojure.java.io :as io]
   [cljfx.api :as fx]))

(set! *warn-on-reflection* true)


(defn start
  []
  (fx/on-fx-thread
   (fx/create-component
    {:fx/type :stage
     :showing true
     :width 1024
     :height 768
     :icons ["logo/logo.png"]
     :scene {:fx/type :scene
             :root {:fx/type :h-box
                    :children [{:fx/type :label :text "find"}
                               {:fx/type :text-field}]}}})))