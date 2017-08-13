(ns comic-reader.macro-util
  (:require [cljs.analyzer.api :as ana-api]
            [devcards.core :as dc]))

(defmacro reactively [hiccup]
  `[(fn []
      ~hiccup)])

(defn list-of-tests [test-namespace]
  (->> (ana-api/ns-publics (symbol test-namespace))
       (remove (comp :test second))
       (remove (comp :anonymous second))
       (map (fn [[short-name details]]
              [(str "**" short-name "**")
               (list (:name details))]))))

(defn tests-matching-regex [test-regex]
  (let [sorted-test-nses (->> (ana-api/all-ns)
                              (map str)
                              (filter #(re-matches test-regex %))
                              (sort))
        test-ns-headers (map #(vector (str "### " %)) sorted-test-nses)
        test-ns-bodies (map list-of-tests sorted-test-nses)]
    (mapcat #(apply concat %1 %2) test-ns-headers test-ns-bodies)))

(defmacro dev-cards-runner [test-regex]
  `(dc/defcard ~'all-test
     (dc/tests
      ~@(tests-matching-regex test-regex))))
