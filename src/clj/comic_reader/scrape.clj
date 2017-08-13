(ns comic-reader.scrape
  (:require [clojure.string :as s]
            [clojure.java.io :as io]
            [org.httpkit.client :as http]
            [tempfile.core :refer [tempfile with-tempfile]]
            [net.cgrand.enlive-html :as html])
  (:import java.net.URL))

(defn ^:dynamic raise-null-selection-error [html selector]
  (throw (ex-info "No selection found." {:html html
                                         :selector selector})))

(defn fetch-url [url]
  (with-tempfile [html-file (tempfile (:body @(http/get url)))]
    (html/html-resource html-file)))

(defn extract-list [html selector normalize]
  (if-let [selection (seq (html/select html selector))]
    (keep normalize selection)
    (raise-null-selection-error html selector)))

(defn fetch-list [{:keys [url selector normalize]}]
  (when (every? (complement nil?) [url selector normalize])
    (extract-list (fetch-url url) selector normalize)))

(defn enlive->hiccup [{:keys [tag attrs content]}]
  (if (nil? content)
    [tag attrs]
    [tag attrs content]))

(defn- force-url-to-https [url]
  (if (and url (string? url))
    (s/replace url #"^http://" "https://")
    ""))

(defn clean-image-tag [[tag attrs & content]]
  (let [attrs (-> attrs
                  (select-keys [:alt :src])
                  (update :src force-url-to-https))]
    [tag attrs]))

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
