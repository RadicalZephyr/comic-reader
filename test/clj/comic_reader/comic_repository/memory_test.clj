(ns comic-reader.comic-repository.memory-test
  (:require [clojure.test :refer :all]
            [clojure.core.async :refer [<!!]]
            [comic-reader.comic-repository :as repo]
            [comic-reader.comic-repository.memory :as sut]))

(deftest test-sites
  (testing "can store and retrieve sites data"
    (let [test-repo (sut/new-memory-repository)
          sites [{:site/id "site-one" :site/name "Site One"}]]
      (repo/store-sites test-repo sites)
      (is (= sites
             (<!! (repo/list-sites test-repo)))))))

(deftest test-comics
  (testing "can store and retrieve comics data"
    (let [test-repo (sut/new-memory-repository)
          site-id :site-one
          comics [{:comic/id :site-one/comic-one :comic/name "Comic One"}]]
      (repo/store-comics test-repo comics)
      (is (= comics
             (<!! (repo/list-comics test-repo site-id)))))))

(deftest test-locations
  (testing "can store and retrieve locations data"
    (let [test-repo (sut/new-memory-repository)
          site-id ::site
          comic-id ::comic
          locations (for [chapter (range 1 3)
                          page (range 1 6)]
                      {:location/chapter {:chapter/title (str "The Gamer " chapter)
                                          :chapter/number chapter}
                       :location/page {:page/number page :page/url (str "url" page)}})]
      (repo/store-locations test-repo comic-id locations)

      (is (= [{:location/chapter {:chapter/title "The Gamer 1"  :chapter/number 1}
               :location/page {:page/number 1 :page/url "url1"}}
              {:location/chapter {:chapter/title "The Gamer 1" :chapter/number 1}
               :location/page {:page/number 2 :page/url "url2"}}
              {:location/chapter {:chapter/title "The Gamer 1" :chapter/number 1}
               :location/page {:page/number 3 :page/url "url3" }}]
             (<!! (repo/next-locations test-repo comic-id (first locations) 3))))
      (is (= [{:location/chapter {:chapter/title "The Gamer 1"  :chapter/number 1}
               :location/page {:page/number 4 :page/url "url4"}}
              {:location/chapter {:chapter/title "The Gamer 1" :chapter/number 1}
               :location/page {:page/number 5 :page/url "url5"}}
              {:location/chapter {:chapter/title "The Gamer 2" :chapter/number 2}
               :location/page {:page/number 1 :page/url "url1" }}]
             (<!! (repo/next-locations test-repo comic-id (nth locations 3) 3))))

      (is (= [{:location/chapter {:chapter/title "The Gamer 1"  :chapter/number 1}
               :location/page {:page/number 4 :page/url "url4"}}
              {:location/chapter {:chapter/title "The Gamer 1" :chapter/number 1}
               :location/page {:page/number 3 :page/url "url3" }}
              {:location/chapter {:chapter/title "The Gamer 1" :chapter/number 1}
               :location/page {:page/number 2 :page/url "url2"}}]
             (<!! (repo/previous-locations test-repo comic-id (nth locations 3) 3))))
      (is (= [{:location/chapter {:chapter/title "The Gamer 1" :chapter/number 1}
               :location/page {:page/number 2 :page/url "url2"}}
              {:location/chapter {:chapter/title "The Gamer 1"  :chapter/number 1}
               :location/page {:page/number 1 :page/url "url1"}}]
             (<!! (repo/previous-locations test-repo comic-id (nth locations 1) 3)))))))
