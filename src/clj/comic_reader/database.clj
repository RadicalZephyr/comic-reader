(ns comic-reader.database
  (:require [com.stuartsierra.component :as component]
            [comic-reader.database.norms :as norms]
            [datomic.api :as d]
            [io.rkn.conformity :as c]))

(defrecord Database [config conn]
  component/Lifecycle

  (start [component]
    (if-let [{:keys [uri] :as config} (:config component)]
      (do
        (println "Comic-Reader: Connecting to database...")
        (let [do-seeds? (d/create-database uri)
              conn (d/connect uri)]
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

(defn new-database []
  (map->Database {}))

(defn get-conn [database]
  (or (:conn database)
      (when-let [uri (:uri database)]
        (d/connect uri))))
