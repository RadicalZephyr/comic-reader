(ns comic-reader.main-test
  (:require [cljs.test :refer-macros [is testing]]
            [devcards.core :refer-macros [deftest]]
            [comic-reader.main :as main]))

(deftest test-main-panel
  (is (= [:div "Hello Re-Frame!"]
         (main/main-panel))))
