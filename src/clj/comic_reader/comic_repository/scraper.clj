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
  (previous-locations [this site comic-id {:keys [chapter page]} n]
    (when (and chapter page)
      (let [chapters (site-scraper/list-chapters scraper site comic-id)
            chapter (some #(when (= % chapter) %) (reverse chapters))
            pages (site-scraper/list-pages scraper site chapter)]
        (->> pages
             reverse
             (drop-while #(not= % page))
             (drop 1)
             (take n)))))

  (next-locations     [this site comic-id {:keys [chapter page]} n]
    (cond->> (site-scraper/list-chapters scraper site comic-id)
      chapter (drop-while #(not= % chapter))
      :always (page-seq scraper site)
      page (drop-while #(not= (:page %) page))
      page (drop 1)
      :always (take n))))

(defn new-scraper-repo []
  (map->ScraperRepository {}))
