(ns alumbra.parser.schema
  (:require [alumbra.parser.antlr :as antlr]
            [alumbra.parser.traverse :as t]
            [alumbra.spec schema]
            [clojure.spec :as s]))

;; ## Parser

(antlr/defparser parse
  "Parse a GraphQL schema."
  {:grammar "alumbra/GraphQLSchema.g4"
   :root    "schema"
   :aliases {}})

;; ## Traversal

;; ### Name Traversal

(defn- read-name [[_ n]] n)
(defn- read-nested-name [[_ [_ n]]] n)
(defn- read-prefixed-name [[_ _ [_ n]]] n)

;; ### Definition Traversal

(defn- traverse-definition
  [definition-type]
  (fn [traverse-fn state [_ & body]]
    (-> (reduce traverse-fn state body)
        (assoc :graphql/definition-type definition-type))))

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
    {:schema                       (t/traverse-body)
     :definition                   (t/collect-as ::definitions)

     :typeDefinition               (traverse-definition :type)
     :typeDefinitionFields         (t/block-as :graphql/type-fields)
     :typeDefinitionField          (t/traverse-body)
     :typeDefinitionFieldType      (t/unwrap-as :graphql/type)
     :fieldName                    (t/as :graphql/field-name read-nested-name)

     :arguments                    (t/block-as :graphql/type-field-arguments)
     :argument                     (t/traverse-body)
     :argumentName                 (t/as :graphql/argument-name read-nested-name)
     :argumentType                 (t/unwrap-as :graphql/argument-type)

     :type                         (t/unwrap)
     :namedType                    (traverse-named-type)
     :nonNullType                  (traverse-non-null-type)
     :listType                     (traverse-list-type)
     :typeName                     (t/as :graphql/type-name read-nested-name)}))

(defn transform
  "Transform the AST produced by [[parse]] to conform to `:graphql/schema`."
  [ast]
  (::definitions (traverse ast)))

(s/fdef transform
        :args (s/cat :ast sequential?)
        :ret  :graphql/schema)
