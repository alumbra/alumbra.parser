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
        :ret  :graphql/document)

(s/fdef schema/transform
        :args (s/cat :ast sequential?)
        :ret  :graphql/schema)

;; ## Parsers

(s/fdef parser/parse-document
        :args (s/alt :string string?
                     :stream stream?)
        :ret (s/alt :document :graphql/document
                    :error     parser/error?))

(s/fdef parser/parse-schema
        :args (s/alt :string string?
                     :stream stream?)
        :ret (s/alt :document :graphql/schema
                    :error     parser/error?))
