(ns comic-reader.scrape-test
  (:require [net.cgrand.enlive-html :refer [html]]
            [comic-reader.scrape :as sut]
            [clojure.test :as t]))

(t/deftest enlive->hiccup-test
  (t/is (= (sut/enlive->hiccup {:tag :img :attrs {}})
           [:img {}]))
  (t/is (= (sut/enlive->hiccup {:tag :p :attrs {:things 1} :content ["stuff"]})
           [:p {:things 1} ["stuff"]])))

(t/deftest clean-image-tag-test
  (t/is (= (sut/clean-image-tag [:img {:src "abc"}])
           [:img {:src "abc"}]))
  (t/is (= (sut/clean-image-tag [:img {:alt "def"}])
           [:img {:alt "def"}]))
  (t/is (= (sut/clean-image-tag [:img {} '[lots of content]])
           [:img {}]))
  (t/is (= (sut/clean-image-tag [:img {:src "def" :walrus "i am"}])
           [:img {:src "def"}])))

(t/deftest extract-image-tag-test
  (let [html (html [:div {}])]
    (t/is (= (sut/extract-image-tag html [:div :img])
             nil)))
  (let [html (html [:div {} [:img {:src "dummy-img"
                                   :alt "alt-text"}]])]
    (t/is (= (sut/extract-image-tag html [:div :img])
             [:img {:src "dummy-img" :alt "alt-text"}])))

  (let [html (html [:div {} [:p {} [:img {:src "img-two"
                                          :alt "text-alt"}]]])]
    (t/is (= (sut/extract-image-tag html [:div :p :img])
             [:img {:src "img-two" :alt "text-alt"}]))))

(t/deftest fetch-image-tag-test
  (let  [html (html [:div {} [:img {:src "dummy-img"
                                    :alt "alt-text"}]])]
    (with-redefs [sut/fetch-url (fn [url] html)]
      (t/is (= (sut/fetch-image-tag {:url "abc" :selector [:div :img]})
               [:img {:src "dummy-img" :alt "alt-text"}])))))

(t/deftest extract-list-test
  (let [html (html [:div {} [:p {}]])]
    (t/is (= (sut/extract-list html [:div :p] :tag)
             [:p]))
    (t/is (= (sut/extract-list html [:div :p] :attrs)
             [{}]))
    (t/is (= (sut/extract-list html [:div :p] (comp seq :content))
             []))))

(t/deftest fetch-list-test
  (let [html (html [:div {} [:p {}]])]
    (with-redefs [sut/fetch-url (fn [url] html)]
      (t/is (= (sut/fetch-list {:url "abc"
                                :selector [:div :p]
                                :normalize :tag})
               [:p])))))
