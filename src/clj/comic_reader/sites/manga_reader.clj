(ns comic-reader.sites.manga-reader
  (:require [comic-reader.sites.protocol :refer [MangaSite]]))

(deftype MangaReader []
  MangaSite
  (get-comic-list [this]
    [])

  (get-chapter-list [this comic-id]
    [])

  (get-page-list [this comic-id chapter]
    [])

  (get-image-data [this comic-id chapter page]
    []))

(def manga-reader
  (MangaReader.))
