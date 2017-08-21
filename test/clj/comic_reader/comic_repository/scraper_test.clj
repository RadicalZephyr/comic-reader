(ns comic-reader.comic-repository.scraper-test
  (:require [clojure.test :as t]
            [comic-reader.comic-repository :as repo]
            [comic-reader.comic-repository.scraper :as sut]
            [comic-reader.site-scraper.mock :refer [mock-scraper]]
            [com.stuartsierra.component :as component]
            [clojure.core.async :refer [<!!]]))

(defn scraper-test-system [scraper]
  (component/system-map
   :site-scraper scraper
   :scraper-repo (component/using
                  (sut/new-scraper-repo)
                  {:scraper :site-scraper})))

(defn test-repo [scraper]
  (-> (scraper-test-system scraper)
      (component/start)
      :scraper-repo))

(t/deftest test-creation
  (let [repo (test-repo (mock-scraper))]
    (t/is (not (nil? repo)))
    (t/is (not (nil? (:scraper repo))))))

(t/deftest test-list-sites
  (t/testing "returns nil when no sites are known"
    (let [repo (test-repo (mock-scraper :sites []))]
      (t/is (= []
               (<!! (repo/list-sites repo))))))

  (t/testing "returns site data with names formatted for display"
    (let [repo (test-repo (mock-scraper
                           :sites ["site-one"
                                   "site-two"
                                   "site-three"]))]
      (t/is (= [{:site/id :site-one   :site/name "Site One"}
                {:site/id :site-two   :site/name "Site Two"}
                {:site/id :site-three :site/name "Site Three"}]
               (<!! (repo/list-sites repo)))))))

(t/deftest test-list-comics
  (let [repo (test-repo (mock-scraper
                         :comics {"manga-fox"
                                  [{:id "the_gamer"
                                    :name "The Gamer"
                                    :url "real_url"}
                                   {:id "other_comic"
                                    :name "Other Comic"
                                    :url "another_url"}]}))]
    (t/testing "returns nil for an unknown site-id"
      (t/is (= []
               (<!! (repo/list-comics repo "pants")))))

    (t/testing "returns comic data for a site"
      (t/is (= [{:comic/id :manga-fox/the_gamer   :comic/name "The Gamer"   :comic/url "real_url"}
                {:comic/id :manga-fox/other_comic :comic/name "Other Comic" :comic/url "another_url"}]
               (<!! (repo/list-comics repo :manga-fox)))))))

