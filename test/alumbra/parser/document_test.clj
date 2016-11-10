(ns alumbra.parser.document-test
  (:require [clojure.test.check
             [clojure-test :refer [defspec]]
             [generators :as gen]
             [properties :as prop]]
            [clojure.spec :as s]
            [alumbra.generators.document :as g]
            [alumbra.parser
             [antlr :as antlr]
             [document :refer [parse transform]]]
            [alumbra.spec.document :as document]))

(defspec t-parse-accepts-valid-queries 500
  (prop/for-all
    [document g/-document]
    (let [ast (parse document)]
      (not (antlr/error? ast)))))

(defspec t-transform-conforms-to-spec 500
  (prop/for-all
    [document g/-document]
    (let [ast (parse document)]
      (when-not (antlr/error? ast)
        (->> (transform ast)
             (s/valid? ::document/document))))))

(defspec t-transform-produces-metadata-in-all-maps 500
  (prop/for-all
    [document g/-document]
    (let [ast (parse document)]
      (when-not (antlr/error? ast)
        (->> (transform ast)
             (tree-seq
               coll?
               (fn [m]
                 (if (map? m)
                   (seq (dissoc m ::document/metadata))
                   m)))
             (filter map?)
             (every? ::document/metadata))))))

(defspec t-transform-collects-all-definitions 50
  (prop/for-all
    [document g/-document]
    (let [ast (parse document)]
      (when-not (antlr/error? ast)
        (let [result (transform ast)]
          (= (count (next ast))
             (+ (count (::document/operations result))
                (count (::document/fragments result)))))))))

(defspec t-transform-collects-all-operation-variables 50
  (prop/for-all
    [document g/-document]
    (let [ast (parse document)]
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
              (->> (transform ast)
                   (::document/operations)
                   (map (comp count ::document/variables)))]
          (= expected-variable-counts variable-counts))))))
