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

   ;; http
   [reitit.http :as http]
   [reitit.ring :as ring]
   [reitit.interceptor.sieppari]
   [sieppari.async.core-async] ;; needed for core.async
   [sieppari.async.manifold]   ;; needed for manifold
   [muuntaja.interceptor]
   [reitit.coercion.spec]
   [reitit.swagger :as swagger]
   [reitit.swagger-ui :as swagger-ui]
   [reitit.ring.coercion :as coercion]
   [reitit.dev.pretty :as pretty]
   [reitit.ring.middleware.muuntaja :as muuntaja]
   [reitit.ring.middleware.exception :as exception]
   [reitit.ring.middleware.multipart :as multipart]
   [reitit.ring.middleware.parameters :as parameters]
   [ring.util.response]
            ;; Uncomment to use
            ; [reitit.ring.middleware.dev :as dev]
            ; [reitit.ring.spec :as spec]
            ; [spec-tools.spell :as spell]
   [ring.adapter.jetty :as jetty]
   [aleph.http :as aleph]
   [muuntaja.core :as m]
   [spec-tools.core :as st]
   [manifold.deferred :as d]
   ;;
   [cljctools.csp.op.spec :as op.spec]
   [cljctools.cljc.core :as cljc.core]


   [ipfscube.app.spec :as app.spec]
   [ipfscube.app.chan :as app.chan]

   [ipfscube.app.tray]
   [clj-docker-client.core :as docker]))

(def ^:const docker-api-version "v1.41")

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

(s/def ::file multipart/temp-file-part)
(s/def ::file-params (s/keys :req-un [::file]))

(s/def ::name string?)
(s/def ::size int?)
(s/def ::file-response (s/keys :req-un [::name ::size]))

(s/def ::x int?)
(s/def ::y int?)
(s/def ::total int?)
(s/def ::math-request (s/keys :req-un [::x ::y]))
(s/def ::math-response (s/keys :req-un [::total]))

