(ns comic-reader.dev
  (:require [clojure.repl :refer :all]
            [clojure.tools.namespace.repl :refer [clear refresh]]
            [clansi.core :refer [style]]
            [clojure.string :as str]))

(declare doc-print goto-ns)

(defn welcome []
  (doc-print
   "Welcome to the Comic Reader!
    - If you want to add a new site, run `(add-site)'.
    - If you want to work on the frontend, run `(develop-frontend)'."))

(defn add-site []
  (doc-print
   "Start by running the tests with `(run-site-tests)'. All the tests
    should pass. Now add a new site definition file and run the tests
    again.

    Follow the instructions in the test errors, and hopefully you'll end
    up with a working site scraper.")
  (goto-ns 'comic-reader.site-dev))

(defn develop-frontend []
  (doc-print
   "To get started run `(start-dev!)'.  This will kick off the
    application server, and the figwheel devcard build.")
  (goto-ns 'comic-reader.frontend-dev))

(defn- doc-print [msg]
  (printf "\n%s\n\n" (str/replace msg  #"\n +" "\n")))

(defn- goto-ns [ns]
  (require ns)
  (in-ns ns))

(defn attempt-server-shutdown []
  (require 'comic-reader.system)
  (if-let [stop-fn (find-var 'comic-reader.system/stop)]
    (stop-fn)))

(welcome)
