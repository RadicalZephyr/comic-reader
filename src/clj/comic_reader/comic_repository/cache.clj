(ns comic-reader.comic-repository.cache
  (:require [comic-reader.comic-repository :as repo]
            [clojure.core.async :as async :refer [<!]]))

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
    )

  (-previous-locations [this comic-id location n]
    )

  (-next-locations [this comic-id location n]
    )

  (-image-tag [this site-id location]
    ))

(defn new-caching-repository []
  (map->CachingRepository {}))
