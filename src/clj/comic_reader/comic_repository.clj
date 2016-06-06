(ns comic-reader.comic-repository)

(defprotocol ComicRepository
  (list-sites         [this] "List all the comic sites available from this repository.")
  (list-comics        [this site] "List all the comics available on this site.")
  (previous-locations [this site comic-id location n] "Get up-to n locations that precede `location' in a comic.")
  (next-locations     [this site comic-id location n] "Get up-to n locations that follow `location' in a comic.")
  (image-tag          [this site location] "Get the hiccup image tag for this comic location."))
