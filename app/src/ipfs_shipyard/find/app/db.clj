(ns ipfs-shipyard.find.app.db
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >! <!! >!!  take! put! offer! poll! alt! alts! close! onto-chan!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.java.io :as io]
   [datahike.api]))


(comment

  (require
   '[ipfs-shipyard.find.app.db :as find.app.db]
   '[datahike.api]
   :reload)

  (let [peer-index 1]
    (io/make-parents (System/getProperty "user.dir") "volumes" (format "peer%s" peer-index) "db"))

  (def cfg {:store {:backend :file :path "./volumes/peer1/db"}})

  (datahike.api/create-database cfg)

  (def conn (datahike.api/connect cfg))

  (datahike.api/transact conn [{:db/ident :name
                                :db/valueType :db.type/string
                                :db/cardinality :db.cardinality/one}
                               {:db/ident :age
                                :db/valueType :db.type/long
                                :db/cardinality :db.cardinality/one}])

  (datahike.api/transact conn [{:name  "Alice", :age   20}
                               {:name  "Bob", :age   30}
                               {:name  "Charlie", :age   40}
                               {:age 15}])

  (datahike.api/q '[:find ?e ?n ?a
                    :where
                    [?e :name ?n]
                    [?e :age ?a]]
                  @conn)

  (datahike.api/transact conn {:tx-data [{:db/id 3 :age 25}]})

  (datahike.api/q {:query '{:find [?e ?n ?a]
                            :where [[?e :name ?n]
                                    [?e :age ?a]]}
                   :args [@conn]})

  (datahike.api/q '[:find ?a
                    :where
                    [?e :name "Alice"]
                    [?e :age ?a]]
                  (datahike.api/history @conn))

  (datahike.api/release conn)

  (datahike.api/delete-database cfg)


  ;
  )



