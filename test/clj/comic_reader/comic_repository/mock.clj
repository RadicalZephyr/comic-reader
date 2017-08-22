(ns comic-reader.comic-repository.mock
  (:require [comic-reader.comic-repository :as repo]
            [clojure.core.async :as async]))

(extend-type clojure.lang.IPersistentMap
  repo/ComicRepository
  (-list-sites [this]
    (async/thread (:sites this)))

  (-list-comics [this site]
    (async/thread (get-in this [:comics site])))

  (-previous-locations [this comic-id location n]
    (async/thread (take n (get-in this [(keyword (namespace comic-id)) :comics (name comic-id) location :previous-locations]))))

  (-next-locations [this comic-id location n]
    (async/thread (take n (get-in this [(keyword (namespace comic-id)) :comics (name comic-id) location :next-locations]))))

  (-image-tag [this site location]
    (async/thread (get-in this [site location]))))

(defn mock-repo
  ([] {})
  ([& {:as mock-data}] mock-data))
