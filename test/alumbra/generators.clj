(ns alumbra.generators
  (:require [clojure.test.check.generators :as gen]
            [alumbra.generators
             [operations :refer [-operation-definition]]]
            [clojure.string :as string]))

(def definition
  (gen/one-of
    [-operation-definition
     #_fragment-definition]))

(def document
  (->> (gen/vector definition 1 5)
       (gen/fmap #(string/join "\n" %))))
