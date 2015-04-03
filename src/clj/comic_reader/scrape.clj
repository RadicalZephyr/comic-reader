(ns comic-reader.scrape
  (:require [clojure.string :as s]
            [net.cgrand.enlive-html :as html])
  (:import java.net.URL))

(defn fetch-feed [url]
  (html/xml-resource (java.net.URL. url)))

(defn fetch-url [url]
  (html/html-resource (java.net.URL. url)))

(defn fetch-comic-list [{:keys [url selector normalize]}]
  (map normalize (html/select (fetch-url url) selector)))
