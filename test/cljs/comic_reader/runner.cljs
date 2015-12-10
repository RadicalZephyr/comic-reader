(ns comic-reader.runner
  (:require [cljs.test :refer-macros [run-tests]]
            [comic-reader.main-test]))

(run-tests 'comic-reader.main-test)
