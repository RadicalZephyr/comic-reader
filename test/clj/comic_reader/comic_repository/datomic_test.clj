(ns comic-reader.comic-repository.datomic-test
  (:require [clojure.test :refer :all]
            [comic-reader.comic-repository :as repo]
            [comic-reader.comic-repository.datomic :as sut]
            [comic-reader.database.test-util :as dtu]
            [com.stuartsierra.component :as component]
            [datomic.api :as d]))

(defn test-system [norms-dir]
  (component/system-map
   :config {:database-uri "datomic:mem://comics-test"
            :norms-dir norms-dir}
   :database (component/using
              (sut/new-datomic-repository)
              [:config])
   :db-cleaner (component/using
                (dtu/new-database-cleaner)
                [:database])))

(defn wrap-cleanup-datomic [f]
  (f)
  (d/shutdown false))

(use-fixtures :each wrap-cleanup-datomic)

(deftest test-database-component
  (let [db (sut/new-datomic-repository)]
    (testing "with no config, no connection is started"
      (is (nil? (:conn (component/start db)))))

    (let [system (test-system nil)]
      (testing "connection is nil before start"
        (is (nil? (:conn (:database system)))))

      (testing "connection is non-nil after successful start"

        (let [started-system (component/start system)]
          (try
            (is (not (nil? (:conn (:database started-system)))))
            (finally
              (component/stop started-system))))))

    (testing "norms conformation happens on startup"
      (let [system (component/start (test-system "database/test-norms"))
            db (:database system)]
        (try
          (is (= 1
                 (count
                  (d/q '[:find ?e
                         :in $
                         :where [?e :db/ident :test.enum/one]]
                       (d/db (:conn db))))))
          (finally
            (component/stop system)))))))

(defn datomic-test-system []
  (component/system-map
   :config {:database-uri "datomic:mem://comics-test"
            :norms-dir "database/norms"}
   :database-cleaner (component/using
                      (dtu/new-database-cleaner)
                      {:database :datomic-repo})
   :datomic-repo (component/using
                  (sut/new-datomic-repository)
                  [:config])))

(deftest test-get-and-store-sites
  (testing "returns no results for an empty database"
    (dtu/with-test-system [test-system (datomic-test-system)
                           test-repo (:datomic-repo test-system)]
      (is (= [] (repo/list-sites test-repo)))))

  (testing "returns one stored site record"
    (dtu/with-test-system [test-system (datomic-test-system)
                           test-repo (:datomic-repo test-system)]
      (repo/store-sites test-repo [{:id :site-one, :name "Site One"}])
      (is (= [{:site/id :site-one :site/name "Site One"}]
             (repo/list-sites test-repo)))))

  (testing "returns many stored site records"
    (dtu/with-test-system [test-system (datomic-test-system)
                           test-repo (:datomic-repo test-system)]
      (repo/store-sites test-repo [{:id :site-one, :name "Site One"}
                                   {:id :site-two, :name "Site Two"}])
      (is (= [{:site/id :site-one :site/name "Site One"}
              {:site/id :site-two :site/name "Site Two"}]
             (repo/list-sites test-repo))))))

(deftest test-get-and-store-comics
  (testing "returns no results for an empty database"
    (dtu/with-test-system [test-system (datomic-test-system)
                           test-repo (:datomic-repo test-system)]
      (is (= [] (repo/list-comics test-repo "site-one")))))

  (testing "returns one stored comic record"
    (dtu/with-test-system [test-system (datomic-test-system)
                           test-repo (:datomic-repo test-system)]
      (let [site {:id :site-one, :name "Site One"}]
        @(repo/store-sites test-repo [site])
        @(repo/store-comics test-repo (:id site) [{:id :comic-one :name "Comic One"}])

        (is (= [{:comic/id :site-one/comic-one :comic/name "Comic One"}]
               (repo/list-comics test-repo (:id site)))))))

  (testing "returns one stored comic record"
    (dtu/with-test-system [test-system (datomic-test-system)
                           test-repo (:datomic-repo test-system)]
      (let [site {:id :site-one, :name "Site One"}
            other-site {:id :site-two, :name "Site Two"}]
        @(repo/store-sites test-repo [site other-site])
        @(repo/store-comics test-repo (:id site) [{:id :comic-one :name "Comic One"}])
        @(repo/store-comics test-repo (:id other-site) [{:id "not-good-comic" :name "Not Good Comic"}])

        (is (= [{:comic/id :site-one/comic-one :comic/name "Comic One"}]
               (repo/list-comics test-repo "site-one"))))))

  (testing "returns many stored comic records"
    (dtu/with-test-system [test-system (datomic-test-system)
                           test-repo (:datomic-repo test-system)]
      (let [site {:id :site-one, :name "Site One"}]
        @(repo/store-sites test-repo [site])
        @(repo/store-comics test-repo (:id site) [{:id :comic-one :name "Comic One"}
                                                  {:id :comic-two :name "Comic Two"}]))

      (is (= #{{:comic/id :site-one/comic-one :comic/name "Comic One"}
               {:comic/id :site-one/comic-two :comic/name "Comic Two"}}
             (set (repo/list-comics test-repo :site-one)))))))

