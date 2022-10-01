(ns comic-reader.scrape
  (:require [clojure.string :as s]
            [clojure.java.io :as io]
            [org.httpkit.client :as http]
            [tempfile.core :refer [tempfile with-tempfile]]
            [net.cgrand.enlive-html :as html]
            [clojure.tools.logging :as log])
  (:import java.net.URL))

(defn ^:dynamic raise-null-selection-error [html selector]
  (throw (ex-info "No selection found." {:html html
                                         :selector selector})))

(defn fetch-url [url]
  (let [html-file (tempfile (:body @(http/get url)))]
    (log/info "TEMP_FILE_NAME" html-file)
    (html/html-resource html-file)))

(defn extract-list [html selector normalize]
  (if-let [selection (seq (html/select html selector))]
    (keep normalize selection)
    (raise-null-selection-error html selector)))

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
