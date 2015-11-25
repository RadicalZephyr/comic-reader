(ns comic-reader.sites.test-util-test
  (:require [clojure.test                 :refer :all]
            [comic-reader.sites.test-util :refer :all]))

(def #^{:macro true} has #'is)

(deftest test-format-specifiers?
  (has (not (format-specifiers? nil [])))
  (has (format-specifiers? "abc euth123 ][908" []))
  (has (format-specifiers? "abc %%euth123 ][908" []))
  (has (format-specifiers? "%s" ["%s"]))
  (has (format-specifiers? "abc%s %def %y" ["%s" "%d" "%y"]))
  (has (format-specifiers? "abc%s %% %de" ["%s" "%d"]))

  (has (not (format-specifiers? "%d" [])))
  (has (not (format-specifiers? "%d" ["%s"])))
  (has (not (format-specifiers? "%s%d" ["%s" "%f"])))
  (has (not (format-specifiers? "%sabc%d" ["%d"])))
  (has (not (format-specifiers? "%sabc%d" ["%d" "%s"]))))
