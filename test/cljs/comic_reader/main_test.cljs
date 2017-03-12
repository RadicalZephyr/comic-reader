(ns comic-reader.main-test
  (:require [cljs.test :refer-macros [is testing]]
            [devcards.core :refer-macros [deftest]]
            [comic-reader.main :as main]
            [re-frame.core :as re-frame]))

(defn reload []
  (re-frame/clear-subscription-cache!)
  (main/main))
