(ns comic-reader.ui.waypoints
  (:require [cljs.reader :as r]
            [clojure.string :as str]
            [reagent.core :as reagent])
  (:import (goog.async Throttle)))

(def ^:private waypoints (atom {}))

(defn- parse-percentage [s]
  (let [percent-str (str "0." (str/replace s #"%|\." ""))
        reader-result (r/read-string percent-str)]
    (if (number? reader-result)
      reader-result
      0)))

(defn- as-percentage [node-height offset-percent]
  (let [percentage (parse-percentage offset-percent)]
    (* node-height percentage)))

(defn- get-offset-position [node offset]
  (cond
    (number? offset)  offset
    (string? offset)  (as-percentage (.-clientHeight node) offset)
    (fn? offset)      (offset node)
    :else             0))

(defn- get-node-position-at-offset [node offset]
  (let [offset-position (get-offset-position node offset)
        rect (.getBoundingClientRect node)]
    (+ (.-top rect)
       (.-pageYOffset js/window)
       offset-position)))

(defn- make-reset-trigger-point [waypoints id options]
  (fn [this]
    (swap! waypoints assoc-in
           [id :trigger-point]
           (get-node-position-at-offset (reagent/dom-node this) (:offset @options)))))

(defn waypoint
  ([child-el] (waypoint {} child-el))
  ([opts child-el]
   (let [id (gensym "waypoint-id")
         options (atom opts)
         reset-trigger-point! (make-reset-trigger-point waypoints id options)]
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
       (fn [opts child-el]
         (reset! options opts)
         child-el)}))))

(defn- page-offset []
  (.-pageYOffset js/window))

(defn- crossed? [trigger-point old-scroll-y new-scroll-y]
  (let [was-before-trigger (<  old-scroll-y trigger-point)
        now-after-trigger  (>= new-scroll-y trigger-point)
        crossed-forward?  (and was-before-trigger
                               now-after-trigger)
        crossed-backward? (and (not was-before-trigger)
                               (not now-after-trigger))]
    (cond
      crossed-forward?  :forward
      crossed-backward? :backward
      :else             nil)))

(defn check-waypoints [old-scroll-y]
  (let [new-scroll-y (page-offset)]
    (doseq [[id {:keys [options trigger-point]}] @waypoints
            :when trigger-point]
      (when-let [direction (crossed? trigger-point old-scroll-y new-scroll-y)]
        (.log js/console "Passed" id "going" direction)
        (when-let [callback (:callback @options)]
          (callback direction))))))

(defn waypoint-context [_]
  (let [state (atom {})
        throttler (Throttle. #(let [old-scroll-y (:old-scroll-y @state)]
                                (swap! state assoc
                                       :ticking false
                                       :old-scroll-y nil)
                                (check-waypoints old-scroll-y))
                             250)
        listener-fn (fn []
                      (when (not (:ticking @state))
                        (swap! state assoc
                               :ticking true
                               :old-scroll-y (page-offset)))
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
