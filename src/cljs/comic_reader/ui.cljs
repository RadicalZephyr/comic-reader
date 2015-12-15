(ns comic-reader.ui
  (:require [re-frame.core :as re-frame]
            [reagent.ratom :refer-macros [reaction]]))

(defn loading []
  [:img.loading {:src "img/loading.svg"}])

(defn site-element [comic]
  ^{:key (:id comic)}
  [:li [:a (:name comic)]])

(defn site-list [status comic-list]
  `[:div [:h1 "Comics List"]
    ~@(case status
        :loading [[loading]]
        :loaded `[[:ul ~@(map site-element comic-list)]]
        nil)])
