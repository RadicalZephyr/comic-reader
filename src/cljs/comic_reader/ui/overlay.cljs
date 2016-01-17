(ns comic-reader.ui.overlay
  (:require [garden.core :as g]))

(defn overlay [{:keys [header footer]} content]
  (let [overlay-styles {:position "absolute"
                        :background "black"
                        :display "block"
                        :width "100%"
                        :padding "0 2em"}]
    [:div {:style {:position "relative"}}
     [:style (g/css [:div.overlay overlay-styles [:h2 {:color "white"}]])]
     [:div.overlay {:style {:top "0"}} header]
     [:div.overlay {:style {:bottom "0"}} footer]
     content]))
