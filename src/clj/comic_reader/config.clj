(ns comic-reader.config
  (:require [com.stuartsierra.component :as component]
            [environ.core :refer [env]]))

(defprotocol PConfig
  (database-uri [cfg])
  (norms-dir [cfg]))

(defrecord Config [database-uri norms-dir]
  component/Lifecycle

  (start [component]
    (println "Comic-Reader: Loading configuration...")
    (assoc component
           :database-uri (env :database-uri)
           :norms-dir    (env :norms-dir)))

  (stop [component]
    component)

  PConfig
  (database-uri [cfg] (:database-uri cfg))

  (norms-dir [cfg] (:norms-dir cfg)))

(extend-type clojure.lang.APersistentMap
  PConfig
  (database-uri [cfg] (:database-uri cfg))

  (norms-dir [cfg] (:norms-dir cfg)))

(defn new-config []
  (map->Config {}))
