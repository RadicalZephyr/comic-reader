(ns comic-reader.resources
  (:require [clojure.java.io :as io]
            [clojure.tools.reader :as r]
            [clojure.string :as str])
  (:import (java.io PushbackReader)
           (java.nio.charset StandardCharsets)
           (java.nio.file Files
                          FileSystem
                          FileSystems
                          FileSystemNotFoundException
                          LinkOption)
           (java.util Collections)))

(defn get-filesystem [uri]
  (try
    (FileSystems/getFileSystem uri)
    (catch FileSystemNotFoundException _
      (FileSystems/newFileSystem uri (Collections/emptyMap)))))

(defn resource-file [name]
  (some-> name io/resource io/as-file))

(defn resource-path [name]
  (some-> name resource-file .toPath))

(defn resource-uri [name]
  (some-> name io/resource .toURI))

(defn is-directory? [path]
  (Files/isDirectory path (into-array LinkOption [])))

(defn path-seq [path]
  (with-open [s (Files/newDirectoryStream path)]
    (let [paths (transient [])
          itr (.iterator s)]
      (while (.hasNext itr)
        (conj! paths (.next itr)))
      (persistent! paths))))

(defn jar-uri-seq [resource]
  (when-let [uri (resource-uri resource)]
    (with-open [fs (get-filesystem uri)]
      (let [root (.getPath fs resource (into-array String []))]
        (doall
         (->> (tree-seq
               (fn [^java.nio.file.Path p] (is-directory? p))
               (fn [^java.nio.file.Path d] (path-seq d))
               root)
              (remove is-directory?)
              (map (memfn toUri))))))))

(defn- file-relative-to [root-file f]
  (let [root-path (.toPath root-file)
        p (.toPath f)]
    (.toFile (.relativize root-path p))))

(defn resource-seq [resource-prefix]
  (when-let [uri (resource-uri resource-prefix)]
    (if (= "jar" (.getScheme uri))
      (->> (jar-uri-seq resource-prefix)
           (map (memfn getSchemeSpecificPart))
           (map #(second (str/split % #"!/" 2))))
      (let [file-prefix (resource-file resource-prefix)]
        (->> file-prefix
             file-seq
             (remove (memfn isDirectory))
             (map #(file-relative-to file-prefix %))
             (map (memfn getPath)))))))

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
