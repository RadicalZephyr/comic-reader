(ns comic-reader.sites.read
  (:require [clojure.java.io :as io]))

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
