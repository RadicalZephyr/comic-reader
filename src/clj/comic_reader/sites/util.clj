(ns comic-reader.sites.util)

(defn gen-link->map [process-name process-url]
  (fn [{name :content
        {url :href} :attrs}]
    {:name (process-name name)
     :url (process-url url)}))

(defn gen-add-key-from-url [key extract-pattern & [process]]
  (let [process (or process identity)]
    (fn [{:keys [url] :as comic-map}]
      (let [[_ data] (re-find extract-pattern url)]
        (assoc comic-map
               key (process data))))))
