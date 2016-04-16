(ns comic-reader.database
  (:require [com.stuartsierra.component :as component]
            [comic-reader.database.norms :as norms]
            [datomic.api :as d]
            [io.rkn.conformity :as c]))

(defn- database-uri [config]
  (:database-uri config))

(defn- create-database [config]
  (d/create-database (database-uri config)))

(defn- connect [config]
  (d/connect (database-uri config)))

(defrecord Database [config conn]
  component/Lifecycle

  (start [component]
    (if-let [config (:config component)]
      (when (database-uri config)
        (println "Comic-Reader: Connecting to database...")
        (let [do-seeds? (create-database config)
              conn (connect config)]
          ;; Conform database to schemas here
          (when-let [norms-dir (:norms-dir config)]
            (println "Comic-Reader: Conforming database to norms...")
            (c/ensure-conforms conn (norms/norms-map norms-dir)))

          ;; Add seeds if database was newly created
          ;; (when do-seeds?
          ;;   @(d/transact conn (seed/data)))
          (assoc component :conn conn)))
      component))

  (stop [component]
    (println "Comic-Reader: Disconnecting from database...")
    (dissoc component :conn)))

(defn database? [e]
  (instance? Database e))

(defn new-database []
  (map->Database {}))

(defn get-conn [database]
  (or (:conn database)
      (connect (:config database))))
