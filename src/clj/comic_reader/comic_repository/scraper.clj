(ns comic-reader.comic-repository.scraper
  (:require [comic-reader.comic-repository.protocol :as protocol]
            [comic-reader.site-scraper :as site-scraper]))

(defn page-seq [scraper site chapters]
  (lazy-seq
   (concat (site-scraper/list-pages scraper site (first chapters))
           (page-seq scraper site (rest chapters)))))

(defrecord ScraperRepository [scraper]
  protocol/ComicRepository
  (previous-pages [this site comic-id location n])

  (next-pages [this site comic-id location n]
    (let [chapters (site-scraper/list-chapters scraper site comic-id)]
      (cond->> (page-seq scraper site chapters)
        location (drop-while #(not= % location))
        :always (take n)))))

(defn new-scraper-repo []
  (map->ScraperRepository {}))
