(ns comic-reader.subscriptions
  (:require [re-frame.core :as rf]
            [reagent.ratom :refer-macros [reaction]]))

(defn re-string [letter]
  (if (= letter "#")
    "^[^a-z]"
    (str "^" letter)))

(defn filter-comics [cl-filter comic-list]
  (if cl-filter
    (let [filter-re (re-pattern (str "(?i)"
                                     (re-string cl-filter)))]
      (filter (fn [{:keys [name]}]
                (re-find filter-re name))
              comic-list))
    comic-list))

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
     (let [cl-filter   (reaction (get @db :comic-list-filter))
           comics-list (reaction (get @db :comic-list))]
       (reaction
        (filter-comics @cl-filter @comics-list)))))

  (rf/register-sub
   :comic-list-filter
   (fn [db _]
     (reaction (get @db :comic-list-filter))))

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
