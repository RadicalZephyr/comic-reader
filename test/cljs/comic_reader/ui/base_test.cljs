(ns comic-reader.ui.base-test
  (:require [devcards.core :refer-macros [defcard-rg]]
            [comic-reader.ui.base :as ui-base]))

(defcard-rg loading
  "## Loading
   This is the loading svg used everywhere on the site."
  [:div {:style {"width" "4em"}}
   (ui-base/loading)])

(defcard-rg four-oh-four
  (ui-base/four-oh-four))

(defcard-rg large-button
  (ui-base/large-button "Button"))
