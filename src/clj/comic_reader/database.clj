(ns comic-reader.database
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [comic-reader.config :as config]
            [comic-reader.database.norms :as norms]
            [datomic.api :as d]
            [io.rkn.conformity :as conformity]))

(defn- site-record [site-name & {:keys [temp-id]}]
  (let [temp-id (if temp-id
                  (d/tempid :db.part/user temp-id)
                  (d/tempid :db.part/user))]
    {:db/id temp-id
     :site/id (d/squuid)
     :site/name site-name}))

(defn- create-database [config]
  (d/create-database (config/database-uri config)))

(defn- connect [config]
  (d/connect (config/database-uri config)))

(defn- setup-and-connect-to-database [config]
  (when (config/database-uri config)
    (log/info "Connecting to database...")
    (let [do-seeds? (create-database config)
          conn (connect config)]
      ;; Conform database to schemas here
      (when-let [norms-dir (config/norms-dir config)]
        (log/info "Conforming database to norms...")
        (conformity/ensure-conforms conn (norms/norms-map norms-dir)))

      ;; Add seeds if database was newly created
      ;; (when do-seeds?
      ;;   @(d/transact conn (seed/data)))
      conn)))

(defprotocol Database
  (get-sites [database] "Returns a seq of the stored site records.")
  (store-sites [database sites] "Store a seq of site records."))

(defrecord DatomicDatabase [config conn]

  component/Lifecycle
  (start [component]
    (if-let [config (:config component)]
      (assoc component :conn
             (setup-and-connect-to-database config))
      component))

  (stop [component]
    (log/info "Disconnecting from database...")
    (dissoc component :conn))

  Database
  (get-sites [database]
    (let [db (d/db conn)]
      (d/q '[:find [(pull ?e [:site/name]) ...]
             :where [?e :site/name]]
           db)))

  (store-sites [database sites]
    (d/transact conn (mapv site-record sites))))

(defn database? [e]
  (instance? DatomicDatabase e))

(defn new-database []
  (map->DatomicDatabase {}))
