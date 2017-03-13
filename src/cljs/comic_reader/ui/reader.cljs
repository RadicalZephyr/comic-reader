(ns comic-reader.ui.reader
  (:require [re-frame.core :as re-frame]
            [comic-reader.ui.image :as image]
            [comic-reader.api :as api]))

(defn partitioned-locations [images current-location]
  (if (seq images)
    (->> (concat [nil] images [nil])
         (partition-by #(= (:image/location %) current-location))
         (map #(keep identity %)))
    []))

(defn current-locations [partitioned-locations n]
  (let [[before current after] partitioned-locations]
    (if (number? n)
      (concat (take-last n before) current (take n after))
      [])))

(defn setup! []
  (re-frame/reg-sub
   :locations
   (fn [app-db _]
     (:locations app-db)))

  (re-frame.core/reg-event-db
   :set-locations
   (fn [app-db [_ locations]]
     (assoc app-db :locations locations)))

  (re-frame/reg-sub
   :current-location
   (fn [app-db _]
     (:current-location app-db)))

  (re-frame/reg-event-db
   :set-current-location
   (fn [app-db [_ current-location]]
     (assoc app-db :current-location current-location)))

  (re-frame/reg-sub
   :buffer-size
   (fn [app-db _]
     (:buffer-size app-db)))

  (re-frame/reg-event-db
   :set-buffer-size
   (fn [app-db [_ n]]
     (assoc app-db :buffer-size n)))


  ;; Level two subscription

  (re-frame/reg-sub
   :partitioned-locations
   :<- [:locations]
   :<- [:current-location]
   (fn [[images current-location] _]
     (partitioned-locations images current-location)))

  (re-frame/reg-sub
   :comic-coordinates
   :<- [:site-id]
   :<- [:comic-id]
   (fn [[site-id comic-id] _]
     {:site-id site-id
      :comic-id comic-id}))

  (re-frame/reg-sub
   :first-image-location
   :<- [:locations]
   (fn [[images] _]
     (first images)))

  (re-frame/reg-sub
   :last-image-location
   :<- [:locations]
   (fn [[images] _]
     (last images)))


  ;; Level three subscriptions

  (re-frame/reg-sub
   :before-locations-count
   :<- [:partitioned-locations]
   (fn [[before _ _] _]
     (count before)))

  (re-frame/reg-sub
   :after-locations-count
   :<- [:partitioned-locations]
   (fn [[_ _ after] _]
     (count after)))

  (re-frame/reg-sub
   :loading-before-buffer
   :<- [:comic-coordinates]
   :<- [:buffer-size]
   :<- [:first-image-location]
   :<- [:before-locations-count]
   (fn [[comic-coord buffer-size first-location before-buffer-size] _]
     (when (> buffer-size before-buffer-size)
       (api/get-prev-locations (:site-id comic-coord)
                               (:comic-id comic-coord)
                               first-location
                               buffer-size
                               {:on-success #(re-frame/dispatch [:add-locations-before %])})
       true)))

  (re-frame/reg-sub
   :loading-after-buffer
   :<- [:comic-coordinates]
   :<- [:buffer-size]
   :<- [:last-image-location]
   :<- [:after-locations-count]
   (fn [[comic-coord buffer-size last-location after-buffer-size] _]
     (when (> buffer-size after-buffer-size)
       (api/get-next-locations (:site-id comic-coord)
                               (:comic-id comic-coord)
                               last-location
                               buffer-size
                               {:on-success #(re-frame/dispatch [:add-locations-after %])})
       true)))

  (re-frame/reg-sub
   :current-locations
   :<- [:partitioned-locations]
   :<- [:buffer-size]
   (fn [[partitioned-locations buffer-size] _]
     (current-locations partitioned-locations buffer-size))))

(defn- location-id [location]
  (str (get-in location [:location/chapter :chapter/number])
       "-"
       (get-in location [:location/page :page/number])))

(defn- make-comic-image [set-current-location location]
  (with-meta
    [image/comic-image-container #(set-current-location location) location]
    {:key (location-id location)}))

(defn comic-location-list [set-current-location locations]
  [:div (map #(make-comic-image set-current-location %) locations)])

(defn reader [set-current-location locations]
  [comic-location-list #(re-frame/dispatch [:set-current-location %]) locations])

(defn view []
  (let [current-locations (re-frame/subscribe [:current-locations])
        loading-before (re-frame/subscribe [:loading-before-buffer])
        loading-after  (re-frame/subscribe [:loading-after-buffer])]
    [reader #(re-frame/dispatch [:set-current-location %]) @current-locations]))
