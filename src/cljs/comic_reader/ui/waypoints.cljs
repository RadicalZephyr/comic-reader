(ns comic-reader.ui.waypoints
  (:require [reagent.core :as reagent])
  (:import (goog.async Throttle)))

(def ^:private waypoints (atom {}))

(defn get-element-top [node]
  (let [rect (.getBoundingClientRect node)]
    (+ (.-top rect) (.-pageYOffset js/window))))

(defn- make-reset-trigger-point [id options]
  (fn [this]
    (swap! waypoints assoc-in
           [id :trigger-point]
           (get-element-top (reagent/dom-node this)))))

(defn waypoint [child-el opts]
  (let [id (gensym "waypoint-id")
        options (atom opts)
        reset-trigger-point! (make-reset-trigger-point id options)]
    (reagent/create-class
     {:display-name "waypoint"

      :component-will-mount
      (fn []
        (swap! waypoints assoc id {:options options}))
      :component-will-unmount
      (fn []
        (swap! waypoints dissoc id))

      :component-did-mount reset-trigger-point!
      :component-did-update reset-trigger-point!

      :reagent-render
      (fn [child-el opts]
        (reset! options opts)
        child-el)})))

(defn- page-offset []
  (.-pageYOffset js/window))

(defn check-waypoints [last-scroll-y]
  (let [new-scroll-y (page-offset)]
    (doseq [[id {:keys [options trigger-point]}] @waypoints]
      (when trigger-point
        (let [was-before-trigger (< last-scroll-y trigger-point)
              now-after-trigger (>= new-scroll-y trigger-point)
              crossed-forward (and was-before-trigger now-after-trigger)
              crossed-backward (and (not was-before-trigger)
                                    (not now-after-trigger))]
          (when (or crossed-forward crossed-backward)
            (let [direction (if crossed-forward :forward :backward)]
              (.log js/console "Passed" id "going" direction)
              (when-let [callback (:callback @options)]
                (callback direction)))))))))

(defn waypoint-context [_]
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
      :reagent-render identity})))
