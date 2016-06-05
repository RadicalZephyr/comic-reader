(ns comic-reader.comic-repository.scraper
  (:require [comic-reader.comic-repository.protocol :as protocol]
            [comic-reader.site-scraper :as site-scraper]))

(defn page-seq [scraper site chapters]
  (lazy-seq
   (if (seq chapters)
     (let [chapter (first chapters)
           location-base {:chapter chapter}]
       (concat (map #(assoc location-base :page %)
                    (site-scraper/list-pages scraper site (first chapters)))
               (page-seq scraper site (rest chapters))))
     nil)))

(defrecord ScraperRepository [scraper]
  protocol/ComicRepository
  (previous-pages [this site comic-id {:keys [chapter page]} n])

  (next-pages [this site comic-id {:keys [chapter page]} n]
    (let [chapters (site-scraper/list-chapters scraper site comic-id)]
      (cond->> (page-seq scraper site chapters)
        page (drop-while #(not= (:page %) page))
        page (drop 1)
        :always (take n)))))

(defn new-scraper-repo []
  (map->ScraperRepository {}))
