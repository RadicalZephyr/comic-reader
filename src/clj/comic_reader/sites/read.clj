(ns comic-reader.sites.read
  (:require [clojure.java.io :as io]
            [clojure.tools.reader :as r]
            [comic-reader.resources :as resources]
            [environ.core :refer [env]])
  (:import java.io.PushbackReader))

(def sites-list-file-name "sites-list.clj")

(defn- base-name [file]
  (->> file
       io/as-file
       .getName
       (re-matches #"^(.*)\..*?$")
       second))

(defn find-all-sites []
  (->> "sites"
       resources/file-seq
       (map base-name)))

(defn get-sites-list []
  (if (io/resource sites-list-file-name)
    (resources/read-resource sites-list-file-name)
    (find-all-sites)))

(defn read-site-options [site-name]
  (if-let [file (str "sites/" site-name ".clj")]
    (resources/read-resource file)
    (throw (IllegalArgumentException.
            (str "`sites/" site-name "' "
                 "was not found in the resources.")))))
