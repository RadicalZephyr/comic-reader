(ns comic-reader.comic-repository
  (:require [com.stuartsierra.component :as component]
            [datomic.api :as d]))

(def db-uri "datomic:mem://comic-reader")

(defrecord ComicRepository [db-uri connection comic-scraper]
  component/Lifecycle

  (start [component]
    (if connection
      component
      (do (d/create-database db-uri)

          (println "Connecting to database...")
          (let [conn (d/connect db-uri)]
            (assoc component :connection conn)))))

  (stop [component]
    (if (not connection)
      component
      (do (println "Releasing database connection...")
          (d/release connection)
          (assoc component :connection nil)))))

(defn new-repository [db-uri]
  (map->ComicRepository {:db-uri db-uri}))
