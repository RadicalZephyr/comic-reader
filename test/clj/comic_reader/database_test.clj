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
