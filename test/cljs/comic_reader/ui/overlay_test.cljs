(ns comic-reader.ui.overlay-test
  (:require [devcards.core :refer-macros [defcard-rg]]
            [reagent.core :as reagent]
            [comic-reader.ui.overlay :as sut]))

(defcard-rg overlay-on
  [:div
   [sut/overlay {:header [:h2 "Top"] :footer [:h2 "Bottom"]}
    [:div
     [:h2 "Content Title"]
     [:p "Body paragraph"]
     [:p "Body paragraph"]
     [:p "Body paragraph"]]]
   [sut/overlay {:header [:h2 "Top"] :footer [:h2 "Bottom"]}
    [:div
     [:h2 "Content Title"]
     [:p "Body paragraph"]
     [:p "Body paragraph"]
     [:p "Body paragraph"]]]])
