(ns comic-reader.resources
  (:refer-clojure :exclude [file-seq])
  (:require [clojure.java.io :as io]
            [clojure.tools.reader :as r])
  (:import java.io.PushbackReader))

(defn resource-file [name]
  (-> name io/resource io/as-file))

(defn try-file-seq [resource-file]
  (try
    (clojure.core/file-seq resource-file)
    (catch Throwable e
      nil)))

(defn file-seq [resource-prefix]
  (some->> resource-prefix
           resource-file
           try-file-seq
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

(defn try-read-resource [resource]
  (when (io/resource resource)
    (try
      (read-resource resource)
      (catch clojure.lang.ExceptionInfo e
        nil))))
