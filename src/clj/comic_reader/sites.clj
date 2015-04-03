(ns comic-reader.sites
  (:refer-clojure :exclude [list])
  (:require [clojure.string :as s]))

(def list
  [(let [canonical-url "http://mangafox.me"
         manga-url (format "%s/manga/" canonical-url)
         manga-pattern (re-pattern (str manga-url "(.*?)/"))]
     {:id :manga-fox
      :name "Manga Fox"
      :url manga-url
      :selector [:div.manga_list :ul :li :a]
      :normalize (fn [{[name] :content
                       {url :href} :attrs}]
                   {:name name
                    :url url})
      :url->feed (fn [url]
                   (some->> url
                            (re-matches manga-pattern)
                            second
                            (format "%s/rss/%s.xml" canonical-url)))})

   (let [canonical-url "http://www.mangareader.net/"]
     {:id :manga-reader
      :name "Manga Reader"
      :url (str canonical-url "alphabetical")
      :selector [:div.series_alpha :ul :li :a]
      :normalize (fn [{[name] :content
                       {url :href} :attrs}]
                   {:name (s/trim name)
                    :url (str canonical-url url)})})])
