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
     (-> db
         (assoc :page page)
         (assoc :site site))))

  (rf/register-handler
   :read
   (fn [db [page location]]
     (-> db
         (assoc :page page)
         (assoc :location location))))

  (rf/register-handler
   :site-list
   (fn [db [_ site-list]]
     (assoc db :site-list site-list)))

  (rf/register-handler
   :comic-list
   (fn [db [_ comic-list]]
     (assoc db :comic-list comic-list)))

  (rf/register-handler
   :comic-urls
   (fn [db [_ comic-urls]]
     (assoc db :comic-urls comic-urls))))
