(ns alumbra.parser.schema
  (:require [alumbra.parser.antlr :as antlr]
            [alumbra.parser.traverse :as t]
            [alumbra.parser.utils :refer :all]
            [clojure.string :as string]))

;; ## Parser

(antlr/defparser parse
  "Parse a GraphQL schema."
  {:grammar "alumbra/GraphQLSchema.g4"
   :root    "schema"
   :aliases {}})

;; ## Traversal

;; ### Directives Parsing

(defn- parse-directive-location
  []
  (fn [traverse-fn state [_ v]]
    (update state
            :alumbra/directive-locations
            (fnil conj [])
            (-> v
                (string/replace "_" "-")
                (string/lower-case)
                (keyword)))))

;; ### Schema Definition Traversal

(defn- traverse-schema-type
  []
  (fn [traverse-fn state [_ operation-type _ schema-type]]
    (assoc state
           :alumbra/operation-type operation-type
           :alumbra/schema-type    (traverse-fn state schema-type))))

;; ### Value Lists

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
    {:schema                       (t/traverse-body)
     :definition                   (t/unwrap)

     :typeDefinition               (t/collect-as :alumbra/type-definitions)
     :typeImplements               (t/traverse-body)
     :typeImplementsTypes          (t/body-as :alumbra/interface-types)
     :typeDefinitionFields         (t/block-as :alumbra/field-definitions)
     :typeDefinitionField          (t/traverse-body)
     :typeDefinitionFieldType      (t/unwrap-as :alumbra/type)
     :fieldName                    (t/as :alumbra/field-name read-name)

     :typeExtensionDefinition      (t/collect-as :alumbra/type-extensions)
     :interfaceDefinition          (t/collect-as :alumbra/interface-definitions)
     :scalarDefinition             (t/collect-as :alumbra/scalar-definitions)

     :inputTypeDefinition          (t/collect-as :alumbra/input-type-definitions)
     :inputTypeDefinitionFields    (t/block-as :alumbra/input-field-definitions)
     :inputTypeDefinitionField     (t/traverse-body)
     :inputTypeDefinitionFieldType (t/unwrap-as :alumbra/type)

     :arguments                    (t/block-as :alumbra/argument-definitions)
     :argument                     (t/traverse-body)
     :argumentName                 (t/as :alumbra/argument-name read-name)
     :argumentType                 (t/unwrap-as :alumbra/argument-type)
     :defaultValue                 (t/unwrap-last-as :alumbra/default-value)

     :schemaDefinition             (t/collect-as :alumbra/schema-definitions)
     :schemaTypes                  (t/body-as :alumbra/schema-fields)
     :schemaType                   (traverse-schema-type)

     :directiveDefinition          (t/collect-as :alumbra/directive-definitions)
     :directiveLocations           (t/traverse-body)
     :directiveLocation            (parse-directive-location)
     :directiveName                (t/as :alumbra/directive-name read-prefixed-name)
     :directives                   (t/body-as :alumbra/directives)
     :directive                    (t/traverse-body)
     :directiveArguments           (t/block-as :alumbra/arguments)
     :directiveArgument            (t/traverse-body)
     :directiveArgumentValue       (t/unwrap-as :alumbra/argument-value)

     :unionDefinition              (t/collect-as :alumbra/union-definitions)
     :unionDefinitionTypes         (t/body-as :alumbra/union-types)

     :enumDefinition               (t/collect-as :alumbra/enum-definitions)
     :enumDefinitionFields         (t/block-as :alumbra/enum-fields)
     :enumDefinitionField          (t/traverse-body)
     :enumDefinitionFieldName      (t/as :alumbra/enum read-name)
     :enumDefinitionType           (t/traverse-body)
     :enumDefinitionIntValue       (t/as :alumbra/integer read-nested-integer)

     :value                        (t/unwrap)
     :enumValue                    (parse-value :enum read-name)
     :intValue                     (parse-value :integer #(Long. ^String %))
     :floatValue                   (parse-value :float #(Double. ^String %))
     :stringValue                  (parse-value :string read-string-literal)
     :nullValue                    (parse-value :null (constantly nil))
     :booleanValue                 (parse-value :boolean #(= % "true"))
     :arrayValue                   (traverse-value-list :list)
     :objectValue                  (traverse-value-list :object)
     :objectField                  (t/traverse-body)
     :objectFieldValue             (t/unwrap-as :alumbra/value)

     :type                         (t/unwrap)
     :namedType                    (traverse-named-type)
     :nonNullType                  (traverse-non-null-type)
     :listType                     (traverse-list-type)
     :typeName                     (t/as :alumbra/type-name read-name)
     :typeCondition                (t/unwrap-last-as :alumbra/type-condition)}))

(defn transform
  "Transform the AST produced by [[parse]] to conform to `:alumbra/schema`."
  [ast]
  (traverse ast))
