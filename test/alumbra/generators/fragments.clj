(ns alumbra.generators.fragments
  (:require [clojure.test.check.generators :as gen]
            [alumbra.generators
             [common :refer [-name rarely]]
             [directives :refer [-directives]]
             [selection-set :refer [-selection-set -type-condition]]]
            [clojure.string :as string]))

(def -fragment-name
  (gen/such-that #(not= % "on") -name))

(def -fragment-definition
  (gen/let [n -fragment-name
            t -type-condition
            d (rarely -directives)
            s -selection-set]
    (str "fragment "
         n " "
         t " "
         (some-> d (str " "))
         s)))
