(ns comic-reader.sites.read-test
  (:refer-clojure :rename {spit core-spit})
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

(defn- get-file-mapping [[resource-name content]]
  (let [file (io/as-file (io/resource resource-name))
        old-content (slurp file)]
    {:file file
     :old-content old-content
     :content content}))

(defn- spit [f content]
  (if (nil? content)
    (io/delete-file f)
    (core-spit f content)))

(defn with-file-contents-fn [file-mappings body-fn]
  (let [file-mappings (map get-file-mapping (partition 2 file-mappings))]
    (doseq [{:keys [file content]} file-mappings]
      (spit file content))
    (body-fn)
    (doseq [{:keys [file old-content]} file-mappings]
      (spit file old-content))))

(defmacro with-file-contents [file-mappings & body]
  `(with-file-contents-fn ~file-mappings
     (fn []
       ~@body)))

(t/deftest find-all-sites-test
  (with-file-contents ["sites/test.site.edn" "{}"
                       "sites/sites-list.edn" nil]
    (t/is (= true
             (contains? (set (sut/find-all-sites)) "test-site"))))

  (with-file-contents ["sites/test.site.edn"  nil
                       "sites/sites-list.edn" nil]
    (t/is (= false
             (contains? (set (sut/find-all-sites)) "test-site"))))

  (with-file-contents ["sites/test.site.edn"  ""
                       "sites/sites-list.edn" nil]
    (t/is (= true
             (contains? (set (sut/find-all-sites)) "test-site"))))

  (with-file-contents ["sites/test.site.edn"  "nonsense-site-definition"
                       "sites/sites-list.edn" nil]
    (t/is (= true
             (contains? (set (sut/find-all-sites)) "test-site")))))

(t/deftest read-site-options-test
  (t/is (= nil (sut/read-site-options "non-existent")))

  (with-file-contents ["sites/test.site.edn" "{}"
                       "sites/sites-list.edn" nil]
    (t/is (= (class (sut/read-site-options "test-site"))
             clojure.lang.PersistentArrayMap))))

(t/deftest get-sites-list-test
  (with-file-contents ["sites/test.site.edn" "{}"
                       "sites/sites-list.edn" nil]
    (with-redefs [sut/find-all-sites (constantly ["a" "b" "c"])]
      (t/is (= ["a" "b" "c"] (sut/get-sites-list)))))

  (with-file-contents ["sites/test.site.edn" "{}"
                       "sites/sites-list.edn" "test-site"]
    (t/is (= true
             (contains? (set (sut/get-sites-list)) "test-site")))))
