(ns ipfs-shipyard.torrent-search.app.cljfx
  (:gen-class)
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >! <!! >!!  take! put! offer! poll! alt! alts! close! onto-chan!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.string]
   [clojure.java.io :as io]
   [cljfx.api :as fx]
   [cljfx.prop :as fx.prop]
   [cljfx.mutator :as fx.mutator]
   [cljfx.lifecycle :as fx.lifecycle])
  (:import
   (javafx.scene.web WebView)))

(set! *warn-on-reflection* true)

(def web-view-with-ext-props
  (fx/make-ext-with-props
   {:on-location-changed (fx.prop/make (fx.mutator/property-change-listener
                                        #(.locationProperty (.getEngine ^WebView %)))
                                       fx.lifecycle/change-listener)}))

(defn start
  []
  (fx/on-fx-thread
   (fx/create-component
    {:fx/type :stage
     :showing true
     :icons ["logo/logo.png"]
     :scene {:fx/type :scene
             :root {:fx/type web-view-with-ext-props
                    :desc {:fx/type :web-view
                           :pref-height 1000
                           :pref-width 1500
                           :url "http://localhost:4080"}
                    #_:props #_{:on-location-changed {:event/type ::url-changed}}}}})))

#_(def ext-with-html
    (fx/make-ext-with-props
     {:html (fx.prop/make
             (fx.mutator/setter #(.loadContent (.getEngine ^WebView %1) %2))
             fx.lifecycle/scalar)}))

#_(defn start
    []
    (println (.toString (io/resource "public/index.html")))
    (println (slurp (io/resource "public/index.html")))
    (let [html (slurp (io/resource "public/index.html"))
          mainjs (io/resource "public/out/main.js")])

    (fx/on-fx-thread
     (fx/create-component
      {:fx/type :stage
       :showing true
       :scene {:fx/type :scene
               :root {:fx/type ext-with-html
                      :props {:html "<h1>hello html!</h1>"}
                      :desc {:fx/type :web-view}}}})))

