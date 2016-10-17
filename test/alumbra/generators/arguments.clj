(ns alumbra.generators.arguments
  (:require [clojure.test.check.generators :as gen]
            [clojure.string :as string]
            [alumbra.generators
             [common :refer :all]
             [value :refer [-value]]]))

(def -argument
  (gen/let [n -name
            v -value]
    (gen/return (str n ":" v))))

(def -arguments
  (gen/let [args (gen/vector -argument 1 3)]
    (gen/return
      (format "(%s)" (string/join ", " args)))))
