(ns comic-reader.ui
  (:require [re-frame.core :as re-frame]
            [reagent.ratom :refer-macros [reaction]])
  (:require-macros [comic-reader.macro-util :refer [defcomponent-2
                                                    with-optional-tail]]))

(defn loading []
  [:img.loading {:src "img/loading.svg"}])

(defn site-element [site]
  ^{:key (:id site)}
  [:li [:a (:name site)]])

(defn get-sites-list [db]
  )

(re-frame/register-sub
 :sites-list
 (fn [app-db v]
   (reaction (get-sites-list @app-db))))

(defcomponent-2 site-list
  [sites-list]
  (with-optional-tail
    [:div [:h1 "Comic Sites"]]
    (cond
      (= :loading sites-list) [loading]
      (seq sites-list)        (into [:ul]
                                    (map site-element sites-list))
      :else nil)))
