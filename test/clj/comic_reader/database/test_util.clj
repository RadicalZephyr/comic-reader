(ns comic-reader.database.test-util
  (:require [com.stuartsierra.component :as component]
            [comic-reader.database.datomic :as db]))

(defmacro with-test-system [[system-binding system-expression & bindings] & body]
  `(let [~system-binding (component/start ~system-expression)
         ~@bindings]
     (try
       ~@body
       (finally
         (component/stop ~system-binding)))))

(defrecord DatabaseCleaner [database]
  component/Lifecycle
  (start [this]
    this)

  (stop [this]
    (db/destroy database)
    this))

(defn new-database-cleaner []
  (map->DatabaseCleaner {}))
