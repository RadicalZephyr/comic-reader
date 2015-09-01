(ns comic-reader.scrape-test
  (:require [net.cgrand.enlive-html :as html]
            [comic-reader.scrape :refer :all]
            [clojure.test :refer :all]))

(deftest extract-image-tag-test
  (let [html (html/html [:div {} [:img {:src "dummy-img"
                                        :alt "alt-text"}]])]
    (is (= (extract-image-tag html [:div :img])
           [:img {:src "dummy-img" :alt "alt-text"}])))

  (let [html (html/html [:div {} [:p {} [:img {:src "img-two"
                                               :alt "text-alt"}]]])]
    (is (= (extract-image-tag html [:div :p :img])
           [:img {:src "img-two" :alt "text-alt"}]))))
