(ns comic-reader.scrape
  (:require [clojure.string :as s]
            [net.cgrand.enlive-html :as html])
  (:import java.net.URL))

(defn fetch-url [url]
  (html/html-resource (java.net.URL. url)))

(defn fetch-list [{:keys [url selector normalize]}]
  (when (every? (complement nil?) [url selector normalize])
    (map normalize (html/select (fetch-url url) selector))))

(defn enlive->hiccup [{:keys [tag attrs content]}]
  (if (nil? content)
    [tag attrs]
    [tag attrs content]))

(defn clean-image-tag [[tag attrs & content]]
  [tag (select-keys attrs [:alt :src])])

(defn extract-image-tag [html selector]
  (-> html
      (html/select selector)
      first
      enlive->hiccup
      clean-image-tag))

(defn fetch-image-tag [{:keys [url selector]}]
  (when (every? (complement nil?) [url selector])
    (-> (fetch-url url)
        extract-image-tag)))
