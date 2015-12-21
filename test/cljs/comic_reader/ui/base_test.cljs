(ns comic-reader.ui.base-test
  (:require [devcards.core :refer-macros [defcard-rg]]
            [comic-reader.ui.base :as sut]))

(defcard-rg loading
  "## Loading
   This is the loading svg used everywhere on the site."
  [:div {:style {"width" "4em"}}
   [sut/loading]])

(defcard-rg four-oh-four
  [sut/four-oh-four])

(defcard-rg large-button
  [sut/large-button "Button"])
