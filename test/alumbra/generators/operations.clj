(ns alumbra.generators.operations
  (:require [clojure.test.check.generators :as gen]
            [alumbra.generators
             [common :refer [-name maybe rarely]]
             [directives :refer [-directives]]
             [selection-set :refer [-selection-set]]
             [variables :refer [-variable-definitions]]]
            [clojure.string :as string]))

(def -operation-type
  (gen/elements ["query" "mutation"]))

(def -operation-definition
  (gen/one-of
    [-selection-set
     (gen/let [t -operation-type
               n (maybe (gen/fmap string/upper-case -name))
               v (maybe -variable-definitions)
               d (rarely -directives)
               s -selection-set]
       (gen/return
         (str (if n (str t " "))
              (if n (str n v " "))
              (when n
                (some-> d (str " ")))
              s)))]))
