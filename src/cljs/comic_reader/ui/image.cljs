(ns comic-reader.ui.image
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [comic-reader.api :as api]
            [comic-reader.ui.base :as base]
            [comic-reader.ui.scroll :as scroll]
            [comic-reader.ui.waypoints :as wp]
            [clairvoyant.core :refer-macros [trace-forms]]
            [re-frame-tracer.core :refer [tracer]]))

(defn setup! []
  (trace-forms {:tracer (tracer :color "green")}
    (re-frame/reg-event-db
     :store-image
     (fn store-image-event [app-db [_ location image-tag]]
       (assoc-in app-db [:images location] image-tag))))

  (trace-forms {:tracer (tracer :color "brown")}
    (re-frame/reg-sub
     :raw-image
     (fn raw-image-sub [app-db [_ location]]
       (get-in app-db [:images location])))

    (re-frame/reg-sub
     :image
     (fn image-sub-meta [[_ location] _]
       [(re-frame/subscribe [:site-id])
        (re-frame/subscribe [:raw-image location])])

     (fn image-sub [[site-id image] [_ location]]
       (if image
         image
         (when site-id
           (api/get-image site-id location {:on-success #(re-frame/dispatch [:store-image location %])})
           :loading))))))

(defn comic-image [set-current-comic tag]
  (if (= tag :loading)
    [base/loading]
    [:div.row
     [:div.medium-12.columns
      [wp/waypoint {:offsets [0 :bottom-in-view]
                    :callback set-current-comic}
       tag]]]))

(defn comic-image-container [set-current-comic location]
  (let [image (re-frame/subscribe [:image location])]
    [comic-image set-current-comic @image]))

(defn- location-id [location]
  (str (get-in location [:location/chapter :chapter/number])
       "-"
       (get-in location [:location/page :page/number])))

(defn- make-comic-image [set-current-location location]
  (with-meta
    [comic-image-container #(set-current-location location) location]
    {:key (location-id location)}))

(defn comic-location-list [set-current-location locations]
  [wp/waypoint-context
   [scroll/stabilizer
    [:div.comic-list (map #(make-comic-image set-current-location %) locations)]]])