(defn make-comic-id [site-id comic-id]
  (keyword (format "%s/%s" (name site-id) (name comic-id))))

(deftest test-get-and-store-locations
  (testing "returns no results for an empty database"
    (dtu/with-test-system [test-system (datomic-test-system)
                           test-repo (:datomic-repo test-system)]
      (is (= [] (repo/next-locations test-repo :site-one/comic-one nil 100)))))

  (testing "returns one location"
    (dtu/with-test-system [test-system (datomic-test-system)
                           test-repo (:datomic-repo test-system)]
      (let [site {:id :site-one :name "Site One"}
            comic {:id :comic-one :name "Comic One"}
            comic-id (make-comic-id (:id site) (:id comic))]
        @(repo/store-sites test-repo [site])
        @(repo/store-comics test-repo (:id site) [comic])
        @(repo/store-locations test-repo comic-id
                               [{:chapter {:name "The Gamer 1" :ch-num 1} :page {:number 1 :url  "url1"}}])

        (is (= [{:location/chapter {:chapter/title "The Gamer 1" :chapter/number 1}
                 :location/page {:page/number 1 :page/url  "url1"}}]
               (repo/next-locations test-repo comic-id nil 100))))))

  (testing "returns multiple locations"
    (dtu/with-test-system [test-system (datomic-test-system)
                           test-repo (:datomic-repo test-system)]
      (let [site {:id :site-one, :name "Site One"}
            comic {:id :comic-one :name "Comic One"}
            comic-id (make-comic-id (:id site) (:id comic))]
        @(repo/store-sites test-repo [site])
        @(repo/store-comics test-repo (:id site) [comic])
        @(repo/store-locations test-repo comic-id
                               [{:chapter {:name "The Gamer 1" :ch-num 1} :page {:number 1 :url  "url1"}}
                                {:chapter {:name "The Gamer 2" :ch-num 2} :page {:number 2 :url  "url2"}}])

        (is (= [{:location/chapter {:chapter/title "The Gamer 1" :chapter/number 1}
                 :location/page {:page/number 1 :page/url  "url1"}}
                {:location/chapter {:chapter/title "The Gamer 2" :chapter/number 2}
                 :location/page {:page/number 2 :page/url  "url2"}}]
               (repo/next-locations test-repo comic-id nil 100))))))

  (testing "only returns location for the desired site and comic"
    (dtu/with-test-system [test-system (datomic-test-system)
                           test-repo (:datomic-repo test-system)]
      (let [site {:id :site-one, :name "Site One"}
            other-site {:id :site-two :name "Site Two"}
            comic {:id :comic-one :name "Comic One"}
            other-comic {:id :comic-other :name "Comic One"}
            third-comic {:id :comic-third :name "Comic Third"}
            comic-id (make-comic-id (:id site) (:id comic))
            other-comic-id (make-comic-id (:id other-site) (:id other-comic))
            third-comic-id (make-comic-id (:id site) (:id third-comic))]

        @(repo/store-sites test-repo [site other-site])
        @(repo/store-comics test-repo (:id site) [comic third-comic])
        @(repo/store-locations test-repo comic-id
                               [{:chapter {:name "The Gamer 1" :ch-num 1} :page {:number 1 :url  "url1"}}
                                {:chapter {:name "The Gamer 2" :ch-num 2} :page {:number 2 :url  "url2"}}])
        @(repo/store-locations test-repo third-comic-id
                               [{:chapter {:name "The Third 1" :ch-num 1} :page {:number 1 :url  "third-url1"}}
                                {:chapter {:name "The Third 2" :ch-num 2} :page {:number 2 :url  "third-url2"}}])


        @(repo/store-comics test-repo (:id other-site) [other-comic])
        @(repo/store-locations test-repo other-comic-id
                               [{:chapter {:name "The Other 1" :ch-num 1} :page {:number 1 :url  "other-url1"}}
                                {:chapter {:name "The Other 2" :ch-num 2} :page {:number 2 :url  "other-url2"}}])

        (is (= [{:location/chapter {:chapter/title "The Gamer 1" :chapter/number 1}
                 :location/page {:page/number 1 :page/url  "url1"}}
                {:location/chapter {:chapter/title "The Gamer 2" :chapter/number 2}
                 :location/page {:page/number 2 :page/url  "url2"}}]
               (repo/next-locations test-repo comic-id nil 100))))))

  (testing "Sorts all locations by chapter and page number"
    (dtu/with-test-system [test-system (datomic-test-system)
                           test-repo (:datomic-repo test-system)]
      (let [site {:id :site-one, :name "Site One"}
            comic {:id :comic-one :name "Comic One"}
            comic-id (make-comic-id (:id site) (:id comic))]
        @(repo/store-sites test-repo [site])
        @(repo/store-comics test-repo (:id site) [comic])
        @(repo/store-locations test-repo comic-id
                               [{:chapter {:name "The Gamer 1" :ch-num 1} :page {:number 1 :url  "url1"}}
                                {:chapter {:name "The Gamer 1" :ch-num 1} :page {:number 2 :url  "url2"}}
                                {:chapter {:name "The Gamer 1" :ch-num 1} :page {:number 3 :url  "url3"}}

                                {:chapter {:name "The Gamer 2" :ch-num 2} :page {:number 1 :url  "url1"}}
                                {:chapter {:name "The Gamer 2" :ch-num 2} :page {:number 2 :url  "url2"}}])

        (is (= [{:location/chapter {:chapter/title "The Gamer 1" :chapter/number 1}
                 :location/page {:page/number 1 :page/url  "url1"}}
                {:location/chapter {:chapter/title "The Gamer 1" :chapter/number 1}
                 :location/page {:page/number 2 :page/url  "url2"}}
                {:location/chapter {:chapter/title "The Gamer 1" :chapter/number 1}
                 :location/page {:page/number 3 :page/url  "url3"}}

                {:location/chapter {:chapter/title "The Gamer 2" :chapter/number 2}
                 :location/page {:page/number 1 :page/url  "url1"}}
                {:location/chapter {:chapter/title "The Gamer 2" :chapter/number 2}
                 :location/page {:page/number 2 :page/url  "url2"}}]
               (repo/next-locations test-repo comic-id nil 100)))

        (is (= [{:location/chapter {:chapter/title "The Gamer 1" :chapter/number 1}
                 :location/page {:page/number 1 :page/url  "url1"}}
                {:location/chapter {:chapter/title "The Gamer 1" :chapter/number 1}
                 :location/page {:page/number 2 :page/url  "url2"}}
                {:location/chapter {:chapter/title "The Gamer 1" :chapter/number 1}
                 :location/page {:page/number 3 :page/url  "url3"}}]
               (repo/next-locations test-repo comic-id nil 3)))

        (is (= [{:location/chapter {:chapter/title "The Gamer 1" :chapter/number 1}
                 :location/page {:page/number 2 :page/url  "url2"}}
                {:location/chapter {:chapter/title "The Gamer 1" :chapter/number 1}
                 :location/page {:page/number 3 :page/url  "url3"}}
                {:location/chapter {:chapter/title "The Gamer 2" :chapter/number 2}
                 :location/page {:page/number 1 :page/url  "url1"}}]
               (repo/next-locations test-repo comic-id
                                    {:location/chapter {:chapter/title "The Gamer 1"
                                                        :chapter/number 1}
                                     :location/page {:page/number 2
                                                     :page/url "url2"}}
                                    3)))))))
