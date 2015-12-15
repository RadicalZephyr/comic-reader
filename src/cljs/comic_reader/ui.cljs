(ns comic-reader.ui
  (:require [re-frame.core :as re-frame]
            [reagent.ratom :refer-macros [reaction]]))

(defn loading []
  [:img.loading {:src "img/loading.svg"}])

(defn site-element [comic]
  ^{:key (:id comic)}
  [:li [:a (:name comic)]])

(defn site-list [comic-list]
  (let [root [:div [:h1 "Comics List"]]
        content (cond
                  (= :loading comic-list) [loading]
                  (seq comic-list)        (into [:ul]
                                                (map site-element comic-list))
                  :else nil)]
    (if content
      (conj root content)
      root)))
