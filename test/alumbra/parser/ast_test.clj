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
    (let [ast (antlr/parse-document document)]
      (when-not (antlr/error? ast)
        (->> (ast/transform ast)
             (s/valid? :graphql/document))))))

(defspec t-transform-produces-metadata-in-all-maps 500
  (prop/for-all
    [document g/document]
    (let [ast (antlr/parse-document document)]
      (when-not (antlr/error? ast)
        (->> (ast/transform ast)
             (tree-seq
               coll?
               (fn [m]
                 (if (map? m)
                   (seq (dissoc m :graphql/metadata))
                   m)))
             (filter map?)
             (every? :graphql/metadata))))))
