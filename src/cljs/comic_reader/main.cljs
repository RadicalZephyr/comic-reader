(ns comic-reader.main
  (:require [reagent.core :as reagent]))

(defn loading []
  [:img.loading {:src "img/loading.svg"}])

(defn site-list []
  [:div [:h1 "Comics List"]
   [loading]])

(defn main-panel
  []
  [:div "Hello Re-Frame!"])

(defn ^:export main
  []
  (reagent/render-component [main-panel]
                            (.getElementById js/document "app")))

(main)
