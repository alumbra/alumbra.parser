(ns alumbra.parser.antlr-test
  (:require [clojure.test.check
             [clojure-test :refer [defspec]]
             [generators :as gen]
             [properties :as prop]]
            [alumbra.generators :as g]
            [alumbra.parser.antlr :as antlr]))

(defspec t-parse 500
  (prop/for-all
    [document g/document]
    (let [result (antlr/parse document)]
      (not (antlr/error? result)))))
