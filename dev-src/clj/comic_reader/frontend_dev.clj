(ns comic-reader.frontend-dev
  (:require [clojure.java.browse :refer [browse-url]]
            [clojure.repl :refer :all]
            [clojure.tools.namespace.repl :refer [refresh]]
            [comic-reader.system :as system]
            [figwheel-sidecar.repl-api :refer :all]))

(defn- cwd []
  (.getCanonicalPath (java.io.File. ".")))

(def figwheel-config
  {:figwheel-options {:http-server-root "public"
                      :css-dirs ["resources/public/css"]}
   :build-ids ["dev" "devcards"]
   :all-builds [{:id "dev"
                 :source-paths ["src/cljs"]
                 :figwheel true
                 :compiler
                 {:main "comic-reader.main"
                  :optimizations :none
                  :source-map true
                  :source-map-timestamp true
                  :output-to "resources/public/js/compiled/main.js"
                  :output-dir (str (cwd) "/resources/public/js/compiled/out")
                  :asset-path "js/compiled/out"}}

                {:id "devcards"
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
(defn- start-dev*! [& args]
  (apply system/go args)
  (start-figwheel! figwheel-config)
  (start-autobuild))

(defn start-dev! [& args]
  (apply start-dev*! args)
  (browse-url "http://localhost:10555/devcards"))

(defn stop-dev! []
  (stop-autobuild)
  (stop-figwheel!)
  (system/stop))

(defn reset []
  (stop-dev!)
  (refresh :after 'comic-reader.frontend-dev/start-dev*!))
