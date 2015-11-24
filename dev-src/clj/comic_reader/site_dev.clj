(ns comic-reader.site-dev
  (:require [clojure.java.io :refer [as-file resource]]
            [clojure.test :refer [run-tests]]
            [clojure.tools.namespace.repl :refer [refresh]]))

(defn- after-run-site-tests [network?]
  (require 'comic-reader.sites-test)
  (let [test-ns (find-ns 'comic-reader.sites-test)]
    (reset! (deref (ns-resolve test-ns 'run-network-tests?)) network?)
    (run-tests test-ns)))

(defn- *run-site-tests-without-network []
  (after-run-site-tests false))

(defn- *run-site-tests-with-network []
  (after-run-site-tests true))

(defn- touch-file [file]
  (.setLastModified (as-file file) (System/currentTimeMillis)))

(defn- *run-site-tests [after-sym]
  (touch-file (resource "comic_reader/sites_test.clj"))
  (refresh :after after-sym))

(defn run-site-tests []
  (*run-site-tests
   'comic-reader.site-dev/*run-site-tests-without-network))

(defn run-site-tests-with-network []
  (*run-site-tests
   'comic-reader.site-dev/*run-site-tests-with-network))
