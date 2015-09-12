(ns comic-reader.sites.manga-fox
  (:require [comic-reader.sites.util :as util]
            [comic-reader.scrape :as scrape]
            [comic-reader.util :refer [safe-read-string]]
            [clojure.string :as s]))

(def ^:private
  comic->url-format "%s%s/")

(def ^:private
  comic-list-selector
  [:div.manga_list :ul :li :a])

(def ^:private
  chapter-number-match-pattern #"/c0*(\d+)/")

(def ^:private
  chapter-list-selector
  [:div#chapters :ul.chlist :li :div #{:h3 :h4} :a])

(def ^:private
  page-normalize-pattern #"^\d+$")

(def ^:private
  page-normalize-format "%s/%s.html")

(def ^:private
  chapter-number-pattern #"/\d+\.html")

(def ^:private
  page-list-selector
  [:div#top_center_bar :form#top_bar :select.m :option])

(def ^:private
  image-selector
  [:div#viewer :img#image])

(def ^:private
  root-url "http://mangafox.me")

(def ^:private
  link-url-normalize identity)

(def ^:private
  link-name-normalize first)

(def ^:private
  manga-pattern-match-portion "(.*?)/")

(def ^:private
  manga-list-format "%s/manga/")

(def ^:private
  manga-url-format "%s/manga/")

(def ^:private
  manga-url
  (format manga-url-format root-url))

(def ^:private
  manga-list-url
  (format manga-list-format root-url))

(def ^:private
  manga-pattern
  (re-pattern (str manga-url manga-pattern-match-portion)))

(def ^:private
  link->map
  (util/gen-link->map link-name-normalize
                      link-url-normalize))

(defn extract-image-tag [html]
  (scrape/extract-image-tag html image-selector))

(defn gen-extract-pages-list-normalize [base-url]
  (util/html-fn {[name] :content}
    (if-let [page-number (re-find page-normalize-pattern
                                  name)]
      {:name name
       :url (format page-normalize-format
                    base-url
                    page-number)})))

(defn extract-pages-list [html chapter-url]
  (let [base-url (s/replace chapter-url
                            chapter-number-pattern
                            "")
        normalize (gen-extract-pages-list-normalize base-url)]
    (scrape/extract-list html
                         page-list-selector
                         normalize)))

(def ^:private chapter-link-normalize
  (comp
   (util/gen-add-key-from-url :ch-num
                              chapter-number-match-pattern
                              safe-read-string)
   link->map))

(defn extract-chapters-list [html comic-url]
  (scrape/extract-list html
                       chapter-list-selector
                       chapter-link-normalize))

(def ^:private comic-link-normalize
  (comp
   (util/gen-add-key-from-url :id
                              manga-pattern)
   link->map))

(defn extract-comics-list [html]
  (scrape/extract-list html
                       comic-list-selector
                       comic-link-normalize))

(defn comic->url [comic-id]
  (format comic->url-format manga-url comic-id))

(defn get-comic-list []
  (-> manga-list-url
      scrape/fetch-url
      extract-comics-list))

(defn get-chapter-list [comic-id]
  (let [comic-url (comic->url comic-id)]
    (-> comic-url
        scrape/fetch-url
        (extract-chapters-list comic-url))))

(defn get-page-list [comic-chapter]
  (let [chapter-url (:url comic-chapter)]
    (-> chapter-url
        scrape/fetch-url
        (extract-pages-list chapter-url))))

(defn get-image-data [comic-id chapter page]
  [])
