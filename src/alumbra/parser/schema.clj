(ns alumbra.parser.schema
  (:require [alumbra.parser.antlr :as antlr]
            #_[alumbra.spec schema]))

;; ## Parser

(antlr/defparser parse
  "Parse a GraphQL schema."
  {:grammar "alumbra/GraphQLSchema.g4"
   :root    "schema"
   :aliases {}})
