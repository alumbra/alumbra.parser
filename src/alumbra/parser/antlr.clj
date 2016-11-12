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

(defn- sexpr-head
  [^ParserRuleContext t ^Parser p]
  (->> (.getRuleIndex t)
       (antlr-common/parser-rule-name p)
       (antlr-common/fast-keyword)))

(defn token->position
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

(defn- node->sexpr*
  [aliases ^ParseTree t ^Parser p]
  (if (instance? ParserRuleContext t)
    (let [k (sexpr-head t p)]
      (->> (antlr-common/children t)
           (mapv #(node->sexpr* aliases % p))
           (cons (get aliases k k))
           (attach-meta t)))
    (.getText t)))

(defn- node->sexpr
  [aliases {:keys [tree parser errors]}]
  (if (seq errors)
    (antlr-common/parse-error errors tree)
    (node->sexpr* aliases tree parser)))

;; ## Parser

(defn make-parser
  [{:keys [grammar root aliases]}]
  (-> grammar
      (io/resource)
      (slurp)
      (antlr/parser
        {:root   root
         :throw? false
         :format #(node->sexpr aliases %)})))

(defmacro defparser
  [sym docstring opts]
  `(let [f# (make-parser ~opts)]
     (defn ~sym
       ~docstring
       [~'data]
       (f# ~'data))))

;; ## Error Container

(defn error?
  "Check whether the given value represents a parser failure
   (`clj_antlr.ParseError`)."
  [value]
  (instance? ParseError value))
