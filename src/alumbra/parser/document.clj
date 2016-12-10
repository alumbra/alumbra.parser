(ns alumbra.parser.document
  (:require [alumbra.parser.antlr :as antlr]
            [alumbra.parser.traverse :as t]
            [alumbra.parser.utils :refer :all]))

;; ## Parser

(antlr/defparser parse
  "Parse a GraphQL document."
  {:grammar "alumbra/GraphQL.g4"
   :root    "document"
   :aliases
   {:valueWithVariable            :value
    :arrayValueWithVariable       :arrayValue
    :objectValueWithVariable      :objectValue
    :objectFieldWithVariable      :objectField
    :objectFieldValueWithVariable :objectFieldValue}})

;; ## Traverse Helpers

;; ### Defaults

(def ^:private operation-default
  {:alumbra/operation-type "query"})

(def ^:private type-default
  {:alumbra/non-null? false})

;; ### Value Traversal

(defn- traverse-value
  [value-type]
  (let [value-key (keyword "alumbra" (name value-type))]
    (fn [traverse-fn state [_ v]]
        (assoc state
               :alumbra/value-type value-type
               value-key (traverse-fn {} v)))))

(defn- traverse-variable-value
  []
  (fn [traverse-fn state [_ v]]
    (-> state
        (assoc :alumbra/value-type :variable)
        (traverse-fn v))))

(defn traverse-value-list
  [value-type]
  (let [value-key (keyword "alumbra" (name value-type))]
    (fn [traverse-fn state [_ _ & body-and-delimiter]]
      (let [body (butlast body-and-delimiter)]
        (assoc state
               :alumbra/value-type value-type
               value-key (mapv #(traverse-fn {} %) body))))))

;; ## Transformation

(def ^:private traverse
  (t/traverser
    {:document            (t/traverse-body)
     :definition          (t/unwrap)
     :operationDefinition (t/collect-as :alumbra/operations operation-default)
     :operationType       (t/as :alumbra/operation-type read-name)
     :operationName       (t/as :alumbra/operation-name read-name)

     :selectionSet        (t/block-as :alumbra/selection-set)
     :selection           (t/unwrap)
     :field               (t/traverse-body)
     :fieldName           (t/as :alumbra/field-name read-name)
     :fieldAlias          (t/as :alumbra/field-alias read-name)
     :fragmentSpread      (t/traverse-body)
     :inlineFragment      (t/traverse-body)

     :fragmentDefinition  (t/collect-as :alumbra/fragments)
     :fragmentName        (t/as :alumbra/fragment-name read-name)
     :typeCondition       (t/unwrap-last-as :alumbra/type-condition)

     :directives          (t/body-as :alumbra/directives)
     :directive           (t/traverse-body)
     :directiveName       (t/as :alumbra/directive-name read-prefixed-name)

     :arguments           (t/block-as :alumbra/arguments)
     :argument            (t/traverse-body)
     :argumentName        (t/as :alumbra/argument-name read-name)
     :argumentValue       (t/unwrap-as :alumbra/argument-value)

     :variableDefinitions (t/block-as :alumbra/variables)
     :variableDefinition  (t/traverse-body)
     :variableName        (t/as :alumbra/variable-name read-prefixed-name)
     :variableType        (t/unwrap-as :alumbra/type)
     :defaultValue        (t/unwrap-last-as :alumbra/default-value)

     :value               (t/unwrap)
     :variableValue       (traverse-variable-value)
     :arrayValue          (traverse-value-list :list)
     :objectValue         (traverse-value-list :object)
     :objectField         (t/traverse-body)
     :objectFieldValue    (t/unwrap-as :alumbra/value)
     :enumValue           (parse-value :enum read-name)
     :intValue            (parse-value :integer #(Long. ^String %))
     :floatValue          (parse-value :float #(Double. ^String %))
     :stringValue         (parse-value :string read-string-literal)
     :nullValue           (parse-value :null (constantly nil))
     :booleanValue        (parse-value :boolean #(= % "true"))

     :type                (t/unwrap)
     :namedType           (traverse-named-type)
     :nonNullType         (traverse-non-null-type)
     :listType            (traverse-list-type)
     :typeName            (t/as :alumbra/type-name read-name)}))

(defn transform
  "Transform the AST produced by [[parse]] to conform to
   `:alumbra.spec.document/document`."
  [ast]
  (traverse ast))
