(ns comic-reader.config.env
  (:require [com.stuartsierra.component :as component]
            [comic-reader.config :as config]
            [environ.core :refer [env]]))

(defrecord EnvConfig [database-uri norms-dir]
  component/Lifecycle

  (start [component]
    (println "Comic-Reader: Loading configuration...")
    (assoc component
           :database-uri (env :database-uri)
           :norms-dir    (env :norms-dir)))

  (stop [component]
    component)

  config/Config
  (database-uri [cfg] (:database-uri cfg))

  (norms-dir [cfg] (:norms-dir cfg)))

(defn new-env-config []
  (map->EnvConfig {}))
