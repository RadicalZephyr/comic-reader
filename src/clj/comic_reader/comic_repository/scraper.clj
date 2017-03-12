(ns comic-reader.comic-repository.scraper
  (:require [clojure.string :as str]
            [comic-reader.comic-repository :as repo]
            [comic-reader.site-scraper :as site-scraper]
            [clojure.set :as set]))

(defn- format-chapter [chapter]
  (set/rename-keys chapter {:name :chapter/title
                            :ch-num :chapter/number}))

(defn- parse-number [page-number]
  (if (string? page-number)
    (Integer/parseInt page-number)
    page-number))

(defn- format-page [page]
  (-> page
      (set/rename-keys {:url :page/url
                        :name :page/number})
      (dissoc :name)
      (update :page/number parse-number)))

(defn- unformat-page [page]
  (-> page
      (set/rename-keys {:page/url :url})
      (dissoc :page/number)
      (assoc :name (str (:page/number page)))))

(defn- page-seq [process scraper site chapters]
  (lazy-seq
   (if (seq chapters)
     (let [chapter (first chapters)
           location-base {:location/chapter (format-chapter chapter)}
           location-with-page #(assoc location-base :location/page (format-page %))]
       (concat (->> (first chapters)
                    (site-scraper/list-pages scraper site)
                    (map location-with-page)
                    process)
               (page-seq process scraper site (rest chapters))))
     nil)))

(defn- locations-for [scraper site direction chapters chapter page n]
  (let [processing-fn {:forward identity
                       :backward reverse}]
    (cond->> chapters
      (= direction :backward) (reverse)
      chapter (drop-while #(not= (format-chapter %) chapter))
      :always (page-seq (processing-fn direction) scraper site)
      page (drop-while #(not= (:location/page %) page))
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
  {:site/id site-id
   :site/name (titleize site-id)})

(defn- format-comic [comic]
  (set/rename-keys comic {:id :comic/id
                          :name :comic/name
                          :url :comic/url}))

(defrecord ScraperRepository [scraper]
  repo/ComicRepository
  (list-sites [this]
    (map format-site (site-scraper/list-sites scraper)))

  (list-comics [this site]
    (map format-comic (site-scraper/list-comics scraper site)))

  (previous-locations [this site comic-id location n]
    (let [{page :location/page chapter :location/chapter} location]
      (when chapter
        (doall
         (locations-for scraper site :backward (site-scraper/list-chapters scraper site comic-id) chapter page n)))))

  (next-locations [this site comic-id location n]
    (let [{page :location/page chapter :location/chapter} location]
      (doall
       (locations-for scraper site :forward (site-scraper/list-chapters scraper site comic-id) chapter page n))))

  (image-tag [this site {page :location/page}]
    (when page
      (site-scraper/get-page-image scraper site (unformat-page page)))))

(defn new-scraper-repo []
  (map->ScraperRepository {}))
