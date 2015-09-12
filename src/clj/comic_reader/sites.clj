(ns comic-reader.sites
  (:refer-clojure :exclude [list])
  (:require [comic-reader.sites.manga-fox :refer [manga-fox]]
            [comic-reader.sites.manga-reader :refer [manga-reader]]
            [comic-reader.util :refer [safe-read-string]]
            [clojure.string :as s]))

(def sites {"manga-fox"    manga-fox
            "manga-reader" manga-reader})
