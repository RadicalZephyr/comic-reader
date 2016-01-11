(ns comic-reader.sites.test-util-test
  (:require [clojure.test                 :as t]
            [comic-reader.sites.test-util :as sut]))

(def #^{:macro true} has #'t/is)

(t/deftest test-format-specifiers?
  (has (not (sut/format-specifiers? nil [])))
  (has (sut/format-specifiers? "abc euth123 ][908" []))
  (has (sut/format-specifiers? "abc %%euth123 ][908" []))
  (has (sut/format-specifiers? "%s" ["%s"]))
  (has (sut/format-specifiers? "abc%s %def %y" ["%s" "%d" "%y"]))
  (has (sut/format-specifiers? "abc%s %% %de" ["%s" "%d"]))

  (has (not (sut/format-specifiers? "%d" [])))
  (has (not (sut/format-specifiers? "%d" ["%s"])))
  (has (not (sut/format-specifiers? "%s%d" ["%s" "%f"])))
  (has (not (sut/format-specifiers? "%sabc%d" ["%d"])))
  (has (not (sut/format-specifiers? "%sabc%d" ["%d" "%s"]))))
