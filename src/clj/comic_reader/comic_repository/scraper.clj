(ns comic-reader.comic-repository.scraper
  (:require [comic-reader.comic-repository.protocol :as protocol]
            [comic-reader.site-scraper :as site-scraper]))

(defrecord ScraperRepository [scraper]
  protocol/ComicRepository
  (previous-pages [this comic location n])
  (next-pages [this comic location n]))

(defn new-scraper-repo []
  (map->ScraperRepository {}))
