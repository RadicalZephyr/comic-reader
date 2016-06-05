(ns comic-reader.comic-repository.scraper-test
  (:require [clojure.test :as t]
            [comic-reader.comic-repository.scraper :as sut]
            [comic-reader.mock-site-scraper :refer [mock-scraper]]
            [com.stuartsierra.component :as component]))

(defn scraper-test-system [scraper]
  (component/system-map
   :site-scraper scraper
   :scraper-repo (component/using
                  (sut/new-scraper-repo)
                  [:site-scraper])))

(defn test-repo [scraper]
  (-> (scraper-test-system scraper)
      (component/start)
      :scraper-repo))

(t/deftest test-creation
  (t/is (not (nil? (test-repo (mock-scraper))))))
