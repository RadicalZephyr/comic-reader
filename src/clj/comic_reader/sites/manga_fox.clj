(ns comic-reader.sites.manga-fox
  (:require [comic-reader.sites.protocol :refer [MangaSite]]
            [comic-reader.sites.util :as util]
            [comic-reader.scrape :as scrape]
            [comic-reader.util :refer [safe-read-string]]
            [clojure.string :as s]))

(def ^:private root-url "http://mangafox.me")

(def ^:private manga-url
  (format "%s/manga/" root-url))

(def ^:private manga-pattern
  (re-pattern (str manga-url "(.*?)/")))

(def ^:private link->map (util/gen-link->map first identity))

(def ^:private image-selector [:div#viewer :img#image])

(defn extract-image-tag [html]
  (scrape/extract-image-tag html image-selector))

(def ^:private page-list-selector
  [:div#top_center_bar :form#top_bar :select.m :option])

(defn extract-pages-list [html chapter-url]
  (let [base-url (s/replace chapter-url
                            #"/\d+\.html"
                            "")
        normalize (util/html-fn  {[name] :content}
                    {:name name
                     :url (format "%s/%s.html"
                                  base-url
                                  name)})]
    (scrape/extract-list html
                         page-list-selector
                         normalize)))

(def ^:private chapter-list-selector
  [:div#chapters :ul.chlist :li :div #{:h3 :h4} :a])

(def ^:private chapter-link-normalize
  (comp
   (util/gen-add-key-from-url :ch-num
                              #"/c0*(\d+)/"
                              safe-read-string)
   link->map))

(defn extract-chapters-list [html comic-url]
  (scrape/extract-list html
                       chapter-list-selector
                       chapter-link-normalize))

(def ^:private comic-list-selector
  [:div.manga_list :ul :li :a])

(def ^:private comic-link-normalize
  (comp
   (util/gen-add-key-from-url
    :id
    manga-pattern)
   link->map))

(defn extract-comics-list [html]
  (scrape/extract-list html
                       comic-list-selector
                       comic-link-normalize))

(defn comic->url [comic-id]
  (format "%s%s/" manga-url comic-id))

(deftype MangaFox []
  MangaSite
  (get-comic-list [this]
    (-> manga-url
        scrape/fetch-url
        extract-comics-list))

  (get-chapter-list [this comic-id]
    (let [comic-url (comic->url comic-id)]
      (-> comic-url
          scrape/fetch-url
          (extract-chapters-list comic-url))))

  (get-page-list [this comic-id chapter]
    [])

  (get-image-data [this comic-id chapter page]
    []))

(def manga-fox
  (MangaFox.))
