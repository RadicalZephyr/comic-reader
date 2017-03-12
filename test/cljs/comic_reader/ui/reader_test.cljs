(ns comic-reader.ui.reader-test
  (:require [cljs.test :refer-macros [is testing]]
            [devcards.core :refer-macros [deftest defcard-rg]]
            [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [comic-reader.ui.reader :as sut]
            [comic-reader.macro-util :refer-macros [reactively]]))

(defcard-rg reader-view
  (fn [_ _]
    (let [images [{:image/location :abc
                   :image/tag [:img {:src "/public/img/tux.png"}]}
                  {:image/location :abc
                   :image/tag [:img {:src "/public/img/loading.svg"}]}]]
      (reactively
       [sut/comic-reader images :abc]))))
