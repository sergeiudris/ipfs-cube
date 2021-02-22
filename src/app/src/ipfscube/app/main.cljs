(ns ipfscube.app.main
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.core.async.impl.protocols :refer [closed?]]
   [clojure.string :as str]
   [cljs.core.async.interop :refer-macros [<p!]]
   [goog.string.format :as format]
   [goog.string :refer [format]]
   [cljs.reader :refer [read-string]]

   [cljctools.csp.op.spec :as op.spec]
   [cljctools.cljc.core :as cljc.core]

   [ipfscube.app.spec :as app.spec]
   [ipfscube.app.chan :as app.chan]))

(defonce fs (js/require "fs"))
(defonce path (js/require "path"))
(defonce axios (.-default (js/require "axios")))
(defonce OrbitDB (js/require "orbit-db"))
(defonce IpfsClient (js/require "ipfs-http-client"))
(defonce http (js/require "http"))
(defonce Url (js/require "url"))
(defonce express (js/require "express"))
(defonce cors (js/require "cors"))
(defonce bodyParser (js/require "body-parser"))
(defonce Docker (js/require "dockerode"))

(defonce channels (merge
                   (app.chan/create-channels)))

(defonce ctx {::app.spec/state*
              (atom
               {})})

(def ^:const port 8080)

(def app (express))
(def server (.createServer http app))

(.use app (.static express "/ctx/ipfs-cube/bin/ui2/resources/public"))
(.use app (cors))
(.use app (.text bodyParser #js {"type" "text/plain" #_"*/*"
                                 "limit" "100kb"}))
(.listen server port)

(.get app "/hello"
      (fn [request response next]
        (go
          (<! (timeout 500))
          (.send response "hello world"))))

(.get app "/foo/:id"
      (fn [request response next]
        (let [{:keys [id]
               :as params} (js->clj (.-params request)
                                    :keywordize-keys true)]
          (go
            (<! (timeout 1000))
            (.send response id)))))

(defn create-proc-ops
  [channels ctx opts]
  (let [{:keys [::app.chan/ops|]} channels
        {:keys [::app.spec/state*]} ctx]
    (go
      (loop []
        (when-let [[value port] (alts! [ops|])]
          (condp = port
            ops|
            (condp = (select-keys value [::op.spec/op-key ::op.spec/op-type ::op.spec/op-orient])

              {::op.spec/op-key ::app.chan/init
               ::op.spec/op-type ::op.spec/fire-and-forget}
              (let []
                (println ::init))))
          (recur))))))


(defn main [& args]
  (println ::main)
  (do (create-proc-ops channels ctx {}))
  (app.chan/op
   {::op.spec/op-key ::app.chan/init
    ::op.spec/op-type ::op.spec/fire-and-forget}
   channels
   {}))

(def exports #js {:main main})

(when (exists? js/module)
  (set! js/module.exports exports))


