(ns alumbra.generators.document
  (:require [clojure.test.check.generators :as gen]
            [alumbra.generators
             [fragments :refer [-fragment-definition]]
             [operations :refer [-operation-definition]]]
            [clojure.string :as string]))

(def -definition
  (gen/one-of
    [-operation-definition
     -fragment-definition]))

(def -document
  (->> (gen/vector -definition 1 5)
       (gen/fmap #(string/join "\n" %))))
