(ns comic-reader.comic-repository.cache
  (:require [comic-reader.comic-repository :as repo]
            [clojure.core.async :as async :refer [<!]]))

(defrecord CachingRepository [storage-repo source-repo]
  repo/ComicRepository
  (-list-sites [this]
    (async/go
      (let [sites (<! (repo/list-sites source-repo))]
        (repo/store-sites storage-repo sites)
        sites)))

  (-list-comics [this site-id]
    (async/go
      (let [comics (<! (repo/list-comics source-repo site-id))]
        (repo/store-comics storage-repo site-id comics)
        comics)))

  (-previous-locations [this comic-id location n]
    (async/go
      (let [locations (<! (repo/previous-locations source-repo comic-id location n))]
        (repo/store-locations storage-repo comic-id locations)
        locations)))

  (-next-locations [this comic-id location n]
    (async/go
      (let [locations (<! (repo/next-locations source-repo comic-id location n))]
        (repo/store-locations storage-repo comic-id locations)
        locations)))

  (-image-tag [this site-id location]
    (async/go
      (let [image-tag (<! (repo/image-tag source-repo site-id location))]

        image-tag))))

(defn new-caching-repository []
  (map->CachingRepository {}))
