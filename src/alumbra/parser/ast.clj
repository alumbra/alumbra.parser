(ns alumbra.parser.ast)

;; ## Helper

(defn- attach-position
  [value form]
  {:pre [(map? value)]}
  (if-let [{:keys [antlr/row antlr/column antlr/index]}
           (some-> form meta :antlr/start)]
    (assoc value
           :alumbra.parser/metadata
           {:alumbra.parser/row    row
            :alumbra.parser/column column
            :alumbra.parser/index  index})
    value))

(defn- attach-positions
  [forms values]
  (map attach-position values forms))

;; ## Traverse AST

(defmulti ^:private traverse*
  (fn [state form]
    (first form))
  :default ::none)

(defmethod traverse* ::none
  [state [k]]
  (throw (Exception. (str k)))
  (vary-meta state update :ast/unknown conj k))

(defn- traverse-all*
  [state forms]
  (reduce traverse* state forms))

;; ### Document

(defmethod traverse* :document
  [state [_ & definitions :as form]]
  (attach-position
    (->> (mapv second definitions)
         (traverse-all* state))
    form))

;; ### Operation

(defmethod traverse* :operationDefinition
  [state [_ & body :as form]]
  (let [data (-> (traverse-all* {} body)
                 (attach-position form))]
    (update state :graphql/operations (fnil conj []) data)))

(defmethod traverse* :operationType
  [state [_ t]]
  (assoc state :graphql/operation-type t))

(defmethod traverse* :operationName
  [state [_ n]]
  (assoc state :graphql/operation-name n))

;; ### Fragment

(defmethod traverse* :fragmentDefinition
  [state [_ _ & body :as form]]
  (let [data (-> (traverse-all* {} body)
                 (attach-position form))]
    (update state :graphql/fragments (fnil conj []) data)))

(defmethod traverse* :typeCondition
  [state [_ _ [_ [_ n]]]]
  (assoc state :graphql/fragment-type n))

;; ### Selection Set

(defmethod traverse* :selectionSet
  [state [_ _ & selections-plus-paren]]
  (let [selections (mapv second (butlast selections-plus-paren))
        data (traverse-all* [] selections)]
    (assoc state :graphql/selection-set data)))

(defmethod traverse* :field
  [state [_ & body :as form]]
  (let [data (-> (traverse-all* {} body)
                 (attach-position form))]
    (conj state data)))

(defmethod traverse* :fieldAlias
  [state [_ [_ a]]]
  (assoc state :graphql/field-alias a))

(defmethod traverse* :fieldName
  [state [_ [_ a]]]
  (assoc state :graphql/field-name a))

(defmethod traverse* :fragmentSpread
  [state [_ _ & body :as form]]
  (let [data (-> (traverse-all* {} body)
                 (attach-position form))]
    (conj state data)))

(defmethod traverse* :fragmentName
  [state [_ n]]
  (assoc state :graphql/fragment-name n))

(defmethod traverse* :inlineFragment
  [state [_ _ & body :as form]]
  (let [data (-> (traverse-all* {} body)
                 (attach-position form))]
    (conj state data)))

;; ### Directives

(defmethod traverse* :directives
  [state [_ & directives]]
  (let [data (traverse-all* [] directives)]
    (assoc state :graphql/directives data)))

(defmethod traverse* :directive
  [state [_ _ [_ directive-name] & body :as form]]
  (let [data (-> (traverse-all* {} body)
                 (assoc :graphql/directive-name directive-name)
                 (attach-position form))]
    (conj state data)))

;; ### Arguments

(defmethod traverse* :arguments
  [state [_ _ & arguments-plus-paren]]
  (let [arguments (butlast arguments-plus-paren)
        data (traverse-all* [] arguments)]
    (assoc state :graphql/arguments data)))

(defmethod traverse* :argument
  [state [_ [_ argument-name] _ argumentValue :as form]]
  (conj state
        (-> {:graphql/argument-name argument-name
             :graphql/argument-value (traverse* {} argumentValue)}
            (attach-position form))))

;; ### Values

(defmethod traverse* :valueWithVariable
  [state [_ v :as form]]
  (if (sequential? v)
    (traverse* state v)
    v))

(defmethod traverse* :value
  [state [_ v :as form]]
  (if (sequential? v)
    (traverse* state v)
    v))

(defmethod traverse* :intValue
  [state [_ v]]
  (Long. v))

(defmethod traverse* :floatValue
  [state [_ v]]
  (Double. v))

(defmethod traverse* :booleanValue
  [state [_ v]]
  (= v "true"))

(defmethod traverse* :enumValue
  [state [_ [_ n] :as form]]
  (-> state
      (assoc :graphql/enum-name n)
      (attach-position form)))

(defmethod traverse* :arrayValueWithVariable
  [state [_ _ & values-plus-paren]]
  (let [values (butlast values-plus-paren)]
    (mapv #(traverse* {} %) values)))

(defmethod traverse* :objectValueWithVariable
  [state [_ _ & fields-plus-paren]]
  (let [fields (butlast fields-plus-paren)]
    (traverse-all* {} fields)))

(defmethod traverse* :objectFieldWithVariable
  [state [_ [_ field-name] _ field-value]]
  (assoc state field-name (traverse* {} field-value)))

(defmethod traverse* :arrayValue
  [state [_ _ & values-plus-paren]]
  (let [values (butlast values-plus-paren)]
    (mapv #(traverse* {} %) values)))

(defmethod traverse* :objectValue
  [state [_ _ & fields-plus-paren]]
  (let [fields (butlast fields-plus-paren)]
    (traverse-all* {} fields)))

(defmethod traverse* :objectField
  [state [_ [_ field-name] _ field-value]]
  (assoc state field-name (traverse* {} field-value)))

;; ## Variable

(defmethod traverse* :variable
  [state [_ _ [_ n] :as form]]
  (-> state
      (assoc :graphql/variable-name n)
      (attach-position form)))

(defmethod traverse* :variableDefinitions
  [state [_ _ & definitions-plus-paren]]
  (let [definitions (butlast definitions-plus-paren)
        data (traverse-all* [] definitions)]
    (assoc state :graphql/variables data)))

(defmethod traverse* :variableDefinition
  [state [_ variable _ & body]]
  (conj state
        (-> {}
            (traverse* variable)
            (traverse-all* body))))

(defmethod traverse* :defaultValue
  [state [_ _ value]]
  (assoc state :graphql/default-value (traverse* {} value)))

;; ## Types

(defmethod traverse* :type
  [state [_ t :as form]]
  (assoc state
         :graphql/type
         (-> {:graphql/non-null? false}
             (traverse* t)
             (attach-position form))))

(defmethod traverse* :typeName
  [state [_ [_ n]]]
  (assoc state :graphql/type-name n))

(defmethod traverse* :listType
  [state [_ _ t]]
  (traverse* state t))

(defmethod traverse* :nonNullType
  [state [_ t]]
  (-> state
      (traverse* t)
      (assoc :graphql/non-null? true)))

;; ## Transform

(defn transform
  [ast]
  (traverse* {} ast))
