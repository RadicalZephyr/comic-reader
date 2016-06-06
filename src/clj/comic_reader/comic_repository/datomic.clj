(ns comic-reader.comic-repository.datomic
  (:require [comic-reader.comic-repository :as repo]))

(defrecord DatomicRepository [database source-repo]
  repo/ComicRepository
  (list-sites         [this])
  (list-comics        [this site])
  (previous-locations [this site comic-id location n])
  (next-locations     [this site comic-id location n])
  (image-tag          [this site location]))

(defn new-repository []
  (map->DatomicRepository {}))
