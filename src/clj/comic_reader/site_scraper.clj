(ns comic-reader.site-scraper
  (:require [clojure.string :as str]
            [comic-reader.sites :as sites]
            [comic-reader.sites.protocol :refer :all]
            [comic-reader.sites.read :as site-read]
            [com.stuartsierra.component :as component]))

(defn make-site-entry [site-name]
  (try
    [site-name (sites/->MangaSite (site-read/read-site-options site-name))]
    (catch IllegalArgumentException e
      [site-name nil])
    (catch RuntimeException e
      [site-name nil])))

(defn get-sites []
  (->> (site-read/get-all-sites)
       (map make-site-entry)
       flatten
       (apply hash-map)))

(defprotocol ISiteScraper
  (list-sites  [this]
               "Returns the list of sites this scraper supports.")
  (list-comics [this site-name]
               "Returns the list of comics available at this site.")
  (list-chapters [this site-name comic-id]
                 "Returns the list of chapter data for the given comic at the given site.")
  (list-pages [this site-name comic-chapter]
              "Returns the list of page data for the given site, comic and chapter.")
  (get-page-image [this site-name comic-page]
                  "Returns the hiccup image tag for the given site, and page url."))

(defrecord SiteScraper [sites]
  ISiteScraper

  (list-sites [this]
    (keys sites))

  (list-comics [this site-name]
    (get-comic-list (get sites site-name)))

  (list-chapters [this site-name comic-id]
    (get-chapter-list (get sites site-name) comic-id))

  (list-pages [this site-name comic-chapter]
    (get-page-list (get sites site-name) comic-chapter))

  (get-page-image [this site-name comic-page]
    (get-image-data (get sites site-name) comic-page))

  component/Lifecycle

  (start [component]
    (println ";; Loading site definitions...")
    (let [sites (dissoc (get-sites) "test-site")]
      (println ";;   Found sites: " (str/join ", " (keys sites)))
      (assoc component :sites sites)))

  (stop [component] component))

(defn new-site-scraper []
  (map->SiteScraper {:sites nil}))
