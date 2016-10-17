(ns alumbra.generators.variables
  (:require [clojure.test.check.generators :as gen]
            [alumbra.generators
             [common :refer  [-variable maybe]]
             [type :refer [-type]]
             [value :refer [-const]]]
            [clojure.string :as string]))

(def -variable-definition
  (gen/let [v -variable
            t -type
            d (maybe -const)]
    (gen/return
      (str v ": " t (some->> d (str " = "))))))

(def -variable-definitions
  (->> (gen/vector -variable-definition 1 3)
       (gen/fmap #(string/join ", " %))
       (gen/fmap #(str "(" % ")"))))
