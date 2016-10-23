(ns alumbra.spec.schema
  (:require [clojure.spec :as s]
            [alumbra.spec document]))

;; ## Schema

(s/def :graphql/schema
  (s/coll-of :graphql/schema-definition
             :min-count 1
             :gen-max 4))

;; ## Schema Definition

(s/def :graphql/definition-type
  #{:type :interface :schema :enum :union
    :input :directive :scalar :extend-type})

(defmulti ^:private definition-type :graphql/definition-type)

(defmethod definition-type :type
  [_]
  (s/keys :req [:graphql/definition-type
                :graphql/type-name
                :graphql/type-fields
                :graphql/metadata]
          :opt [:graphql/type-implements]))

(defmethod definition-type :extend-type
  [_]
  (s/keys :req [:graphql/definition-type
                :graphql/type-name
                :graphql/type-fields
                :graphql/metadata]
          :opt [:graphql/type-implements]))

(defmethod definition-type :interface
  [_]
  (s/keys :req [:graphql/definition-type
                :graphql/type-name
                :graphql/type-fields
                :graphql/metadata]))

(defmethod definition-type :scalar
  [_]
  (s/keys :req [:graphql/definition-type
                :graphql/type-name
                :graphql/metadata]))

(defmethod definition-type :directive
  [_]
  (s/keys :req [:graphql/definition-type
                :graphql/directive-name
                :graphql/type-condition
                :graphql/metadata]))

(defmethod definition-type :input
  [_]
  (s/keys :req [:graphql/definition-type
                :graphql/type-name
                :graphql/input-type-fields
                :graphql/metadata]))

(defmethod definition-type :union
  [_]
  (s/keys :req [:graphql/definition-type
                :graphql/type-name
                :graphql/union-types
                :graphql/metadata]))

(defmethod definition-type :enum
  [_]
  (s/keys :req [:graphql/definition-type
                :graphql/type-name
                :graphql/enum-fields
                :graphql/metadata]))

(defmethod definition-type :schema
  [_]
  (s/keys :req [:graphql/schema-fields
                :graphql/metadata]))

(s/def :graphql/schema-definition
  (s/multi-spec definition-type :graphql/definition-type))

;; ## Type Definition

(s/def :graphql/implements
  (s/coll-of :graphql/implements-type
             :min-count 1
             :gen-max 3))

(s/def :graphql/implements-type
  (s/keys :req [:graphql/type-name
                :graphql/metadata]))

(s/def :graphql/type-fields
  (s/coll-of :graphql/type-field
             :min-count 1
             :gen-max 3))

(s/def :graphql/type-field
  (s/keys :req [:graphql/field-name
                :graphql/type
                :graphql/metadata]
          :opt [:graphql/type-field-arguments]))

(s/def :graphql/type-field-arguments
  (s/coll-of :graphql/type-field-argument
             :min-count 1
             :gen-max 3))

(s/def :graphql/type-field-argument
  (s/keys :req [:graphql/argument-name
                :graphql/argument-type
                :graphql/metadata]
          :opt [:graphql/argument-default-value]))

(s/def :graphql/argument-type
  :graphql/type)

(s/def :graphql/argument-default-value
  (s/and :graphql/constant
         (comp
           #{:integer :float :boolean :string :enum}
           :graphql/value-type)))

;; ## Input Type Definition

(s/def :graphql/input-type-fields
  (s/coll-of :graphql/input-type-field
             :min-count 1
             :gen-max 3))

(s/def :graphql/input-type-field
  (s/keys :req [:graphql/field-name
                :graphql/type
                :graphql/metadata]))

;; ## Union Definition

(s/def :graphql/union-types
  (s/coll-of :graphql/union-type
             :min-count 1
             :gen-max 3))

(s/def :graphql/union-type
  (s/keys :req [:graphql/type-name
                :graphql/metadata]))

;; ## Enum Definition

(s/def :graphql/enum-fields
  (s/coll-of :graphql/enum-field
             :min-count 1
             :gen-max 3))

(s/def :graphql/enum-field
  (s/keys :req [:graphql/enum
                :graphql/metadata]
          :opt [:graphql/integer]))

;; ## Schema Definition

(s/def :graphql/schema-fields
  (s/coll-of :graphql/schema-field
             :min-count 1
             :gen-max 3))

(s/def :graphql/schema-field
  (s/keys :req [:graphql/operation-type
                :graphql/schema-type
                :graphql/metadata]))

(s/def :graphql/schema-type
  (s/keys :req [:graphql/type-name
                :graphql/metadata]))
