(ns comic-reader.main
  (:require [comic-reader.api :as api]
            [comic-reader.session :as session]
            [comic-reader.history :as history]
            [comic-reader.pages.sites
             :refer [site-list]]
            [comic-reader.pages.comics
             :refer [comic-list]]
            [comic-reader.pages.viewer
             :refer [comic-viewer]]
            [reagent.core :as reagent :refer [atom]]))

(defn page [data]
  [(session/get :current-page) data])

(defn init! []
  (api/get-sites
   (fn [data]
     (reagent/render-component [page data]
                               (.-body js/document)))))
