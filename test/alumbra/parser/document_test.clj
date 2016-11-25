(ns alumbra.parser.document-test
  (:require [clojure.test.check
             [clojure-test :refer [defspec]]
             [generators :as gen]
             [properties :as prop]]
            [clojure.spec :as s]
            [alumbra.generators :as alumbra-gen]
            [alumbra.parser
             [antlr :as antlr]
             [document :refer [parse transform]]]
            alumbra.spec))

(defspec t-parse-accepts-valid-queries 500
  (prop/for-all
    [document (alumbra-gen/raw-document)]
    (let [ast (parse document)]
      (not (antlr/error? ast)))))

(defspec t-transform-conforms-to-spec 500
  (prop/for-all
    [document (alumbra-gen/raw-document)]
    (let [ast (parse document)]
      (when-not (antlr/error? ast)
        (->> (transform ast)
             (s/valid? :alumbra/document))))))

(defspec t-transform-produces-metadata-in-all-maps 500
  (prop/for-all
    [document (alumbra-gen/raw-document)]
    (let [ast (parse document)]
      (when-not (antlr/error? ast)
        (->> (transform ast)
             (tree-seq
               coll?
               (fn [m]
                 (if (map? m)
                   (seq (dissoc m :alumbra/metadata))
                   m)))
             (filter map?)
             (every? :alumbra/metadata))))))

(defspec t-transform-collects-all-definitions 50
  (prop/for-all
    [document (alumbra-gen/raw-document)]
    (let [ast (parse document)]
      (when-not (antlr/error? ast)
        (let [result (transform ast)]
          (= (count (next ast))
             (+ (count (:alumbra/operations result))
                (count (:alumbra/fragments result)))))))))

(defspec t-transform-collects-all-operation-variables 50
  (prop/for-all
    [document (alumbra-gen/raw-document)]
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
                   (:alumbra/operations)
                   (map (comp count :alumbra/variables)))]
          (= expected-variable-counts variable-counts))))))
