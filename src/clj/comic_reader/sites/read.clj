(ns comic-reader.sites.read
  (:require [clojure.java.io :as io]
            [clojure.tools.reader :as r]
            [clojure.string :as str]
            [comic-reader.resources :as resources]
            [environ.core :refer [env]])
  (:import java.io.PushbackReader))

(def sites-list-file-name "sites-list.edn")

(defn- file-name [file]
  (->> file
       io/as-file
       .getName))

(def process-sites
  (comp
   (filter #(str/ends-with? % ".site.edn"))
   (map file-name)
   (map #(str/replace % #"\.site\.edn$" ""))))

(defn find-all-sites []
  (into [] process-sites
        (resources/resource-seq "sites")))

(defn get-sites-list []
  (or
   (seq (resources/try-read-resource sites-list-file-name))
   (find-all-sites)))

(defn read-site-options [site-name]
  (if-let [file (str "sites/" site-name ".site.edn")]
    (resources/read-resource file)
    (throw (IllegalArgumentException.
            (str "`sites/" site-name "' "
                 "was not found in the resources.")))))
