(ns comic-reader.ui.reader)

(defn comic-reader [images current-location]
  (into [:div]
        (map :image/tag images)))
