(ns comic-reader.database
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [comic-reader.config :as config]
            [comic-reader.database.norms :as norms]
            [datomic.api :as d]
            [io.rkn.conformity :as conformity]))

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
  (connection [database] "Returns a connection to the database."))

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
  (connection [database] conn))

(defn database? [e]
  (instance? DatomicDatabase e))

(defn new-database []
  (map->DatomicDatabase {}))
