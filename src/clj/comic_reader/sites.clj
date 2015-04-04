(ns comic-reader.sites
  (:refer-clojure :exclude [list])
  (:require [clojure.string :as s]))

(defn gen-link->map [process-name process-url]
  (fn [{[name] :content
        {url :href} :attrs}]
    {:name (process-name name)
     :url (process-url url)}))

(def list
  [(let [canonical-url "http://mangafox.me"
         manga-url (format "%s/manga/" canonical-url)
         manga-pattern (re-pattern (str manga-url "(.*?)/"))
         link->map (gen-link->map identity identity)]
     {:id :manga-fox
      :name "Manga Fox"

      :comic-list-data {:url manga-url
                        :selector [:div.manga_list :ul :li :a]
                        :normalize link->map}
      :chapter-list-data-for-comic
      (fn [comic-url]
        {:url comic-url
         :selector [:div#chapters :ul.chlist
                    :li :div #{:h3 :h4} :a]
         :normalize link->map})
      :page-list-data-for-comic-chapter
      (fn [chapter-url]
        {:url chapter-url
         :selector [:div]
         :normalize link->map})})

   (let [canonical-url "http://www.mangareader.net"
         link->map (gen-link->map s/trim
                                  (partial str canonical-url))]
     {:id :manga-reader
      :name "Manga Reader"
      :comic-list-data {:url (str canonical-url "/alphabetical")
                        :selector [:div.series_alpha :ul :li :a]
                        :normalize link->map}
      :chapter-list-data-for-comic
      (fn [chapter-url]
        {:url chapter-url
         :selector [:div#chapterlist :tr :td :a]
         :normalize link->map})
      :page-list-data-for-comic-chapter
      (fn [chapter-url]
        {:url chapter-url
         :selector [:div]
         :normalize link->map})})])
