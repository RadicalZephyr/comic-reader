(ns comic-reader.scrape
  (:require [clojure.string :as s]
            [clojure.java.io :as io]
            [clj-http.client :as client]
            [tempfile.core :refer [tempfile with-tempfile]]
            [net.cgrand.enlive-html :as html])
  (:import java.net.URL))

(defn fetch-url [url]
  (with-tempfile [html-file (tempfile (:body (client/get url)))]
   (html/html-resource html-file)))

(defn extract-list [html selector normalize]
  (map normalize (html/select html selector)))

(defn fetch-list [{:keys [url selector normalize]}]
  (when (every? (complement nil?) [url selector normalize])
    (extract-list (fetch-url url) selector normalize)))

(defn enlive->hiccup [{:keys [tag attrs content]}]
  (if (nil? content)
    [tag attrs]
    [tag attrs content]))

(defn clean-image-tag [[tag attrs & content]]
  [tag (select-keys attrs [:alt :src])])

(defn extract-image-tag [html selector]
  (some-> html
          (html/select selector)
          seq
          first
          enlive->hiccup
          clean-image-tag))

(defn fetch-image-tag [{:keys [url selector]}]
  (when (every? (complement nil?) [url selector])
    (-> (fetch-url url)
        (extract-image-tag selector))))
