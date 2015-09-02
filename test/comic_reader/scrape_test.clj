(ns comic-reader.scrape-test
  (:require [net.cgrand.enlive-html :refer [html]]
            [comic-reader.scrape :refer :all]
            [clojure.test :refer :all]))

(deftest enlive->hiccup-test
  (is (= (enlive->hiccup {:tag :img :attrs {}})
         [:img {}]))
  (is (= (enlive->hiccup {:tag :p :attrs {:things 1} :content ["stuff"]})
         [:p {:things 1} ["stuff"]])))

(deftest clean-image-tag-test
  (is (= (clean-image-tag [:img {:src "abc"}])
         [:img {:src "abc"}]))
  (is (= (clean-image-tag [:img {:alt "def"}])
         [:img {:alt "def"}]))
  (is (= (clean-image-tag [:img {} '[lots of content]])
         [:img {}]))
  (is (= (clean-image-tag [:img {:src "def" :walrus "i am"}])
         [:img {:src "def"}])))

(deftest extract-image-tag-test
  (let [html (html [:div {}])]
    (is (= (extract-image-tag html [:div :img])
           nil)))
  (let [html (html [:div {} [:img {:src "dummy-img"
                                   :alt "alt-text"}]])]
    (is (= (extract-image-tag html [:div :img])
           [:img {:src "dummy-img" :alt "alt-text"}])))

  (let [html (html [:div {} [:p {} [:img {:src "img-two"
                                          :alt "text-alt"}]]])]
    (is (= (extract-image-tag html [:div :p :img])
           [:img {:src "img-two" :alt "text-alt"}]))))

(deftest fetch-image-tag-test
  (let  [html (html [:div {} [:img {:src "dummy-img"
                                    :alt "alt-text"}]])]
    (with-redefs [fetch-url (fn [url] html)]
      (is (= (fetch-image-tag {:url "abc" :selector [:div :img]})
             [:img {:src "dummy-img" :alt "alt-text"}])))))

(deftest extract-list-test
  (let [html (html [:div {} [:p {}]])]
    (is (= (extract-list html [:div :p] :tag)
           [:p]))
    (is (= (extract-list html [:div :p] :attrs)
           [{}]))
    (is (= (extract-list html [:div :p] (comp seq :content))
           [nil]))))

(deftest fetch-list-test
  (let [html (html [:div {} [:p {}]])]
    (with-redefs [fetch-url (fn [url] html)]
      (is (= (fetch-list {:url "abc"
                          :selector [:div :p]
                          :normalize :tag})
             [:p])))))
