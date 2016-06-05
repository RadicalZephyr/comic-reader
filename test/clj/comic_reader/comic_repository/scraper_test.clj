(ns comic-reader.comic-repository.scraper-test
  (:require [clojure.test :as t]
            [comic-reader.comic-repository.protocol :as repo-protocol]
            [comic-reader.comic-repository.scraper :as sut]
            [comic-reader.mock-site-scraper :refer [mock-scraper]]
            [com.stuartsierra.component :as component]))

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

(t/deftest test-next-locations
  (let [repo (test-repo (mock-scraper :chapters {"manga-fox"
                                                 {"the-gamer" [{:name "The Gamer 1" :ch-num 1}]}}
                                      :pages {"manga-fox"
                                              {{:name "The Gamer 1" :ch-num 1}
                                               [{:name "1", :url  "url1"}
                                                {:name "2", :url  "url2"}
                                                {:name "3", :url  "url3"}
                                                {:name "4", :url  "url4"}]}}))]

    (t/testing "starts at the beginning when location is nil"
      (t/is (= [{:chapter {:name "The Gamer 1" :ch-num 1} :page {:name "1", :url  "url1"}}]
               (repo-protocol/next-locations repo "manga-fox" "the-gamer" nil 1))))

    (t/testing "doesn't include the passed page when starting at a page"
      (let [location {:chapter {:name "The Gamer 1" :ch-num 1} :page {:name "2", :url  "url2"}}]
        (t/is (= [{:chapter {:name "The Gamer 1" :ch-num 1} :page {:name "3", :url  "url3"}}
                  {:chapter {:name "The Gamer 1" :ch-num 1} :page {:name "4", :url  "url4"}}]
                 (repo-protocol/next-locations repo "manga-fox" "the-gamer" location 2))))))

  (let [repo (test-repo (mock-scraper :chapters {"manga-fox"
                                                 {"the-gamer" [{:name "The Gamer 1" :ch-num 1}
                                                               {:name "The Gamer 2" :ch-num 2}]}}
                                      :pages {"manga-fox"
                                              {{:name "The Gamer 1" :ch-num 1}
                                               [{:name "1", :url  "url1"}
                                                {:name "2", :url  "url2"}
                                                {:name "3", :url  "url3"}]

                                               {:name "The Gamer 2" :ch-num 2}
                                               [{:name "4", :url  "url4"}
                                                {:name "5", :url  "url5"}]}}))]

    (t/testing "it crosses chapter boundaries to fetch n pages"
      (let [location {:chapter {:name "The Gamer 1" :ch-num 1} :page {:name "2", :url  "url2"}}]
        (t/is (= [{:chapter {:name "The Gamer 1" :ch-num 1} :page {:name "3", :url  "url3"}}
                  {:chapter {:name "The Gamer 2" :ch-num 2} :page {:name "4", :url  "url4"}}
                  {:chapter {:name "The Gamer 2" :ch-num 2} :page {:name "5", :url  "url5"}}]
                 (repo-protocol/next-locations repo "manga-fox" "the-gamer" location 3)))))

    (t/testing "it only fetches as many pages as there are up-to the requested n"
      (let [location {:chapter {:name "The Gamer 1" :ch-num 1} :page {:name "1", :url  "url1"}}]
        (t/is (= [{:chapter {:name "The Gamer 1" :ch-num 1} :page {:name "2", :url  "url2"}}
                  {:chapter {:name "The Gamer 1" :ch-num 1} :page {:name "3", :url  "url3"}}
                  {:chapter {:name "The Gamer 2" :ch-num 2} :page {:name "4", :url  "url4"}}
                  {:chapter {:name "The Gamer 2" :ch-num 2} :page {:name "5", :url  "url5"}}]
                 (repo-protocol/next-locations repo "manga-fox" "the-gamer" location 10)))))

    (t/testing "it can start at an arbitrary chapter (with no page)"
      (let [location {:chapter {:name "The Gamer 2" :ch-num 2}}]
        (t/is (= [{:chapter {:name "The Gamer 2" :ch-num 2} :page {:name "4", :url  "url4"}}
                  {:chapter {:name "The Gamer 2" :ch-num 2} :page {:name "5", :url  "url5"}}]
                 (repo-protocol/next-locations repo "manga-fox" "the-gamer" location 10)))))))

