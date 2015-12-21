(ns comic-reader.ui.base)

(defn loading []
  [:img.loading {:src "img/loading.svg"}])

(defn four-oh-four []
  [:div
   [:h1 "Sorry!"]
   "There's nothing to see here. Try checking out the "
   [:a {:href "/#"} "site list."]])

(defn large-button [content]
  [:a.large.button.radius content])

(defn map-into-list [base-el f coll]
  (into base-el
        (map (fn [data]
               ^{:key (:id data)} [:li (f data)]) coll)))

(defn with-optional-tail
  "If the `content' is not falsey, append it to the `root'."
  [root content]
  (if content
    (conj root content)
    root))

(defn list-with-loading [options coll]
  (let [{:keys [heading list-element item->li]}
        options]
    (with-optional-tail
      [:div [:h1 heading]]
      (cond
        (= :loading coll) [loading]
        (seq coll)        (map-into-list list-element item->li coll)
        :else nil))))
