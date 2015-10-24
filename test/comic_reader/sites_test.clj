(ns comic-reader.sites-test
  (:require [clojure.test :refer :all]
            [comic-reader.sites.protocol :refer :all]
            [comic-reader.sites :refer :all]
            [comic-reader.sites.read :refer :all]
            [comic-reader.sites.test-util :as tu]
            [clojure.java.io :as io]
            [net.cgrand.enlive-html :as html]))

(deftest make-site-entry-test
  (is (= (make-site-entry "non-existent")
         ["non-existent" nil]))

  (let [[label opts] (make-site-entry "test-site")]
    (is (= label
           "test-site"))
    (is (= (class opts)
           comic_reader.sites.MangaSite))))

(defn expect-opts-are-map [site]
  (try
    (let [opts (read-site-options site)]
      (is (map? opts)
          (str "Contents of `sites/" site ".clj'"
               " must be a map literal")))
    (catch java.lang.RuntimeException re
      (is false
          (str "Contents of `sites/" site ".clj'"
               " cannot be empty")))))

(defn try-read-file [filename]
  (try
    (read-file filename)
    (catch java.lang.RuntimeException re
      (is false
          (str "Contents of `" filename "'"
               " cannot be empty")))))

(def ^:dynamic site-name)

(defn site-test-folder []
  (format "test/%s" site-name))

(defn resource-exists? [path]
  (some-> path
          io/resource
          io/as-file
          .exists))

(defn site-test-resource [resource]
  (let [resource-path (format "%s/%s"
                              (site-test-folder)
                              resource)]
    (if (resource-exists? resource-path)
      (io/resource resource-path)
      nil)))

(defn has-test-folder? []
  (some-> (site-test-folder)
          resource-exists?))

(defn error-must-have-test-data []
  (is false
      (str "There must be a site test data folder at "
           "`resources/test/" site-name "'")))

(defn image-page-html []
  (when-let [image-resource (site-test-resource "image.html")]
    (html/html-resource image-resource)))

(defn chapter-list-html []
  (when-let [image-resource (site-test-resource "chapter_list.html")]
    (html/html-resource image-resource)))

(defn comic-list-html []
  (when-let [image-resource (site-test-resource "comic_list.html")]
    (html/html-resource image-resource)))

(defn test-extract-image-tag [html image-tag]
  (and
   (tu/ensure-dependencies-defined extract-image-tag)
   (is (= image-tag
          (extract-image-tag html)))))

(defn test-extract-pages-list [html pages-list chapter-url]
  (and
   (tu/ensure-dependencies-defined extract-pages-list)
   (is (= pages-list
          (extract-pages-list html chapter-url)))))

(defn test-image-page-extraction []
  (let [results (try-read-file
                 (site-test-resource "image.clj"))]
    (if-let [html (image-page-html)]
      (and
       (test-extract-image-tag html (:image-tag results))
       (test-extract-pages-list html
                                (:pages-list results)
                                (:chapter-url results)))
      (is false
          (str "There must be a sample image html page at "
               "`resources/test/" site-name "/image.html'")))))

(defn test-extract-chapters-list []
  (let [results (try-read-file
                 (site-test-resource "chapter_list.clj"))]
    (if-let [html (chapter-list-html)]
      (and
       (tu/ensure-dependencies-defined extract-chapters-list)
       (is (= (:chapter-list results)
              (extract-chapters-list html ""))))
      (is false
          (str "There must be a sample chapter list html page "
               "at `resources/test/" site-name "/chapter_list.html'")))))

(defn test-extract-comic-list []
  (let [results (try-read-file
                 (site-test-resource "comic_list.clj"))]
    (if-let [html (comic-list-html)]
      (and
       (tu/ensure-dependencies-defined extract-comics-list)
       (is (= (:comic-list results)
              (extract-comics-list html))))
      (is false
          (str "There must be a sample chapter list html page "
               "at `resources/test/" site-name "/comic_list.html'")))))

(defn connected-to-network? []
  (try
    (slurp (io/as-url "http://www.google.com"))
    true
    (catch java.net.SocketException e
      false)
    (catch java.net.UnknownHostException e
      false)))

(defonce run-network-tests? (atom false))

(defn try-fetch-url [url]
  (some-> url
          slurp))

(defmacro test-url [url-fn-sym]
  `(and
    (tu/ensure-dependencies-defined ~url-fn-sym)
    (is (not (nil? (try-fetch-url (~url-fn-sym)))))))

(defn test-scrape-urls []
  (and
   @run-network-tests?

   (is (not (nil? (test-url root-url))))
   (is (not (nil? (test-url manga-list-url))))))

(defn ensure-all-dependencies []
  (tu/ensure-dependencies-defined get-comic-list)
  (tu/ensure-dependencies-defined get-chapter-list)
  (tu/ensure-dependencies-defined get-page-list)
  (tu/ensure-dependencies-defined get-image-data))

(defn test-full-site-traversal [site]
  (and
   (call-with-options site #(ensure-all-dependencies))
   @run-network-tests?

   (is (not
        (nil?
         (let [site ((get-sites) site-name)
               comic-list (get-comic-list site)
               first-comic (first comic-list)

               chapter-list (get-chapter-list site (:id first-comic))
               last-chapter (last chapter-list)

               page-list (get-page-list site last-chapter)
               third-page (nth 3 page-list)]
           (get-image-data site third-page)))))))

(defn testdef-form [site-name]
  `(deftest ~(symbol (str site-name "-test"))
     (is
      (binding [~'site-name ~site-name]
        (expect-opts-are-map ~site-name)
        (call-with-options
         ((get-sites) site-name)
         #(and
           (if (has-test-folder?)
             (and
              (test-image-page-extraction)
              (test-extract-chapters-list)
              (test-extract-comic-list))
             (error-must-have-test-data))
           (when (connected-to-network?)
             (test-scrape-urls))))
        (when (connected-to-network?)
          (test-full-site-traversal ((get-sites) site-name)))
        true))))

(defmacro defsite-tests []
  (try
    (let [site-names (->> (get-sites)
                          (map first)
                          (filter (complement #{"test-site"})))]
      `(do ~@(map testdef-form site-names)))
    (catch RuntimeException e
      `(do))))

(defsite-tests)
