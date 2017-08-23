(ns comic-reader.comic-repository.scraper
  (:require [clojure.core.async :as async]
            [clojure.set :as set]
            [clojure.string :as str]
            [comic-reader.comic-repository :as repo]
            [comic-reader.site-scraper :as site-scraper]
            [comic-reader.util :as util]))

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
      (dissoc :url :name)
      (update :page/number parse-number)))

(defn- page-seq [process sentinel scraper site chapters]
  (lazy-seq
   (if (seq chapters)
     (let [chapter (first chapters)
           location-base {:location/chapter (format-chapter chapter)}
           location-with-page #(assoc location-base :location/page (format-page %))]
       (concat
        (->> (first chapters)
             (site-scraper/list-pages scraper site)
             (map location-with-page)
             process)
        (page-seq process sentinel scraper site (rest chapters))))
     [{:location/boundary sentinel}])))

(defn- locations-for [direction scraper site comic-id chapter page]
  (let [processing-fn {:forward identity
                       :backward reverse}
        sentinel-val {:forward  :boundary/last
                      :backward :boundary/first}]
    (cond->> (site-scraper/list-chapters scraper site comic-id)
      (= direction :backward) (reverse)
      chapter (drop-while #(not= (format-chapter %) chapter))
      :always (page-seq (processing-fn direction) (sentinel-val direction) scraper site)
      page (drop-while #(not= (:location/page %) page))
      page (drop 1))))


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
  {:site/id (keyword site-id)
   :site/name (titleize site-id)})

(defn- format-comic [site-name comic]
  (let [comic (set/rename-keys comic {:id :comic/id
                                      :url :comic/url
                                      :name :comic/name})]
    (update comic :comic/id #(util/make-comic-id site-name %))))

(defrecord ScraperRepository [scraper]
  repo/ComicRepository
  (-list-sites [this]
    (async/thread
      (map format-site (site-scraper/list-sites scraper))))

  (-list-comics [this site-id]
    (async/thread
      (map #(format-comic (name site-id) %) (site-scraper/list-comics scraper (name site-id)))))

  (-previous-locations [this comic-id location n]
    (let [{:keys [:location/page :location/chapter]} location]
      (when chapter
        (async/thread
          (take n (locations-for :backward scraper (namespace comic-id) (name comic-id) chapter page))))))

  (-next-locations [this comic-id location n]
    (let [{:keys [:location/page :location/chapter]} location]
      (async/thread
        (take n (locations-for :forward scraper (namespace comic-id) (name comic-id) chapter page)))))

  (-image-tag [this site-id {page :location/page}]
    (when page
      (async/thread (site-scraper/get-page-image scraper (name site-id) page)))))

(defn new-scraper-repo []
  (map->ScraperRepository {}))
