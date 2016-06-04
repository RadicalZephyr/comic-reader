(ns comic-reader.comic-repository.datomic
  (:require [comic-reader.comic-repository.protocol :as protocol]))

(defrecord DatomicRepository [database source-repo]
  protocol/ComicRepository
  (previous-pages [this site comic-id location n])
  (next-pages [this site comic-id location n]))

(defn new-repository []
  (map->DatomicRepository {}))
