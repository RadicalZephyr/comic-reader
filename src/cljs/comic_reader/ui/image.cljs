(ns comic-reader.ui.image
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [comic-reader.api :as api]
            [cljsjs.waypoints]
            [comic-reader.ui.base :as base])
  (:import (goog.async Throttle)))

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

(def ^:private waypoints (atom {}))

(defn get-element-top [node]
  (let [rect (.getBoundingClientRect node)]
    (+ (.-top rect) (.-pageYOffset js/window))))

(defn- make-reset-trigger-point [id]
  (fn [this]
    (swap! waypoints assoc-in
           [id :trigger-point]
           (get-element-top (reagent/dom-node this)))))

(defn waypoint [child-el options]
  (let [id (gensym "waypoint-id")
        reset-trigger-point! (make-reset-trigger-point id)]
    (reagent/create-class
     {:display-name "waypoint"

      :component-will-mount
      (fn []
        (swap! waypoints assoc id {:callback (:callback options)}))
      :component-will-unmount
      (fn []
        (swap! waypoints dissoc id))

      :component-did-mount reset-trigger-point!
      :component-did-update reset-trigger-point!

      :reagent-render
      (fn [child-el]
        child-el)})))

(defn- page-offset []
  (.-pageYOffset js/window))

(defn check-waypoints [last-scroll-y]
  (let [new-scroll-y (page-offset)]
    (doseq [[id {:keys [callback trigger-point]}] @waypoints]
      (when trigger-point
        (let [was-before-trigger (< last-scroll-y trigger-point)
              now-after-trigger (>= new-scroll-y trigger-point)
              crossed-forward (and was-before-trigger now-after-trigger)
              crossed-backward (and (not was-before-trigger)
                                    (not now-after-trigger))]
          (when (or crossed-forward crossed-backward)
            (let [direction (if crossed-forward :forward :backward)]
              (.log js/console "Passed" id "going" direction)
              (when callback (callback direction)))))))))

(defn waypoint-context [child-el]
  (let [state (atom {})
        throttler (Throttle. #(let [last-scroll-y (:last-scroll-y @state)]
                                (swap! state assoc
                                       :ticking false
                                       :last-scroll-y nil)
                                (check-waypoints last-scroll-y)) 250)
        listener-fn (fn []
                      (when (not (:ticking @state))
                        (swap! state assoc
                               :ticking true
                               :last-scroll-y (page-offset)))
                      (.fire throttler))]
    (reagent/create-class
     {:display-name "waypoint-context"
      :component-did-mount
      (fn []
        (.addEventListener js/window "scroll" listener-fn))
      :component-will-unmount
      (fn []
        (.removeEventListener js/window "scroll" listener-fn))
      :reagent-render
      (fn [child-el]
        child-el)})))

(defn comic-image [set-current-comic tag]
  (if (= tag :loading)
    [base/loading]
    [:div.row
     [:div.medium-12.columns
      [waypoint tag {}]]]))

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
  (let [storage (atom {})]
    (reagent/create-class
     {:display-name "comic-location-list"
      :component-will-update
      (fn [this]
        (let [node (reagent/dom-node this)]
          (swap! storage assoc
                 :scroll-height (.-scrollHeight node)
                 :scroll-top (.-scrollTop node))))
      :component-did-update
      (fn [this]
        (let [node (reagent/dom-node this)
              curr-scroll-height (.-scrollTop node)
              prev-scroll-height (:scroll-height @storage)
              prev-scroll-top    (:scroll-top @storage)]
          (set! (.-scrollTop node)
                (+ prev-scroll-top (- curr-scroll-height prev-scroll-height)))))
      :reagent-render
      (fn [set-current-location locations]
        [waypoint-context
         [:div.comic-list (map #(make-comic-image set-current-location %) locations)]])})))
