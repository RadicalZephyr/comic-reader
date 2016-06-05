(ns comic-reader.comic-repository.datomic
  (:require [comic-reader.comic-repository.protocol :as protocol]))

(defrecord DatomicRepository [database source-repo]
  protocol/ComicRepository
  (previous-pages [this site comic-id page n])
  (next-pages [this site comic-id page n]))

(defn new-repository []
  (map->DatomicRepository {}))
