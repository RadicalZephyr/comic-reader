(ns comic-reader.test-helper
  (:require [clojure.walk :as walk]
            [cljs.test :refer-macros [is testing]]
            [devcards.core :refer-macros [deftest defcard-rg]]))

(defn base-key [key-str]
  (re-find #"^[^.]*" key-str))

(deftest test-base-key
  (is (= "abc" (base-key "abc.def")))
  (is (= "def" (base-key "def.abc"))))

(defn keyword-without-classes [kw]
  (-> kw
      name
      base-key
      keyword))

(deftest test-keyword-without-classes
  (is (= :abc (keyword-without-classes :abc.def)))
  (is (= :def (keyword-without-classes :def.abc))))

(defn strip-classes [hiccup]
  (walk/postwalk
   (fn [el]
     (if (keyword? el)
       (keyword-without-classes el)
       el))
   hiccup))

(deftest test-strip-classes
  (is (= [:abc] (strip-classes [:abc.def])))
  (is (= [:abc [:hgi]] (strip-classes [:abc.def [:hgi.aoe]]))))
