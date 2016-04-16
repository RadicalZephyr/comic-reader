(ns comic-reader.database-test
  (:require [clojure.test :refer :all]
            [com.stuartsierra.component :as component]
            [comic-reader.database :as sut]
            [datomic.api :as d]))

(deftest database-component-test
  (let [db (sut/new-database)]
    (is (sut/database? db))

    (let [not-started-db (component/start db)]
     (is (sut/database? not-started-db))

     (is (nil? (:conn not-started-db))))

    (let [config {:database-uri "datomic:mem://comics-test"
                  :norms-dir nil}
          db (assoc db :config config)]
      (is (not (nil? (sut/get-conn db))))

      (let [started-db (component/start db)]
        (is (not (nil? (:conn started-db))))))

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
                   (d/db (:conn db)))))))))
