(ns ipfs-shipyard.torrent-search.cljfx
  (:gen-class)
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >! <!! >!!  take! put! offer! poll! alt! alts! close! onto-chan!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.string]
   [clojure.java.io :as io]
   [cljfx.api])
  (:import
   (javafx.event Event EventHandler)
   (javafx.stage WindowEvent)
   (javafx.scene.control DialogEvent Dialog ButtonType ButtonBar$ButtonData)
   (javafx.application Platform)))

(set! *warn-on-reflection* true)

(defn root
  [{:as opts
    :keys [state]}]
  {:fx/type :stage
   :showing true
   #_:on-close-request #_(fn [^WindowEvent event]
                           (println :on-close-request)
                           #_(.consume event))
   :width 1024
   :height 768
   :icons [(str (io/resource "icon256x256.png"))]
   :scene {:fx/type :scene
           :root {:fx/type :h-box
                  :children [{:fx/type :label :text "find"}
                             {:fx/type :text-field
                              :text (:searchS state)}]}}})

(def renderer (cljfx.api/create-renderer))

(defn render
  [ctx new-state]
  (renderer (-> ctx
                (assoc :fx/type root)
                (assoc :state new-state))))

(defn start
  [{:as ctx
    :keys [stateA]}]
  (Platform/setImplicitExit true)
  (render ctx @stateA)
  #_(cljfx.api/mount-renderer stateA render))

