(ns comic-reader.sites.manga-fox
  (:require [comic-reader.sites.util :as util]
            [comic-reader.scrape :as scrape]
            [comic-reader.util :refer [safe-read-string]]
            [clojure.string :as s]))

(def ^:dynamic options
  {:chapter-list-selector [:div#chapters :ul.chlist :li :div #{:h4 :h3} :a]
   :chapter-number-match-pattern #"/c0*(\d+)/"
   :chapter-number-pattern #"/\d+\.html"

   :comic->url-format "%s%s/"
   :comic-list-selector [:div.manga_list :ul :li :a]

   :image-selector [:div#viewer :img#image]

   :link-name-normalize clojure.core/first
   :link-url-normalize  clojure.core/identity

   :manga-list-format "%s/manga/"
   :manga-pattern-match-portion "(.*?)/"
   :manga-url-format "%s/manga/"

   :page-list-selector [:div#top_center_bar :form#top_bar :select.m :option]
   :page-normalize-format "%s/%s.html"
   :page-normalize-pattern #"^\d+$"

   :root-url "http://mangafox.me"})


;;; Data functions

(defn comic->url-format []
  (:comic->url-format options))

(defn comic-list-selector []
  (:comic-list-selector options))

(defn chapter-number-match-pattern []
  (:chapter-number-match-pattern options))

(defn chapter-list-selector []
  (:chapter-list-selector options))

(defn page-normalize-pattern []
  (:page-normalize-pattern options))

(defn page-normalize-format []
  (:page-normalize-format options))

(defn chapter-number-pattern []
  (:chapter-number-pattern options))

(defn page-list-selector []
  (:page-list-selector options))

(defn image-selector []
  (:image-selector options))

(defn root-url []
  (:root-url options))

(defn link-url-normalize []
  (:link-url-normalize options))

(defn link-name-normalize []
  (:link-name-normalize options))

(defn manga-pattern-match-portion []
  (:manga-pattern-match-portion options))

(defn manga-list-format []
  (:manga-list-format options))

(defn manga-url-format []
  (:manga-url-format options))

;;; Real functions


;; ############################################################
;; ## Get Comic List Functions
;; ############################################################

(defn manga-url []
  (format (manga-url-format) (root-url)))

(defn manga-list-url []
  (format (manga-list-format) (root-url)))

(defn manga-pattern []
  (re-pattern (str (manga-url) (manga-pattern-match-portion))))

(defn link->map [{name :content
                  {url :href} :attrs}]
  {:name ((link-name-normalize) name)
   :url  ((link-url-normalize)  url)})

(defn comic-link-add-id [{:keys [url] :as comic-map}]
  (let [[_ data] (re-find (manga-pattern) url)]
    (assoc comic-map
           :id data)))

(defn comic-link-normalize [link]
  (-> link
      link->map
      comic-link-add-id))

(defn extract-comics-list [html]
  (scrape/extract-list html
                       (comic-list-selector)
                       comic-link-normalize))

(defn get-comic-list []
  (-> (manga-list-url)
      scrape/fetch-url
      extract-comics-list))


;; ############################################################
;; ## Get Chapter List functions
;; ############################################################

(defn chapter-link-add-ch-num [{:keys [url]
                                :as comic-map}]
  (let [[_ data] (re-find (chapter-number-match-pattern) url)]
    (assoc comic-map
           :ch-num (safe-read-string data))))

(defn chapter-link-normalize [link]
  (-> link
      link->map
      chapter-link-add-ch-num))

(defn extract-chapters-list [html comic-url]
  (scrape/extract-list html
                       (chapter-list-selector)
                       chapter-link-normalize))

(defn comic->url [comic-id]
  (format (comic->url-format) (manga-url) comic-id))

(defn get-chapter-list [comic-id]
  (let [comic-url (comic->url comic-id)]
    (-> comic-url
        scrape/fetch-url
        (extract-chapters-list comic-url))))


;; ############################################################
;; ## Get Page List functions
;; ############################################################

(defn extract-image-tag [html]
  (scrape/extract-image-tag html (image-selector)))

(defn gen-extract-pages-list-normalize [base-url]
  (util/html-fn {[name] :content}
    (if-let [page-number (re-find (page-normalize-pattern)
                                  name)]
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

(defn get-page-list [comic-chapter]
  (let [chapter-url (:url comic-chapter)]
    (-> chapter-url
        scrape/fetch-url
        (extract-pages-list chapter-url))))


;; ############################################################
;; ## Get Image Data functions
;; ############################################################

(defn get-image-data [comic-id chapter page]
  [])
