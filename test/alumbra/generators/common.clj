(ns alumbra.generators.common
  (:require [clojure.test.check.generators :as gen]
            [clojure.string :as string]))

(defn maybe
  [g]
  (gen/frequency
    [[1 (gen/return nil)]
     [99 g]]))

(defn rarely
  [g]
  (gen/frequency
    [[9 (gen/return nil)]
     [1 g]]))

(def -name
  (->> (gen/tuple
         gen/char-alpha
         gen/string-alpha-numeric)
       (gen/fmap #(apply str %))))

(def -variable
  (gen/fmap #(str "$" %) -name))
