(ns comic-reader.comic-repository.scraper
  (:require [clojure.string :as str]
            [comic-reader.comic-repository :as repo]
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

(defn- locations-for [scraper site direction chapters chapter page n]
  (let [processing-fn {:forward identity
                       :backward reverse}]
    (cond->> chapters
      chapter (drop-while #(not= % chapter))
      :always (page-seq (processing-fn direction) scraper site)
      page (drop-while #(not= (:page %) page))
      page (drop 1)
      :always (take n))))

(defn- capitalize-all [words]
  (map str/capitalize words))

(defn- spacify [words]
  (str/join " " words))

(defn- titleize [id]
  (-> id
      (str/split #"-")
      capitalize-all
      spacify))

(defn- format-site [site-id]
  {:id site-id
   :name (titleize site-id)})

(defrecord ScraperRepository [scraper]
  repo/ComicRepository
  (list-sites [this]
    (map format-site (site-scraper/list-sites scraper)))

  (list-comics [this site]
    (site-scraper/list-comics scraper site))

  (previous-locations [this site comic-id {:keys [chapter page]} n]
    (when chapter
      (locations-for scraper site :backward (reverse (site-scraper/list-chapters scraper site comic-id)) chapter page n)))

  (next-locations [this site comic-id {:keys [chapter page]} n]
    (locations-for scraper site :forward (site-scraper/list-chapters scraper site comic-id) chapter page n))

  (image-tag [this site {:keys [page]}]
    (when page
      (site-scraper/get-page-image scraper site page))))

(defn new-scraper-repo []
  (map->ScraperRepository {}))
