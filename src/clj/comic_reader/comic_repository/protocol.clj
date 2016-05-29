(ns comic-reader.comic-repository.protocol)

(defprotocol ComicRepository
  (get-first-location [this] "Get first page in a comic.")
  (previous-pages [this location n] "Get up-to n pages that precede location in a comic.")
  (next-pages [this location n] "Get up-to n pages that follow location in a comic"))