(s/def ::seed string?)
(s/def ::results
  (st/spec
   {:spec (s/and int? #(< 0 % 100))
    :description "between 1-100"
    :swagger/default 10
    :reason "invalid number"}))

(defn interceptor [f x]
  {:enter (fn [ctx] (f (update-in ctx [:request :via] (fnil conj []) {:enter x})))
   :leave (fn [ctx] (f (update-in ctx [:response :body] conj {:leave x})))})

(defn handler [f]
  (fn [{:keys [via]}]
    (f {:status 200
        :body (conj via :handler)})))

(def <async> #(go %))
(def <deferred> d/success-deferred)

(def app
  (http/ring-handler
   (http/router
    [["/swagger.json"
      {:get {:no-doc true
             :swagger {:info {:title "my-api"}}
             :handler (swagger/create-swagger-handler)}}]

     ["/files"
      {:swagger {:tags ["files"]}}

      ["/upload"
       {:post {:summary "upload a file"
               :parameters {:multipart ::file-params}
               :responses {200 {:body ::file-response}}
               :handler (fn [{{{:keys [file]} :multipart} :parameters}]
                          {:status 200
                           :body {:name (:filename file)
                                  :size (:size file)}})}}]

      ["/download"
       {:get {:summary "downloads a file"
              :swagger {:produces ["image/png"]}
              :handler (fn [_]
                         {:status 200
                          :headers {"Content-Type" "image/png"}
                          :body (io/input-stream
                                 (io/resource "reitit.png"))})}}]]

     ["/random-user"
      {:get {:swagger {:tags ["random-user"]}
             :summary "fetches random users asynchronously over the internet"
             :parameters {:query (s/keys :req-un [::results] :opt-un [::seed])}
             :responses {200 {:body any?}}
             :handler (fn [{{{:keys [seed results]} :query} :parameters}]
                        (go
                          (<! (timeout 1000))
                          @(d/chain
                            (aleph/get
                             "https://randomuser.me/api/"
                             {:query-params {:seed seed, :results results}})
                            :body
                            (partial m/decode "application/json")
                            :results
                            (fn [results]
                              {:status 200
                               :body results}))))}}]

     ["/async2"
      {:interceptors [(interceptor <async> :async)]
       :get {:swagger {:tags ["async"]}
             :interceptors [(interceptor <async> :get)]
             :handler (fn [request]
                        (go
                          (<! (timeout 1000))
                          {:status 200
                           :body [:async]}))}}]

     ["/async"
      {:interceptors [(interceptor <async> :async)]
       :get {:interceptors [(interceptor <async> :get)]
             :handler (handler <async>)}}]

     ["/deferred"
      {:interceptors [(interceptor <deferred> :deferred)]
       :get {:swagger {:tags ["deferred"]}
             :interceptors [(interceptor <deferred> :get)]
             :handler (handler <deferred>)}}]

     ["/math"
      {:swagger {:tags ["math"]}}

      ["/plus"
       {:get {:summary "plus with spec query parameters"
              :parameters {:query ::math-request}
              :responses {200 {:body ::math-response}}
              :handler (fn [{{{:keys [x y] :as query} :query} :parameters}]
                         (println query)
                         (go
                           (<! (timeout 1000))
                           {:status 200
                            :body {:total (+ x y)}}))}
        :post {:summary "plus with spec body parameters"
               :parameters {:body ::math-request}
               :responses {200 {:body ::math-response}}
               :handler (fn [{{{:keys [x y]} :body} :parameters}]
                          {:status 200
                           :body {:total (+ x y)}})}}]]]

    {;;:reitit.middleware/transform dev/print-request-diffs ;; pretty diffs
       ;;:validate spec/validate ;; enable spec validation for route data
       ;;:reitit.spec/wrap spell/closed ;; strict top-level validation
     :exception pretty/exception
     :data {:coercion reitit.coercion.spec/coercion
            :muuntaja m/instance
            :middleware [;; swagger feature
                         swagger/swagger-feature
                           ;; query-params & form-params
                         parameters/parameters-middleware
                           ;; content-negotiation
                         muuntaja/format-negotiate-middleware
                           ;; encoding response body
                         muuntaja/format-response-middleware
                           ;; exception handling
                         exception/exception-middleware
                           ;; decoding request body
                         muuntaja/format-request-middleware
                           ;; coercing response bodys
                         coercion/coerce-response-middleware
                           ;; coercing request parameters
                         coercion/coerce-request-middleware
                           ;; multipart
                         multipart/multipart-middleware]}})
   (ring/routes
    (swagger-ui/create-swagger-ui-handler
     {:path "/swagger-ui"
      :config {:validatorUrl nil
               :operationsSorter "alpha"}})
    (ring/redirect-trailing-slash-handler #_{:method :add})
    (fn handle-index
      ([request]
       (when (= (:uri request) "/")
         (->
          (ring.util.response/resource-response "index.html" {:root "public"})
          (ring.util.response/content-type "text/html"))))
      ([request respond raise]
       (respond (handle-index request))))
    (ring/create-resource-handler {:path "/"
                                   :root "public"
                                   :index-files ["index.html"]})
    (ring/create-default-handler))
   {:executor reitit.interceptor.sieppari/executor
    :interceptors [(muuntaja.interceptor/format-interceptor)]}))

(defn -main [& args]
  (println ::-main)
  (create-proc-ops channels {})
  #_(ipfscube.app.tray/create)
  (let [port 8080]
    #_(jetty/run-jetty #'app {:port port :host "0.0.0.0" :join? false :async? true})
    (aleph/start-server (aleph/wrap-ring-async-handler #'app) {:port port :host "0.0.0.0"})
    (println (format "server running in port %d" port)))
  (app.chan/op
   {::op.spec/op-key ::app.chan/init
    ::op.spec/op-type ::op.spec/fire-and-forget}
   channels
   {}))


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