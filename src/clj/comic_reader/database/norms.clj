(ns comic-reader.database.norms
  (:require [clojure.java.io :as io]
            [clojure.string  :as str]
            [clojure.edn     :as edn])
  (:import (java.io File
                    PushbackReader)))

(defn norms-seq [norms-dir]
  (->> norms-dir
       io/resource
       io/as-file
       file-seq
       (remove (memfn ^File isDirectory))
       (filter #(str/ends-with? % ".edn"))
       seq))

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
