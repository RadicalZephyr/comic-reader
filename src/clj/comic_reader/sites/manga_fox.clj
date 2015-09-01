(ns comic-reader.sites.manga-fox
  (:require [comic-reader.sites.protocol :refer [MangaSite]]))

(deftype MangaFox []
  MangaSite
  (get-comic-list [this]
    [])

  (get-chapter-list [this comic-id]
    [])

  (get-page-list [this comic-id chapter]
    [])

  (get-image-data [this comic-id chapter page]
    []))

(def manga-fox
  (MangaFox.))
