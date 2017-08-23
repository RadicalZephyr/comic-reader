(ns comic-reader.sites
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [comic-reader.scrape :as scrape]
            [comic-reader.sites.protocol :refer :all]
            [comic-reader.sites.util :as util]
            [comic-reader.util :refer [safe-read-string]]
            [com.stuartsierra.component :as component]))

(declare root-url)

(defn get-normalize-fn [options normalize-fn-key]
  (get {:nothing identity
        :trim-first  (comp str/trim first)
        :trim-second (comp str/trim second)
        :concat-with-root-url (fn [segment]
                                (str (root-url options)
                                     segment))}
       normalize-fn-key))

;;; Data functions
(defn root-url
  "The root URL of the manga site without a trailing slash."
  [options]
  (:site/root-url options))

(defn manga-list-format
  "A format string that when used to format the root URL will produce
  the URL of the list of all manga."
  [options]
  (:site/manga-list-format options))

(defn manga-url-format
  "A format string that when used to format the root URL will produce
  the prefix of all comic urls."
  [options]
  (:site/manga-url-format options))

(defn manga-url-suffix-pattern
  "A regular expression to match the comic-id portion of a comics
  chapter list URL."
  [options]
  (when-let [suffix-pattern (:site/manga-url-suffix-pattern options)]
    (str suffix-pattern)))

(defn comic->url-format
  "A format string that when used to format the manga URL and a comic
  id will produce the URL of a comics chapter page."
  [options]
  (:site/comic->url-format options))

(defn comic-list-selector
  "An enlive selector for selecting all of the links to comics on the
  comic list page."
  [options]
  (:site/comic-list-selector options))

(defn chapter-number-match-pattern
  "A regular expression that can extract the number from a chapter
  url."
  [options]
  (:site/chapter-number-match-pattern options))

(defn chapter-list-selector
  "An enlive selector that extracts all the chapter links from the
  comics chapter page."
  [options]
  (:site/chapter-list-selector options))

(defn page-normalize-pattern
  "A regular expression to extract the page number from the page
  select option value."
  [options]
  (:site/page-normalize-pattern options))

(defn page-normalize-format
  "A format string that when used to format the base-url of a comic
  and the page number will make the url for that specific page."
  [options]
  (:site/page-normalize-format options))

(defn chapter-number-pattern
  "A regular expression that will match the chapter specific portion
  of a url for creating a base url."
  [options]
  (:site/chapter-number-pattern options))

(defn page-list-selector
  "An enlive selector that extracts the list of pages from a manga
  chapter page."
  [options]
  (:site/page-list-selector options))

(defn image-selector
  "An enlive selector that extracts the image tag from a manga page."
  [options]
  (:site/image-selector options))

(defn comic-link-url-normalize
  "An expression that evals to a function that will normalize the link
  url."
  [options]
  (get-normalize-fn options (:site/comic-link-url-normalize options)))

(defn comic-link-name-normalize
  "An expression that evals to a function that will normalize the link
  name."
  [options]
  (get-normalize-fn options (:site/comic-link-name-normalize options)))

(defn chapter-link-url-normalize
  "An expression that evals to a function that will normalize the
  chapter link url."
  [options]
  (get-normalize-fn options (:site/chapter-link-url-normalize options)))

(defn chapter-link-name-normalize
  "An expression that evals to a function that will normalize the link
  name."
  [options]
  (get-normalize-fn options (:site/chapter-link-name-normalize options)))


;; ############################################################
;; ## Get Comic List Functions
;; ############################################################

(defn manga-url [options]
  (format (manga-url-format options) (root-url options)))

(defn manga-list-url [options]
  (format (manga-list-format options) (root-url options)))

(defn manga-pattern [options]
  (re-pattern (str (manga-url options)
                   (manga-url-suffix-pattern options))))

(defn comic-link->map [options {name :content {url :href} :attrs}]
  {:comic/name ((comic-link-name-normalize options) name)
   :comic/url  ((comic-link-url-normalize options)  url)})

(defn comic-link-add-id [options comic-map]
  (when-let [url (:comic/url comic-map)]
    (let [[_ data] (re-find (manga-pattern options) url)]
      (assoc comic-map :comic/id data))))

(defn comic-link-normalize [options link]
  (->> link
       (comic-link->map options)
       (comic-link-add-id options)))

(defn extract-comics-list [options html]
  (scrape/extract-list html
                       (comic-list-selector options)
                       (partial comic-link-normalize options)))


;; ############################################################
;; ## Get Chapter List functions
;; ############################################################

(defn chapter-link->map [options {name :content {url :href} :attrs}]
  {:chapter/title ((chapter-link-name-normalize options) name)
   :chapter/url   ((chapter-link-url-normalize options)  url)})

(defn chapter-link-add-ch-num [options comic-map]
  (let [url (:chapter/url comic-map)
        [_ data] (re-find (chapter-number-match-pattern options) url)]
    (assoc comic-map :chapter/number (safe-read-string data))))

(defn chapter-link-normalize [options link]
  (->> link
       (chapter-link->map options)
       (chapter-link-add-ch-num options)))

(defn extract-chapters-list [options html comic-url]
  (if-let [raw-list (seq (scrape/extract-list html
                                              (chapter-list-selector options)
                                              (partial chapter-link-normalize options)))]
    (->> raw-list
         (filter :chapter/number)
         (sort-by :chapter/number))))

(defn comic->url [options comic-id]
  (format (comic->url-format options) (manga-url options) comic-id))


;; ############################################################
;; ## Get Page List functions
;; ############################################################

(defn gen-extract-pages-list-normalize [options base-url]
  (util/html-fn {[name] :content {:keys [value]} :attrs}
    (let [page-number (re-find (page-normalize-pattern options)
                               value)]
      {:page/number (safe-read-string name)
       :page/url (format (page-normalize-format options)
                         base-url
                         (or page-number ""))})))

(defn extract-pages-list [options html chapter-url]
  (let [base-url (str/replace chapter-url
                              (chapter-number-pattern options)
                              "")
        normalize (gen-extract-pages-list-normalize options base-url)]
    (scrape/extract-list html
                         (page-list-selector options)
                         normalize)))


;; ############################################################
;; ## Get Image Data functions
;; ############################################################

(defn extract-image-tag [options html]
  (scrape/extract-image-tag html (image-selector options)))


;; ############################################################
;; ## Protocol functions
;; ############################################################

(defrecord MangaSite [options]
  PMangaSite

  (get-comic-list [this]
    (let [extract-comics-list (partial extract-comics-list options)]
      (some-> (manga-list-url options)
              scrape/fetch-url
              extract-comics-list)))

  (get-chapter-list [this comic-id]
    (let [extract-chapters-list (partial extract-chapters-list options)]
      (if-let [comic-url (comic->url options comic-id)]
        (some-> comic-url
                scrape/fetch-url
                (extract-chapters-list comic-url)))))

  (get-page-list [this {chapter-url :chapter/url}]
    (let [extract-pages-list (partial extract-pages-list options)]
      (some-> chapter-url
              scrape/fetch-url
              (extract-pages-list chapter-url))))

  (get-image-data [this {page-url :page/url}]
    (let [extract-image-tag (partial extract-image-tag options)]
      (some-> page-url
              scrape/fetch-url
              extract-image-tag))))
