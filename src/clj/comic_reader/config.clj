(ns comic-reader.config
  (:require [com.stuartsierra.component :as component]
            [environ.core :refer [env]]))

(defrecord Config [database-uri norms-dir]
  component/Lifecycle

  (start [component]
    (println "Comic-Reader: Loading configuration...")
    (assoc component
           :database-uri (env :database-uri)
           :norms-dir    (env :norms-dir)))

  (stop [component]))

(defn new-config []
  (map->Config {}))
