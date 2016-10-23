(ns alumbra.parser
  "GraphQL Parsing API"
  (:require [alumbra.parser
             [antlr :as antlr]
             [document :as document]
             [schema :as schema]]
            [alumbra.spec document]
            [clojure.spec :as s]))

;; ## Public API

(defn error?
  "Check whether the given value represents a parser error produced by
   [[parse-document]] or [[parse-schema]]."
  [value]
  (antlr/error? value))

(defn errors
  "Retrieve a seq of maps representing all ANTLR4 parse errors. Example:

   ```clojure
   {:symbol #object[org.antlr.v4.runtime.CommonToken 0x6af1e82c \"[@1,1:1='}',<7>,1:1]\"],
    :line 1,
    :char 1,
    :message \"mismatched input '}' expecting {'...', 'on', 'fragment', 'query', 'mutation', 'subscription', NAME}\",
    :rule #object[org.antlr.v4.runtime.InterpreterRuleContext 0x51bcf45 \"[129 89 85 80]\"],
    :state 139,
    :expected #object[org.antlr.v4.runtime.misc.IntervalSet 0x58e4ad58 \"{8, 14..19}\"],
    :token #object[org.antlr.v4.runtime.CommonToken 0x6af1e82c \"[@1,1:1='}',<7>,1:1]\"]}
   ```
   "
  [error-value]
  {:pre [(error? error-value)]}
  @error-value)

(defn parse-document
  "Parse a GraphQL document and return its in-memory representation, conforming
   to the `:graphql/document` spec.

   ```clojure
   (parse-schema \"type Person { id: Int!, name: String! }\")
   ;; => #:graphql{:operations
   ;;              [#:graphql{:operation-type \"query\",
   ;;                         :selection-set
   ;;                         [#:graphql{:field-name \"id\",
   ;;                                    :metadata {:row 0, :column 2, :index 2}}
   ;;                          #:graphql{:field-name \"name\",
   ;;                                    :metadata {:row 0, :column 6, :index 6}}],
   ;;                         :metadata {:row 0, :column 0, :index 0}}],
   ;;              :metadata {:row 0, :column 0, :index 0}}
   ```

   If the parser fails, an error container is returned that can be checked for
   using [[error?]]."
  [document]
  (let [result (document/parse document)]
    (if-not (error? result)
      (document/transform result)
      result)))

(defn parse-schema
  "Parse a GraphQL schema and return its in-memory representation, conforming
   to the `:graphql/schema` spec.

   ```clojure
   (parse-document \"{ id, name }\")
   ;; => [#:graphql{:type-name \"Person\",
   ;;               :metadata {:row 0, :column 5, :index 5},
   ;;               :type-fields
   ;;               [#:graphql{:field-name \"id\",
   ;;                          :metadata {:row 0, :column 14, :index 14},
   ;;                          :type #:graphql{:type-class :named-type,
   ;;                                          :type-name \"Int\",
   ;;                                          :non-null? true,
   ;;                                           :metadata {:row 0, :column 18, :index 18}}}
   ;;                #:graphql{:field-name \"name\",
   ;;                          :metadata {:row 0, :column 24, :index 24},
   ;;                          :type #:graphql{:type-class :named-type,
   ;;                                          :type-name \"String\",
   ;;                                          :non-null? true,
   ;;                                          :metadata {:row 0, :column 30, :index 30}}}],
   ;;               :definition-type :type}]
   ```

   If the parser fails, an error container is returned that can be checked for
   using [[error?]]."
  [document]
  (let [result (schema/parse document)]
    (if-not (error? result)
      (schema/transform result)
      result)))

;; ## Specs

(s/fdef parse-document
        :args (s/alt :string string?)
        :ret (s/alt :document :graphql/document
                    :error     error?))

(s/fdef parse-schema
        :args (s/alt :string string?)
        :ret (s/alt :document :graphql/schema
                    :error     error?))
