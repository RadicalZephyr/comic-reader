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

(defn gen-add-key-from-url [key extract-pattern & [process]]
  (let [process (or process identity)]
    (fn [{:keys [url] :as comic-map}]
      (let [[_ data] (re-find extract-pattern url)]
        (assoc comic-map
               key (process data))))))
