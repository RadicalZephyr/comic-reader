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

(t/deftest creates-new-repository
  (t/is (not (nil? (test-repo (mock-repo))))))
