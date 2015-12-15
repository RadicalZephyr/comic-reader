(ns comic-reader.ui
  (:require [re-frame.core :as re-frame]
            [reagent.ratom :refer-macros [reaction]]))

(defn loading []
  [:img.loading {:src "img/loading.svg"}])

(defn site-list [status]
  `[:div [:h1 "Comics List"]
    ~@(case status
        :loading [[loading]]
        nil)])
