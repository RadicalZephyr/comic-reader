(ns comic-reader.devcards
  (:require [devcards.core :refer-macros [defcard defcard-rg]]
            [comic-reader.main :as main]
            [comic-reader.main-test]))

(defcard-rg loading-card
  "# Loading
   This is the loading svg used everywhere on the site."
  (main/loading))
