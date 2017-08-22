(ns comic-reader.comic-repository
  (:require [clojure.core.async :as async]
            [clojure.core.async.impl.protocols :as ap]
            [clojure.spec.alpha :as s]))

(s/def ::async/readable-port #(satisfies? ap/ReadPort %))
(s/def ::async/writable-port #(satisfies? ap/WritePort %))

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


(s/def :location/boundary #{:boundary/first
                            :boundary/last})
(s/def :location/chapter ::chapter)
(s/def :location/page ::page)

(s/def ::location (s/keys :opt [:location/boundary
                                :location/chapter
                                :location/page]))


(defprotocol ComicRepository
  (-list-sites         [this] "List all the comic sites available from this repository.")
  (-list-comics        [this site-id] "List all the comics available on this site.")
  (-previous-locations [this comic-id location n] "Get up-to n locations that precede `location' in a comic.")
  (-next-locations     [this comic-id location n] "Get up-to n locations that follow `location' in a comic.")
  (-image-tag          [this site-id location] "Get the hiccup image tag for this comic location."))

(s/def ::repository #(satisfies? ComicRepository %))

(defn list-sites [this]
  (-list-sites this))

(s/fdef list-sites
  :args (s/cat :this ::repository)
  :ret  ::async/readable-port)

(defn list-comics [this site-id]
  (-list-comics this site-id))

(s/fdef list-comics
  :args (s/cat :this ::repository :site-id :site/id)
  :ret  ::async/readable-port)

(defn previous-locations [this comic-id location n]
  (-previous-locations this comic-id location n))

(s/fdef previous-locations
  :args (s/cat :this ::repository
               :comic-id :comic/id
               :location ::location
               :n int?)
  :ret  ::async/readable-port)

(defn next-locations [this comic-id location n]
  (-next-locations this comic-id location n))

(s/fdef next-locations
  :args (s/cat :this ::repository
               :comic-id :comic/id
               :location (s/nilable ::location)
               :n int?)
  :ret  ::async/readable-port)

(defn image-tag [this site-id location]
  (-image-tag this site-id location))

(s/fdef image-tag
  :args (s/cat :this ::repository
               :site-id :site/id
               :location (s/keys :req [:location/page]))
  :ret  ::async/readable-port)


(defprotocol WritableComicRepository
  (-store-sites [this sites] "Store a seq of site records.")
  (-store-comics [this comics] "Store a seq of comic records.")
  (-store-locations [this comic-id locations] "Store a seq of location records."))

(s/def ::writable-repository #(satisfies? WritableComicRepository))

(defn store-sites [this sites]
  (-store-sites this sites))

(s/fdef store-sites
  :args (s/cat :this ::repository :sites (s/coll-of ::site))
  :ret  ::async/readable-port)

(defn store-comics [this comics]
  (-store-comics this comics))

(s/fdef store-comics
  :args (s/cat :this ::repository :comics (s/coll-of ::comic))
  :ret  ::async/readable-port)

(defn store-locations [this comic-id locations]
  (-store-locations this comic-id locations))

(s/fdef store-locations
  :args (s/cat :this ::repository
               :comic-id :comic/id
               :locations (s/coll-of ::location))
  :ret  ::async/readable-port)
