(ns comic-reader.ui.image
  (:require [reagent.core :as reagent]
            [cljsjs.waypoints]))

(defn make-waypoint [options]
  (js/Waypoint. (clj->js options)))

(defn make-img-did-mount [set-waypoints! set-current-comic]
  (fn [this]
    (let [node (reagent/dom-node this)
          wp-down (make-waypoint {:element node
                                  :handler #(when (= %
                                                     "down")
                                              (set-current-comic))})
          wp-up (make-waypoint {:element node
                                :offset "bottom-in-view"
                                :handler #(when (= %
                                                   "up")
                                            (set-current-comic))})]
      (set-waypoints! [wp-up wp-down]))))

(defn comic-image [set-current-comic tag]
  (let [waypoints (atom nil)]
    (reagent/create-class
     {:display-name "comic-image"
      :component-did-mount
      (make-img-did-mount
       #(reset! waypoints %)
       set-current-comic)
      :component-will-unmount
      (fn []
        (map #(.destroy %) @waypoints)
        (reset! waypoints nil))
      :reagent-render
      (fn [_ tag]
        [:div.row
         [:div.medium-12.columns tag]])})))
