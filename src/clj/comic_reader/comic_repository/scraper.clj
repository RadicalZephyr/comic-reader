(ns comic-reader.comic-repository.scraper
  (:require [comic-reader.comic-repository.protocol :as protocol]
            [comic-reader.site-scraper :as site-scraper]))

(defrecord ScraperRepository [scraper]
  protocol/ComicRepository
  (previous-pages [this site comic-id location n])
  (next-pages [this site comic-id location n]
    (let [chapters (site-scraper/list-chapters scraper site comic-id)
          chapter (first chapters)
          pages (site-scraper/list-pages scraper site chapter)]
      (cond->> pages
        location (drop-while #(not= % location))
        :always (take n)))))

(defn new-scraper-repo []
  (map->ScraperRepository {}))
