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
     (api/get-comic-imgs site location)
     (assoc db
            :site site
            :page page
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
   :comic-imgs
   (fn [db [_ comic-imgs]]
     (assoc db :comic-imgs comic-imgs))))
