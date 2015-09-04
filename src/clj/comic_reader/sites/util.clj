(ns comic-reader.sites.util
  (:require [clojure.walk :as w]))

(defn walk-for-symbols [form]
  (let [symbols (atom [])
        append! #(swap! symbols conj %)]
    (w/prewalk (fn [val]
                 (when (symbol? val)
                   (append! val))
                 val)
               form)
    @symbols))

(defmacro html-fn [bindings & body]
  (let [symbols (walk-for-symbols bindings)]
    `(fn [~bindings]
       (when (and ~@symbols)
         ~@body))))

(defn gen-link->map [process-name process-url]
  (html-fn {name :content
            {url :href} :attrs}
    {:name (process-name name)
     :url (process-url url)}))

(defn gen-page-list-normalize [base-url fmt-string extract-chapter]
  (fn [{[name] :content
        {chapter :value} :attrs}]
    {:name name
     :url (format fmt-string
                  base-url (extract-chapter name))}))

(defn gen-add-key-from-url [key extract-pattern & [process]]
  (let [process (or process identity)]
    (fn [{:keys [url] :as comic-map}]
      (let [[_ data] (re-find extract-pattern url)]
        (assoc comic-map
               key (process data))))))
