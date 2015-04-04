(ns comic-reader.handlers
  (:require [re-frame.core :as rf]))

(defonce initial-state
  {:page :sites})

(defn init-handlers! []
  (rf/register-handler
   :initialize
   (fn [db _]
     (merge db initial-state)))

  (rf/register-handler
   :unknown
   (fn [db [page]]
     (assoc db :page :unknown)))

  (rf/register-handler
   :sites
   (fn [db [page]]
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
         (assoc :location location)))))
