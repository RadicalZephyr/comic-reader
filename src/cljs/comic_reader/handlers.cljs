(ns comic-reader.handlers
  (:require [cljs.reader :refer [read-string]]
            [comic-reader.api :as api]
            [comic-reader.history :as h]
            [comic-reader.routes :as r]
            [re-frame.core :as rf]
            [secretary.core :as secretary]))

(defn go-to-location [db {:keys [chapter page]
                          :as location}]
  (assoc db :location location))

(defn get-next-image [{:keys [site page-list location]
                       {:keys [chapter]} :location
                       :as db}]
  (api/get-img-tag site (first page-list))
  (let [{:keys [page-list] :as db}
        (assoc db
               :page-list (next page-list)
               :waiting true)
        new-location (assoc location
                            :chapter (inc
                                      (read-string chapter))
                            :page 1)]
    (when (not page-list)
      (api/get-comic-pages site new-location))
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
   (fn [db [page site filter]]
     (if-not (= (:site db) site)
       (api/get-comics site))
     (-> db
         (assoc :page page
                :site site
                :comic-list-filter filter))))

  (rf/register-handler
   :read
   (fn [db [page site location]]
     (if (and (= (:site db) site)
              (= (get-in db [:location  :comic])
                 (get-in      location [:comic])))
       (go-to-location db location)
       (do
         (api/get-comic-pages site location)
         (assoc db
                :page page
                :site site
                :location location
                :comic-imgs [])))))

  (rf/register-handler
   :site-list
   (fn [db [_ site-list]]
     (assoc db :site-list site-list)))

  (rf/register-handler
   :comic-list
   (fn [db [_ comic-list]]
     (assoc db :comic-list comic-list)))

  (rf/register-handler
   :page-list
   (fn [db [_ page-list]]
     (rf/dispatch [:scroll])
     (-> db
         (assoc :page-list page-list))))

  (rf/register-handler
   :scroll
   (fn [db _]
     (if (and (not (:waiting db))
              (= (:page db)
                 :read))
       (let [scroll-y (.-scrollY js/window)
             window-height (.-innerHeight js/window)
             screen-bottom (+ scroll-y window-height)
             document-height (-> js/document
                                 (.getElementById "app")
                                 .-clientHeight)]
         (if (> screen-bottom
                (- document-height 800))
           (get-next-image db)
           db))
       db)))

  (rf/register-handler
   :next-image
   (fn [db [_ img-data]]
     (-> db
         (assoc :waiting false)
         (update-in [:comic-imgs] conj img-data)))))
