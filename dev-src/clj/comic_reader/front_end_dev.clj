(ns comic-reader.front-end-dev
  (:require [comic-reader.system :refer [go]]
            [figwheel-sidecar.repl-api :refer :all]))

(defn- cwd []
  (.getCanonicalPath (java.io.File. ".")))

(def figwheel-config
  {:figwheel-options {:http-server-root "public"
                      :css-dirs ["resources/public/css"]}
   :build-ids ["dev"]
   :all-builds [{:id "dev"
                 :source-paths ["src/cljs" "test/cljs"]
                 :figwheel {:devcards true}
                 :compiler
                 {:main "comic-reader.devcards"
                  :optimizations :none
                  :source-map true
                  :source-map-timestamp true
                  :output-to "resources/public/js/compiled/devcards.js"
                  :output-dir (str (cwd) "/resources/public/js/compiled/devcards_out")
                  :asset-path "js/compiled/devcards_out"}}]})

(defn start-dev! [& args]
  (apply go args)
  (start-figwheel! figwheel-config))
