(ns comic-reader.handlers
  (:require [cljs.reader :refer [read-string]]
            [comic-reader.api :as api]
            [comic-reader.history :as h]
            [comic-reader.routes :as r]
            [re-frame.core :as rf]
            [secretary.core :as secretary]))

(defn get-next-image [{:keys [site url-list location]
                       {:keys [chapter]} :location
                       :as db}]
  (api/get-img-tag site (first url-list))
  (let [{:keys [url-list] :as db} (assoc db
                                         :url-list (next url-list)
                                         :waiting true)
        new-location (assoc location :chapter
                            (inc
                             (read-string chapter)))]
    (when (not url-list)
      (api/get-comic-urls site new-location))
    db))

(defn init-handlers! []
  (rf/register-handler
   :unknown
   (fn [db [page]]
     (assoc db :page :unknown)))

  (rf/register-handler
   :sites
   (fn [db [page]]
     (api/get-sites)
     (assoc db :page page)))

  (rf/register-handler
   :comics
   (fn [db [page site]]
     (api/get-comics site)
     (-> db
         (assoc :page page)
         (assoc :site site))))

  (rf/register-handler
   :read
   (fn [db [page site location]]
     (api/get-comic-urls site location)
     (if (and (= (:site db) site)
              (= (get-in db [:location  :comic])
                 (get-in      location [:comic])))
       (assoc db :location location)
       (assoc db
              :page page
              :site site
              :location location
              :comic-imgs []))))

  (rf/register-handler
   :site-list
   (fn [db [_ site-list]]
     (assoc db :site-list site-list)))

  (rf/register-handler
   :comic-list
   (fn [db [_ comic-list]]
     (assoc db :comic-list comic-list)))

  (rf/register-handler
   :url-list
   (fn [db [_ url-list]]
     (-> db
         (assoc :url-list url-list)
         get-next-image)))

  (rf/register-handler
   :scroll
   (let [counter (atom 2)
         scroll-threshold (atom (/ 1 2))]
     (fn [db _]
       (if (and (not (:waiting db))
                (= (:page db)
                   :read))
         (let [scroll-y (.-scrollY js/window)
               window-height (.-innerHeight js/window)
               screen-bottom (+ scroll-y window-height)
               document-height (-> js/document
                                   .-body
                                   .-clientHeight)]
           (.log js/console "Threshold: " @scroll-threshold)
           (if (> screen-bottom
                  (* @scroll-threshold document-height))
             (do
               (swap! counter inc)
               (swap! scroll-threshold + (/ 1
                                            (.pow js/Math
                                                  2 counter)))
               (get-next-image db))
             db))
         db))))

  (rf/register-handler
   :next-image
   (fn [db [_ img-tag]]
     (-> db
         (assoc :waiting false)
         (update-in [:url-list] rest)
         (update-in [:comic-imgs] conj img-tag)))))
