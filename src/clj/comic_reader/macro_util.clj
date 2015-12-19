(ns comic-reader.macro-util
  (:require [schema.core :as s]))

(defmacro with-optional-tail
  "If the `content' is not falsey, append it to the `root'."
  [root content]
  `(let [root# ~root
         content# ~content]
     (if content#
       (conj root# content#)
       root#)))

(def SubscriptionVector
  [(s/one s/Keyword :subscription-key) s/Any])

(def SubscriptionSpec
  (s/either s/Symbol [(s/one s/Symbol :binding-symbol) SubscriptionVector]))

(def valid-spec?
  (comp not (s/checker SubscriptionSpec)))

(defprotocol SubscriptionSpec
  (subscription-symbol [spec] "Extract the subscription binding symbol from this spec.")
  (subscription-key [spec] "Extract the subscription key from this spec."))

(extend-protocol SubscriptionSpec
  clojure.lang.Symbol
  (subscription-symbol [spec] spec)
  (subscription-key [spec] [(keyword spec)])

  clojure.lang.PersistentVector
  (subscription-symbol [spec] (first spec))
  (subscription-key [spec] (second spec)))

(defn subscription-binding [subscription-spec]
  (let [generate-spec (fn [spec]
                        `[~(subscription-symbol spec)
                          (re-frame.core/subscribe
                           ~(subscription-key spec))])]
    (if (valid-spec? subscription-spec)
      (generate-spec subscription-spec)
      (throw (ex-info "Invalid subscription spec. Must be a symbol or a two-element vector."
                      {:spec subscription-spec})))))

(defn container-name [component-name]
  (symbol (format "%s-container"
                  (name component-name))))

(defmacro defcomponent-2 [name subscriptions & body]
  `(do
     (defn ~name ~subscriptions
       ~@body)

     (defn ~(container-name name) []
       (let [~@(mapcat subscription-binding subscriptions)]
         (fn []
           [~name ~@(map (fn [sub] `(deref ~(subscription-symbol sub))) subscriptions)])))))
