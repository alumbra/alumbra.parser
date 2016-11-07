(ns alumbra.parser.document
  (:require [alumbra.parser.antlr :as antlr]
            [alumbra.parser.traverse :as t]))

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
  {:graphql/operation-type "query"})

(def ^:private type-default
  {:graphql/non-null? false})

;; ### Name Traversal

(defn- read-name [[_ n]] n)
(defn- read-nested-name [[_ [_ n]]] n)
(defn- read-prefixed-name [[_ _ [_ n]]] n)

;; ### Value Traversal

(defn- traverse-value
  [value-type]
  (let [value-key (keyword "graphql" (name value-type))]
    (fn [traverse-fn state [_ v]]
        (assoc state
               :graphql/value-type value-type
               value-key (traverse-fn {} v)))))

(defn traverse-value-list
  [value-type]
  (let [value-key (keyword "graphql" (name value-type))]
    (fn [traverse-fn state [_ _ & body-and-delimiter]]
      (let [body (butlast body-and-delimiter)]
        (assoc state
               :graphql/value-type value-type
               value-key (mapv #(traverse-fn {} %) body))))))

(defn- parse-value
  [value-type f]
  (let [value-key (keyword "graphql" (name value-type))]
    (fn [traverse-fn state [_ v]]
      (assoc state
             :graphql/value-type value-type
             value-key (f v)))))

;; ### Type Traversal

(defn- traverse-named-type
  []
  (fn [traverse-fn state [_ [_ [_ n]]]]
    (assoc state
           :graphql/type-class   :named-type
           :graphql/type-name    n
           :graphql/non-null?    false)))

(defn- traverse-list-type
  []
  (fn [traverse-fn state [_ _ element-type]]
    (assoc state
           :graphql/type-class   :list-type
           :graphql/non-null?    false
           :graphql/element-type (traverse-fn {} element-type))))

(defn- traverse-non-null-type
  []
  (fn [traverse-fn state [_ inner-type]]
    (-> (traverse-fn state inner-type)
        (assoc :graphql/non-null? true))))

;; ## Transformation

(def ^:private traverse
  (t/traverser
    {:document            (t/traverse-body)
     :definition          (t/unwrap)
     :operationDefinition (t/collect-as :graphql/operations operation-default)
     :operationType       (t/as :graphql/operation-type read-name)
     :operationName       (t/as :graphql/operation-name read-name)

     :selectionSet        (t/block-as :graphql/selection-set)
     :selection           (t/unwrap)
     :field               (t/traverse-body)
     :fieldName           (t/as :graphql/field-name read-nested-name)
     :fieldAlias          (t/as :graphql/field-alias read-nested-name)
     :fragmentSpread      (t/traverse-body)
     :inlineFragment      (t/traverse-body)

     :fragmentDefinition  (t/collect-as :graphql/fragments)
     :fragmentName        (t/as :graphql/fragment-name read-name)
     :typeCondition       (t/unwrap-last-as :graphql/type-condition)

     :directives          (t/body-as :graphql/directives)
     :directive           (t/traverse-body)
     :directiveName       (t/as :graphql/directive-name read-prefixed-name)

     :arguments           (t/block-as :graphql/arguments)
     :argument            (t/traverse-body)
     :argumentName        (t/as :graphql/argument-name read-nested-name)
     :argumentValue       (t/unwrap-as :graphql/argument-value)

     :variableDefinitions (t/block-as :graphql/variables)
     :variableDefinition  (t/traverse-body)
     :variableName        (t/as :graphql/variable-name read-prefixed-name)
     :variableType        (t/unwrap-as :graphql/type)
     :defaultValue        (t/unwrap-last-as :graphql/default-value)

     :value               (t/unwrap)
     :variableValue       (traverse-value :variable)
     :arrayValue          (traverse-value-list :list)
     :objectValue         (traverse-value-list :object)
     :objectField         (t/traverse-body)
     :objectFieldValue    (t/unwrap-as :graphql/value)
     :enumValue           (parse-value :enum read-name)
     :intValue            (parse-value :integer #(Long. %))
     :floatValue          (parse-value :float #(Double. %))
     :stringValue         (parse-value :string str)
     :booleanValue        (parse-value :boolean #(= % "true"))

     :type                (t/unwrap)
     :namedType           (traverse-named-type)
     :nonNullType         (traverse-non-null-type)
     :listType            (traverse-list-type)
     :typeName            (t/as :graphql/type-name read-nested-name)}))

(defn transform
  "Transform the AST produced by [[parse]] to conform to `:graphql/document`."
  [ast]
  (traverse ast))
