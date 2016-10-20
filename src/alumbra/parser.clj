(ns alumbra.parser
  "GraphQL Parsing API"
  (:require [alumbra.parser
             [antlr :as antlr]
             [document :as document]]
            [alumbra.spec document]
            [clojure.spec :as s]))

;; ## Public API

(defn error?
  "Check whether the given value represents a parser error produced by
   [[parse]]."
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

   If the parser fails, an error container is returned that can be checked for
   using [[error?]]."
  [document]
  (let [result (document/parse document)]
    (if-not (error? result)
      (document/transform result)
      result)))

;; ## Specs

(s/fdef parse
        :args (s/alt :string string?)
        :ret (s/alt :document :graphql/document
                    :error     error?))
