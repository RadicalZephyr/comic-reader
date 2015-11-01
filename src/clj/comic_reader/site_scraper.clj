(ns comic-reader.site-scraper
  (:require [clojure.java.io :as io]
            [clojure.string :as s]
            [clojure.string :as str]
            [comic-reader.scrape :as scrape]
            [comic-reader.sites.protocol :refer :all]
            [comic-reader.sites.read :as site-read]
            [comic-reader.sites.util :as util]
            [comic-reader.util :refer [safe-read-string]]
            [com.stuartsierra.component :as component]))

;; TODO: Remove this dynamic var.  Pass the options through all of the
;; function calls explicitly.
(def ^:dynamic options
  {:root-url                     nil
   :manga-list-format            nil
   :manga-url-format             nil
   :manga-url-suffix-pattern     nil

   :comic->url-format            nil

   :chapter-list-selector        nil
   :comic-list-selector          nil
   :image-selector               nil
   :page-list-selector           nil

   :chapter-number-pattern       nil
   :chapter-number-match-pattern nil

   :comic-link-name-normalize    nil
   :comic-link-url-normalize     nil

   :chapter-link-name-normalize  nil
   :chapter-link-url-normalize   nil

   :page-normalize-format        nil
   :page-normalize-pattern       nil})


;;; Data functions
(defn root-url
  "The root URL of the manga site without a trailing slash."
  []
  (:root-url options))

(defn manga-list-format
  "A format string that when used to format the root URL will produce
  the URL of the list of all manga."
  []
  (:manga-list-format options))

(defn manga-url-format
  "A format string that when used to format the root URL will produce
  the prefix of all comic urls."
  []
  (:manga-url-format options))

(defn manga-url-suffix-pattern
  "A regular expression to match the comic-id portion of a comics
  chapter list URL."
  []
  (:manga-url-suffix-pattern options))

(defn comic->url-format
  "A format string that when used to format the manga URL and a comic
  id will produce the URL of a comics chapter page."
  []
  (:comic->url-format options))

(defn comic-list-selector
  "An enlive selector for selecting all of the links to comics on the
  comic list page."
  []
  (:comic-list-selector options))

(defn chapter-number-match-pattern
  "A regular expression that can extract the number from a chapter
  url."
  []
  (:chapter-number-match-pattern options))

(defn chapter-list-selector
  "An enlive selector that extracts all the chapter links from the
  comics chapter page."
  []
  (:chapter-list-selector options))

(defn page-normalize-pattern
  "A regular expression to extract the page number from the content."
  []
  (:page-normalize-pattern options))

(defn page-normalize-format
  "A format string that when used to format the base-url of a comic
  and the page number will make the url for that specific page."
  []
  (:page-normalize-format options))

(defn chapter-number-pattern
  "A regular expression that will match the chapter specific portion
  of a url for creating a base url."
  []
  (:chapter-number-pattern options))

(defn page-list-selector
  "An enlive selector that extracts the list of pages from a manga
  chapter page."
  []
  (:page-list-selector options))

(defn image-selector
  "An enlive selector that extracts the image tag from a manga page."
  []
  (:image-selector options))

(defn comic-link-url-normalize
  "An expression that evals to a function that will normalize the link
  url."
  []
  (eval (:comic-link-url-normalize options)))

(defn comic-link-name-normalize
  "An expression that evals to a function that will normalize the link
  name."
  []
  (eval (:comic-link-name-normalize options)))

(defn chapter-link-url-normalize
  "An expression that evals to a function that will normalize the link
  url."
  []
  (eval (:chapter-link-url-normalize options)))

(defn chapter-link-name-normalize
  "An expression that evals to a function that will normalize the link
  name."
  []
  (eval (:chapter-link-name-normalize options)))


;; ############################################################
;; ## Get Comic List Functions
;; ############################################################

(defn manga-url []
  (format (manga-url-format) (root-url)))

(defn manga-list-url []
  (format (manga-list-format) (root-url)))

(defn manga-pattern []
  (re-pattern (str (manga-url) (manga-url-suffix-pattern))))

