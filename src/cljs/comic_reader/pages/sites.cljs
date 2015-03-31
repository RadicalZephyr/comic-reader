(ns comic-reader.pages.sites)

(defn manga-site [site-data]
  [:li (:name site-data)])

(defn site-list [data]
  (let [data @data]
    [:div
     [:h1 (:heading data)]
     [:ul (map manga-site (:sites data))]]))
