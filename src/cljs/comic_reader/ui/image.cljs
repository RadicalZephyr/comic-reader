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

(defn waypoint [child-el options]
  (let [id (gensym "waypoint-id")
        trigger-point (atom nil)
        reset-trigger-point! (fn [n]
                               (reset! trigger-point n))]
    (reagent/create-class
     {:display-name "waypoint"
      :component-will-mount
      (fn []
        (.log js/console (str id " will-mount"))
        (swap! waypoints assoc id (fn []
                                    (.log js/console (str "Waypoint " id " triggered.")))))
      :component-did-mount
      (fn []
        (.log js/console (str id " did-mount")))
      :component-will-unmount
      (fn []
        (.log js/console (str id " will-unmount"))
        (swap! waypoints dissoc id))
      :component-did-update
      (fn []
        (.log js/console (str id " did-update")))
      :reagent-render
      (fn [child-el]
        child-el)})))

(defn check-waypoints [last-scroll-y]
  (.log js/console "Last scroll was " last-scroll-y))

(defn waypoint-context [child-el]
  (let [state (atom {})
        throttler (Throttle. #(do
                                (swap! state assoc :ticking false)
                                (check-waypoints (:last-scroll-y @state))) 200)
        listener-fn (fn []
                      (when (not (:ticking @state))
                        (swap! state assoc
                               :ticking true
                               :last-scroll-y (.-scrollY js/window)))
                      (.fire throttler))]
    (reagent/create-class
     {:display-name "waypoint-context"
      :component-did-mount
      (fn []
        (.addEventListener js/window "scroll" listener-fn)
        (.log js/console "waypoint-context did-mount"))
      :component-will-unmount
      (fn []
        (.removeEventListener js/window "scroll" listener-fn)
        (.log js/console "waypoint-context did-unmount"))
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
