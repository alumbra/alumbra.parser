(ns alumbra.parser.ast)

;; ## Helper

(defn- attach-position
  [value form]
  {:pre [(map? value)]}
  (if-let [{:keys [antlr/row antlr/column antlr/index]}
           (some-> form meta :antlr/start)]
    (assoc value
           :graphql/metadata
           {:row    row
            :column column
            :index  index})
    value))

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

(defmethod traverse* :value
  [state [_ v :as form]]
  (-> (if (= (first v) :variable)
        (traverse* state [:variableValue v])
        (traverse* state v))
      (attach-position form)))

(defmethod traverse* :variableValue
  [state [_ v]]
  (assoc state
         :graphql/value-type :variable
         :graphql/variable (traverse* {} v)))

(defmethod traverse* :intValue
  [state [_ v]]
  (assoc state
          :graphql/value-type :integer
          :graphql/integer    (Long. v)))

(defmethod traverse* :floatValue
  [state [_ v]]
  (assoc state
         :graphql/value-type :float
         :graphql/float      (Double. v)))

(defmethod traverse* :stringValue
  [state [_ v]]
  (assoc state
         :graphql/value-type :string
         :graphql/string     v))

(defmethod traverse* :booleanValue
  [state [_ v]]
  (assoc state
         :graphql/value-type :boolean
         :graphql/boolean    (= v "true")))

(defmethod traverse* :enumValue
  [state [_ [_ n] :as form]]
  (assoc state
         :graphql/value-type :enum
         :graphql/enum n))

(defmethod traverse* :arrayValue
  [state [_ _ & values-plus-paren]]
  (let [values (butlast values-plus-paren)]
    (assoc state
           :graphql/value-type :list
           :graphql/list       (mapv #(traverse* {} %) values))))

(defmethod traverse* :objectValue
  [state [_ _ & fields-plus-paren]]
  (let [fields (butlast fields-plus-paren)]
    (assoc state
          :graphql/value-type    :object
          :graphql/object-fields (mapv #(traverse* {} %) fields))))

(defmethod traverse* :objectField
  [state [_ [_ field-name] _ field-value :as form]]
  (-> state
      (assoc :graphql/field-name field-name
             :graphql/value      (traverse* {} field-value))
      (attach-position form)))

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
