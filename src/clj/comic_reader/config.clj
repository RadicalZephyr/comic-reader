(ns comic-reader.config)

(defprotocol Config
  (database-uri [cfg])
  (norms-dir [cfg]))

(extend-type clojure.lang.APersistentMap
  Config
  (database-uri [cfg] (:database-uri cfg))

  (norms-dir [cfg] (:norms-dir cfg)))
