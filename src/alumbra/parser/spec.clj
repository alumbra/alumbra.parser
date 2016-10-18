(ns alumbra.parser.spec
  (:require [clojure.spec :as s]
            [clojure.test.check.generators :as gen]))

;; ## Names

(s/def :graphql/name
  (s/and string? #(re-matches #"[_a-zA-Z][_0-9a-zA-Z]*" %)))

;; ## Values

(s/def :graphql/value
  (s/or :variable :graphql/variable
        :integer  integer?
        :float    float?
        :string   string?
        :boolean  :graphql/boolean
        :enum     :graphql/enum
        :list     :graphql/list
        :object   :graphql/object))

(s/def :graphql/variable
  (s/keys :req [:variable/name]))

(s/def :variable/name
  :graphql/name)

(s/def :graphql/boolean
  (s/with-gen
    #(contains? #{true false} %)
    #(gen/elements [true false])))

(s/def :graphql/enum
  (s/keys :req [:enum/name]))

(s/def :enum/name
  :graphql/name)

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

;; ## Definitions

(s/def :graphql/definition
  (s/or :operation :graphql/operation
        :fragment  :graphql/fragment))

;; ## Operation

(s/def :graphql/operation
  (s/keys :req [:graphql/selection-set]
          :opt [:operation/name
                :operation/type
                :operation/variables
                :graphql/directives]))

;; ## Selection Set

(s/def :graphql/selection-set
  (s/with-gen
    (s/coll-of :graphql/selection)
    #(gen/vector (s/gen :graphql/selection) 1 2)))

(s/def :graphql/selection
  (s/or :field           :selection/field
        :fragment-spread :selection/fragment-spread
        :inline-fragment :selection/inline-fragment))

(s/def :selection/field
  (s/keys :req [:field/name]
          :opt [:field/alias
                :graphql/arguments
                :graphql/directives
                :graphql/selection-set]))

(s/def :field/name
  :graphql/name)

(s/def :field/alias
  :graphql/name)

(s/def :selection/fragment-spread
  (s/keys :req [:fragment/name]
          :opt [:graphql/directives]))

(s/def :selection/inline-fragment
  (s/keys :req [:graphql/selection-set]
          :opt [:graphql/directives
                :fragment/type]))

;; ## Fragments

(s/def :graphql/fragment
  (s/keys :req [:fragment/name
                :fragment/type
                :graphql/selection-set]
          :opt [:graphql/directives]))

(s/def :fragment/name
  :graphql/name)

(s/def :fragment/type
  :graphql/name)

;; ## Directives

(s/def :graphql/directives
  (s/with-gen
    (s/coll-of :graphql/directive)
    #(gen/vector (s/gen :graphql/directive) 1 2)))

(s/def :graphql/directive
  (s/keys :req [:directive/name]
          :opt [:graphql/arguments]))

(s/def :directive/name
  :graphql/name)

;; ## Arguments

(s/def :graphql/arguments
  (s/with-gen
    (s/coll-of :graphql/argument)
    #(gen/vector (s/gen :graphql/argument) 1 3)))

(s/def :graphql/argument
  (s/keys :req [:argument/name
                :argument/value]))

(s/def :argument/name
  :graphql/name)

(s/def :argument/value
  :graphql/value)
