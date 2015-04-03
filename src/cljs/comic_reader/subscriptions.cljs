(ns comic-reader.subscriptions
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent.ratom :refer-macros [reaction]]
            [re-frame.core :refer [dispatch
                                   register-sub]]))

(register-sub
 :page
 (fn [db _]
   (reaction (get @db :page))))

(register-sub
 :site-list
 (fn [db _]
   (reaction (get @db :site-list))))

(register-sub
 :site
 (fn [db _]
   (reaction (get @db :site))))

(register-sub
 :comic-list
 (fn [db _]
   (reaction (get @db :comic-list))))

(register-sub
 :comic
 (fn [db _]
   (reaction (get @db :comic))))

(register-sub
 :location
 (fn [db _]
   (reaction (get @db :location))))
