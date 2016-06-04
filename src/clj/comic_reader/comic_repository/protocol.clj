(ns comic-reader.comic-repository.protocol)

(defprotocol ComicRepository
  (previous-pages [this site comic-id location n] "Get up-to n pages that precede location in a comic.")
  (next-pages [this site comic-id location n] "Get up-to n pages that follow location in a comic"))
