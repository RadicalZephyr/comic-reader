(ns comic-reader.database.norms
  (:require [clojure.java.io :as io]
            [clojure.string  :as str]
            [clojure.edn     :as edn]
            [comic-reader.resources :as resources])
  (:import (java.io File
                    PushbackReader)))

(defn norms-seq [norms-dir]
  (seq
   (->> norms-dir
        resources/resource-seq
        (filter #(str/ends-with? % ".edn"))
        (keep (comp io/as-file io/resource)))))

(defn- norm-name [file]
  (->> file
       .getName
       (re-find #"(.*)\.edn$")
       second
       keyword))

(defn- norm-content [file]
  (with-open [f (PushbackReader. (io/reader file))]
    (edn/read {:readers *data-readers*} f)))

(defn- file->norm-entry [file]
  [(norm-name file)
   {:txes [(norm-content file)]}])

(defn files->norms-map [norms-files]
  (->> norms-files
       (map file->norm-entry)
       (into {})))

(defn norms-map [norms-dir]
  (-> norms-dir
      norms-seq
      files->norms-map))
