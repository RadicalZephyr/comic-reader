(ns comic-reader.ui.scroll
  (:require [reagent.core :as reagent]))

(defn stabilizer [child]
  (let [storage (atom {})]
    (reagent/create-class
     {:display-name "scroll-stabilizer"
      :component-will-update
      (fn [this]
        (let [node (reagent/dom-node this)]
          (swap! storage assoc
                 :scroll-height (.-scrollHeight node)
                 :scroll-top (.-scrollTop node))))
      :component-did-update
      (fn [this]
        (let [node (reagent/dom-node this)
              curr-scroll-height (.-scrollTop node)
              prev-scroll-height (:scroll-height @storage)
              prev-scroll-top    (:scroll-top @storage)]
          (set! (.-scrollTop node)
                (+ prev-scroll-top (- curr-scroll-height prev-scroll-height)))))
      :reagent-render identity})))