(t/deftest test-next-locations
  (let [repo (test-repo (mock-scraper :chapters {"manga-fox"
                                                 {:the-gamer [{:name "The Gamer 1" :ch-num 1}]}}
                                      :pages {"manga-fox"
                                              {{:name "The Gamer 1" :ch-num 1}
                                               [{:name "1" :url "url1"}
                                                {:name "2" :url "url2"}
                                                {:name "3" :url "url3"}
                                                {:name "4" :url "url4"}]}}))]

    (t/testing "starts at the beginning when location is nil"
      (t/is (= [{:location/chapter {:chapter/title "The Gamer 1" :chapter/number 1}
                 :location/page {:page/number 1 :page/url "url1"}}]
               (<!! (repo/next-locations repo :manga-fox :the-gamer nil 1)))))

    (t/testing "starts at the beginning when location is an empty map"
      (t/is (= [{:location/chapter {:chapter/title "The Gamer 1" :chapter/number 1}
                 :location/page {:page/number 1 :page/url "url1"}}]
               (<!! (repo/next-locations repo :manga-fox :the-gamer {} 1)))))

    (t/testing "doesn't include the passed page when starting at a page"
      (let [location {:location/chapter {:chapter/title "The Gamer 1" :chapter/number 1}
                      :location/page {:page/number 2 :page/url "url2"}}]
        (t/is (= [{:location/chapter {:chapter/title "The Gamer 1" :chapter/number 1}
                   :location/page {:page/number 3 :page/url "url3"}}
                  {:location/chapter {:chapter/title "The Gamer 1" :chapter/number 1}
                   :location/page {:page/number 4 :page/url "url4"}}]
                 (<!! (repo/next-locations repo :manga-fox :the-gamer location 2))))))

    (t/testing "returns a sentinel value when no more locations are available"
      (let [location {:location/chapter {:chapter/title "The Gamer 1" :chapter/number 1}
                      :location/page {:page/number 4 :page/url "url4"}}]
        (t/is (= [{:location/boundary :boundary/last}]
                 (<!! (repo/next-locations repo :manga-fox :the-gamer location 2)))))))

  (let [repo (test-repo (mock-scraper :chapters {"manga-fox"
                                                 {:the-gamer [{:name "The Gamer 1" :ch-num 1}
                                                               {:name "The Gamer 2" :ch-num 2}]}}
                                      :pages {"manga-fox"
                                              {{:name "The Gamer 1" :ch-num 1}
                                               [{:name "1" :url  "url1"}
                                                {:name "2" :url  "url2"}
                                                {:name "3" :url  "url3"}]

                                               {:name "The Gamer 2" :ch-num 2}
                                               [{:name "4" :url  "url4"}
                                                {:name "5" :url  "url5"}]}}))]

    (t/testing "it crosses chapter boundaries to fetch n pages"
      (let [location {:location/chapter {:chapter/title "The Gamer 1" :chapter/number 1}
                      :location/page {:page/number 2 :page/url "url2"}}]
        (t/is (= [{:location/chapter {:chapter/title "The Gamer 1" :chapter/number 1}
                   :location/page {:page/number 3 :page/url "url3"}}
                  {:location/chapter {:chapter/title "The Gamer 2" :chapter/number 2}
                   :location/page {:page/number 4 :page/url "url4"}}
                  {:location/chapter {:chapter/title "The Gamer 2" :chapter/number 2}
                   :location/page {:page/number 5 :page/url "url5"}}]
                 (<!! (repo/next-locations repo :manga-fox :the-gamer location 3))))))

    (t/testing "it only fetches as many pages as there are up-to the requested n"
      (let [location {:location/chapter {:chapter/title "The Gamer 1" :chapter/number 1}
                      :location/page {:page/number 1 :page/url "url1"}}]
        (t/is (= [{:location/chapter {:chapter/title "The Gamer 1" :chapter/number 1}
                   :location/page {:page/number 2 :page/url "url2"}}
                  {:location/chapter {:chapter/title "The Gamer 1" :chapter/number 1}
                   :location/page {:page/number 3 :page/url "url3"}}
                  {:location/chapter {:chapter/title "The Gamer 2" :chapter/number 2}
                   :location/page {:page/number 4 :page/url "url4"}}
                  {:location/chapter {:chapter/title "The Gamer 2" :chapter/number 2}
                   :location/page {:page/number 5 :page/url "url5"}}
                  {:location/boundary :boundary/last}]
                 (<!! (repo/next-locations repo :manga-fox :the-gamer location 10))))))

    (t/testing "it can start at an arbitrary chapter (with no page)"
      (let [location {:location/chapter {:chapter/title "The Gamer 2" :chapter/number 2}}]
        (t/is (= [{:location/chapter {:chapter/title "The Gamer 2" :chapter/number 2}
                   :location/page {:page/number 4 :page/url "url4"}}
                  {:location/chapter {:chapter/title "The Gamer 2" :chapter/number 2}
                   :location/page {:page/number 5 :page/url "url5"}}
                  {:location/boundary :boundary/last}]
                 (<!! (repo/next-locations repo :manga-fox :the-gamer location 10))))))))

