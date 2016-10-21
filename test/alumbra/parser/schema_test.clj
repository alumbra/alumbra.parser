(ns alumbra.parser.schema-test
  (:require [clojure.test.check
             [clojure-test :refer [defspec]]
             [generators :as gen]
             [properties :as prop]]
            [clojure.spec :as s]
            [alumbra.generators.schema :as g]
            [alumbra.parser
             [antlr :as antlr]
             [schema :as schema]]))

(defspec t-parse-accepts-valid-queries 500
  (prop/for-all
    [schema g/-schema]
    (let [ast (schema/parse schema)]
      (not (antlr/error? ast)))))
