(ns comic-reader.ui
  (:require [re-frame.core :as re-frame]
            [reagent.ratom :refer-macros [reaction]]))

(defn loading []
  [:img.loading {:src "img/loading.svg"}])

(re-frame/register-sub
 :comic-list-status
 (fn [db _]
   (reaction (get-in @db [:comic-list :status]))))

(re-frame/register-handler
 :fetch-comic-sites
 (fn [app-state _]
   (assoc-in app-state [:comic-list :status] :loading)))

(re-frame/register-handler
 :initialize-db
 (fn [_ _]
   {:comic-list {:status :none}}))

(defn site-list []
  (let [status (re-frame/subscribe [:comic-list-status])]
    (fn []
      `[:div [:h1 "Comics List"]
        ~@(case @status
            :loading [[loading]]
            nil)])))
