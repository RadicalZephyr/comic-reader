(ns comic-reader.database.datomic-test
  (:require [clojure.test :refer :all]
            [com.stuartsierra.component :as component]
            [comic-reader.database.datomic :as sut]
            [comic-reader.database.test-util :as test-util]
            [datomic.api :as d]))

(defn test-system [norms-dir]
  (component/system-map
   :config {:database-uri "datomic:mem://comics-test"
            :norms-dir norms-dir}
   :database (component/using
              (sut/new-database)
              [:config])
   :db-cleaner (component/using
                (test-util/new-database-cleaner)
                [:database])))

(defn wrap-cleanup-datomic [f]
  (f)
  (d/shutdown false))

(use-fixtures :each wrap-cleanup-datomic)

(deftest test-database-component
  (let [db (sut/new-database)]
    (testing "constructor and start always returns a database"
      (is (sut/database? db))
      (is (sut/database? (component/start db))))

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
