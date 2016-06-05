(ns comic-reader.comic-repository.scraper
  (:require [comic-reader.comic-repository.protocol :as protocol]
            [comic-reader.site-scraper :as site-scraper]))

(defn- page-seq [process scraper site chapters]
  (lazy-seq
   (if (seq chapters)
     (let [chapter (first chapters)
           location-base {:chapter chapter}]
       (concat (->> (first chapters)
                    (site-scraper/list-pages scraper site)
                    (map #(assoc location-base :page %))
                    process)
               (page-seq process scraper site (rest chapters))))
     nil)))

(defrecord ScraperRepository [scraper]
  protocol/ComicRepository
  (previous-locations [this site comic-id {:keys [chapter page]} n]
    (when chapter
      (cond->> (site-scraper/list-chapters scraper site comic-id)
        :always reverse
        :always (drop-while #(not= % chapter))
        :always (page-seq reverse scraper site)
        page (drop-while #(not= (:page %) page))
        page (drop 1)
        :always (take n))))

  (next-locations     [this site comic-id {:keys [chapter page]} n]
    (cond->> (site-scraper/list-chapters scraper site comic-id)
      chapter (drop-while #(not= % chapter))
      :always (page-seq identity scraper site)
      page (drop-while #(not= (:page %) page))
      page (drop 1)
      :always (take n))))

(defn new-scraper-repo []
  (map->ScraperRepository {}))
