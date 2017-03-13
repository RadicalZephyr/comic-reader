(ns comic-reader.ui.image
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [comic-reader.api :as api]
            [cljsjs.waypoints]
            [comic-reader.ui.base :as base]))

(defn setup! []
  (re-frame/reg-sub
   :raw-image
   (fn [app-db [_ location]]
     (get-in app-db [:images location])))

  (re-frame/reg-sub
   :image
   (fn [[_ location] _]
     [(re-frame/subscribe [:site-id])
      (re-frame/subscribe [:raw-image location])])

   (fn [[site-id image] [_ location]]
     (if image
       image
       (when site-id
         (api/get-image site-id location {:on-success #(re-frame/dispatch [:store-image location %])})
         :loading))))

  (re-frame/reg-event-db
   :store-image
   (fn [app-db [_ location image-tag]]
     (assoc-in app-db [:images location] image-tag))))

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
        (if (= tag :loading)
          [base/loading]
          [:div.row
           [:div.medium-12.columns tag]]))})))

(defn comic-image-container [set-current-comic location]
  (let [image (re-frame/subscribe [:image location])]
    [comic-image set-current-comic @image]))
