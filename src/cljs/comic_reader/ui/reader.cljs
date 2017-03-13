(ns comic-reader.ui.reader
  (:require [re-frame.core :as re-frame]
            [comic-reader.ui.image :as image]
            [comic-reader.api :as api]))

(defn partitioned-images [images current-location]
  (if (seq images)
    (->> (concat [nil] images [nil])
         (partition-by #(= (:image/location %) current-location))
         (map #(keep identity %)))
    []))

(defn current-images [partitioned-images n]
  (let [[before current after] partitioned-images]
    (concat (take-last n before) current (take n after))))

(defn setup! []
  (re-frame/reg-sub
   :locations
   (fn [app-db _]
     (:locations app-db)))

  (re-frame.core/reg-event-db
   :set-locations
   (fn [app-db [_ images]]
     (assoc app-db :locations images)))

  (re-frame/reg-sub
   :images
   (fn [app-db _]
     (:images app-db)))

  (re-frame.core/reg-event-db
   :set-images
   (fn [app-db [_ images]]
     (assoc app-db :images images)))

  (re-frame/reg-sub
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
     (partitioned-images images current-location)))

  (re-frame/reg-sub
   :comic-coordinates
   :<- [:site-id]
   :<- [:comic-id]
   (fn [[site-id comic-id] _]
     {:site-id site-id
      :comic-id comic-id}))

  (re-frame/reg-sub
   :first-image-location
   :<- [:images]
   (fn [[images] _]
     (:image/location (first images))))

  (re-frame/reg-sub
   :last-image-location
   :<- [:images]
   (fn [[images] _]
     (:image/location (last images))))


  ;; Level three subscriptions

  (re-frame/reg-sub
   :before-images-count
   :<- [:partitioned-images]
   (fn [[before _ _] _]
     (count before)))

  (re-frame/reg-sub
   :after-images-count
   :<- [:partitioned-images]
   (fn [[_ _ after] _]
     (count after)))

  (re-frame/reg-sub
   :loading-before-buffer
   :<- [:comic-coordinates]
   :<- [:buffer-size]
   :<- [:first-image-location]
   :<- [:before-images-count]
   (fn [[comic-coord buffer-size first-location before-buffer-size] _]
     (when (> buffer-size before-buffer-size)
       (api/get-prev-locations (:site-id comic-coord)
                               (:comic-id comic-coord)
                               first-location
                               buffer-size
                               {:on-success #(re-frame/dispatch [:add-images-before %])})
       true)))

  (re-frame/reg-sub
   :loading-after-buffer
   :<- [:comic-coordinates]
   :<- [:buffer-size]
   :<- [:last-image-location]
   :<- [:after-images-count]
   (fn [[comic-coord buffer-size last-location after-buffer-size] _]
     (when (> buffer-size after-buffer-size)
       (api/get-next-locations (:site-id comic-coord)
                               (:comic-id comic-coord)
                               last-location
                               buffer-size
                               {:on-success #(re-frame/dispatch [:add-images-after %])})
       true)))

  (re-frame/reg-sub
   :current-images
   :<- [:partitioned-images]
   :<- [:buffer-size]
   (fn [[partitioned-images buffer-size] _]
     (current-images partitioned-images buffer-size))))

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
  (let [current-locations (re-frame/subscribe [:current-locations])]
    [reader #(re-frame/dispatch [:set-current-location %]) @current-locations]))
