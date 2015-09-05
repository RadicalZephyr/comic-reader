(ns comic-reader.sites.manga-reader
  (:require [comic-reader.sites.protocol :refer [MangaSite]]
            [comic-reader.sites.util :as util]
            [comic-reader.scrape :as scrape]
            [comic-reader.util :refer [safe-read-string]]
            [clojure.string :as s]))

(def ^:private root-url "http://mangareader.net")

(def ^:private manga-url
  (format "%s/" root-url))

(def ^:private manga-pattern
  (re-pattern (str manga-url "/(.*)(\\.html)?$")))

(def ^:private link->map
  (util/gen-link->map (comp s/trim first)
                      (partial str root-url)))

(def ^:private image-selector
  [:div#imgholder :a :img#img])

(defn extract-image-tag [html]
  (scrape/extract-image-tag html image-selector))

(def ^:private page-list-selector
  [:div#selectpage :select#pageMenu :option])

(defn- page-list-normalize [{[name] :content
                             {url :value} :attrs}]
  {:name name
   :url (str root-url url)})

(defn extract-pages-list [html chapter-url]
  (let [base-url (s/replace chapter-url
                            #"/\d+$"
                            "")
        normalize (util/html-fn {[name] :content}
                    {:name name
                     :url (format "%s/%s" base-url
                                  (re-find #"\d+$" name))})]
    (scrape/extract-list html
                         page-list-selector
                         normalize)))

(def ^:private chapter-list-selector
  [:div#chapterlist :tr :td :a])

(def ^:private chapter-link-normalize
  (comp
   (util/gen-add-key-from-url :ch-num
                              #"0*(\d+)$"
                              safe-read-string)
   link->map))

(defn extract-chapters-list [html comic-url]
  (scrape/extract-list html
                       chapter-list-selector
                       chapter-link-normalize))

(deftype MangaReader []
  MangaSite
  (get-comic-list [this]
    [])

  (get-chapter-list [this comic-id]
    [])

  (get-page-list [this comic-id chapter]
    [])

  (get-image-data [this comic-id chapter page]
    []))

(def manga-reader
  (MangaReader.))
