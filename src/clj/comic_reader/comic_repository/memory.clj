(ns comic-reader.comic-repository.memory
  (:require [comic-reader.comic-repository :as repo]
            [clojure.core.async :as async :refer [<!]]))

(defrecord MemoryRepository [store]
  repo/ComicRepository
  (-list-sites [this]
    (async/thread (:sites @store)))

  (-list-comics [this site-id]
    (async/thread (get-in @store [:comics site-id])))

  (-previous-locations [this comic-id location n]
    (async/thread
      (->> (get-in @store [:locations comic-id])
           rseq
           (drop-while #(not= % location))
           (take n))))

  (-next-locations [this comic-id location n]
    (async/thread
      (->> (get-in @store [:locations comic-id])
           seq
           (drop-while #(not= % location))
           (take n))))

  (-image-tag [this site location]
    (async/thread (get-in @store [site location])))

  repo/WritableComicRepository
  (-store-sites [this sites]
    (swap! store assoc :sites sites))

  (-store-comics [this site-id comics]
    (swap! store assoc-in [:comics site-id] comics))

  (-store-locations [this comic-id locations]
    (let [sorted-locations (->> locations
                                (sort-by #(get-in % [:location/page :page/number]))
                                (sort-by #(get-in % [:location/chapter :chapter/number])))]
      (swap! store assoc-in [:locations comic-id] (vec sorted-locations)))))

(defn new-memory-repository []
  (map->MemoryRepository {:store (atom {})}))
