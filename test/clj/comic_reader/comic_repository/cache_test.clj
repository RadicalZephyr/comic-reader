(ns comic-reader.comic-repository.cache-test
  (:require [clojure.test :refer :all]
            [comic-reader.comic-repository :as repo]
            [comic-reader.comic-repository.cache :as sut]
            [comic-reader.comic-repository.mock :refer [mock-repo]]
            [comic-reader.comic-repository.spy :as spy :refer [spy-repo]]
            [com.stuartsierra.component :as component]
            [clojure.core.async :refer [<!!]]))

(defn cache-test-system [source-repo storage-repo]
  (component/system-map
   :source-repo source-repo
   :storage-repo storage-repo
   :cache-repo (component/using
                (sut/new-caching-repository)
                [:storage-repo :source-repo])))

(defn test-repo [storage-repo & mock-data]
  (let [source-repo (apply mock-repo mock-data)]
    (-> (cache-test-system source-repo storage-repo)
        component/start
        :cache-repo)))

(deftest test-fetch-and-store
  (testing "fetches data from the source-repo and stores it in the storage-repo"
    (testing "for list-sites"
      (let [sites [{:site/id :site-one :site/name "Site One"}]
            spy-repo (spy-repo)
            test-repo (test-repo spy-repo :sites sites)]
        (is (= sites (<!! (repo/list-sites test-repo))))
        (is (= [{:args [sites]}] (spy/calls spy-repo :store-sites)))))

    (testing "for list-comics"
      (let [comics [{:comic/id :site-one/comic-one :comic/name "Comic One"}]
            spy-repo (spy-repo)
            test-repo (test-repo spy-repo :comics {:site-one comics})]
        (is (= comics (<!! (repo/list-comics test-repo :site-one))))
        (is (= [{:args [comics]}] (spy/calls spy-repo :store-comics)))))

    (testing "for previous-locations"
      (let [location [:fake-comic-location]
            previous-locations [:loc-1 :loc-2]
            spy-repo (spy-repo)
            test-repo (test-repo spy-repo :site-one {:comics {"comic-one" {location {:previous-locations previous-locations}}}})]
        (is (= previous-locations
               (<!! (repo/previous-locations test-repo :site-one/comic-one location 2))))
        (is (= [{:args [:site-one/comic-one previous-locations]}]
               (spy/calls spy-repo :store-locations)))))

    (testing "for next-locations"
      (let [location [:fake-comic-location]
            next-locations [:loc-1 :loc-2]
            spy-repo (spy-repo)
            test-repo (test-repo spy-repo :site-one {:comics {"comic-one" {location {:next-locations next-locations}}}})]
        (is (= next-locations
               (<!! (repo/next-locations test-repo :site-one/comic-one location 2))))
        (is (= [{:args [:site-one/comic-one next-locations]}]
               (spy/calls spy-repo :store-locations)))))

    (testing "for image-tag"
      (let [image-tag [:img {:src "img.jpg"}]
            location [:fake-comic-location]
            spy-repo (spy-repo)
            test-repo (test-repo spy-repo :site-one {location image-tag})]
        (is (= image-tag (<!! (repo/image-tag test-repo :site-one location))))
        #_(is (= [{:args [:site-one location]}] (spy/calls spy-repo :store-image-tag)))))))
