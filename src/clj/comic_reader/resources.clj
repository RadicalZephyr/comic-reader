(ns comic-reader.resources
  (:refer-clojure :exclude [file-seq])
  (:require [clojure.java.io :as io]
            [clojure.tools.reader :as r])
  (:import java.io.PushbackReader))

(defn file-seq [resource-prefix]
  (->> resource-prefix
       io/resource
       io/as-file
       clojure.core/file-seq
       (remove (memfn isDirectory))))

(defn- resource->stream [resource]
  (->> (str resource)
       (.getResourceAsStream (clojure.lang.RT/baseLoader))))

(defn read-resource [resource]
  (when-let [r1 (some-> resource
                        resource->stream
                        io/reader
                        PushbackReader.)]
    (with-open [r r1]
      (r/read r))))
