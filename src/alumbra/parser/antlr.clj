(ns alumbra.parser.antlr
  (:require [clj-antlr.core :as antlr]
            [clj-antlr.common :as antlr-common]
            [clojure.java.io :as io])
  (:import [clj_antlr ParseError]
           [org.antlr.v4.runtime.tree ParseTree]
           [org.antlr.v4.runtime Parser ParserRuleContext Token]))

;; ## Print Helper

(defmethod print-method ParseError
  [^ParseError value ^java.io.Writer w]
  (.write w "#<clj_antlr.ParseError ")
  (print-method @value w)
  (.write w ">"))

;; ## Coercion

(defn- sexpr-key
  [^ParserRuleContext t ^Parser p]
  (->> (.getRuleIndex t)
       (antlr-common/parser-rule-name p)
       (antlr-common/fast-keyword)))

(defn- token->position
  [^Token t k]
  {:antlr/row    (dec (.getLine t))
   :antlr/column (.getCharPositionInLine t)
   :antlr/index  (if (= k :start)
                   (.getStartIndex t)
                   (.getStopIndex t))})

(defn- attach-meta
  [^ParserRuleContext t sexpr]
  (let [^Token start-token (.getStart t)
        ^Token stop-token (.getStop t)]
    (->> {:antlr/start (token->position start-token :start)
          :antlr/stop (token->position stop-token :stop)}
         (with-meta sexpr))))

(defn- sexpr-with-meta
  [^ParseTree t ^Parser p]
  (if (instance? ParserRuleContext t)
    (->> (antlr-common/children t)
         (mapv #(sexpr-with-meta % p))
         (cons (sexpr-key t p))
         (attach-meta t))
    (.getText t)))

(defn- tree->sexpr-with-meta
  [{:keys [tree parser errors]}]
  (if (seq errors)
    (antlr-common/parse-error errors tree)
    (sexpr-with-meta tree parser)))

;; ## Parser + Error Container

(def parser
  (-> "alumbra/GraphQL.g4"
      (io/resource)
      (slurp)
      (antlr/parser
        {:root   "document"
         :format tree->sexpr-with-meta})))

(defn parse
  "Parse a GraphQL document."
  [document]
  (parser document))

(defn error?
  "Check whether the given value represents a parser failure
   (`clj_antlr.ParseError`)."
  [value]
  (instance? ParseError value))
