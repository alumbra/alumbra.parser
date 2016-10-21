(ns alumbra.generators.type
  (:require [clojure.test.check.generators :as gen]
            [alumbra.generators.common :refer [-name]]
            [clojure.string :as string]))

(def -type-name
  (gen/fmap string/capitalize -name))

(def -type
  (gen/let [n     -type-name
            list? gen/boolean
            req?  gen/boolean]
    (gen/return
      (cond->> n
        list? (format "[%s]")
        req?  (format "%s!")))))
