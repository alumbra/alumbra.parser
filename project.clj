(defproject alumbra/parser "0.1.6"
  :description "A GraphQL parser for Clojure using ANTLR4."
  :url "https://github.com/alumbra/alumbra.parser"
  :license {:name "MIT License"
            :url "https://opensource.org/licenses/MIT"
            :author "Yannick Scherer"
            :year 2016
            :key "mit"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha14" :scope "provided"]
                 [alumbra/spec "0.1.6" :scope "provided"]
                 [clj-antlr "0.2.4"]]
  :profiles {:dev {:dependencies [[org.clojure/test.check "0.9.0"]
                                  [alumbra/generators "0.2.2"]]}
             :codox {:plugins [[lein-codox "0.10.0"]]
                     :dependencies [[codox-theme-rdash "0.1.1"]]
                     :codox {:project {:name "alumbra.parser"}
                             :metadata {:doc/format :markdown}
                             :themes [:rdash]
                             :source-uri "https://github.com/alumbra/alumbra.parser/blob/v{version}/{filepath}#L{line}"
                             :namespaces [alumbra.parser]}}}
  :aliases {"codox" ["with-profile" "+codox" "codox"]}
  :pedantic? :abort)
