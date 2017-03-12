(ns comic-reader.ui.reader
  (:require [re-frame.core :as re-frame]
            [comic-reader.ui.image :as image]))

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
   :partitioned-images
   :<- [:images]
   :<- [:current-location]
   (fn [[images current-location] _]
     (partitioned-images images current-location)))

  (re-frame/reg-sub
   :preceding-images-count
   :<- [:partitioned-images]
   (fn [[preceding _ _] _]
     (count preceding)))

  (re-frame/reg-sub
   :following-images-count
   :<- [:partitioned-images]
   (fn [[_ _ following] _]
     (count following)))

  (re-frame/reg-sub
   :current-images
   :<- [:partitioned-images]
   :<- [:preceding-images-count]
   :<- [:following-images-count]
   (fn [[partitioned-images preceding-count following-count] [_ n]]
     (when (> (/ n 2) preceding-count)
       )
     (when (> (/ n 2) following-count)
       )
     (current-images partitioned-images n))))

(defn- image-id [image]
  (:image/location image))

(defn- make-comic-image [set-current-location image]
  (with-meta
    [image/comic-image #(set-current-location (:image/location image)) (:image/tag image)]
    {:key (image-id image)}))

(defn comic-image-list [set-current-location images]
  [:div (map #(make-comic-image set-current-location %) images)])

(defn view []
  )
