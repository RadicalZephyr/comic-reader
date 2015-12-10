(ns comic-reader.main-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [comic-reader.main :refer [main-panel]]))

(deftest test-main-panel
  (is (= [:div "Hello Re-Frame!"]
         (main-panel))))
