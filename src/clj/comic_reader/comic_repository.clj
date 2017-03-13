(ns comic-reader.comic-repository)

(defprotocol ComicRepository
  (list-sites         [this] "List all the comic sites available from this repository.")
  (list-comics        [this site-id] "List all the comics available on this site.")
  (previous-locations [this site-id comic-id location n] "Get up-to n locations that precede `location' in a comic.")
  (next-locations     [this site-id comic-id location n] "Get up-to n locations that follow `location' in a comic.")
  (image-tag          [this site-id location] "Get the hiccup image tag for this comic location."))

(defprotocol WritableComicRepository
  (store-sites [this sites] "Store a seq of site records.")
  (store-comics [this site-id comics] "Store a seq of comic records.")
  (store-locations [this site-id comic-id locations] "Store a seq of location records."))
