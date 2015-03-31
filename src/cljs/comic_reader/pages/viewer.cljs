(ns comic-reader.pages.viewer
  (:require [comic-reader.session :as session]
            [reagent.core :as reagent :refer [atom]]
            [secretary.core :refer [dispatch!]]))

(defn comic-viewer []
  [:div "View all the comics..."])
