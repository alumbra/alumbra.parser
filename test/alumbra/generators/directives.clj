(ns alumbra.generators.directives
  (:require [clojure.test.check.generators :as gen]
            [clojure.string :as string]
            [alumbra.generators
             [arguments :refer [-arguments]]
             [common :refer [-name rarely]]]))

(def -directive
  (gen/let [n -name
            a (rarely -arguments)]
    (gen/return (str "@" n a))))

(def -directives
  (->> (gen/vector -directive 1 3)
       (gen/fmap #(string/join " " %))))
