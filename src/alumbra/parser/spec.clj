(ns alumbra.parser.spec
  (:require [alumbra.parser
             [document :as document]
             [schema :as schema]]
            [alumbra.parser :as parser]
            [alumbra.spec :as alumbra]
            [clojure.spec :as s]))

;; ## Transformations

(s/fdef document/transform
        :args (s/cat :ast sequential?)
        :ret  :alumbra/document)

(s/fdef schema/transform
        :args (s/cat :ast sequential?)
        :ret  :alumbra/schema)

;; ## Parsers

(s/def parser/parse-document
  ::alumbra/document-parser)

(s/def parser/parse-document
  ::alumbra/schema-parser)
