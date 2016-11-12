(ns alumbra.parser.spec
  (:require [alumbra.parser
             [document :as document]
             [schema :as schema]]
            [alumbra.parser :as parser]
            [alumbra.spec]
            [clojure.spec :as s]))

;; ## Helper

(defn- stream?
  [value]
  (instance? java.io.InputStream value))

;; ## Transformations

(s/fdef document/transform
        :args (s/cat :ast sequential?)
        :ret  :alumbra/document)

(s/fdef schema/transform
        :args (s/cat :ast sequential?)
        :ret  :alumbra/schema)

;; ## Parsers

(s/def ::parser-errors
  (s/keys :req [:alumbra/parser-errors]))

(s/fdef parser/parse-document
        :args (s/alt :string string?
                     :stream stream?)
        :ret (s/alt :document :alumbra/document
                    :error     ::parser-errors))

(s/fdef parser/parse-schema
        :args (s/alt :string string?
                     :stream stream?)
        :ret (s/alt :document :alumbra/schema
                    :error     ::parser-errors))
