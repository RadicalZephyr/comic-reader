(ns comic-reader.sites.manga-here
  (:require [comic-reader.sites.protocol :refer [PMangaSite]]))

(deftype MangaHere []
  PMangaSite
  (get-comic-list [this]
    [])

  (get-chapter-list [this comic-id]
    [])

  (get-page-list [this comic-chapter]
    [])

  (get-image-data [this comic-id chapter page]
    []))

(def manga-here
  (MangaHere.))
