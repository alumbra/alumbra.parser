(ns alumbra.parser.spec
  (:require [clojure.spec :as s]
            [clojure.test.check.generators :as gen]))

;; ## Names

(s/def :graphql/name
  (s/and string? #(re-matches #"[_a-zA-Z][_0-9a-zA-Z]*" %)))

(s/def :graphql/fragment-name
  :graphql/name)

(s/def :graphql/fragment-type
  :graphql/name)

(s/def :graphql/field-name
  :graphql/name)

(s/def :graphql/field-alias
  :graphql/name)

(s/def :graphql/enum-name
  :graphql/name)

(s/def :graphql/operation-name
  :graphql/name)

;; ## Values

(s/def :graphql/value
  (s/or :variable :graphql/variable
        :integer  integer?
        :float    float?
        :string   string?
        :boolean  :graphql/boolean
        :enum     :graphql/enum
        :object   :graphql/object
        :list     :graphql/list))

(s/def :graphql/boolean
  (s/with-gen
    #(contains? #{true false} %)
    #(gen/elements [true false])))

(s/def :graphql/enum
  (s/keys :req [:graphql/enum-name]))

(s/def :graphql/list
  (s/with-gen
    (s/coll-of :graphql/value)
    #(gen/vector (s/gen :graphql/value) 0 3)))

(s/def :graphql/object
  (s/with-gen
    (s/map-of :graphql/name :graphql/value)
    #(->> (gen/vector
            (gen/tuple
              (s/gen :graphql/name)
              (s/gen :graphql/value))
            0 3)
          (gen/fmap (partial into {})))))

;; ## Variables

(s/def :graphql/variable-name
  :graphql/name)

(s/def :graphql/variable
  (s/keys :req [:graphql/variable-name]))

(s/def :graphql/variable-definition
  (s/keys :req [:graphql/variable-name
                :graphql/type]
          :opt [:graphql/default-value]))

(s/def :graphql/variables
  (s/with-gen
    (s/coll-of :graphql/variable-definition)
    #(gen/vector (s/gen :graphql/variable) 1 3)))

;; ## Types

(s/def :graphql/type
  (s/or :type :graphql/named-type
        :list :graphql/list-type))

(s/def :graphql/list-type
  (s/keys :req [:graphql/type
                :graphql/non-null?]))

(s/def :graphql/named-type
  (s/keys :req [:graphql/type-name
                :graphql/non-null?]))

(s/def :graphql/non-null?
  :graphql/boolean)

;; ## Arguments

(s/def :graphql/arguments
  (s/with-gen
    (s/coll-of :graphql/argument)
    #(gen/vector (s/gen :graphql/argument) 1 3)))

(s/def :graphql/argument
  (s/keys :req [:graphql/argument-name
                :graphql/argument-value]))

(s/def :graphql/argument-name
  :graphql/name)

(s/def :graphql/argument-value
  :graphql/value)

;; ## Directives

(s/def :graphql/directives
  (s/with-gen
    (s/coll-of :graphql/directive)
    #(gen/vector (s/gen :graphql/directive) 1 2)))

(s/def :graphql/directive
  (s/keys :req [:graphql/directive-name]
          :opt [:graphql/arguments]))

(s/def :graphql/directive-name
  :graphql/name)

;; ## Selection Set

(s/def :graphql/selection-set
  (s/with-gen
    (s/coll-of :graphql/selection)
    #(gen/vector (s/gen :graphql/selection) 1 2)))

(s/def :graphql/selection
  (s/or :field           :graphql/field
        :fragment-spread :graphql/fragment-spread
        :inline-fragment :graphql/inline-fragment))

(s/def :graphql/field
  (s/keys :req [:graphql/field-name]
          :opt [:graphql/field-alias
                :graphql/arguments
                :graphql/directives
                :graphql/selection-set]))

(s/def :graphql/fragment-spread
  (s/keys :req [:graphql/fragment-name]
          :opt [:graphql/directives]))

(s/def :graphql/inline-fragment
  (s/keys :req [:graphql/selection-set]
          :opt [:graphql/directives
                :graphql/fragment-type]))

;; ## Document

(s/def :graphql/document
  (s/keys :opt [:graphql/operations
                :graphql/fragments]))

;; ### Operation

(s/def :graphql/operations
  (s/coll-of :graphql/operation))

(s/def :graphql/operation
  (s/keys :req [:graphql/selection-set]
          :opt [:graphql/operation-name
                :graphql/operation-type
                :graphql/variables
                :graphql/directives]))

(s/def :graphql/operation-type
  #{"mutation" "query" "subscription"})

;; ### Fragments

(s/def :graphql/fragments
  (s/coll-of :graphql/fragment))

(s/def :graphql/fragment
  (s/keys :req [:graphql/fragment-name
                :graphql/fragment-type
                :graphql/selection-set]
          :opt [:graphql/directives]))
