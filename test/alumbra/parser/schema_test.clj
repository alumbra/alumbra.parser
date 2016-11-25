(ns alumbra.parser.schema-test
  (:require [clojure.test.check
             [clojure-test :refer [defspec]]
             [generators :as gen]
             [properties :as prop]]
            [clojure.spec :as s]
            [clojure.test :refer :all]
            [alumbra.generators :as alumbra-gen]
            [alumbra.parser
             [antlr :as antlr]
             [schema :as schema]]
            alumbra.spec))

(defspec t-parse-accepts-valid-queries 500
  (prop/for-all
    [schema (alumbra-gen/raw-schema)]
    (let [ast (schema/parse schema)]
      (not (antlr/error? ast)))))

(defspec t-transform-conforms-to-spec 500
  (prop/for-all
    [schema (alumbra-gen/raw-schema)]
    (let [ast (schema/parse schema)]
      (when-not (antlr/error? ast)
        (->> (schema/transform ast)
             (s/valid? :alumbra/schema))))))

(deftest t-transform-collects-all-directive-locations
  (let [schema (schema/transform
                 (schema/parse
                   "directive @skip on FIELD, INLINE_FRAGMENT, FRAGMENT_SPREAD"))]
    (is (= #{:field :inline-fragment :fragment-spread}
           (set
             (get-in schema [:alumbra/directive-definitions
                             0
                             :alumbra/directive-locations]))))))
