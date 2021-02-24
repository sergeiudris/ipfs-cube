(ns ipfscube.app.main
  (:gen-class)
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.string :as str]
   [clojure.spec.alpha :as s]
   [clojure.java.io :as io]

   [clojure.spec.gen.alpha :as sgen]
   #_[clojure.spec.test.alpha :as stest]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]

   [cljctools.csp.op.spec :as op.spec]
   [cljctools.cljc.core :as cljc.core]

   
   [ipfscube.app.spec :as app.spec]
   [ipfscube.app.chan :as app.chan]

   [ipfscube.app.http]
   [ipfscube.app.tray]
   [clj-docker-client.core :as docker]))

(def ^:const docker-api-version "v1.41")

(comment

  (docker/categories docker-api-version)

  (def images (docker/client {:category :images
                              :api-version docker-api-version
                              :conn     {:uri "unix:///var/run/docker.sock"}}))

  (docker/ops images)

  (def image-list (docker/invoke images {:op     :ImageList}))
  (count image-list)

  (->> image-list
       (drop 5)
       (take 5))

  (filter (fn [img]
            (some #(str/includes? % "app") (:RepoTags img))) image-list)

 ;;
  )

#_(defn -main [& args]
    (println ::main)
    (println (clojure-version))
    (println (s/conform ::foo 42))
    (println (gen/generate foo1))
    (go (loop []
          (<! (timeout 1000))
          (swap! counter1 inc)
          (println ::loop-a @counter1)
          (recur)))
    (go (loop []
          (<! (timeout 1000))
          (>! foo| @counter1)
          (recur)))
    (go (loop []
          (when-let [value (<! foo|)]
            (println ::loop-b value)
            (recur))))
    (create-server))

(def channels (merge
               (app.chan/create-channels)))

(def ctx {::app.spec/state* (atom {})})

(defn create-proc-ops
  [channels ctx]
  (let [{:keys [::app.chan/ops|]} channels]
    (go
      (loop []
        (when-let [[value port] (alts! [ops|])]
          (condp = port
            ops|
            (condp = (select-keys value [::op.spec/op-key ::op.spec/op-type ::op.spec/op-orient])

              {::op.spec/op-key ::app.chan/init
               ::op.spec/op-type ::op.spec/fire-and-forget}
              (let [{:keys []} value]
                (println ::init)

                (go (let [images (docker/client {:category :images
                                                 :api-version docker-api-version
                                                 :conn     {:uri "unix:///var/run/docker.sock"}})
                          image-list (docker/invoke images {:op     :ImageList})]
                      (println ::docker-images (count image-list))))
                (println ::init-done)))))
        (recur)))))

;; (def _ (create-proc-ops channels {})) ;; cuases native image to fail

(defn -main [& args]
  (println ::-main)
  (create-proc-ops channels {})
  (ipfscube.app.tray/create)
  (ipfscube.app.http/start)
  (app.chan/op
   {::op.spec/op-key ::app.chan/init
    ::op.spec/op-type ::op.spec/fire-and-forget}
   channels
   {}))