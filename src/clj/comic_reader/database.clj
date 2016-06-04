(ns comic-reader.database
  (:require [com.stuartsierra.component :as component]
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
    (println "Comic-Reader: Connecting to database...")
    (let [do-seeds? (create-database config)
          conn (connect config)]
      ;; Conform database to schemas here
      (when-let [norms-dir (config/norms-dir config)]
        (println "Comic-Reader: Conforming database to norms...")
        (conformity/ensure-conforms conn (norms/norms-map norms-dir)))

      ;; Add seeds if database was newly created
      ;; (when do-seeds?
      ;;   @(d/transact conn (seed/data)))
      conn)))

(defprotocol Database
  (get-conn [database] "Return the connection to the database."))

(defrecord DatomicDatabase [config conn]

  component/Lifecycle

  (start [component]
    (if-let [config (:config component)]
      (assoc component :conn
             (setup-and-connect-to-database config))
      component))

  (stop [component]
    (println "Comic-Reader: Disconnecting from database...")
    (dissoc component :conn))

  Database
  (get-conn [database] (:conn database)))

(defn database? [e]
  (instance? Database e))

(defn new-database []
  (map->DatomicDatabase {}))
