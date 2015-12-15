(ns comic-reader.util
  (:require [clojure.string :as s]
            [clojure.tools.reader.edn :as edn]))

(defn unknown-val [tag val]
  {:unknown-tag tag
   :value val})

(defn safe-read-string [s]
  (edn/read-string {:default unknown-val} s))

(defn keyword->words [kw]
  (-> kw
      name
      (s/replace #"-" " ")
      (s/split #" ")))

(defn keyword->title [kw]
  (->> kw
       keyword->words
       (map s/capitalize)
       (s/join " ")))

(defmacro with-optional-tail
  "If the `content' is not falsey, append it to the `root'."
  [root content]
  (let [root# ~root
        content# ~content]
    (if content#
      (conj root# content#)
      content#)))
