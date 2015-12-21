(ns comic-reader.ui.comic-list
  (:require [re-frame.core :as re-frame]
            [reagent.ratom :refer-macros [reaction]]
            [comic-reader.ui.base :as base])
  (:require-macros [comic-reader.macro-util :refer [defcomponent-2
                                                    with-optional-tail]]))

(defn set-comic-list [db [_ comics]]
  (assoc db :comic-list comics))

(defn get-comic-list [db]
  (get db :comic-list))

(defn setup-comic-list! []
  (re-frame/register-sub
   :comic-list
   (fn [app-db v]
     (reaction (get-comic-list @app-db))))
  (re-frame/register-handler
   :set-comic-list
   set-comic-list))

(defcomponent-2 comic-list
  [[comics :comic-list]]
  (with-optional-tail
    [:div [:h1 "Comics"]]
    (cond
      (= :loading comics) [base/loading]
      (seq comics)        (base/map-into-list
                           [:ul.no-bullet]
                           (fn [comic]
                             [base/large-button (:name comic)])
                           comics)
      :else nil)))
