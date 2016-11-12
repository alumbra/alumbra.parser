(ns alumbra.parser-test
  (:require [clojure.test.check
             [clojure-test :refer [defspec]]
             [generators :as gen]
             [properties :as prop]]
            [alumbra.parser :as parser]
            [clojure.spec :as s]
            alumbra.parser.spec
            alumbra.spec))

(defspec t-document-parser-errors-conform-to-spec 50
  (prop/for-all
    [nonsense gen/string-ascii]
    (let [{:keys [alumbra/parser-errors]} (parser/parse-document nonsense)]
      (or (nil? parser-errors)
          (s/valid? :alumbra/parser-errors parser-errors)))))

(defspec t-schema-parser-errors-conform-to-spec 50
  (prop/for-all
    [nonsense gen/string-ascii]
    (let [{:keys [alumbra/parser-errors]} (parser/parse-schema nonsense)]
      (or (nil? parser-errors)
          (s/valid? :alumbra/parser-errors parser-errors)))))
