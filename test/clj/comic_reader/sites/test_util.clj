(ns comic-reader.sites.test-util
  (:require [clojure.string :as str]
            [clojure.test :refer :all]
            [clojure.template :as template]
            [clojure.template :refer [do-template]]
            [clj-http.client :as client]
            [loom.graph :as graph]
            [loom.alg :as alg]))

(def dependency-dag
  (graph/digraph
   [:get-image-data :extract-image-tag]

   [:get-page-list :extract-pages-list]

   [:get-chapter-list :comic->url]
   [:get-chapter-list :extract-chapters-list]

   [:get-comic-list :manga-list-url]
   [:get-comic-list :extract-comics-list]

   [:extract-image-tag :image-selector]

   [:extract-pages-list :chapter-number-pattern]
   [:extract-pages-list :page-list-selector]
   [:extract-pages-list :gen-extract-pages-list-normalize]

   [:gen-extract-pages-list-normalize :page-normalize-pattern]
   [:gen-extract-pages-list-normalize :page-normalize-format]

   [:comic->url :comic->url-format]
   [:comic->url :manga-url]

   [:extract-chapters-list :chapter-list-selector]
   [:extract-chapters-list :chapter-link-normalize]

   [:chapter-link-normalize :chapter-link->map]
   [:chapter-link-normalize :chapter-link-add-ch-num]

   [:chapter-link-add-ch-num :chapter-number-match-pattern]

   [:extract-comics-list :comic-list-selector]
   [:extract-comics-list :comic-link-normalize]

   [:comic-link-normalize :comic-link->map]
   [:comic-link-normalize :comic-link-add-id]

   [:comic-link-add-id :manga-pattern]

   [:comic-link->map :comic-link-name-normalize]
   [:comic-link->map :comic-link-url-normalize]

   [:chapter-link->map :chapter-link-name-normalize]
   [:chapter-link->map :chapter-link-url-normalize]

   [:manga-pattern :manga-url]
   [:manga-pattern :manga-url-suffix-pattern]

   [:manga-list-url :root-url]
   [:manga-list-url :manga-list-format]

   [:manga-url :root-url]
   [:manga-url :manga-url-format]
   ))

(def data-function?
  #{:root-url
    :manga-list-format
    :manga-url-format
    :manga-url-suffix-pattern

    :comic->url-format

    :chapter-list-selector
    :comic-list-selector
    :image-selector
    :page-list-selector

    :chapter-number-pattern
    :chapter-number-match-pattern

    :chapter-link-name-normalize
    :chapter-link-url-normalize

    :comic-link-name-normalize
    :comic-link-url-normalize

    :page-normalize-format
    :page-normalize-pattern})

(defn- key->sym
  ([k]
   (-> k name symbol comp))
  ([ns-key k]
   (->> [ns-key k]
        (map name)
        (str/join "/")
        key->sym)))

(defmacro test-data-functions-not-nil []
  `(are [selector] (is (not= (selector)
                             nil))
     ~@(->> (seq data-function?)
            (map (partial key->sym :sut)))))

(defmacro and-template [argv expr & values]
  (let [c (count argv)]
    `(and ~@(map (fn [a] (template/apply-template argv expr a))
                 (partition c values)))))

(defmacro are-with-msg [argv expr & args]
  (if (or
       ;; (are [] true) is meaningless but ok
       (and (empty? argv) (empty? args))
       ;; Catch wrong number of args
       (and (pos? (count argv))
            (pos? (count args))
            (zero? (mod (count args) (count argv)))))
    `(and-template ~argv ~expr ~@args)
    (throw (IllegalArgumentException. "The number of args doesn't match are's argv."))))

(defmacro get-doc-string [sym]
  `(-> ~sym
       var
       meta
       :doc))

(defn- split-fn-name [fn-name]
  (map keyword [(namespace fn-name) (name fn-name)]))

(defmacro ensure-dependencies-defined
  ([options-sym fn-name]
   `(ensure-dependencies-defined ~options-sym ~fn-name nil))
  ([options-sym fn-name syms-ns]
   (let [[ns-keyword fn-keyword] (split-fn-name fn-name)
         syms-ns (or syms-ns ns-keyword)]
     (if (graph/has-node? dependency-dag fn-keyword)
       `(are-with-msg [selector#]
                      (is (not= (selector# ~options-sym)
                                nil)
                          (str "Site config "
                               "`" 'selector# "'"
                               " cannot be undefined.\n"
                               "Should be:\n"
                               (get-doc-string selector#)
                               "\n"))

                      ~@(->> fn-keyword
                             (alg/topsort dependency-dag)
                             (filter data-function?)
                             (map (partial key->sym syms-ns))))
       (throw (IllegalArgumentException.
               (str fn-name " is not a valid function name.")))))))

(defn has-zero-arity? [fn-sym]
  (let [sites-ns (find-ns 'comic-reader.sites)
        fn-var (ns-resolve sites-ns fn-sym)]
    (some #{[]}
          (-> fn-var
              meta
              :arglists))))

(defn render-print-data-function [fn-sym]
  `(str ~(str fn-sym) ": '" (~fn-sym) "'"))

(defmacro display-dependent-data-values [fn-name]
  (let [[ns-keyword fn-keyword] (split-fn-name fn-name)]
    (if (graph/has-node? dependency-dag fn-keyword)
      `(str ~(str fn-name) " depends on these data values:\n"
            (str/join "\n"
                      ~(->> fn-keyword
                            (alg/topsort dependency-dag)
                            (map (partial key->sym ns-keyword))
                            (filter has-zero-arity?)
                            (mapv render-print-data-function)))
            "\n")

      (throw (IllegalArgumentException.
              (str fn-name " is not a valid function name."))))))

(defn format-specifiers? [fmt specs]
  (and
   fmt
   (let [intermediate-matcher (re-matcher #"(?<!%)%(?!%)" fmt)]
     (if (seq specs)
       (loop [m (re-matcher (re-pattern (first specs)) fmt)
              specs (rest specs)
              start 0]
         (if (.find m)
           (let [end (.start m)]
             (.region intermediate-matcher start end)
             (if (.find intermediate-matcher)
               false
               (if (seq specs)
                 (recur (.usePattern m (re-pattern (first specs)))
                        (rest specs)
                        (.end m))
                 true)))
           false))
       (not (.find intermediate-matcher))))))
