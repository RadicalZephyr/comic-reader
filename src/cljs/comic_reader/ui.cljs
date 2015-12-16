(ns comic-reader.ui
  (:require [re-frame.core :as re-frame]
            [reagent.ratom :refer-macros [reaction]])
  (:require-macros [comic-reader.macro-util :refer [defcomponent-2
                                                    with-optional-tail]]))

(defn loading []
  [:img.loading {:src "img/loading.svg"}])

(defn site-element [comic]
  ^{:key (:id comic)}
  [:li [:a (:name comic)]])
(defn get-sites-list [db]
  )

(re-frame/register-sub
 :sites-list
 (fn [app-db v]
   (reaction (get-sites-list @app-db))))

(defcomponent-2 site-list
  [comic-list]
  (with-optional-tail
    [:div [:h1 "Comics List"]]
    (cond
      (= :loading comic-list) [loading]
      (seq comic-list)        (into [:ul]
                                    (map site-element comic-list))
      :else nil)))
