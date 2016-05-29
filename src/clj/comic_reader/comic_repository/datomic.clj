(ns comic-reader.comic-repository.datomic
  (:require [comic-reader.comic-repository.protocol :as protocol]))

(defrecord DatomicRepository [config conn site comic]
  protocol/ComicRepository
  (get-first-location [this])
  (previous-pages [this location n])
  (next-pages [this location n]))

(defn new-repository []
  (map->DatomicRepository {}))
