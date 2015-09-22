(ns comic-reader.sites.protocol)

(defprotocol PMangaSite
  "A protocol for traversing a manga site."

  (call-with-options [this f]
    "Apply the given arguments to the given function within a dynamic
    context where the sites options are bound.")

  (get-comic-list [this]
    "Retrieve the list of all comics available at the site.")

  (get-chapter-list [this comic-id]
    "Retrieve the list of all chapters in a comic.")

  (get-page-list [this comic-chapter]
    "Retrieve the list of all pages in a chapter.")

  (get-image-data [this page]
    "Retrieve the image tag for displaying a particular page."))
