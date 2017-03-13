(ns comic-reader.ui.image-test
  (:require [devcards.core :refer-macros [defcard-rg]]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [comic-reader.api :as api]
            [comic-reader.main :as main]
            [comic-reader.ui.image :as sut]))

(defcard-rg image-card
  [sut/comic-image identity [:img {:src "/public/img/tux.png"}]])

(main/setup!)
(sut/setup!)
(with-redefs [api/get-comics (constantly nil)]
  (re-frame/dispatch-sync [:view-comics "manga-reader"]))

(defcard-rg fetched-image-card
  (let [location {:location/chapter {:chapter/title "The Gamer 1"
                                     :chapter/url "http://www.mangareader.net/the-gamer/1"
                                     :chapter/number 1}
                  :location/page {:page/number 1
                                  :page/url "http://www.mangareader.net/the-gamer/1"}}]
    [sut/comic-image-container identity location]))
