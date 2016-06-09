(ns comic-reader.comic-repository.datomic-test
  (:require [clojure.test :as t]
            [comic-reader.comic-repository :as repo]
            [comic-reader.comic-repository.datomic :as sut]
            [comic-reader.comic-repository.mock :refer [mock-repo]]
            [comic-reader.database :as database]
            [com.stuartsierra.component :as component]))

(defn datomic-test-system [source-repo]
  (component/system-map
   :config {:database-uri "datomic:mem://comics-test"}
   :database (component/using
              (database/new-database)
              [:config])
   :source-repo source-repo
   :datomic-repo (component/using
                  (sut/new-repository)
                  [:database :source-repo])))

(defn test-repo [source-repo]
  (-> (datomic-test-system source-repo)
      (component/start)
      :datomic-repo))

(t/deftest test-list-sites
  (t/testing "passes directly through to source-repo"
    (t/is (= [] (repo/list-sites (test-repo (mock-repo :sites [])))))

    (let [sites [{:id "site-one", :name "Site One"}
                 {:id "site-two", :name "Site Two"}
                 {:id "site-three", :name "Site Three"}]]
      (t/is (= sites (repo/list-sites (test-repo (mock-repo :sites sites))))))))
