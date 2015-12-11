(ns comic-reader.main
  (:require [reagent.core :as reagent]))

(defn main-panel
  []
  [:div "Hello Re-Frame!"])

(defn ^:export main
  []
  (reagent/render-component [main-panel]
                            (.getElementById js/document "app")))

(main)
