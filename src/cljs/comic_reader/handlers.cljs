(ns comic-reader.handlers
  (:require [comic-reader.api :as api]
            [comic-reader.history :as h]
            [re-frame.core :as rf]
            [secretary.core :as secretary]))

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
     (assoc db
            :page page
            :site site
            :location location)))

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
     (assoc db :url-list url-list)))

  (rf/register-handler
   :next-image
   (fn [db [_ img-tag]]
     (if (vector? (:comic-imgs db))
       (update-in db [:comic-imgs] conj img-tag)
       (assoc db :comic-imgs [img-tag])))))
