(ns comic-reader.sites.read
  (:require [clojure.java.io :as io]
            [clojure.tools.reader :as r]
            [environ.core :refer [env]])
  (:import java.io.PushbackReader))

(def sites-list-file-name "sites-list.clj")

(defn base-name [file]
  (let [[_ base-name]
        (->> file
             io/as-file
             .getName
             (re-matches #"^(.*)\..*?$"))]
    base-name))

(defn find-all-sites []
  (->> (io/resource "sites")
       io/as-file
       file-seq
       (remove (memfn isDirectory))
       (map base-name)))

(defn resource->stream [resource]
  (->> (str resource)
       (.getResourceAsStream (clojure.lang.RT/baseLoader))))

(defn read-resource [resource]
  (when-let [r1 (some-> resource
                        resource->stream
                        io/reader
                        PushbackReader.)]
    (with-open [r r1]
      (r/read r))))

(defn get-sites-list []
  (if (io/resource sites-list-file-name)
    (read-resource sites-list-file-name)
    (find-all-sites)))

(defn read-site-options [site-name]
  (if-let [file (str "sites/" site-name ".clj")]
    (read-resource file)
    (throw (IllegalArgumentException.
            (str "`sites/" site-name "' "
                 "was not found in the resources.")))))
