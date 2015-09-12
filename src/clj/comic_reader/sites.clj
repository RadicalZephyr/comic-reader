(ns comic-reader.sites
  (:require [comic-reader.sites.protocol :refer :all]
            [comic-reader.sites.util :as util]
            [comic-reader.scrape :as scrape]
            [comic-reader.util :refer [safe-read-string]]
            [clojure.java.io :as io]
            [clojure.string :as s]))

(def ^:dynamic options
  {:chapter-number-match-pattern nil
   :page-list-selector nil
   :link-url-normalize nil
   :manga-pattern-match-portion nil
   :page-normalize-format nil
   :manga-list-format nil
   :page-normalize-pattern nil
   :chapter-number-pattern nil
   :comic->url-format nil
   :link-name-normalize nil
   :chapter-list-selector nil
   :manga-url-format nil
   :root-url nil
   :comic-list-selector nil
   :image-selector nil})


;;; Data functions

(defn comic->url-format []
  (:comic->url-format options))

(defn comic-list-selector []
  (:comic-list-selector options))

(defn chapter-number-match-pattern []
  (:chapter-number-match-pattern options))

(defn chapter-list-selector []
  (:chapter-list-selector options))

(defn page-normalize-pattern []
  (:page-normalize-pattern options))

(defn page-normalize-format []
  (:page-normalize-format options))

(defn chapter-number-pattern []
  (:chapter-number-pattern options))

(defn page-list-selector []
  (:page-list-selector options))

(defn image-selector []
  (:image-selector options))

(defn root-url []
  (:root-url options))

(defn link-url-normalize []
  (eval (:link-url-normalize options)))

(defn link-name-normalize []
  (eval (:link-name-normalize options)))

(defn manga-pattern-match-portion []
  (:manga-pattern-match-portion options))

(defn manga-list-format []
  (:manga-list-format options))

(defn manga-url-format []
  (:manga-url-format options))


;; ############################################################
;; ## Get Comic List Functions
;; ############################################################

(defn manga-url []
  (format (manga-url-format) (root-url)))

(defn manga-list-url []
  (format (manga-list-format) (root-url)))

(defn manga-pattern []
  (re-pattern (str (manga-url) (manga-pattern-match-portion))))

(defn link->map [{name :content
                  {url :href} :attrs}]
  {:name ((link-name-normalize) name)
   :url  ((link-url-normalize)  url)})

(defn comic-link-add-id [{:keys [url] :as comic-map}]
  (let [[_ data] (re-find (manga-pattern) url)]
    (assoc comic-map
           :id data)))

(defn comic-link-normalize [link]
  (-> link
      link->map
      comic-link-add-id))

(defn extract-comics-list [html]
  (scrape/extract-list html
                       (comic-list-selector)
                       comic-link-normalize))


;; ############################################################
;; ## Get Chapter List functions
;; ############################################################

(defn chapter-link-add-ch-num [{:keys [url]
                                :as comic-map}]
  (let [[_ data] (re-find (chapter-number-match-pattern) url)]
    (assoc comic-map
           :ch-num (safe-read-string data))))

(defn chapter-link-normalize [link]
  (-> link
      link->map
      chapter-link-add-ch-num))

(defn extract-chapters-list [html comic-url]
  (scrape/extract-list html
                       (chapter-list-selector)
                       chapter-link-normalize))

(defn comic->url [comic-id]
  (format (comic->url-format) (manga-url) comic-id))


;; ############################################################
;; ## Get Page List functions
;; ############################################################

(defn gen-extract-pages-list-normalize [base-url]
  (util/html-fn {[name] :content}
    (if-let [page-number (re-find (page-normalize-pattern)
                                  name)]
      {:name name
       :url (format (page-normalize-format)
                    base-url
                    page-number)})))

(defn extract-pages-list [html chapter-url]
  (let [base-url (s/replace chapter-url
                            (chapter-number-pattern)
                            "")
        normalize (gen-extract-pages-list-normalize base-url)]
    (scrape/extract-list html
                         (page-list-selector)
                         normalize)))


;; ############################################################
;; ## Get Image Data functions
;; ############################################################

(defn extract-image-tag [html]
  (scrape/extract-image-tag html (image-selector)))


;; ############################################################
;; ## Protocol functions
;; ############################################################

(deftype MangaSite [opt-map]
  PMangaSite

  (get-comic-list [this]
    (binding [options opt-map]
      (-> (manga-list-url)
          scrape/fetch-url
          extract-comics-list)))

  (get-chapter-list [this comic-id]
    (binding [options opt-map]
      (let [comic-url (comic->url comic-id)]
        (-> comic-url
            scrape/fetch-url
            (extract-chapters-list comic-url)))))

  (get-page-list [this comic-chapter]
    (binding [options opt-map]
      (let [chapter-url (:url comic-chapter)]
        (-> chapter-url
            scrape/fetch-url
            (extract-pages-list chapter-url)))))

  (get-image-data [this {page-url :url}]
    (binding [options opt-map]
      (-> page-url
          scrape/fetch-url
          extract-image-tag))))

(defn read-site-options [site-name]
  (with-open [r (-> (str "sites/" site-name ".clj")
                    io/resource
                    io/reader
                    java.io.PushbackReader.)]
    (read r)))

(defn make-site-entry [site-name]
  [site-name (->MangaSite (read-site-options site-name))])

(def sites
  (->> ["manga-fox"]
       (map make-site-entry)
       flatten
       (apply hash-map)))
