(ns comic-reader.scrape
  (:require [clojure.string :as s]
            [net.cgrand.enlive-html :as html])
  (:import java.net.URL))

(defn fetch-feed [url]
  (html/xml-resource (java.net.URL. url)))

(defn fetch-url [url]
  (html/html-resource (java.net.URL. url)))

(defn fetch-list [{:keys [url selector normalize]}]
  (when (every? (complement nil?) [url selector normalize])
    (map normalize (html/select (fetch-url url) selector))))

(defn enlive->hiccup [{:keys [tag attrs content]}]
  (if (nil? content)
    [tag attrs]
    [tag attrs content]))

(defn fetch-image-tag [url]
  (-> (fetch-url url)
      (html/select [:div#imgholder :a :img#img])
      first
      enlive->hiccup))
