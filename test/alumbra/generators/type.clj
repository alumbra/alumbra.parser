(ns alumbra.generators.type
  (:require [clojure.test.check.generators :as gen]
            [alumbra.generators.common :refer [-name]]))

(def -type
  (gen/let [n -name
            list? gen/boolean
            req? gen/boolean]
    (gen/return
      (cond->> n
        list? (format "[%s]")
        req?  (format "%s!")))))
