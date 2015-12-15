(ns comic-reader.ui-test
  (:require [cljs.test :refer-macros [is testing]]
            [devcards.core :refer-macros [deftest defcard-rg]]
            [re-frame.core :as re-frame]
            [comic-reader.ui :as ui]
            [comic-reader.main-test]))

(defcard-rg loading-card
  "# Loading
   This is the loading svg used everywhere on the site."
  (ui/loading))


(deftest test-site-list
  (is (= [:div [:h1 "Comics List"]]
         (ui/site-list nil)))

  (is (= [:div [:h1 "Comics List"]
          [ui/loading]]
         (ui/site-list :loading))))
