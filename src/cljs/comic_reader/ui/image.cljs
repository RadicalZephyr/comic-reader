(ns comic-reader.ui.image
  (:require [reagent.core :as reagent]
            [cljsjs.waypoints]))

(defn make-waypoint [options]
  (js/Waypoint. (clj->js options)))

(defn make-img-did-mount [set-waypoints! goto-comic]
  (fn [this]
    (let [node (reagent/dom-node this)
          wp-down (make-waypoint {:element node
                                  :handler #(when (= %
                                                     "down")
                                              (goto-comic))})
          wp-up (make-waypoint {:element node
                                :offset "bottom-in-view"
                                :handler #(when (= %
                                                   "up")
                                            (goto-comic))})]
      (set-waypoints! [wp-up wp-down]))))

(defn img-component [img-data tag]
  (let [waypoints (atom nil)]
    (reagent/create-class
     {:display-name "image-component"
      :component-did-mount
      (make-img-did-mount
       #(reset! waypoints %)
       identity ;; FIXME: where does goto-comic come from?
       )
      :component-will-unmount
      (fn []
        (map #(.destroy %) @waypoints)
        (reset! waypoints nil))
      :reagent-render
      (fn [_img-data tag]
        [:div.row
         [:div.medium-12.columns tag]])})))
