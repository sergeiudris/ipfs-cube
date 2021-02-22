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

   [io.pedestal.http :as pedestal.http]
   [io.pedestal.http.route :as pedestal.http.route]
   [io.pedestal.http.body-params :as pedestal.http.body-params]
   [io.pedestal.http.content-negotiation :as pedestal.http.content-negotiation]
   [io.pedestal.http.ring-middlewares :as pedestal.http.ring-middlewares]
   [ipfscube.app.spec :as app.spec]
   [ipfscube.app.chan :as app.chan]

   [clj-docker-client.core :as docker])
  (:import
   spark.Spark
   spark.Route

  ;;  com.github.dockerjava.api.DefaultDockerClientConfig
  ;;  com.github.dockerjava.api.DockerHttpClient
  ;;  com.github.dockerjava.api.DockerClient
   ))

(def counter1 (atom 0))
(def counter2 (atom 0))

(def foo| (chan (sliding-buffer 10)))

(s/def ::foo string?)

(def foo1 (s/gen ::foo))

(def host "0.0.0.0")
(def port 8080)

(def supported-types
  ["text/html" "application/edn"  "text/plain" "application/transit+json"])

(def content-negotiation-interceptor
  (pedestal.http.content-negotiation/negotiate-content supported-types))

(def common-interceptors [(pedestal.http.body-params/body-params)
                          pedestal.http/html-body
                          content-negotiation-interceptor])

(def routes
  #{#_["/"
       :get
       (fn [request]
         (ring.util.response/redirect "/index.html")
         #_(ring.util.response/file-response
            (.getAbsolutePath ^java.io.File (io/as-file (io/resource "public/index.html")))))
       :route-name :root]

    ["/clojure-version"
     :get
     (->>
      (fn [request] {:body (clojure-version) :status 200})
      (conj common-interceptors))
     :route-name :clojure-version]

    ["/echo"
     :get
     (->>
      (fn [request] {:body (pr-str request) :status 200})
      (conj common-interceptors))
     :route-name :echo]

    ["/bar"
     :get
     (->>
      {:enter
       (fn [{:keys [:request] :as context}]
         (go
           (let [response {:body "bar"
                           :status 200}]
             (<! (timeout 1000))
             (assoc context :response response))))}
      (conj common-interceptors))
     :route-name :bar]})

(comment

  (io/resource "public")

  (ring.util.response/file-response
   (.getAbsolutePath ^java.io.File (io/as-file (io/resource "public")))
   {:index-files? true})
  (.isDirectory (io/as-file (io/resource "public")))

  ;;
  )

(defn create-service
  []
  (let []
    (merge
     {:env :prod
              ;; You can bring your own non-default interceptors. Make
              ;; sure you include routing and set it up right for
              ;; dev-mode. If you do, many other keys for configuring
              ;; default interceptors will be ignored.
              ;; ::http/interceptors []
      ::pedestal.http/routes
      #(pedestal.http.route/expand-routes routes #_(deref #'service/routes))

              ;; Uncomment next line to enable CORS support, add
              ;; string(s) specifying scheme, host and port for
              ;; allowed source(s):
              ;;
              ;; "http://localhost:8080"
              ;;
      ::pedestal.http/allowed-origins ["*"]
      ::pedestal.http/secure-headers {:content-security-policy-settings {:object-src "none"}}

              ;; Root for resource interceptor that is available by default.
      ::pedestal.http/resource-path "/public"

              ;; Either :jetty, :immutant or :tomcat (see comments in project.clj)
      ::pedestal.http/type :jetty

      ::pedestal.http/container-options (merge
                                         {}
                                         #_(when false #_ws-paths
                                                 {:context-configurator #(pedestal.ws/add-ws-endpoints % ws-paths)}))
      ::pedestal.http/host host
      ::pedestal.http/port port}
     {:env :dev
      ::pedestal.http/join? false
      ::pedestal.http/allowed-origins {:creds true :allowed-origins (constantly true)}})))

(defn create-server
  []
  (let [service (create-service)
        server (-> service ;; start with production configuration
                   pedestal.http/default-interceptors
                   (update ::pedestal.http/interceptors conj (pedestal.http.ring-middlewares/fast-resource "resources/public" {:index-files? true}))
                   #_(update ::pedestal.http/interceptors conj (pedestal.http.ring-middlewares/file-info))
                   #_(update ::pedestal.http/interceptors conj (pedestal.http.ring-middlewares/file "/ctx/ipfs-cube/bin/ui2/resources"))
                   pedestal.http/dev-interceptors
                   pedestal.http/create-server
                   pedestal.http/start)]
    (println (format "Starting http server on %s:%s" (::pedestal.http/host service) (::pedestal.http/port service)))))


;; (defn create-server []
;;   (println (format "; starting http server on %s:%s" host port))
;;   (def ^:dynamic *tmp* "something defined in runtime")
;;   (Spark/port port)
;;   (.location Spark/staticFiles  "/public")
;;   (Spark/init)
;;   (Spark/get "/hello" (reify Route
;;                         (handle [_ req res]
;;                           (format "hello world, %s" *tmp*)))))

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
  (create-server)
  (app.chan/op
   {::op.spec/op-key ::app.chan/init
    ::op.spec/op-type ::op.spec/fire-and-forget}
   channels
   {}))