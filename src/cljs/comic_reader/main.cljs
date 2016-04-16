(ns comic-reader.main
  (:require
    [comic-reader.ui.base :as base]
    [comic-reader.ui.site-list :as site-list]
    [reagent.core :as reagent]
    [re-frame.core :as re-frame]))

(defn main-panel
  [page-key]
  (case page-key
    :site [site-list/site-list-container]
    [base/four-oh-four]))

(defn ^:export main
  []
  (site-list/setup!)

  (re-frame/dispatch [:set-site-list [{:id :a :name "Comic A"}]])
  (reagent/render-component [main-panel :site]
                            (.getElementById js/document "app")))

(main)
