(ns alumbra.parser
  "GraphQL Parsing API"
  (:require [alumbra.parser
             [antlr :as antlr]
             [document :as document]
             [schema :as schema]]))

;; ## Public API

(defn- error-location
  [{:keys [token line char]}]
  (if token
    (let [{:keys [antlr/row antlr/column antlr/index]}
          (antlr/token->position token :start)]
      {:row row, :column column, :index index})
    {:row (dec line), :column char}))

(defn- collect-errors
  [result]
  {:alumbra/parser-errors
   (for [{:keys [message] :as error} @result]
     {:alumbra/location      (error-location error)
      :alumbra/error-message message})})

(defn parse-document
  "Parse a GraphQL document and return its in-memory representation, conforming
   to the `:alumbra/document` spec.

   If the parser fails, an error map containing the key `:alumbra/parser-errors`
   is returned."
  [document]
  (let [result (document/parse document)]
    (if-not (antlr/error? result)
      (document/transform result)
      (collect-errors result))))

(defn parse-schema
  "Parse a GraphQL schema and return its in-memory representation, conforming
   to the `:alumbra/schema` spec.

   If the parser fails, an error map containing the key `:alumbra/parser-errors`
   is returned."
  [document]
  (let [result (schema/parse document)]
    (if-not (antlr/error? result)
      (schema/transform result)
      (collect-errors result))))
