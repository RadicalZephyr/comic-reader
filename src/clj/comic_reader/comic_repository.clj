(ns comic-reader.comic-repository
  (:require [clojure.spec.alpha :as s]))

(s/def :site/id keyword?)
(s/def :site/name string?)

(s/def ::site (s/keys :req [:site/id :site/name]))


(s/def :comic/id qualified-keyword?)
(s/def :comic/name string?)
(s/def :comic/url string?)

(s/def ::comic (s/keys :req [:comic/id :comic/name :comic/url]))


(s/def :chapter/number int?)
(s/def :chapter/title string?)

(s/def ::chapter (s/keys :req [:chapter/number :chapter/title]))


(s/def :page/number int?)
(s/def :page/url string?)

(s/def ::page (s/keys :req [:page/number :page/url]))


(s/def :location/boundary (s/and qualified-keyword?
                                 #{:boundary/first
                                   :boundary/last}))
(s/def :location/chapter (s/keys :req [:chapter/number :chapter/title]))
(s/def :location/page (s/keys :req [:page/number :page/url]))

(s/def ::location
  (s/or :present (s/keys :req [:location/chapter :location/chapter])
        :absent  (s/keys :req [:location/boundary])))


(defprotocol ComicRepository
  (-list-sites         [this] "List all the comic sites available from this repository.")
  (-list-comics        [this site-id] "List all the comics available on this site.")
  (-previous-locations [this site-id comic-id location n] "Get up-to n locations that precede `location' in a comic.")
  (-next-locations     [this site-id comic-id location n] "Get up-to n locations that follow `location' in a comic.")
  (-image-tag          [this site-id location] "Get the hiccup image tag for this comic location."))

(defn list-sites [this]
  (-list-sites this))

(defn list-comics [this site-id]
  (-list-comics this site-id))

(defn previous-locations [this site-id comic-id location n]
  (-previous-locations this site-id comic-id location n))

(defn next-locations [this site-id comic-id location n]
  (-next-locations this site-id comic-id location n))

(defn image-tag [this site-id location]
  (-image-tag this site-id location))


(defprotocol WritableComicRepository
  (-store-sites [this sites] "Store a seq of site records.")
  (-store-comics [this site-id comics] "Store a seq of comic records.")
  (-store-locations [this site-id comic-id locations] "Store a seq of location records."))

(defn store-sites [this sites]
  (-store-sites this sites))

(defn store-comics [this site-id comics]
  (-store-comics this site-id comics))

(defn store-locations [this site-id comic-id locations]
  (-store-locations this site-id comic-id locations))
