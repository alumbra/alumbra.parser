# alumbra.parser

An [ANTLR4][antlr]-based [GraphQL] parser for Clojure.

[![Build Status](https://travis-ci.org/alumbra/alumbra.parser.svg?branch=master)](https://travis-ci.org/alumbra/alumbra.parser)
[![Clojars Project](https://img.shields.io/clojars/v/alumbra/parser.svg)](https://clojars.org/alumbra/parser)

[antlr]: http://www.antlr.org/
[ql]: http://graphql.org/

## Usage

TODO

## Tests

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
