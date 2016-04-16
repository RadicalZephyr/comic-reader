(ns comic-reader.sites.read-test
  (:require [clojure.java.io :as io]
            [clojure.test :as t]
            [comic-reader.sites.read :as sut]))

(def test-base-name (deref (var sut/base-name)))

(t/deftest base-name-test
  (t/is (= (test-base-name "abc.123")
           "abc"))
  (t/is (= (test-base-name "thingy.clj")
           "thingy"))
  (t/is (= (test-base-name "thing.part.two.clj")
           "thing.part.two"))
  (t/is (= (test-base-name "dir/two/three/four.clj")
           "four")))

(t/deftest find-all-sites-test
  (let [test-file (io/as-file "resources/sites/test-site.clj")]
    (spit test-file "{}")
    (t/is (some #{"test-site"} (sut/find-all-sites)))
    (io/delete-file test-file))

  (let [test-file (io/as-file "resources/sites/abc")]
    (spit test-file "nothing important")
    (t/is (= (not-any? #{"abc"} (sut/find-all-sites))))
    (io/delete-file test-file)))

(t/deftest read-site-options-test
  (t/is (= nil (sut/read-site-options "non-existent")))

  (let [test-file (io/as-file "resources/sites/test-site.clj")]
    (spit test-file "{}")
    (t/is (= (class (sut/read-site-options "test-site"))
             clojure.lang.PersistentArrayMap))
    (io/delete-file test-file)))

(t/deftest get-sites-list-test

  (with-redefs [sut/find-all-sites (constantly ["a" "b" "c"])]
    (t/is (= ["a" "b" "c"] (sut/get-sites-list))))

  (let [test-file (io/file "resources" sut/sites-list-file-name)]
    (spit test-file  (prn-str ["abc"]))
    (t/is (= ["abc"]
             (sut/get-sites-list)))
    (io/delete-file test-file :silently)))
