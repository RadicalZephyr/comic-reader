(ns comic-reader.comic-repository.spy
  (:require [comic-reader.comic-repository :as repo]))

(def ^:private vconj (fnil conj []))

(defn- store-call [call-log fn-name args]
  (swap! call-log update fn-name vconj {:args args})
  nil)

(defprotocol Spy
  (calls [this fn-name] "Return the calls seq for the given function."))

(defrecord SpyRepository [call-log]
  Spy
  (calls [this fn-name]
    (get @call-log fn-name []))

  repo/ComicRepository
  (-list-sites [this]
    (store-call call-log :list-sites []))

  (-list-comics [this site-id]
    (store-call call-log :list-comics [site-id]))

  (-previous-locations [this comic-id location n]
    (store-call call-log :previous-locations [(namespace comic-id) comic-id location n]))

  (-next-locations [this comic-id location n]
    (store-call call-log :next-locations [(namespace comic-id) comic-id location n]))

  (-image-tag [this site-id location]
    (store-call call-log :image-tag [site-id location]))

  repo/WritableComicRepository
  (-store-sites [this sites]
    (store-call call-log :store-sites [sites]))

  (-store-comics [this comics]
    (store-call call-log :store-comics [comics]))

  (-store-locations [this comic-id locations]
    (store-call call-log :store-locations [comic-id locations])))

(defn spy-repo []
  (map->SpyRepository {:call-log (atom {})}))
