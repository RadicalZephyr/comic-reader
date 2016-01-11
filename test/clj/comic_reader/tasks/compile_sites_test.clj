(ns comic-reader.tasks.compile-sites-test
  (:require [clojure.java.io :as io]
            [clojure.test :as t]
            [comic-reader.sites.read :as site-read]
            [comic-reader.tasks.compile-sites :as sut]))

(t/deftest -main-test
  (let [sites-list (site-read/get-sites-list)]
    (sut/-main)
    (t/is (= sites-list
             (vec (site-read/get-sites-list))))
    (io/delete-file (io/file "resources" site-read/sites-list-file-name))))
