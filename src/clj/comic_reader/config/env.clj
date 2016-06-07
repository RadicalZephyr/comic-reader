(ns comic-reader.config.env
  (:require [com.stuartsierra.component :as component]
            [comic-reader.config :as config]
            [environ.core :refer [env]]))

(defn- env-key-and-key [key-or-vec]
  (if (vector? key-or-vec)
    key-or-vec
    [key-or-vec key-or-vec]))

(defn- assoc-env [cfg key]
  (let [[env-key key] (env-key-and-key key)]
   (if-let [val (env env-key)]
     (assoc cfg key val)
     cfg)))

(defrecord EnvConfig [database-uri norms-dir server-port]
  component/Lifecycle

  (start [component]
    (println "Comic-Reader: Loading configuration...")
    (reduce assoc-env component [:database-uri
                                 :norms-dir
                                 [:port :server-port]]))

  (stop [component]
    component)

  config/Config
  (database-uri [cfg] (:database-uri cfg))

  (norms-dir [cfg] (:norms-dir cfg))

  (server-port [cfg] (:server-port cfg)))

(defn new-env-config [defaults]
  (map->EnvConfig defaults))
