(defn get-cleartext [prompt]
  (print prompt)
  (read-line))

(defn get-password [prompt]
  (print prompt)
  (apply str (.readPassword (System/console))))

(require '[clojure.string :as str])

(defn get-env-or-prompt [prefix prompt-fmt word get-fn]
  (let [env-name (str prefix word)]
    (or (System/getenv env-name)
        (get-fn (format prompt-fmt env-name (str/capitalize word))))))

(let [[username password] (mapv #(get-env-or-prompt "DATOMIC_"
                                                    "%s was not defined.\n%s"
                                                    %1 %2)
                                ["USERNAME"    "PASSWORD"]
                                [get-cleartext get-password])]

  (set-env! :source-paths #{"src/clj" "src/cljs"}
            :resource-paths #{"resources"}
            :dependencies (template
                           [[org.clojure/clojure ~(clojure-version)]

                            ;; Core app dependencies
                            [com.stuartsierra/component "0.3.2"]
                            [prismatic/schema "1.1.3"]
                            [environ "1.1.0"]
                            [org.clojure/tools.logging "0.3.1"
                             :exclusions [[org.slf4j/slf4j-log4j12 :extension "jar"]]]
                            [ch.qos.logback/logback-classic "1.1.11"]
                            [org.clojure/core.async "0.3.441"]

                            ;; Web server
                            [ring "1.5.1"]
                            [http-kit "2.2.0"]
                            [compojure "1.5.2"]
                            [radicalzephyr/ring.middleware.logger "0.6.0"
                             :exclusions [[org.slf4j/slf4j-log4j12 :extension "jar"]]]
                            [fogus/ring-edn "0.3.0"]
                            [hiccup "1.0.5"]
                            [garden "1.3.2"]

                            ;; Data storage
                            [com.datomic/datomic-pro "0.9.5404"
                             :exclusions [org.clojure/clojure
                                          com.google.guava/guava
                                          org.apache.httpcomponents/httpclient
                                          org.slf4j/slf4j-nop]]
                            [io.rkn/conformity "0.4.0"]
                            [org.clojure/java.jdbc "0.6.1"]
                            [org.postgresql/postgresql "9.4-1201-jdbc41"]

                            ;; Comic scraping
                            [clj-http "2.3.0"]
                            [tempfile "0.2.0"]
                            [enlive "1.1.6"]

                            ;; Clojurescript frontend
                            [org.clojure/clojurescript "1.9.495"]
                            [re-frame "0.9.2" :exclusions
                             [[org.clojure/clojurescript
                               :extension "jar"]]]
                            [cljs-ajax "0.5.8"]
                            [funcool/bide "1.4.0"]

                            ;; Dev Dependencies
                            [radicalzephyr/clansi "1.2.0" :scope "test"]
                            [aysylu/loom "1.0.0" :scope "test"]

                            [ring/ring-mock "0.3.0" :scope "test"]
                            [devcards "0.2.1-7" :scope "test"]
                            [day8/re-frame-tracer "0.1.1-SNAPSHOT" :scope "test"]
                            [org.clojars.stumitchell/clairvoyant "0.2.0" :scope "test"]
                            [com.cemerick/piggieback "0.2.1" :scope "test"]
                            [weasel "0.7.0"     :scope "test"]
                            [org.clojure/tools.nrepl "0.2.12" :scope "test"]
                            [binaryage/devtools "0.9.2" :scope "test"]

                            ;; Boot Dependencies
                            [adzerk/boot-test "1.2.0" :scope "test"]
                            [adzerk/boot-cljs "1.7.228-2" :scope "test"]
                            [adzerk/boot-cljs-repl "0.3.3" :scope "test"]
                            [crisptrutski/boot-cljs-test   "0.3.0"     :scope "test"]
                            [pandeiro/boot-http "0.7.6" :scope "test"]
                            [adzerk/boot-reload "0.4.13" :scope "test"]
                            [powerlaces/boot-cljs-devtools "0.2.0" :scope "test"]])

            :repositories #(conj % ["my.datomic.com" {:url "https://my.datomic.com/repo" :username username :password password}])))

(task-options!
 pom {:project 'radicalzephyr/comic-reader
      :version "0.1.0-SNAPSHOT"
      :description "An app for reading comics/manga on or offline"
      :url "https://github.com/radicalzephyr/comic-reader"
      :scm {:name "git"
            :url "https://github.com/radicalzephyr/comic-reader"}
      :license {:name "Eclipse Public License"
                :url "http://www.eclipse.org/legal/epl-v10.html"}})

(require
 '[adzerk.boot-test      :refer [test] :rename {test test-clj}]
 '[adzerk.boot-cljs      :refer [cljs]]
 '[adzerk.boot-cljs-repl :refer [cljs-repl-env start-repl]]
 '[adzerk.boot-reload    :refer [reload]]
 '[pandeiro.boot-http    :refer [serve]]
 '[crisptrutski.boot-cljs-test :refer [test-cljs]]
 '[powerlaces.boot-cljs-devtools :refer [cljs-devtools]]
 '[clojure.java.io :as io]
 '[clojure.string :as str]
 '[boot.util :as util])

(deftask run-sym
  "Run vars as a pre- and post- tasks."
  [b before SYM sym "The symbol to run inside a pre-wrap task"
   a after  SYM sym "The symbol to run inside a post-wrap task"]
  (let [run-symbol (fn [s]
                     (when s
                       (when-let [ns (-> s namespace symbol)]
                         (require ns))
                       (if-let [v (resolve s)]
                         (v)
                         (util/warn "Could not find var #'%s\n" s))))]
    (when (and before (not (namespace before)))
      (util/warn "Before symbol `%s` should be fully namespace qualified.\n" (pr-str before)))
    (when (and after (not (namespace after)))
      (util/warn "After symbol `%s` should be fully namespace qualified.\n" (pr-str after)))
    (comp
     (if before
       (with-pass-thru _
         (run-symbol before))
       identity)
     (if after
       (with-post-wrap _
         (run-symbol after))
       identity))))

(deftask run-server []
  (run-sym :before 'comic-reader.system/stop
           :after  'comic-reader.system/go))

(deftask run []
  (comp (watch)
        (cljs-repl-env)
        (cljs-devtools)
        (reload)
        (run-server)
        (notify :visual true)
        (cljs)))

(deftask development []
  (set-env! :resource-paths #(conj %  "dev-resources"))
  (task-options! cljs {:optimizations :none
                       :compiler-options {:closure-defines {"clairvoyant.core.devmode" true}}}
                 reload {:on-jsload 'comic-reader.main/dev-reload})
  identity)

(deftask testing []
  (set-env! :source-paths #(conj %  "test/clj" "test/cljs"))
  identity)

(deftask dev
  "Simple alias to run application in development mode"
  []
  (comp (testing)
        (development)
        (run)))

;;; This prevents a name collision WARNING between the test task and
;;; clojure.core/test, a function that nobody really uses or cares
;;; about.
(ns-unmap 'boot.user 'test)

(deftask test []
  (comp (testing)
        (development)
        (test-clj)
        (test-cljs :js-env :phantom
                   :exit?  true)))

(deftask auto-test []
  (comp (testing)
        (development)
        (watch)
        (test-clj)
        (test-cljs :js-env :phantom)))

(deftask production []
  (task-options! cljs {:ids #{"public/js/main"}
                       :optimizations :advanced
                       :compiler-options {:output-wrapper true
                                          :closure-defines {:goog.DEBUG false}}}
                 jar {:file "comic-reader.jar"
                      :main 'comic-reader.system})
  identity)

(deftask compile-sites-file []
  (let [output-dir (tmp-dir!)]
    (with-pre-wrap fs
      (require 'comic-reader.tasks.compile-sites)
      (let [file-and-contents (resolve 'comic-reader.tasks.compile-sites/file-and-contents)
            [filename content] (file-and-contents)
            output-file (io/file output-dir filename)]
        (util/info "Compiling sites list file...\n")
        (spit output-file content)
        (-> fs (add-resource output-dir) commit!)))))

(deftask build
  "Create the production uberjar."
  []
  (comp (production)
        (compile-sites-file)
        (cljs)
        (aot :namespace '#{comic-reader.system})
        (uber)
        (sift :include #{#"\.cljs$"} :invert true)
        (jar)
        (sift :include #{#"comic-reader.jar"})
        (target :dir #{"target"})))
