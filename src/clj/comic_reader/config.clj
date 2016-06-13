(ns comic-reader.config)

(defprotocol Config
  (testing? [cfg])
  (database-uri [cfg])
  (norms-dir [cfg])
  (server-port [cfg]))

(extend-protocol Config
  nil
  (testing? [cfg])
  (database-uri [cfg])
  (norms-dir [cfg])
  (server-port [cfg])

  clojure.lang.APersistentMap
  (testing? [cfg] (:testing? cfg))

  (database-uri [cfg] (:database-uri cfg))

  (norms-dir [cfg] (:norms-dir cfg))

  (server-port [cfg] (:server-port cfg)))
