# alumbra.parser

An [ANTLR4][antlr]-based [GraphQL][ql] parser for Clojure.

[![Build Status](https://travis-ci.org/alumbra/alumbra.parser.svg?branch=master)](https://travis-ci.org/alumbra/alumbra.parser)
[![Clojars Project](https://img.shields.io/clojars/v/alumbra/parser.svg)](https://clojars.org/alumbra/parser)

[antlr]: http://www.antlr.org/
[ql]: http://graphql.org/

## Usage

There are two different parsers – one for GraphQL query documents (as specified
[here][query-spec]), as well as one for GraphQL type system definitions. The
latter ones do not seem to have a specification yet, so the parser
implementation is currently based on the official "Schema and Types" guide
available [here][schema-guide].

```clojure
(require '[alumbra.parser :as graphql])
```

The data produced by both parsers conforms to the specs given in
[alumbra.spec][alumbra-spec].

[query-spec]: https://facebook.github.io/graphql/#sec-Appendix-Grammar-Summary
[schema-guide]: http://graphql.org/learn/schema/
[alumbra-spec]: https://github.com/alumbra/alumbra.spec

### Query Documents

__[Documentation](https://alumbra.github.io/alumbra.parser/alumbra.parser.html#var-parse-document)__

```clojure
(graphql/parse-document
  "query People($limit: Int = 10, $offset: Int = 0) {
     people(limit: $limit, offset: $offset) {
       name
       friends { name }
     }
   }")
;; => #:alumbra{:operations
;;              [#:alumbra{:operation-type "query",
;;                         :metadata {:row 0, :column 0, :index 0},
;;                         :operation-name "People",
;;                         ...}]}
```

The resulting AST will conform to the spec `:alumbra/document`.

### Type System

__[Documentation](https://alumbra.github.io/alumbra.parser/alumbra.parser.html#var-parse-schema)__

```clojure
(graphql/parse-schema
  "type Person {
     name: String!
   }

   type QueryRoot {
     people(limit: Int, offset: Int): [Person]
   }")
;; => [#:alumbra{:type-name "Person",
;;               :metadata {:row 0, :column 5, :index 5},
;;               :type-fields [#:alumbra{:field-name "name",
;;                                       :metadata {:row 1, :column 6, :index 20},
;;                                       ...}]}]
```

The resulting AST will conform to the spec `:alumbra/schema`.

### Tests

This project uses [alumbra.generators][gens] and [test.check][tc] to verify
parser functionality. You can run all tests using:

```
$ lein test
```

[gens]: https://github.com/alumbra/alumbra.generators
[tc]: https://github.com/clojure/test.check

## License

```
MIT License

Copyright (c) 2016 Yannick Scherer

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
