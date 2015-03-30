(ns comic-reader.scrape
  (:require [clojure.string :refer [trim]]
            [net.cgrand.enlive-html :as html]))

(def sites
  {"Manga Fox" {:url "http://mangafox.me/manga/"
                :selector [:div.manga_list :ul :li :a]
                :normalize (fn [{name :content
                                 {url :href} :attrs}]
                             {:name name
                              :url url})}
   "Manga Reader" {:url "http://www.mangareader.net/alphabetical"
                   :selector [:div.series_alpha :ul :li :a]
                   :normalize (fn [{name :content
                                    {url :href} :attrs}]
                                {:name (trim name)
                                 :url url})}})

(defn fetch-url [url]
  (html/html-resource (java.net.URL. url)))

(defn fetch-site-list [{:keys [url selector normalize]}])
