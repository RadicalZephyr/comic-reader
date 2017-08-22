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

(defn mock-spy-system [& mock-data]
  (let [source-repo (apply mock-repo mock-data)
        storage-repo (spy-repo)]
    (-> (cache-test-system source-repo storage-repo)
        component/start
        ((juxt :storage-repo :cache-repo)))))

(defn spy-mock-system [& mock-data]
  (let [source-repo (spy-repo)
        storage-repo (apply mock-repo mock-data)]
    (-> (cache-test-system source-repo storage-repo)
        component/start
        ((juxt :source-repo :cache-repo)))))

(deftest test-list-sites
  (testing "checks storage repo for content first"
    (let [sites [{:site/id :site-one :site/name "Site One"}]
          [spy-repo test-repo] (spy-mock-system :sites sites)]
      (is (= sites (<!! (repo/list-sites test-repo))))
      (is (= 0 (count (spy/calls spy-repo :list-sites))))))

  (testing "when storage repo returns nothing, fetches from source"
    (let [sites [{:site/id :site-one :site/name "Site One"}]
          [spy-repo test-repo] (mock-spy-system :sites sites)]
      (is (= sites (<!! (repo/list-sites test-repo))))
      (is (= 1 (count (spy/calls spy-repo :list-sites))))

      (testing "and stores the data from source into storage"
        (is (= [{:args [sites]}] (spy/calls spy-repo :store-sites)))))))

(deftest test-list-comics
  (testing "checks storage repo for content first"
    (let [comics [{:comic/id :site-one/comic-one :comic/name "Comic One"}]
          [spy-repo test-repo] (spy-mock-system :comics {:site-one comics})]
      (is (= comics (<!! (repo/list-comics test-repo :site-one))))
      (is (= 0 (count (spy/calls spy-repo :list-sites))))))

  (testing "when storage repo returns nothing, fetches from source"
    (let [comics [{:comic/id :site-one/comic-one :comic/name "Comic One"}]
          [spy-repo test-repo] (mock-spy-system :comics {:site-one comics})]
      (is (= comics (<!! (repo/list-comics test-repo :site-one))))
      (is (= [{:args [:site-one]}] (spy/calls spy-repo :list-comics)))

      (testing "and stores the data from source into storage"
        (is (= [{:args [comics]}] (spy/calls spy-repo :store-comics)))))))
