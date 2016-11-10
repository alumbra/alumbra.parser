(ns alumbra.parser
  "GraphQL Parsing API"
  (:require [alumbra.parser
             [antlr :as antlr]
             [document :as document]
             [schema :as schema]]))

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
   to the `:alumbra/document` spec.

   ```clojure
   (parse-document \"{ id, name }\")
   ;; => #:alumbra{:operations
   ;;              [#:alumbra{:operation-type \"query\",
   ;;                         :selection-set
   ;;                         [#:alumbra{:field-name \"id\",
   ;;                                    :metadata {:row 0, :column 2, :index 2}}
   ;;                          #:alumbra{:field-name \"name\",
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
   to the `:alumbra/schema` spec.

   ```clojure
   (parse-schema \"type Person { id: Int!, name: String! }\")
   ;; => [#:alumbra{:type-name \"Person\",
   ;;               :metadata {:row 0, :column 5, :index 5},
   ;;               :type-fields
   ;;               [#:alumbra{:field-name \"id\",
   ;;                          :metadata {:row 0, :column 14, :index 14},
   ;;                          :type #:alumbra{:type-class :named-type,
   ;;                                          :type-name \"Int\",
   ;;                                          :non-null? true,
   ;;                                           :metadata {:row 0, :column 18, :index 18}}}
   ;;                #:alumbra{:field-name \"name\",
   ;;                          :metadata {:row 0, :column 24, :index 24},
   ;;                          :type #:alumbra{:type-class :named-type,
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
