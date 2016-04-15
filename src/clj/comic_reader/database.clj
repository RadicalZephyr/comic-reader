(ns comic-reader.database
  (:require [com.stuartsierra.component :as component]
            [datomic.api :as d]))

(def uri "datomic:dev://localhost:4334/comics")

(defrecord Database [uri conn]
  component/Lifecycle

  (start [component]
    (println "Comic-Reader: Connecting to database...")
    (let [uri (:uri component)
          do-seeds? (d/create-database uri)
          conn (d/connect uri)]
      ;; Conform database to schemas here

      ;; Add seeds if database was newly created
      ;; (when do-seeds?
      ;;   @(d/transact conn (seed/data)))
      (assoc component :conn conn)))

  (stop [component]
    (println "Comic-Reader: Disconnecting from database...")
    (dissoc component :conn)))

(defn new-database []
  (map->Database {}))

(defn get-conn [database]
  (or (:conn database)
      (when-let [uri (:uri database)]
        (d/connect uri))))