(t/deftest test-previous-locations
  (let [repo (test-repo (mock-scraper))]
    (t/testing "it does nothing if given a nil location"
      (t/is (= nil
               (repo/previous-locations repo :manga-fox :the-gamer nil 1))))

    (t/testing "it does nothing if given an empty map as location"
      (t/is (= nil
               (repo/previous-locations repo :manga-fox :the-gamer {} 1)))))

  (let [repo (test-repo (mock-scraper :chapters {"manga-fox"
                                                 {:the-gamer [{:name "The Gamer 1" :ch-num 1}
                                                               {:name "The Gamer 2" :ch-num 2}]}}
                                      :pages {"manga-fox"
                                              {{:name "The Gamer 1" :ch-num 1}
                                               [{:name "1" :url  "url1"}
                                                {:name "2" :url  "url2"}
                                                {:name "3" :url  "url3"}]

                                               {:name "The Gamer 2" :ch-num 2}
                                               [{:name "4" :url  "url4"}
                                                {:name "5" :url  "url5"}]}}))]

    (t/testing "returns a sentinel value when no more locations are available"
      (let [location {:location/chapter {:chapter/title "The Gamer 1" :chapter/number 1}
                      :location/page {:page/number 1 :page/url "url1"}}]
        (t/is (= [{:location/boundary :boundary/first}]
                 (<!! (repo/previous-locations repo :manga-fox :the-gamer location 2))))))

    (t/testing "it returns n locations that precede the given location"
      (let [location {:location/chapter {:chapter/title "The Gamer 1" :chapter/number 1}
                      :location/page {:page/number 3 :page/url "url3"}}]
        (t/is (= [{:location/chapter {:chapter/title "The Gamer 1" :chapter/number 1}
                   :location/page {:page/number 2 :page/url "url2"}}
                  {:location/chapter {:chapter/title "The Gamer 1" :chapter/number 1}
                   :location/page {:page/number 1 :page/url "url1"}}]
                 (<!! (repo/previous-locations repo :manga-fox :the-gamer location 2))))))

    (t/testing "it returns locations across chapter boundaries"
      (let [location {:location/chapter {:chapter/title "The Gamer 2" :chapter/number 2}
                      :location/page {:page/number 5 :page/url "url5"}}]
        (t/is (= [{:location/chapter {:chapter/title "The Gamer 2" :chapter/number 2}
                   :location/page {:page/number 4 :page/url "url4"}}
                  {:location/chapter {:chapter/title "The Gamer 1" :chapter/number 1}
                   :location/page {:page/number 3 :page/url "url3"}}]
                 (<!! (repo/previous-locations repo :manga-fox :the-gamer location 2))))))

    (t/testing "only fetches as many pages as there are"
      (let [location {:location/chapter {:chapter/title "The Gamer 2" :chapter/number 2}
                      :location/page {:page/number 5 :page/url "url5"}}]
        (t/is (= [{:location/chapter {:chapter/title "The Gamer 2" :chapter/number 2}
                   :location/page {:page/number 4 :page/url "url4"}}
                  {:location/chapter {:chapter/title "The Gamer 1" :chapter/number 1}
                   :location/page {:page/number 3 :page/url "url3"}}
                  {:location/chapter {:chapter/title "The Gamer 1" :chapter/number 1}
                   :location/page {:page/number 2 :page/url "url2"}}
                  {:location/chapter {:chapter/title "The Gamer 1" :chapter/number 1}
                   :location/page {:page/number 1 :page/url "url1"}}
                  {:location/boundary :boundary/first}]
                 (<!! (repo/previous-locations repo :manga-fox :the-gamer location 10))))))

    (t/testing "it can start at an arbitrary chapter (with no page)"
      (let [location {:location/chapter {:chapter/title "The Gamer 2" :chapter/number 2}}]
        (t/is (= [{:location/chapter {:chapter/title "The Gamer 2" :chapter/number 2}
                   :location/page {:page/number 5 :page/url "url5"}}
                  {:location/chapter {:chapter/title "The Gamer 2" :chapter/number 2}
                   :location/page {:page/number 4 :page/url "url4"}}
                  {:location/chapter {:chapter/title "The Gamer 1" :chapter/number 1}
                   :location/page {:page/number 3 :page/url "url3"}}]
                 (<!! (repo/previous-locations repo :manga-fox :the-gamer location 3))))))))

(t/deftest test-image-tag
  (t/testing "returns nil when"
    (let [repo (test-repo (mock-scraper))]
      (t/testing "given a nil location"
        (t/is (= nil (repo/image-tag repo "manga-fox" nil))))

      (t/testing "given an empty location"
        (t/is (= nil (repo/image-tag repo "manga-fox" {}))))

      (t/testing "given a location without a page component"
        (t/is (= nil (repo/image-tag repo "manga-fox" {:location/chapter {:chapter/title "The Gamer 1" :chapter/number 1}}))))))

  (t/testing "returns a valid image tag"
    (let [repo (test-repo (mock-scraper
                           :images {"manga-fox"
                                    {{:page/number 3 :page/url "url3"} [:img {:src "an-img-url"}]}}))]

      (t/is (= [:img {:src "an-img-url"}]
               (<!! (repo/image-tag repo "manga-fox" {:location/page {:page/number 3 :page/url "url3"}})))))))
