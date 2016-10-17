(ns alumbra.parser.antlr
  (:require [clj-antlr.core :as antlr]
            [clojure.java.io :as io])
  (:import [clj_antlr ParseError]))

;; ## Print Helper

(defmethod print-method ParseError
  [^ParseError value ^java.io.Writer w]
  (.write w "#<clj_antlr.ParseError ")
  (print-method @value w)
  (.write w ">"))

;; ## Parser + Error Container

(def parser
  (-> "alumbra/GraphQL.g4"
      (io/resource)
      (slurp)
      (antlr/parser
        {:root "document"})))

(defn parse
  "Parse a GraphQL document."
  [document]
  (try
    (parser document)
    (catch ParseError e
      e)))

(defn error?
  "Check whether the given value represents a parser failure
   (`clj_antlr.ParseError`)."
  [value]
  (instance? ParseError value))
