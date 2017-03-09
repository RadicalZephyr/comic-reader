(ns comic-reader.ui.image-test
  (:require [devcards.core :refer-macros [defcard-rg]]
            [reagent.core :as reagent]
            [comic-reader.ui.image :as sut]))

(defcard-rg image-card
  [sut/comic-image identity [:img {:src "/public/img/tux.png"}]])
