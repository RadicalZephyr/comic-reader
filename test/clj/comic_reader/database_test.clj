(ns comic-reader.database-test
  (:require [clojure.test :refer :all]
            [com.stuartsierra.component :as component]
            [comic-reader.database :as sut]))

(deftest test-database-component
  (let [db (sut/new-database)]
    (is (sut/database? db))

    (let [not-started-db (component/start db)]
     (is (sut/database? not-started-db))

     (is (nil? (:conn not-started-db))))

    (let [config {:database-uri "datomic:mem://comics"
                  :norms-dir nil}
          db (assoc db :config config)]
      (is (not (nil? (sut/get-conn db))))

      (let [started-db (component/start db)]
        (is (not (nil? (:conn started-db))))))))
