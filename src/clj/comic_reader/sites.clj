(ns comic-reader.sites
  (:refer-clojure :exclude [list])
  (:require [clojure.string :as s]))

(defn gen-link->map [process-name process-url]
  (fn [{[name] :content
        {url :href} :attrs}]
    {:name (process-name name)
     :url (process-url url)}))

(def simple-link->map (gen-link->map identity identity))

(def list
  [(let [canonical-url "http://mangafox.me"
         manga-url (format "%s/manga/" canonical-url)
         manga-pattern (re-pattern (str manga-url "(.*?)/"))]
     {:id :manga-fox
      :name "Manga Fox"
      :url manga-url
      :selector [:div.manga_list :ul :li :a]
      :normalize simple-link->map
      :chapter-list-data (fn [chapter-url]
                           {:url chapter-url
                            :selector [:div#chapters :ul.chlist
                                       :li :div :h3 :a]
                            :normalize simple-link->map})
      :url->feed (fn [url]
                   (some->> url
                            (re-matches manga-pattern)
                            second
                            (format "%s/rss/%s.xml" canonical-url)))})

   (let [canonical-url "http://www.mangareader.net"
         link->map (gen-link->map s/trim
                                  (partial str canonical-url))]
     {:id :manga-reader
      :name "Manga Reader"
      :url (str canonical-url "/alphabetical")
      :selector [:div.series_alpha :ul :li :a]
      :normalize link->map
      :chapter-list-data (fn [chapter-url]
                           {:url chapter-url
                            :selector [:div#chapterlist :tr :td :a]
                            :normalize link->map})})])
