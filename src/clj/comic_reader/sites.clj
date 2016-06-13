(ns comic-reader.sites
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [comic-reader.scrape :as scrape]
            [comic-reader.sites.protocol :refer :all]
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
  [options]
  (:root-url options))

(defn manga-list-format
  "A format string that when used to format the root URL will produce
  the URL of the list of all manga."
  [options]
  (:manga-list-format options))

(defn manga-url-format
  "A format string that when used to format the root URL will produce
  the prefix of all comic urls."
  [options]
  (:manga-url-format options))

(defn manga-url-suffix-pattern
  "A regular expression to match the comic-id portion of a comics
  chapter list URL."
  [options]
  (when-let [suffix-pattern (:manga-url-suffix-pattern options)]
    (str suffix-pattern)))

(defn comic->url-format
  "A format string that when used to format the manga URL and a comic
  id will produce the URL of a comics chapter page."
  [options]
  (:comic->url-format options))

(defn comic-list-selector
  "An enlive selector for selecting all of the links to comics on the
  comic list page."
  [options]
  (:comic-list-selector options))

(defn chapter-number-match-pattern
  "A regular expression that can extract the number from a chapter
  url."
  [options]
  (:chapter-number-match-pattern options))

(defn chapter-list-selector
  "An enlive selector that extracts all the chapter links from the
  comics chapter page."
  [options]
  (:chapter-list-selector options))

(defn page-normalize-pattern
  "A regular expression to extract the page number from the page
  select option value."
  [options]
  (:page-normalize-pattern options))

(defn page-normalize-format
  "A format string that when used to format the base-url of a comic
  and the page number will make the url for that specific page."
  [options]
  (:page-normalize-format options))

(defn chapter-number-pattern
  "A regular expression that will match the chapter specific portion
  of a url for creating a base url."
  [options]
  (:chapter-number-pattern options))

(defn page-list-selector
  "An enlive selector that extracts the list of pages from a manga
  chapter page."
  [options]
  (:page-list-selector options))

(defn image-selector
  "An enlive selector that extracts the image tag from a manga page."
  [options]
  (:image-selector options))

(defn comic-link-url-normalize
  "An expression that evals to a function that will normalize the link
  url."
  [options]
  (eval (:comic-link-url-normalize options)))

(defn comic-link-name-normalize
  "An expression that evals to a function that will normalize the link
  name."
  [options]
  (eval (:comic-link-name-normalize options)))

(defn chapter-link-url-normalize
  "An expression that evals to a function that will normalize the
  chapter link url."
  [options]
  (eval (:chapter-link-url-normalize options)))

(defn chapter-link-name-normalize
  "An expression that evals to a function that will normalize the link
  name."
  [options]
  (eval (:chapter-link-name-normalize options)))


;; ############################################################
;; ## Get Comic List Functions
;; ############################################################

(defn manga-url []
  (format (manga-url-format options) (root-url options)))

(defn manga-list-url [options]
  (format (manga-list-format options) (root-url options)))

(defn manga-pattern [options]
  (re-pattern (str (manga-url)
                   (manga-url-suffix-pattern options))))

(defn comic-link->map [{name :content {url :href} :attrs}]
  {:comic/name ((comic-link-name-normalize options) name)
   :comic/url  ((comic-link-url-normalize options)  url)})

(defn comic-link-add-id [comic-map]
  (when-let [url (:comic/url comic-map)]
    (let [[_ data] (re-find (manga-pattern options) url)]
      (assoc comic-map :comic/id data))))

(defn comic-link-normalize [link]
  (-> link
      comic-link->map
      comic-link-add-id))

(defn extract-comics-list [options html]
  (scrape/extract-list html
                       (comic-list-selector options)
                       comic-link-normalize))


;; ############################################################
;; ## Get Chapter List functions
;; ############################################################

(defn chapter-link->map [{name :content {url :href} :attrs}]
  {:chapter/title ((chapter-link-name-normalize options) name)
   :chapter/url   ((chapter-link-url-normalize options)  url)})

(defn chapter-link-add-ch-num [comic-map]
  (let [url (:chapter/url comic-map)
        [_ data] (re-find (chapter-number-match-pattern options) url)]
    (assoc comic-map :chapter/number (safe-read-string data))))

(defn chapter-link-normalize [link]
  (-> link
      chapter-link->map
      chapter-link-add-ch-num))

(defn extract-chapters-list [html comic-url]
  (if-let [raw-list (seq (scrape/extract-list html
                                              (chapter-list-selector options)
                                              chapter-link-normalize))]
    (->> raw-list
         (filter :chapter/number)
         (sort-by :chapter/number))))

(defn comic->url [comic-id]
  (format (comic->url-format options) (manga-url) comic-id))


;; ############################################################
;; ## Get Page List functions
;; ############################################################

(defn gen-extract-pages-list-normalize [base-url]
  (util/html-fn {[name] :content {:keys [value]} :attrs}
    (let [page-number (re-find (page-normalize-pattern options)
                               value)]
      {:page/number (safe-read-string name)
       :page/url (format (page-normalize-format options)
                         base-url
                         (or page-number ""))})))

(defn extract-pages-list [html chapter-url]
  (let [base-url (str/replace chapter-url
                            (chapter-number-pattern options)
                            "")
        normalize (gen-extract-pages-list-normalize base-url)]
    (scrape/extract-list html
                         (page-list-selector options)
                         normalize)))


;; ############################################################
;; ## Get Image Data functions
;; ############################################################

(defn extract-image-tag [html]
  (scrape/extract-image-tag html (image-selector options)))


;; ############################################################
;; ## Protocol functions
;; ############################################################

(deftype MangaSite [opt-map]
  PMangaSite

  (call-with-options [this f]
    (binding [options opt-map]
      (f)))

  (get-comic-list [this]
    (let [options opt-map]
      (->> (manga-list-url options)
           scrape/fetch-url
           (extract-comics-list options))))

  (get-chapter-list [this comic-id]
    (binding [options opt-map]
      (if-let [comic-url (comic->url comic-id)]
        (-> comic-url
            scrape/fetch-url
            (extract-chapters-list comic-url)
            doall))))

  (get-page-list [this {chapter-url :chapter/url}]
    (binding [options opt-map]
      (some-> chapter-url
              scrape/fetch-url
              (extract-pages-list chapter-url)
              doall)))

  (get-image-data [this {page-url :page/url}]
    (binding [options opt-map]
      (some-> page-url
              scrape/fetch-url
              extract-image-tag))))
