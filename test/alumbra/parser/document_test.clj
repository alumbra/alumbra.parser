(ns alumbra.parser.document-test
  (:require [clojure.test.check
             [clojure-test :refer [defspec]]
             [generators :as gen]
             [properties :as prop]]
            [clojure.spec :as s]
            [alumbra.generators.document :as g]
            [alumbra.parser
             [antlr :as antlr]
             [document :as document]
             spec]))

(defspec t-parse-accepts-valid-queries 500
  (prop/for-all
    [document g/-document]
    (let [ast (document/parse document)]
      (not (antlr/error? ast)))))

(defspec t-transform-conforms-to-spec 500
  (prop/for-all
    [document g/-document]
    (let [ast (document/parse document)]
      (when-not (antlr/error? ast)
        (->> (document/transform ast)
             (s/valid? :graphql/document))))))

(defspec t-transform-produces-metadata-in-all-maps 500
  (prop/for-all
    [document g/-document]
    (let [ast (document/parse document)]
      (when-not (antlr/error? ast)
        (->> (document/transform ast)
             (tree-seq
               coll?
               (fn [m]
                 (if (map? m)
                   (seq (dissoc m :graphql/metadata))
                   m)))
             (filter map?)
             (every? :graphql/metadata))))))

(defspec t-transform-collects-all-definitions 50
  (prop/for-all
    [document g/-document]
    (let [ast (document/parse document)]
      (when-not (antlr/error? ast)
        (let [result (document/transform ast)]
          (= (count (next ast))
             (+ (count (:graphql/operations result))
                (count (:graphql/fragments result)))))))))

(defspec t-transform-collects-all-operation-variables 50
  (prop/for-all
    [document g/-document]
    (let [ast (document/parse document)]
      (when-not (antlr/error? ast)
        (let [expected-variable-counts
              (for [[_ [k & rst]] (next ast)
                    :when (= k :operationDefinition)]
                (or (->> rst
                     (some
                       (fn [[k & body]]
                         (when (= k :variableDefinitions)
                           (- (count body) 2)))))
                    0))
              variable-counts
              (->> (document/transform ast)
                   (:graphql/operations)
                   (map (comp count :graphql/variables)))]
          (= expected-variable-counts variable-counts))))))