(t/deftest test-previous-locations
  (let [repo (test-repo (mock-scraper))]
    (t/testing "it does nothing if given a nil location"
      (t/is (= nil
               (repo-protocol/previous-locations repo "manga-fox" "the-gamer" nil 1))))

    (t/testing "it does nothing if given an empty map as location"
      (t/is (= nil
               (repo-protocol/previous-locations repo "manga-fox" "the-gamer" {} 1)))))

  (let [repo (test-repo (mock-scraper :chapters {"manga-fox"
                                                 {"the-gamer" [{:name "The Gamer 1" :ch-num 1}
                                                               {:name "The Gamer 2" :ch-num 2}]}}
                                      :pages {"manga-fox"
                                              {{:name "The Gamer 1" :ch-num 1}
                                               [{:name "1", :url  "url1"}
                                                {:name "2", :url  "url2"}
                                                {:name "3", :url  "url3"}]

                                               {:name "The Gamer 2" :ch-num 2}
                                               [{:name "4", :url  "url4"}
                                                {:name "5", :url  "url5"}]}}))]

    (t/testing "it returns n locations that precede the given location"
      (let [location {:chapter {:name "The Gamer 1" :ch-num 1} :page {:name "3" :url  "url3"}}]
        (t/is (= [{:chapter {:name "The Gamer 1" :ch-num 1} :page {:name "2" :url  "url2"}}
                  {:chapter {:name "The Gamer 1" :ch-num 1} :page {:name "1" :url  "url1"}}]
                 (repo-protocol/previous-locations repo "manga-fox" "the-gamer" location 2)))))

    (t/testing "it returns locations across chapter boundaries"
      (let [location {:chapter {:name "The Gamer 2" :ch-num 2} :page {:name "5" :url  "url5"}}]
        (t/is (= [{:chapter {:name "The Gamer 2" :ch-num 2} :page {:name "4" :url  "url4"}}
                  {:chapter {:name "The Gamer 1" :ch-num 1} :page {:name "3" :url  "url3"}}]
                 (repo-protocol/previous-locations repo "manga-fox" "the-gamer" location 2)))))

    (t/testing "only fetches as many pages as there are"
      (let [location {:chapter {:name "The Gamer 2" :ch-num 2} :page {:name "5" :url  "url5"}}]
        (t/is (= [{:chapter {:name "The Gamer 2" :ch-num 2} :page {:name "4" :url  "url4"}}
                  {:chapter {:name "The Gamer 1" :ch-num 1} :page {:name "3" :url  "url3"}}
                  {:chapter {:name "The Gamer 1" :ch-num 1} :page {:name "2" :url  "url2"}}
                  {:chapter {:name "The Gamer 1" :ch-num 1} :page {:name "1" :url  "url1"}}]
                 (repo-protocol/previous-locations repo "manga-fox" "the-gamer" location 10)))))

    (t/testing "it can start at an arbitrary chapter (with no page)"
      (let [location {:chapter {:name "The Gamer 2" :ch-num 2}}]
        (t/is (= [{:chapter {:name "The Gamer 2" :ch-num 2} :page {:name "5" :url  "url5"}}
                  {:chapter {:name "The Gamer 2" :ch-num 2} :page {:name "4" :url  "url4"}}
                  {:chapter {:name "The Gamer 1" :ch-num 1} :page {:name "3" :url  "url3"}}]
                 (repo-protocol/previous-locations repo "manga-fox" "the-gamer" location 3)))))))
