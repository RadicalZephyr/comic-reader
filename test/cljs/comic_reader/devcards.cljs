(ns comic-reader.devcards
  (:require [devcards.core :as dc]
            comic-reader.main-test
            comic-reader.api-test
            comic-reader.ui.base-test
            comic-reader.ui.comic-list-test
            comic-reader.ui.component-css-test
            comic-reader.ui.image-test
            comic-reader.ui.overlay-test
            comic-reader.ui.site-list-test
            comic-reader.ui.reader-test
            comic-reader.ui.waypoints-test))

(defn main []
  (dc/start-devcard-ui!))
