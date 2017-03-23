(ns comic-reader.ui.waypoints
  (:require [cljs.reader :as r]
            [clojure.string :as str]
            [reagent.core :as reagent])
  (:import (goog.async Throttle)))

(def ^:private all-trigger-points (atom {}))

(defn- parse-percentage [s]
  (let [percent-str (str "0." (str/replace s #"%|\." ""))
        reader-result (r/read-string percent-str)]
    (if (number? reader-result)
      reader-result
      0)))

(defn- as-percentage [node-height offset-percent]
  (let [percentage (parse-percentage offset-percent)]
    (* node-height percentage)))

(defn- as-keyword [node offset]
  (case offset
    :bottom-in-view (- (.-clientHeight node)
                       (.-innerHeight js/window))
    0))

(defn- get-offset-position [node offset]
  (cond
    (number? offset)  offset
    (string? offset)  (as-percentage (.-clientHeight node) offset)
    (fn? offset)      (offset node)
    (keyword? offset) (as-keyword node offset)
    :else             0))

(defn- get-node-position-at-offset [node offset]
  (let [offset-position (get-offset-position node offset)
        rect (.getBoundingClientRect node)]
    (+ (.-top rect)
       (.-pageYOffset js/window)
       offset-position)))

(defn- make-reset-trigger-point [all-trigger-points id options]
  (fn [node]
    (swap! all-trigger-points assoc-in
           [id :trigger-point]
           (get-node-position-at-offset node (:offset @options)))))

(defn- make-trigger-point [all-trigger-points opts]
  (let [id (gensym "waypoint-id")
        options (atom opts)]
    {:id id
     :options options
     :reset-trigger-point! (make-reset-trigger-point all-trigger-points id options)}))

(defn- make-trigger-points [all-trigger-points opts]
  (if-let [offsets (:offsets opts)]
    (mapv (fn [offset]
            (make-trigger-point all-trigger-points (assoc opts :offset offset)))
          offsets)
    [(make-trigger-point all-trigger-points opts)]))

(defn waypoint
  ([child-el] (waypoint {} child-el))
  ([opts child-el]
   (let [trigger-points (make-trigger-points all-trigger-points opts)
         reset-trigger-points! (fn [this]
                                 (doseq [trigger-point trigger-points]
                                   ((:reset-trigger-point! trigger-point) (reagent/dom-node this))))]
     (reagent/create-class
      {:display-name "waypoint"

       :component-will-mount
       (fn []
         (doseq [trigger-point trigger-points]
           (swap! all-trigger-points assoc (:id trigger-point) (select-keys trigger-point [:options]))))

       :component-will-unmount
       (fn []
         (doseq [trigger-point trigger-points]
           (swap! all-trigger-points dissoc (:id trigger-point))))

       :component-did-mount  reset-trigger-points!
       :component-did-update reset-trigger-points!

       :reagent-render
       (fn [opts child-el]
         (doseq [trigger-point trigger-points]
           (swap! (:options trigger-point) merge opts))
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

(defn check-trigger-points [old-scroll-y]
  (let [new-scroll-y (page-offset)]
    (doseq [[id {:keys [options trigger-point]}] @all-trigger-points
            :when trigger-point]
      (when-let [direction (crossed? trigger-point old-scroll-y new-scroll-y)]
        (when-let [callback (:callback @options)]
          (callback direction))))))

(defn waypoint-context [_]
  (let [state (atom {})
        throttler (Throttle. #(let [old-scroll-y (:old-scroll-y @state)]
                                (swap! state assoc
                                       :ticking false
                                       :old-scroll-y nil)
                                (check-trigger-points old-scroll-y))
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
