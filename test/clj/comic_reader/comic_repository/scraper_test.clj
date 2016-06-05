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

(t/deftest test-next-pages
  (t/testing "starts at the beginning when location is nil"
    (let [repo (test-repo (mock-scraper :chapters {"manga-fox"
                                                   {"the-gamer" [{:name "The Gamer 1",
                                                                  :ch-num 1}]}}
                                        :pages {"manga-fox"
                                                {{:name "The Gamer 1" :ch-num 1}
                                                 [{:name "1", :url  "http://www.mangahere.co/manga/the_gamer/c002/"}
                                                  {:name "2", :url  "http://www.mangahere.co/manga/the_gamer/c002/2.html"}]}}))]

      (t/is (= [{:name "1", :url  "http://www.mangahere.co/manga/the_gamer/c002/"}]
               (repo-protocol/next-pages repo "manga-fox" "the-gamer" {} 1))))))
