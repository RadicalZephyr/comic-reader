(ns comic-reader.comic-repository.memory
  (:require [comic-reader.comic-repository.protocol :as protocol]))

(defrecord MemoryRepository [config site comic]
  protocol/ComicRepository
  (get-first-location [this])
  (previous-pages [this location n])
  (next-pages [this location n]))

(defn new-repository []
  (map->MemoryRepository {}))
