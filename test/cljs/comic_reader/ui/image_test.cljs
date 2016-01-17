(ns comic-reader.ui.image-test
  (:require [devcards.core :refer-macros [defcard-rg]]
            [reagent.core :as reagent]
            [comic-reader.ui.image :as sut]))

(defcard-rg image-card
  [sut/comic-image {} [:img {:src "http://lovemeow.com/wp-content/uploads/2011/08/6073678501_98445d2648_z.jpg"}]])
