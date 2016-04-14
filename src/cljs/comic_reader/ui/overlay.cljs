(ns comic-reader.ui.overlay
  (:require [comic-reader.ui.base :as base]
            [garden.core :as g]))

(defn show [class-name]
  (.fadeIn (js/jQuery class-name)))

(defn hide [class-name]
  (.fadeOut (js/jQuery class-name)))

(defn overlay [{:keys [header footer display? toggle-overlay content]}]
  (let [overlay-styles {:position "absolute"
                        :background "black"
                        :width "100%"
                        :padding "0 2em"}
        overlay-class (base/unique-class :div "overlay")]
    (fn [{:keys [header footer display? toggle-overlay content]}]
      (let [change-display-fn (if display? show hide)]
        (change-display-fn (name overlay-class)))
      [:div {:style {:position "relative"}
             :on-click #(toggle-overlay)}
       [:style (g/css [overlay-class overlay-styles
                       [:h2 {:color "white"}]])]
       [overlay-class {:style {:top "0"}} header]
       [overlay-class {:style {:bottom "0"}} footer]
       content])))
