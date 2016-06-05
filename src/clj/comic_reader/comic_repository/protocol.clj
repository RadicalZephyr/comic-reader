(ns comic-reader.comic-repository.protocol)

(defprotocol ComicRepository
  (previous-pages [this site comic-id page n] "Get up-to n pages that precede `page' in a comic.")
  (next-pages [this site comic-id page n] "Get up-to n pages that follow `page' in a comic"))
