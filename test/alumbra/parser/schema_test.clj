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

;; ## Directive Locations

(deftest t-transform-collects-all-directive-locations
  (let [schema (schema/transform
                 (schema/parse
                   "directive @skip on FIELD, INLINE_FRAGMENT, FRAGMENT_SPREAD"))]
    (is (= #{:field :inline-fragment :fragment-spread}
           (set
             (get-in schema [:alumbra/directive-definitions
                             0
                             :alumbra/directive-locations]))))))

;; ## Default Values

(defn- collect-argument-default-values
  [{:keys [alumbra/argument-definitions]}]
  (->> (for [{:keys [alumbra/argument-name alumbra/default-value]}
             argument-definitions]
         [argument-name (:alumbra/value-type default-value)])
       (into {})))

(deftest t-transform-collects-argument-default-values
  (let [schema (schema/transform
                 (schema/parse
                   "type X {
                      field(
                        simpleValue: S = 0,
                        objectValue: O = {a: 0}
                        listValue: L = [1 2 3]
                        noValue: O
                      ): Int
                    }"))
        argument->value-type
        (collect-argument-default-values
          (get-in schema [:alumbra/type-definitions  0
                          :alumbra/field-definitions 0]))]
    (are [argument-name expected-type]
         (= expected-type (argument->value-type argument-name))
         "simpleValue" :integer
         "objectValue" :object
         "listValue"   :list
         "noValue"     nil)))

(deftest t-transform-collects-directive-default-values
  (let [schema (schema/transform
                 (schema/parse
                   "directive @some (
                      simpleValue: S = 0,
                      objectValue: O = {a: 0}
                      listValue: L = [1 2 3]
                      noValue: O
                    ) on FIELD"))
        argument->value-type
        (collect-argument-default-values
          (get-in schema [:alumbra/directive-definitions 0]))]
    (are [argument-name expected-type]
         (= expected-type (argument->value-type argument-name))
         "simpleValue" :integer
         "objectValue" :object
         "listValue"   :list
         "noValue"     nil)))
