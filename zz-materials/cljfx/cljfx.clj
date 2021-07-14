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
   (javafx.scene.control DialogEvent Dialog ButtonType ButtonBar$ButtonData)))

(set! *warn-on-reflection* true)

(defonce stateA (atom {::searchS ""}))

(defn root
  [{:keys [::searchS]}]
  {:fx/type :stage
   :showing true
   :on-close-request (fn [^WindowEvent event]
                       (println :on-close-request)
                       #_(.consume event))
   :width 1024
   :height 768
   :icons ["logo/logo.png"]
   :scene {:fx/type :scene
           :root {:fx/type :h-box
                  :children [{:fx/type :label :text "find"}
                             {:fx/type :text-field
                              :text searchS}]}}})

(def renderer (cljfx.api/create-renderer))

(defn render
  ([]
   (render @stateA))
  ([new-state]
   (renderer (merge
              {:fx/type root #_(var-get #'root)}
              new-state))))

(defonce _ (add-watch stateA :a-key (fn [k stateA old-state new-state] (render new-state))))

(defn start
  []
  (render)
  #_(cljfx.api/mount-renderer stateA render))

(comment

  (in-ns 'ipfs-shipyard.torrent-search.cljfx)

  (render)

  (swap! stateA assoc ::searchS "123")
  
  ;
  )