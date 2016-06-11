(ns comic-reader.database-test
  (:require [clojure.test :refer :all]
            [com.stuartsierra.component :as component]
            [comic-reader.database :as sut]
            [datomic.api :as d]))

(deftest test-database-component
  (let [db (sut/new-database)]
    (testing "constructor and start always returns a database"
      (is (sut/database? db))
      (is (sut/database? (component/start db))))

    (testing "with no config, no connection is started"
      (is (nil? (:conn (component/start db)))))

    (let [config {:database-uri "datomic:mem://comics-test"
                  :norms-dir nil}
          db (assoc db :config config)]
      (testing "connection is nil before start"
        (is (nil? (:conn db))))

      (testing "connection is non-nil after successful start"
        (let [started-db (component/start db)]
          (is (not (nil? (:conn started-db)))))))

    (testing "norms conformation happens on startup"
      (let [config {:database-uri "datomic:mem://comics-test"
                    :norms-dir "database/test-norms"}
            db (-> db
                   (assoc :config config)
                   component/start)]
        (is (= 1
               (count
                (d/q '[:find ?e
                       :in $
                       :where [?e :db/ident :test.enum/one]]
                     (d/db (:conn db))))))))))

(defn database-test-system []
  (component/system-map
   :config {:database-uri "datomic:mem://comics-test"
            :norms-dir "database/norms"}
   :database (component/using
              (sut/new-database)
              [:config])))

(defmacro with-test-db [db-binding & body]
  `(let [test-system# (component/start (database-test-system))
         ~db-binding (:database test-system#)]
     (try
       ~@body
       (finally
         (d/delete-database (get-in test-system# [:config :database-uri]))))))

(deftest test-get-and-store-sites
  (testing "returns no results for an empty database"
    (with-test-db test-database
      (is (= [] (sut/get-sites test-database)))))

  (testing "returns one stored site record"
    (with-test-db test-database
      (sut/store-sites test-database [{:id "site-one", :name "Site One"}])
      (is (= [{:site/id "site-one"
               :site/name "Site One"}]
             (sut/get-sites test-database)))))

  (testing "returns many stored site records"
    (with-test-db test-database
      (sut/store-sites test-database [{:id "site-one", :name "Site One"}
                                      {:id "site-two", :name "Site Two"}])
      (is (= [{:site/id "site-one"
               :site/name "Site One"}
              {:site/id "site-two"
               :site/name "Site Two"}]
             (sut/get-sites test-database))))))

(deftest test-get-and-store-comics
  (testing "returns no results for an empty database"
    (with-test-db test-database
      (is (= [] (sut/get-comics test-database "site-one")))))

  (testing "returns one stored comic record"
    (with-test-db test-database
      (sut/store-sites test-database [{:id "site-one", :name "Site One"}])
      (let [site-id (sut/get-site-id test-database "site-one")]

        (sut/store-comics test-database site-id [{:id "comic-one" :name "Comic One"}])

        (is (= [{:comic/id "comic-one" :comic/name "Comic One"}]
               (sut/get-comics test-database "site-one"))))))

  (testing "returns many stored comic records"
    (with-test-db test-database
      (sut/store-sites test-database [{:id "site-one", :name "Site One"}])
      (let [site-id (sut/get-site-id test-database "site-one")]

        (sut/store-comics test-database site-id [{:id "comic-one" :name "Comic One"}
                                                 {:id "comic-two" :name "Comic Two"}])

        (is (= #{{:comic/id "comic-one" :comic/name "Comic One"}
                 {:comic/id "comic-two" :comic/name "Comic Two"}}
               (set (sut/get-comics test-database "site-one"))))))))
