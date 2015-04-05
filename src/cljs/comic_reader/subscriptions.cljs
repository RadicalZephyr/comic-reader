(ns comic-reader.subscriptions
  (:require [re-frame.core :as rf]
            [reagent.ratom :refer-macros [reaction]]))

(defn init-subscriptions! []
  (rf/register-sub
   :page
   (fn [db _]
     (reaction (get @db :page))))

  (rf/register-sub
   :site-list
   (fn [db _]
     (reaction (get @db :site-list))))

  (rf/register-sub
   :site
   (fn [db _]
     (reaction (get @db :site))))

  (rf/register-sub
   :comic-list
   (fn [db _]
     (reaction (get @db :comic-list))))

  (rf/register-sub
   :url-list
   (fn [db _]
     (reaction (get @db :url-list))))

  (rf/register-sub
   :comic-imgs
   (fn [db _]
     (reaction (get @db :comic-imgs))))

  (rf/register-sub
   :location
   (fn [db _]
     (reaction (get @db :location)))))
