(ns comic-reader.ui.reader
  (:require [comic-reader.ui.image :as image]))

(defn- image-id [image]
  (:image/location image))

(defn- make-comic-image [set-current-location image]
  (with-meta
    [image/comic-image #(set-current-location (:image/location image)) (:image/tag image)]
    {:key (image-id image)}))

(defn comic-image-list [set-current-location images current-location]
  [:div (map #(make-comic-image set-current-location %) images)])
