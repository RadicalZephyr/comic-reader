(ns comic-reader.comic-repository.protocol)

(defprotocol ComicRepository
  (previous-locations [this site comic-id location n] "Get up-to n locations that precede `location' in a comic.")
  (next-locations     [this site comic-id location n] "Get up-to n locations that follow `location' in a comic.")
  (image-tag [this site comic-id location] "Get the hiccup image tag for this comic location."))
