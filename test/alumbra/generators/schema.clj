(ns alumbra.generators.schema
  (:require [clojure.test.check.generators :as gen]
            [alumbra.generators
             [common :refer [-name maybe]]
             [type :refer [-type -type-name]]
             [value :refer [-float -string -bool -enum -integer]]]
            [clojure.string :as string]))

;; ## Type Definition

(def -type-definition-default-value
  (gen/one-of
    [-float
     -string
     -bool
     -enum
     -integer]))

(def -type-definition-argument
  (gen/let [n -name
            t -type
            d (maybe -type-definition-default-value)]
    (str n ": " t
         (if (not= (last t) \!)
           (some->> d (str " = "))))))

(def -type-definition-arguments
  (gen/let [a (gen/vector -type-definition-argument 1 3)]
    (str "(" (string/join ", " a) ")")))

(def -type-definition-field
  (gen/let [n -name
            a (maybe -type-definition-arguments)
            t -type]
    (str n a ": " t)))

(def -type-definition-fields
  (gen/let [f (gen/vector -type-definition-field 1 5)]
    (str "{" (string/join ", " f) "}")))

(def -type-implements
  (gen/let [is (gen/vector -type-name 1 3)]
    (str "implements " (string/join ", " is))))

(def -type-definition
  (gen/let [n -type-name
            i -type-implements
            f -type-definition-fields]
    (str "type " n
         (some->> i (str " "))
         " "
         f)))

(def -type-extends-definition
  (gen/fmap #(str "extend " %) -type-definition))

;; ## Interface Definition

(def -interface-definition
  (gen/let [n -type-name
            f -type-definition-fields]
    (str "interface " n " " f)))

;; ## Input Types

(def -input-type-definition-field
  (gen/let [n -name
            t -type]
    (str n ": " t)))

(def -input-type-definition
  (gen/let [n -type-name
            f (gen/vector -input-type-definition-field 1 5)]
    (str "input " n " {"
         (string/join ", " f)
         "}")))

;; ## Scalar Definition

(def -scalar-definition
  (gen/fmap #(str "scalar " %) -type-name))

;; ## Enum Definition

(def -enum-definition
  (gen/let [vs (gen/vector
                 (gen/fmap string/upper-case -name)
                 1 4)
            n -type-name]
    (str "enum " n " {"
         (string/join ", " vs)
         "}")))

;; ## Union Definition

(def -union-definition
  (gen/let [n -type-name
            vs (gen/vector -type-name 1 5)]
    (str "union " n " = " (string/join " | " vs))))

;; ## Directive Definition

(def -directive-definition
  (gen/let [n -name
            t -type-name]
    (str "directive @" n " on " t)))

;; ## Schema Definition

(def -schema-definition
  (gen/let [ks (gen/set
                 (gen/elements
                   ["query"
                    "mutation"
                    "subscription"])
                 {:min-elements 1
                  :max-elements 3})
            vs (gen/vector -type-name (count ks))]
    (str "schema {"
         (->> (map #(str %1 ": " %2) ks vs)
              (string/join ", "))
         "}")))

;; ## Type System

(def -schema
  (gen/let [schema-def (maybe -schema-definition)
            other-defs (-> (gen/one-of
                             [-type-definition
                              -type-extends-definition
                              -scalar-definition
                              -enum-definition
                              -interface-definition
                              -union-definition
                              -input-type-definition
                              -directive-definition])
                           (gen/vector 1 8))]
    (->> (cons schema-def other-defs)
         (filter identity)
         (string/join "\n"))))
