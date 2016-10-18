(ns alumbra.parser.ast-test
  (:require [clojure.test.check
             [clojure-test :refer [defspec]]
             [generators :as gen]
             [properties :as prop]]
            [clojure.spec :as s]
            [alumbra.generators :as g]
            [alumbra.parser
             [antlr :as antlr]
             [ast :as ast]
             [spec :as qls]]))

(defspec t-transform 500
  (prop/for-all
    [document g/document]
    (let [ast (antlr/parse document)]
      (when-not (antlr/error? ast)
        (->> (ast/transform ast)
             (s/valid? :graphql/document))))))
