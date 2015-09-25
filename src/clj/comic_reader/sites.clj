(ns comic-reader.sites
  (:require [comic-reader.sites.protocol :refer :all]
            [comic-reader.sites.util :as util]
            [comic-reader.scrape :as scrape]
            [comic-reader.util :refer [safe-read-string]]
            [clojure.java.io :as io]
            [clojure.string :as s]))

(def ^:dynamic options
  {:root-url                     nil
   :manga-list-format            nil
   :manga-url-format             nil
   :manga-pattern-match-portion  nil

   :comic->url-format            nil

   :chapter-list-selector        nil
   :comic-list-selector          nil
   :image-selector               nil
   :page-list-selector           nil

   :chapter-number-pattern       nil
   :chapter-number-match-pattern nil

   :link-name-normalize          nil
   :link-url-normalize           nil

   :page-normalize-format        nil
   :page-normalize-pattern       nil})


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

  (call-with-options [this f]
    (binding [options opt-map]
      (f)))

  (get-comic-list [this]
    (binding [options opt-map]
      (-> (manga-list-url)
          scrape/fetch-url
          extract-comics-list
          doall)))

  (get-chapter-list [this comic-id]
    (binding [options opt-map]
      (let [comic-url (comic->url comic-id)]
        (-> comic-url
            scrape/fetch-url
            (extract-chapters-list comic-url)
            doall))))

  (get-page-list [this comic-chapter]
    (binding [options opt-map]
      (let [chapter-url (:url comic-chapter)]
        (-> chapter-url
            scrape/fetch-url
            (extract-pages-list chapter-url)
            doall))))

  (get-image-data [this {page-url :url}]
    (binding [options opt-map]
      (-> page-url
          scrape/fetch-url
          extract-image-tag))))

(defn base-name [file]
  (let [[_ base-name]
        (->> file
             io/as-file
             .getName
             (re-matches #"^(.*)\..*?$"))]
    base-name))

(defn get-all-sites []
  (->> (io/resource "sites")
       io/as-file
       file-seq
       (filter (complement (memfn isDirectory)))
       (map base-name)))

(defn read-file [file]
  (when-let [r1 (some-> file
                        io/reader
                        java.io.PushbackReader.)]
    (with-open [r r1]
      (read r))))

(defn read-site-options [site-name]
  (if-let [file (-> (str "sites/" site-name ".clj")
                    io/resource)]
    (read-file file)
    (throw (IllegalArgumentException.
            (str "`sites/" site-name "' "
                 "was not found in the resources.")))))

(defn make-site-entry [site-name]
  (try
    [site-name (->MangaSite (read-site-options site-name))]
    (catch IllegalArgumentException e
      [site-name nil])
    (catch RuntimeException e
      [site-name nil])))

(defn get-sites []
  (->> (get-all-sites)
       (map make-site-entry)
       flatten
       (apply hash-map)))

(def sites (dissoc (get-sites) "test-site"))
