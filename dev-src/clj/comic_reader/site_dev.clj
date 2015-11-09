(ns comic-reader.site-dev
  (:require [clojure.java.io :refer [as-file resource]]
            [clojure.test :refer [run-tests]]
            [clojure.tools.namespace.repl :refer [refresh]]))

(defn run-site-tests* []
  (require 'comic-reader.sites-test)
  (run-tests (find-ns 'comic-reader.sites-test)))

(defn touch-file [file]
  (.setLastModified (as-file file) (System/currentTimeMillis)))

(defn run-site-tests []
  (touch-file (resource "comic_reader/sites_test.clj"))
  (refresh :after 'comic-reader.site-dev/run-site-tests*))
