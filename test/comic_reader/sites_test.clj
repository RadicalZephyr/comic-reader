(ns comic-reader.sites-test
  (:require [clojure.test :refer :all]
            [comic-reader.sites.protocol :refer :all]
            [comic-reader.sites :refer :all]
            [comic-reader.sites.test-util :as tu]
            [clojure.java.io :as io]
            [net.cgrand.enlive-html :as html]))

(deftest base-name-test
  (is (= (base-name "abc.123")
         "abc"))
  (is (= (base-name "thingy.clj")
         "thingy"))
  (is (= (base-name "thing.part.two.clj")
         "thing.part.two"))
  (is (= (base-name "dir/two/three/four.clj")
         "four")))

(deftest get-all-sites-test
  (is (some #{"test-site"} (get-all-sites))))

(deftest read-site-options-test
  (is (thrown?
       java.lang.IllegalArgumentException
       (read-site-options "non-existent")))
  (is (= (class (read-site-options "test-site"))
         clojure.lang.PersistentArrayMap)))

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

(defn test-extract-pages-list [html pages-list]
  (and
   (tu/ensure-dependencies-defined extract-pages-list)
   (is (= pages-list
          (extract-pages-list html "")))))

(defn test-image-page-extraction []
  (let [results (try-read-file
                 (site-test-resource "image.clj"))]
    (if-let [html (image-page-html)]
      (and
       (test-extract-image-tag html (:image-tag results))
       (test-extract-pages-list html (:pages-list results)))
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

(defn testdef-form [site-name]
  `(deftest ~(symbol (str site-name "-test"))
     (is
      (binding [~'site-name ~site-name]
        (expect-opts-are-map ~site-name)
        (if (has-test-folder?)
          (call-with-options
           ((get-sites) site-name)
           #(and
             (test-image-page-extraction)
             (test-extract-chapters-list)
             (test-extract-comic-list)))
          (error-must-have-test-data))
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
