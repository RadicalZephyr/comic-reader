(ns comic-reader.comic-repository.cache
  (:require [comic-reader.comic-repository :as repo]))

(defrecord CachingRepository [storage-repo source-repo]
  repo/ComicRepository
  (list-sites [this]
    (let [sites (repo/list-sites source-repo)]
      (repo/store-sites storage-repo sites)
      sites))

  (list-comics [this site-id]
    (let [comics (repo/list-comics source-repo site-id)]
      (repo/store-comics storage-repo site-id comics)
      comics))

  (previous-locations [this site-id comic-id location n]
    (let [locations (repo/previous-locations source-repo site-id comic-id location n)]
      (repo/store-locations storage-repo site-id comic-id locations)
      locations))

  (next-locations [this site-id comic-id location n]
    (let [locations (repo/next-locations source-repo site-id comic-id location n)]
      (repo/store-locations storage-repo site-id comic-id locations)
      locations))

  (image-tag [this site-id location]
    (repo/image-tag source-repo site-id location)))

(defn new-caching-repository []
  (map->CachingRepository {}))
