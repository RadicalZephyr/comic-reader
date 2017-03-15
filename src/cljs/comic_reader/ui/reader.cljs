(ns comic-reader.ui.reader
  (:require [re-frame.core :as re-frame]
            [comic-reader.ui.image :as image]
            [comic-reader.api :as api]
            [comic-reader.ui.base :as base]
            [clairvoyant.core :refer-macros [trace-forms]]
            [re-frame-tracer.core :refer [tracer]]))

(defn partitioned-locations [locations current-location]
  (let [partitioned (if (seq locations)
                      (->> (concat [nil] locations [nil])
                           (partition-by #(= % current-location))
                           (map #(keep identity %)))
                      [])]
    partitioned))

(defn current-locations [partitioned-locations]
  (let [[before current after] partitioned-locations]
    (concat before current (take 2 after))))

(defn location-key [location]
  [(get-in location [:location/chapter :chapter/number])
   (get-in location [:location/page :page/number])])

(defn location-sorted-set []
  (sorted-set-by (fn [loc-a loc-b]
                   (compare (location-key loc-a)
                            (location-key loc-b)))))

(def conj-locations
  (fnil
   (fn [locations new-locations]
     (apply conj locations new-locations))
   (location-sorted-set)))

(defn add-locations [app-db locations]
  (if (seq locations)
    (update app-db :locations conj-locations locations)
    app-db))

(defn setup! []
  (trace-forms {:tracer (tracer :color "green")}
    (re-frame/reg-event-db
     :add-locations
     (fn add-locations-event [app-db [_ direction locations]]
       (-> app-db
           (add-locations locations)
           (assoc-in [:loading direction] false))))

    (re-frame/reg-event-db
     :set-current-location
     (fn set-current-location-event [app-db [_ current-location]]
       (assoc app-db :current-location current-location)))

    (re-frame/reg-event-db
     :set-buffer-size
     (fn set-buffer-size-event [app-db [_ n]]
       (assoc app-db :buffer-size n)))

    (re-frame/reg-event-db
     :set-loading-before
     (fn set-loading-before-event [app-db _]
       (assoc-in app-db [:loading :before] true)))

    (re-frame/reg-event-db
     :set-loading-after
     (fn set-loading-after-event [app-db _]
       (assoc-in app-db [:loading :after] true))))

  (trace-forms {:tracer (tracer :color "brown")}
    (re-frame/reg-sub
     :locations
     (fn locations-sub [app-db _]
       (:locations app-db)))

    (re-frame/reg-sub
     :current-location
     (fn current-location-sub [app-db _]
       (:current-location app-db)))

    (re-frame/reg-sub
     :buffer-size
     (fn buffer-size-sub [app-db _]
       (:buffer-size app-db)))

    (re-frame/reg-sub
     :loading-before
     (fn loading-before-sub [app-db _]
       (get-in app-db [:loading :before])))

    (re-frame/reg-sub
     :loading-after
     (fn loading-after-sub [app-db _]
       (get-in app-db [:loading :after])))

    ;; Level two subscription

    (re-frame/reg-sub
     :partitioned-locations
     :<- [:locations]
     :<- [:current-location]
     (fn partitioned-locations-sub [[locations current-location] _]
       (partitioned-locations (seq locations) current-location)))

    (re-frame/reg-sub
     :comic-coordinates
     :<- [:site-id]
     :<- [:comic-id]
     (fn comic-coordinates-sub [[site-id comic-id] _]
       {:site-id site-id
        :comic-id comic-id}))

    (re-frame/reg-sub
     :first-image-location
     :<- [:locations]
     (fn first-image-location-sub [locations _]
       (when locations
         (first (seq locations)))))

    (re-frame/reg-sub
     :last-image-location
     :<- [:locations]
     (fn last-image-location-sub [locations _]
       (when locations
         (first (rseq locations)))))


    ;; Level three subscriptions

    (re-frame/reg-sub
     :before-locations-count
     :<- [:partitioned-locations]
     (fn before-locations-count-sub [[before _ _] _]
       (count before)))

    (re-frame/reg-sub
     :after-locations-count
     :<- [:partitioned-locations]
     (fn after-locations-count-sub [[_ _ after] _]
       (count after)))

    (re-frame/reg-sub
     :current-locations
     :<- [:partitioned-locations]
     (fn current-locations-sub [partitioned-locations _]
       (current-locations partitioned-locations)))))

(defn reader [set-current-location locations loading-before loading-after]
  [:div
   [image/comic-location-list #(re-frame/dispatch [:set-current-location %]) locations]])

(defn view []
  (let [current-locations (re-frame/subscribe [:current-locations])]
    [reader
     #(re-frame/dispatch [:set-current-location %])
     @current-locations]))
