(ns comic-reader.database.norms
  (:require [clojure.java.io :as io]
            [io.rkn.conformity :as c]
            [clojure.string :as str])
  (:import java.io.File))

(def norms-dir "database/norms")

(defn norms-seq [norms-dir]
  (->> norms-dir
       io/resource
       io/as-file
       file-seq
       (remove (memfn ^File isDirectory))
       (filter #(str/ends-with? % ".edn"))
       seq))
