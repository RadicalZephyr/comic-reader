(ns comic-reader.site-scraper.mock
  (:require [comic-reader.site-scraper :as site-scraper]))

(extend-type clojure.lang.IPersistentMap
  site-scraper/PSiteScraper

  (list-sites [this]
    (:sites this))

  (list-comics [this site-name]
    (get-in this [:comics site-name]))

  (list-chapters [this site-name comic-id]
    (get-in this [:chapters site-name comic-id]))

  (list-pages [this site-name comic-chapter]
    (get-in this [:pages site-name comic-chapter]))

  (get-page-image [this site-name comic-page]
    (get-in this [:images site-name comic-page])))

(defn mock-scraper
  ([] {})
  ([& {:as mock-data}] mock-data))
