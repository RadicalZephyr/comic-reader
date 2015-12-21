(ns comic-reader.macro-util
  (:require [schema.core :as s]))

(s/defschema SubscriptionVector
  [(s/one s/Keyword :subscription-key) s/Any])

(s/defschema SubscriptionCallSpec
  (s/either s/Keyword SubscriptionVector))

(s/defschema SubscriptionSpec
  (s/either s/Symbol [(s/one s/Symbol :binding-symbol)
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
