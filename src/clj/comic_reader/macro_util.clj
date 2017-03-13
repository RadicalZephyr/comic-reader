(ns comic-reader.macro-util
  (:require [schema.core :as s]
            [cljs.analyzer.api :as ana-api]
            [devcards.core :as dc]))

(s/defschema SubscriptionVector
  [(s/one s/Keyword :subscription-key) s/Any])

(s/defschema SubscriptionCallSpec
  (s/cond-pre s/Keyword SubscriptionVector))

(s/defschema SubscriptionSpec
  (s/cond-pre s/Symbol [(s/one s/Symbol :binding-symbol)
                        (s/one SubscriptionCallSpec :call-spec)]))

(defprotocol PSubscriptionSpec
  (subscription-symbol [spec] "Extract the subscription binding symbol from this spec.")
  (subscription-key [spec] "Extract the subscription key from this spec."))

(extend-protocol PSubscriptionSpec
  clojure.lang.Symbol
  (subscription-symbol [spec] spec)
  (subscription-key [spec] [(keyword spec)])

  clojure.lang.PersistentVector
  (subscription-symbol [spec] (first spec))
  (subscription-key [spec]
    (let [call-spec (second spec)]
      (if (keyword? call-spec)
        [call-spec]
        call-spec))))

(s/defn ^:always-validate subscription-binding
  [subscription-spec :- SubscriptionSpec]
  `[~(subscription-symbol subscription-spec)
    (re-frame.core/subscribe
     ~(subscription-key subscription-spec))])

(defn container-name [component-name]
  (symbol (format "%s-container"
                  (name component-name))))

(defmacro defcomponent-2 [name subscriptions & body]
  `(do
     (defn ~name ~(mapv subscription-symbol subscriptions)
       ~@body)

     (defn ~(container-name name) []
       (let [~@(mapcat subscription-binding subscriptions)]
         (fn []
           [~name ~@(map (fn [sub] `(deref ~(subscription-symbol sub))) subscriptions)])))))

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