(defn comic-link->map [{name :content
                  {url :href} :attrs}]
  {:name ((comic-link-name-normalize) name)
   :url  ((comic-link-url-normalize)  url)})

(defn comic-link-add-id [{:keys [url] :as comic-map}]
  (let [[_ data] (re-find (manga-pattern) url)]
    (assoc comic-map
           :id data)))

(defn comic-link-normalize [link]
  (-> link
      comic-link->map
      comic-link-add-id))

(defn extract-comics-list [html]
  (scrape/extract-list html
                       (comic-list-selector)
                       comic-link-normalize))


;; ############################################################
;; ## Get Chapter List functions
;; ############################################################

(defn chapter-link->map [{name :content
                          {url :href} :attrs}]
  {:name ((chapter-link-name-normalize) name)
   :url  ((chapter-link-url-normalize)  url)})

(defn chapter-link-add-ch-num [{:keys [url]
                                :as comic-map}]
  (let [[_ data] (re-find (chapter-number-match-pattern) url)]
    (assoc comic-map :ch-num (safe-read-string data))))

(defn chapter-link-normalize [link]
  (-> link
      chapter-link->map
      chapter-link-add-ch-num))

(defn extract-chapters-list [html comic-url]
  (->> (scrape/extract-list html
                            (chapter-list-selector)
                            chapter-link-normalize)
       (filter :ch-num)
       (sort-by :ch-num)))

(defn comic->url [comic-id]
  (format (comic->url-format) (manga-url) comic-id))


;; ############################################################
;; ## Get Page List functions
;; ############################################################

(defn gen-extract-pages-list-normalize [base-url]
  (util/html-fn {[name] :content {:keys [value]} :attrs}
    (let [page-number (re-find (page-normalize-pattern)
                               value)]
      {:name name
       :url (format (page-normalize-format)
                    base-url
                    page-number)})))

(defn extract-pages-list [html chapter-url]
  (let [base-url (s/replace chapter-url
                            (chapter-number-pattern)
                            "")
        normalize (gen-extract-pages-list-normalize base-url)]
    (scrape/extract-list html
                         (page-list-selector)
                         normalize)))


;; ############################################################
;; ## Get Image Data functions
;; ############################################################

(defn extract-image-tag [html]
  (scrape/extract-image-tag html (image-selector)))


;; ############################################################
;; ## Protocol functions
;; ############################################################

(deftype MangaSite [opt-map]
  PMangaSite

  (call-with-options [this f]
    (binding [options opt-map]
      (f)))

  (get-comic-list [this]
    (binding [options opt-map]
      (-> (manga-list-url)
          scrape/fetch-url
          extract-comics-list
          doall)))

  (get-chapter-list [this comic-id]
    (binding [options opt-map]
      (if-let [comic-url (comic->url comic-id)]
        (-> comic-url
            scrape/fetch-url
            (extract-chapters-list comic-url)
            doall))))

  (get-page-list [this comic-chapter]
    (binding [options opt-map]
      (if-let [chapter-url (:url comic-chapter)]
        (-> chapter-url
            scrape/fetch-url
            (extract-pages-list chapter-url)
            doall))))

  (get-image-data [this {page-url :url}]
    (binding [options opt-map]
      (-> page-url
          scrape/fetch-url
          extract-image-tag))))


(defn make-site-entry [site-name]
  (try
    [site-name (->MangaSite (site-read/read-site-options site-name))]
    (catch IllegalArgumentException e
      [site-name nil])
    (catch RuntimeException e
      [site-name nil])))

(defn get-sites []
  (->> (site-read/get-all-sites)
       (map make-site-entry)
       flatten
       (apply hash-map)))

(def sites (dissoc (get-sites) "test-site"))

(defrecord SiteScraper [sites]
  component/Lifecycle

  (start [component]
    (println ";; Loading site definitions...")
    (let [sites (dissoc (get-sites) "test-site")]
      (println ";;   Found sites: " (str/join ", " (keys sites)))
      (assoc component :sites sites)))

  (stop [component]))

(defn new-site-scraper []
  (map->SiteScraper {:sites nil}))
