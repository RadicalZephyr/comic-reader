(ns comic-reader.comic-repository.cache
  (:require [clojure.core.async :as async :refer [<!]]
            [comic-reader.comic-repository :as repo]))

(defrecord CachingRepository [storage-repo source-repo]
  repo/ComicRepository
  (-list-sites [this]
    (async/go
      (let [stored-sites (<! (repo/list-sites storage-repo))]
        (if (seq stored-sites)
          stored-sites
          (let [fetched-sites (<! (repo/list-sites source-repo))]
            (repo/store-sites storage-repo fetched-sites)
            fetched-sites)))))

  (-list-comics [this site-id]
    (async/go
      (let [stored-comics (<! (repo/list-comics storage-repo site-id))]
        (if (seq stored-comics)
          stored-comics
          (let [fetched-comics (<! (repo/list-comics source-repo site-id))]
            (repo/store-comics storage-repo fetched-comics)
            fetched-comics)))))

  (-previous-locations [this comic-id location n]
    )

  (-next-locations [this comic-id location n]
    )

  (-image-tag [this site-id location]
    ))

(defn new-caching-repository []
  (map->CachingRepository {}))
