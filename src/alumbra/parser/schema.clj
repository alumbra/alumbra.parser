(ns alumbra.parser.schema
  (:require [alumbra.parser.antlr :as antlr]
            [alumbra.parser.traverse :as t]))

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
  (let [value-key (keyword "alumbra" (name value-type))]
    (fn [traverse-fn state [_ v]]
      (assoc state
             :alumbra/value-type value-type
             value-key (f v)))))

(defn- parse-directive-location
  []
  (fn [traverse-fn state [_ [_ v]]]
    ;; TODO
    (assoc state :alumbra/directive-locations [])))

(defn- read-nested-integer [[_ [_ v]]] (Long. v))

;; ### Type Definition Traversal

(defn- traverse-named-type
  []
  (fn [traverse-fn state [_ [_ [_ n]]]]
    (assoc state
           :alumbra/type-class   :named-type
           :alumbra/type-name    n
           :alumbra/non-null?    false)))

(defn- traverse-list-type
  []
  (fn [traverse-fn state [_ _ element-type]]
    (assoc state
           :alumbra/type-class   :list-type
           :alumbra/non-null?    false
           :alumbra/element-type (traverse-fn {} element-type))))

(defn- traverse-non-null-type
  []
  (fn [traverse-fn state [_ inner-type]]
    (-> (traverse-fn state inner-type)
        (assoc :alumbra/non-null? true))))

;; ### Schema Definition Traversal

(defn- traverse-schema-type
  []
  (fn [traverse-fn state [_ operation-type _ schema-type]]
    (assoc state
           :alumbra/operation-type operation-type
           :alumbra/schema-type    (traverse-fn state schema-type))))

;; ## Transformation

(def ^:private traverse
  (t/traverser
    {:schema                       (t/traverse-body)
     :definition                   (t/unwrap)

     :typeDefinition               (t/collect-as :alumbra/type-definitions)
     :typeImplements               (t/traverse-body)
     :typeImplementsTypes          (t/body-as :alumbra/interface-types)
     :typeDefinitionFields         (t/block-as :alumbra/type-fields)
     :typeDefinitionField          (t/traverse-body)
     :typeDefinitionFieldType      (t/unwrap-as :alumbra/type)
     :fieldName                    (t/as :alumbra/field-name read-nested-name)

     :typeExtensionDefinition      (t/collect-as :alumbra/type-extensions)
     :interfaceDefinition          (t/collect-as :alumbra/interface-definitions)
     :scalarDefinition             (t/collect-as :alumbra/scalar-definitions)

     :inputTypeDefinition          (t/collect-as :alumbra/input-type-definitions)
     :inputTypeDefinitionFields    (t/block-as :alumbra/input-type-fields)
     :inputTypeDefinitionField     (t/traverse-body)
     :inputTypeDefinitionFieldType (t/unwrap-as :alumbra/type)

     :arguments                    (t/block-as :alumbra/type-field-arguments)
     :argument                     (t/traverse-body)
     :argumentName                 (t/as :alumbra/argument-name read-nested-name)
     :argumentType                 (t/unwrap-as :alumbra/argument-type)
     :defaultValue                 (t/unwrap-last-as :alumbra/default-value)

     :schemaDefinition             (t/collect-as :alumbra/schema-definitions)
     :schemaTypes                  (t/body-as :alumbra/schema-fields)
     :schemaType                   (traverse-schema-type)

     :directiveDefinition          (t/collect-as :alumbra/directive-definitions)
     :directiveLocations           (t/unwrap)
     :directiveLocation            (parse-directive-location)
     :directiveName                (t/as :alumbra/directive-name read-prefixed-name)

     :unionDefinition              (t/collect-as :alumbra/union-definitions)
     :unionDefinitionTypes         (t/body-as :alumbra/union-types)

     :enumDefinition               (t/collect-as :alumbra/enum-definitions)
     :enumDefinitionFields         (t/block-as :alumbra/enum-fields)
     :enumDefinitionField          (t/traverse-body)
     :enumDefinitionFieldName      (t/as :alumbra/enum read-nested-name)
     :enumDefinitionType           (t/traverse-body)
     :enumDefinitionIntValue       (t/as :alumbra/integer read-nested-integer)

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
     :typeName                     (t/as :alumbra/type-name read-nested-name)
     :typeCondition                (t/unwrap-last-as :alumbra/type-condition)}))

(defn transform
  "Transform the AST produced by [[parse]] to conform to `:alumbra/schema`."
  [ast]
  (traverse ast))
