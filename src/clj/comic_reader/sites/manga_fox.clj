(ns comic-reader.sites.manga-fox
  (:require [comic-reader.sites.protocol :refer [MangaSite]]
            [comic-reader.scrape :as scrape]))

(def ^:private image-selector [:div#viewer :img#image])

(defn extract-image-tag [html]
  (scrape/extract-image-tag html image-selector))

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
