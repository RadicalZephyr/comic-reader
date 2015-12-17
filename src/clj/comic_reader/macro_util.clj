(ns comic-reader.macro-util)

(defmacro with-optional-tail
  "If the `content' is not falsey, append it to the `root'."
  [root content]
  `(let [root# ~root
         content# ~content]
     (if content#
       (conj root# content#)
       root#)))

(defmulti valid-spec?
  "Validate whether this subscription spec is valid."
  class)

(defmethod valid-spec? :default [spec] false)

(defmethod valid-spec? clojure.lang.Symbol [spec] true)

(defmethod valid-spec?
  clojure.lang.PersistentVector
  [spec]
  (= 2 (count spec)))

(defmethod valid-spec?
  clojure.lang.PersistentUnrolledVector
  [spec]
  (= 2 (count spec)))

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
