(ns comic-reader.comic-repository.mock
  (:require [comic-reader.comic-repository :as repo]))

(extend-type clojure.lang.IPersistentMap
  repo/ComicRepository
  (list-sites [this]
    (:sites this))

  (list-comics [this site]
    (get-in this [:comics site]))

  (previous-locations [this site comic-id location n]
    (take n (get-in this [site :comics comic-id location :previous-locations])))

  (next-locations [this site comic-id location n]
    (take n (get-in this [site :comics comic-id location :next-locations])))

  (image-tag [this site location]
    (get-in this [site location])))

(defn mock-repo
  ([] {})
  ([& {:as mock-data}] mock-data))
