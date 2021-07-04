(ns ipfs-shipyard.find.cljfx
  (:gen-class)
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >! <!! >!!  take! put! offer! poll! alt! alts! close! onto-chan!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.string]
   [clojure.java.io :as io]
   [cljfx.api]))

(set! *warn-on-reflection* true)

(defonce stateA (atom {::searchS "asd"}))

(defn root
  [{:keys [::searchS]}]
  {:fx/type :stage
   :showing true
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
  []
  (renderer (merge
             {:fx/type root}
             @stateA)))

(defonce _ (add-watch stateA :a-key (fn [k stateA old-state new-state]
                                      (renderer (merge
                                                 {:fx/type root #_(var-get #'root)}
                                                 new-state)))))

(defn start
  []
  #_(cljfx.api/mount-renderer stateA render))


(comment

  (in-ns 'ipfs-shipyard.find.cljfx)

  (require
   '[cljfx.api]
   :reload)

  (render)

  (swap! stateA assoc ::searchS "123")


  ;
  )