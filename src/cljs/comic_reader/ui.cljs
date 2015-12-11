(ns comic-reader.ui)

(defn loading []
  [:img.loading {:src "img/loading.svg"}])

(defn site-list []
  [:div [:h1 "Comics List"]
   [loading]])
