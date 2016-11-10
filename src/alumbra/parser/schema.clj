(ns alumbra.parser.schema
  (:require [alumbra.parser.antlr :as antlr]
            [alumbra.parser.traverse :as t]
            [alumbra.spec.schema :as schema]))

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
  (let [value-key (keyword "alumbra.spec.schema" (name value-type))]
    (fn [traverse-fn state [_ v]]
      (assoc state
             ::schema/value-type value-type
             value-key (f v)))))

(defn- parse-directive-location
  []
  (fn [traverse-fn state [_ [_ v]]]
    ;; TODO
    (assoc state ::schema/directive-locations [])))

(defn- read-nested-integer [[_ [_ v]]] (Long. v))

;; ### Type Definition Traversal

(defn- traverse-named-type
  []
  (fn [traverse-fn state [_ [_ [_ n]]]]
    (assoc state
           ::schema/type-class   :named-type
           ::schema/type-name    n
           ::schema/non-null?    false)))

(defn- traverse-list-type
  []
  (fn [traverse-fn state [_ _ element-type]]
    (assoc state
           ::schema/type-class   :list-type
           ::schema/non-null?    false
           ::schema/element-type (traverse-fn {} element-type))))

(defn- traverse-non-null-type
  []
  (fn [traverse-fn state [_ inner-type]]
    (-> (traverse-fn state inner-type)
        (assoc ::schema/non-null? true))))

;; ### Schema Definition Traversal

(defn- traverse-schema-type
  []
  (fn [traverse-fn state [_ operation-type _ schema-type]]
    (assoc state
           ::schema/operation-type operation-type
           ::schema/schema-type    (traverse-fn state schema-type))))

;; ## Transformation

(def ^:private traverse
  (t/traverser
    {:schema                       (t/traverse-body)
     :definition                   (t/unwrap)

     :typeDefinition               (t/collect-as ::schema/type-definitions)
     :typeImplements               (t/traverse-body)
     :typeImplementsTypes          (t/body-as ::schema/interface-types)
     :typeDefinitionFields         (t/block-as ::schema/type-fields)
     :typeDefinitionField          (t/traverse-body)
     :typeDefinitionFieldType      (t/unwrap-as ::schema/type)
     :fieldName                    (t/as ::schema/field-name read-nested-name)

     :typeExtensionDefinition      (t/collect-as ::schema/type-extensions)
     :interfaceDefinition          (t/collect-as ::schema/interface-definitions)
     :scalarDefinition             (t/collect-as ::schema/scalar-definitions)

     :inputTypeDefinition          (t/collect-as ::schema/input-type-definitions)
     :inputTypeDefinitionFields    (t/block-as ::schema/input-type-fields)
     :inputTypeDefinitionField     (t/traverse-body)
     :inputTypeDefinitionFieldType (t/unwrap-as ::schema/type)

     :arguments                    (t/block-as ::schema/type-field-arguments)
     :argument                     (t/traverse-body)
     :argumentName                 (t/as ::schema/argument-name read-nested-name)
     :argumentType                 (t/unwrap-as ::schema/argument-type)
     :defaultValue                 (t/unwrap-last-as ::schema/default-value)

     :schemaDefinition             (t/collect-as ::schema/schema-definitions)
     :schemaTypes                  (t/body-as ::schema/schema-fields)
     :schemaType                   (traverse-schema-type)

     :directiveDefinition          (t/collect-as ::schema/directive-definitions)
     :directiveLocations           (t/unwrap)
     :directiveLocation            (parse-directive-location)
     :directiveName                (t/as ::schema/directive-name read-prefixed-name)

     :unionDefinition              (t/collect-as ::schema/union-definitions)
     :unionDefinitionTypes         (t/body-as ::schema/union-types)

     :enumDefinition               (t/collect-as ::schema/enum-definitions)
     :enumDefinitionFields         (t/block-as ::schema/enum-fields)
     :enumDefinitionField          (t/traverse-body)
     :enumDefinitionFieldName      (t/as ::schema/enum read-nested-name)
     :enumDefinitionType           (t/traverse-body)
     :enumDefinitionIntValue       (t/as ::schema/integer read-nested-integer)

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
     :typeName                     (t/as ::schema/type-name read-nested-name)
     :typeCondition                (t/unwrap-last-as ::schema/type-condition)}
    ::schema/metadata))

(defn transform
  "Transform the AST produced by [[parse]] to conform to `::schema/schema`."
  [ast]
  (traverse ast))
