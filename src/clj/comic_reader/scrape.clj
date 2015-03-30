(ns comic-reader.scrape
  (:require [clojure.string :refer [trim]]
            [net.cgrand.enlive-html :as html])
  (:import java.net.URL))

(def sites
  [{:name "Manga Fox"
    :url "http://mangafox.me/manga/"
    :selector [:div.manga_list :ul :li :a]
    :normalize (fn [{[name] :content
                     {url :href} :attrs}]
                 {:name name
                  :url url})}
   {:name "Manga Reader"
    :url "http://www.mangareader.net/alphabetical"
    :selector [:div.series_alpha :ul :li :a]
    :normalize (fn [{[name] :content
                     {url :href} :attrs}]
                 {:name (trim name)
                  :url url})}])

(defn fetch-url [url]
  (html/html-resource (java.net.URL. url)))

(defn fetch-site-list [{:keys [url selector normalize]}]
  (map normalize (html/select (fetch-url url) selector)))
