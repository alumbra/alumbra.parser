(ns alumbra.parser.document
  (:require [alumbra.parser.antlr :as antlr]
            [alumbra.parser.traverse :as t]
            [alumbra.spec.document :as document]))

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
  {::document/operation-type "query"})

(def ^:private type-default
  {::document/non-null? false})

;; ### Name Traversal

(defn- read-name [[_ n]] n)
(defn- read-nested-name [[_ [_ n]]] n)
(defn- read-prefixed-name [[_ _ [_ n]]] n)

;; ### Value Traversal

(defn- traverse-value
  [value-type]
  (let [value-key (keyword "alumbra.spec.document" (name value-type))]
    (fn [traverse-fn state [_ v]]
        (assoc state
               ::document/value-type value-type
               value-key (traverse-fn {} v)))))

(defn- traverse-variable-value
  []
  (fn [traverse-fn state [_ v]]
    (-> state
        (assoc ::document/value-type :variable)
        (traverse-fn v))))

(defn traverse-value-list
  [value-type]
  (let [value-key (keyword "alumbra.spec.document" (name value-type))]
    (fn [traverse-fn state [_ _ & body-and-delimiter]]
      (let [body (butlast body-and-delimiter)]
        (assoc state
               ::document/value-type value-type
               value-key (mapv #(traverse-fn {} %) body))))))

(defn- parse-value
  [value-type f]
  (let [value-key (keyword "alumbra.spec.document" (name value-type))]
    (fn [traverse-fn state [_ v]]
      (assoc state
             ::document/value-type value-type
             value-key (f v)))))

;; ### Type Traversal

(defn- traverse-named-type
  []
  (fn [traverse-fn state [_ [_ [_ n]]]]
    (assoc state
           ::document/type-class   :named-type
           ::document/type-name    n
           ::document/non-null?    false)))

(defn- traverse-list-type
  []
  (fn [traverse-fn state [_ _ element-type]]
    (assoc state
           ::document/type-class   :list-type
           ::document/non-null?    false
           ::document/element-type (traverse-fn {} element-type))))

(defn- traverse-non-null-type
  []
  (fn [traverse-fn state [_ inner-type]]
    (-> (traverse-fn state inner-type)
        (assoc ::document/non-null? true))))

;; ## Transformation

(def ^:private traverse
  (t/traverser
    {:document            (t/traverse-body)
     :definition          (t/unwrap)
     :operationDefinition (t/collect-as ::document/operations operation-default)
     :operationType       (t/as ::document/operation-type read-name)
     :operationName       (t/as ::document/operation-name read-name)

     :selectionSet        (t/block-as ::document/selection-set)
     :selection           (t/unwrap)
     :field               (t/traverse-body)
     :fieldName           (t/as ::document/field-name read-nested-name)
     :fieldAlias          (t/as ::document/field-alias read-nested-name)
     :fragmentSpread      (t/traverse-body)
     :inlineFragment      (t/traverse-body)

     :fragmentDefinition  (t/collect-as ::document/fragments)
     :fragmentName        (t/as ::document/fragment-name read-name)
     :typeCondition       (t/unwrap-last-as ::document/type-condition)

     :directives          (t/body-as ::document/directives)
     :directive           (t/traverse-body)
     :directiveName       (t/as ::document/directive-name read-prefixed-name)

     :arguments           (t/block-as ::document/arguments)
     :argument            (t/traverse-body)
     :argumentName        (t/as ::document/argument-name read-nested-name)
     :argumentValue       (t/unwrap-as ::document/argument-value)

     :variableDefinitions (t/block-as ::document/variables)
     :variableDefinition  (t/traverse-body)
     :variableName        (t/as ::document/variable-name read-prefixed-name)
     :variableType        (t/unwrap-as ::document/type)
     :defaultValue        (t/unwrap-last-as ::document/default-value)

     :value               (t/unwrap)
     :variableValue       (traverse-variable-value)
     :arrayValue          (traverse-value-list :list)
     :objectValue         (traverse-value-list :object)
     :objectField         (t/traverse-body)
     :objectFieldValue    (t/unwrap-as ::document/value)
     :enumValue           (parse-value :enum read-name)
     :intValue            (parse-value :integer #(Long. %))
     :floatValue          (parse-value :float #(Double. %))
     :stringValue         (parse-value :string str)
     :booleanValue        (parse-value :boolean #(= % "true"))

     :type                (t/unwrap)
     :namedType           (traverse-named-type)
     :nonNullType         (traverse-non-null-type)
     :listType            (traverse-list-type)
     :typeName            (t/as ::document/type-name read-nested-name)}
    ::document/metadata))

(defn transform
  "Transform the AST produced by [[parse]] to conform to
   `:alumbra.spec.document/document`."
  [ast]
  (traverse ast))
