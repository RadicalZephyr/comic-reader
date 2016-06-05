(ns comic-reader.comic-repository.protocol)

(defprotocol ComicRepository
  (previous-locations [this site comic-id location n] "Get up-to n locations that precede `location' in a comic.")
  (next-locations     [this site comic-id location n] "Get up-to n locations that follow `location' in a comic"))
