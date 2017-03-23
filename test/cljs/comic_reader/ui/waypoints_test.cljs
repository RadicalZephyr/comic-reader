(ns comic-reader.ui.waypoints-test
  (:require [cljs.test :refer-macros [is testing]]
            [devcards.core :refer-macros [deftest defcard-rg]]
            [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [garden.core :as g]
            [comic-reader.ui.waypoints :as sut]
            [comic-reader.macro-util :refer-macros [reactively]]))

(defcard-rg Waypoints
  "## Welcome to re-waypoints!

  This is a Clojurescript/Reagent port of the excellent [Waypoints
  javascript library][waypoints-js] by [imakewebthings].

  Why a port to Clojurescript/Reagent? Well, it turns out that the
  Waypoints code doesn't play very nicely with React, and there are
  some significant issues with trying to make it work. So I thought
  I'd see how hard it would be to start reimplementing it.

  [waypoints-js]: http://imakewebthings.com/waypoints
  [imakewebthings]: http://imakewebthings.com/"
  [:div
   [:style {:dangerouslySetInnerHTML
            {:__html
             (g/css [[:div.demo-box {:background "#cdcecf"
                                     :padding "20px"
                                     :border "3px black solid"
                                     :margin "5px"}]
                     [:div.code-and-demo {:background "#454644"}]
                     [:pre.code-sample {:background "#787977"}]])}}]])

(defn notify [text]
  (.log js/console text))

(defn demo-box [message options]
  [sut/waypoint {:callback #(notify message)}
   [:div.demo-box message]])

(defn demo-container [message options n]
  [sut/waypoint-context
   [:div.demo-container
    (map #(with-meta
            [demo-box (str message " " %) options]
            {:key %})
         (range n))]])

(defcard-rg waypoint-simple
  "By default Waypoints trigger when the top of the viewport passes
  across the top of the element.


  ``` clojure
  [sut/waypoint-context
   [:div.demo-container
    [sut/waypoint {:callback #(notify \"Hello re-waypoints!\")}
     [:div.demo-box \"First waypoint\"]]]]
  ```"
  (reactively
   [sut/waypoint-context
    [:div.demo-container
     [sut/waypoint {:callback #(notify "Hello re-waypoints!")}
      [:div.demo-box "First waypoint"]]]]))

(defcard-rg waypoint-bottom
  "Fires the waypoint event when the bottom of the element passes the
  bottom of the viewport."
  (reactively
   [:div
    [sut/waypoint {:offset 20
                   :callback #(notify "Hello numerical offset")}
     [:div.demo-box "Number offset waypoint"]]
    [sut/waypoint {:offset "50%"
                   :callback #(notify "Hello percentage offset")}
     [:div.demo-box "Percentage waypoint"]]
    [sut/waypoint {:offset :bottom-in-view
                   :callback #(notify "Hello bottom-in-view offset")}
     [:div.demo-box "Bottom-in-view waypoint"]]
    [sut/waypoint {:offsets [0 :bottom-in-view]
                   :callback #(notify "Hello multiple offsets")}
     [:div.demo-box "Multiple offsets waypoint"]]]))
