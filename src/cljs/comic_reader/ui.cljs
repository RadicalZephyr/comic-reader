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

(defn site-list [comic-list]
  (with-optional-tail
    [:div [:h1 "Comics List"]]
    (cond
      (= :loading comic-list) [loading]
      (seq comic-list)        (into [:ul]
                                    (map site-element comic-list))
      :else nil)))
