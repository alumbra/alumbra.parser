(defproject alumbra/parser "0.1.0-SNAPSHOT"
  :description "A GraphQL parser for Clojure using ANTLR4."
  :url "https://github.com/xsc/alumbra.parser"
  :license {:name "MIT License"
            :url "https://opensource.org/licenses/MIT"
            :author "Yannick Scherer"
            :year 2016
            :key "mit"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha13"]
                 [org.clojure/test.check "0.9.0"]
                 [clj-antlr "0.2.3"]]
  :pedantic? :abort)
