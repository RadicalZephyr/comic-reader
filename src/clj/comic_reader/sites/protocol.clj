(ns comic-reader.sites.protocol)

(defprotocol PMangaSite
  "A protocol for traversing a manga site."

  (get-comic-list [this]
    "Retrieve the list of all comics available at the site.")

  (get-chapter-list [this comic-id]
    "Retrieve the list of all chapters in a comic.")

  (get-page-list [this comic-chapter]
    "Retrieve the list of all pages in a chapter.")

  (get-image-data [this comic-id chapter page]
    "Retrieve the image tag for displaying a particular page."))
