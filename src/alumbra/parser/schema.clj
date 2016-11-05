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

;; ### Value Parsing

(defn- parse-value
  [value-type f]
  (let [value-key (keyword "graphql" (name value-type))]
    (fn [traverse-fn state [_ v]]
      (assoc state
             :graphql/value-type value-type
             value-key (f v)))))

(defn- read-nested-integer [[_ [_ v]]] (Long. v))

;; ### Definition Traversal

(defn- traverse-definition
  [definition-type]
  (fn [traverse-fn state [_ & body]]
    (-> (reduce traverse-fn state body)
        (assoc :graphql/definition-type definition-type))))

;; ### Type Definition Traversal

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

;; ### Schema Definition Traversal

(defn- traverse-schema-type
  []
  (fn [traverse-fn state [_ operation-type _ schema-type]]
    (assoc state
           :graphql/operation-type operation-type
           :graphql/schema-type    (traverse-fn state schema-type))))

;; ## Transformation

(def ^:private traverse
  (t/traverser
    {:schema                       (t/traverse-body)
     :definition                   (t/collect-as ::definitions)

     :typeDefinition               (traverse-definition :type)
     :typeImplements               (t/traverse-body)
     :typeImplementsTypes          (t/body-as :graphql/interface-types)
     :typeDefinitionFields         (t/block-as :graphql/type-fields)
     :typeDefinitionField          (t/traverse-body)
     :typeDefinitionFieldType      (t/unwrap-as :graphql/type)
     :fieldName                    (t/as :graphql/field-name read-nested-name)

     :typeExtensionDefinition      (traverse-definition :extend-type)
     :interfaceDefinition          (traverse-definition :interface)
     :scalarDefinition             (traverse-definition :scalar)

     :inputTypeDefinition          (traverse-definition :input)
     :inputTypeDefinitionFields    (t/block-as :graphql/input-type-fields)
     :inputTypeDefinitionField     (t/traverse-body)
     :inputTypeDefinitionFieldType (t/unwrap-as :graphql/type)

     :arguments                    (t/block-as :graphql/type-field-arguments)
     :argument                     (t/traverse-body)
     :argumentName                 (t/as :graphql/argument-name read-nested-name)
     :argumentType                 (t/unwrap-as :graphql/argument-type)
     :defaultValue                 (t/unwrap-last-as :graphql/default-value)

     :schemaDefinition             (traverse-definition :schema)
     :schemaTypes                  (t/body-as :graphql/schema-fields)
     :schemaType                   (traverse-schema-type)

     :directiveDefinition          (traverse-definition :directive)
     :directiveName                (t/as :graphql/directive-name read-prefixed-name)

     :unionDefinition              (traverse-definition :union)
     :unionDefinitionTypes         (t/body-as :graphql/union-types)

     :enumDefinition               (traverse-definition :enum)
     :enumDefinitionFields         (t/block-as :graphql/enum-fields)
     :enumDefinitionField          (t/traverse-body)
     :enumDefinitionFieldName      (t/as :graphql/enum read-nested-name)
     :enumDefinitionType           (t/traverse-body)
     :enumDefinitionIntValue       (t/as :graphql/integer read-nested-integer)

     :value                        (t/unwrap)
     :enumValue                    (parse-value :enum read-name)
     :intValue                     (parse-value :integer #(Long. %))
     :floatValue                   (parse-value :float #(Double. %))
     :stringValue                  (parse-value :string str)
     :booleanValue                 (parse-value :boolean #(= % "true"))

     :type                         (t/unwrap)
     :namedType                    (traverse-named-type)
     :nonNullType                  (traverse-non-null-type)
     :listType                     (traverse-list-type)
     :typeName                     (t/as :graphql/type-name read-nested-name)
     :typeCondition                (t/unwrap-last-as :graphql/type-condition)}))

(defn transform
  "Transform the AST produced by [[parse]] to conform to `:graphql/schema`."
  [ast]
  (::definitions (traverse ast)))

(s/fdef transform
        :args (s/cat :ast sequential?)
        :ret  :graphql/schema)
