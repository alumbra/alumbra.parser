/*
   The MIT License (MIT)

   Copyright (c) 2015 Joseph T. McBride

   Permission is hereby granted, free of charge, to any person obtaining a copy
   of this software and associated documentation files (the "Software"), to
   deal in the Software without restriction, including without limitation the
   rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
   sell copies of the Software, and to permit persons to whom the Software is
   furnished to do so, subject to the following conditions:

   The above copyright notice and this permission notice shall be included in
   all copies or substantial portions of the Software.

   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
   SOFTWARE.
*/

/*
    GraphQL grammar derived from:

       * GraphQL Draft Specification, April 2016
       * https://github.com/graphql-dotnet/graphql-dotnet/ \
            blob/620151b8420d897cf83bb5c1fef5d6a2a91daea5/ \
            src/GraphQL/GraphQL.g4
*/

grammar GraphQL;

document
    : definition+
    ;

definition
    : operationDefinition
    | fragmentDefinition
    ;

operationDefinition
    : selectionSet
    | operationType  NAME? variableDefinitions? directives? selectionSet
    ;

operationType
    : 'query'
    | 'mutation'
    | 'subscription'
    ;

variableDefinitions
    : '(' variableDefinition+ ')'
    ;

variableDefinition
    : variable ':' type defaultValue?
    ;

variable
    : '$' NAME
    ;

defaultValue
    : '=' value
    ;

selectionSet
    :  '{' selection+ '}'
    ;

selection
    : field
    | fragmentSpread
    | inlineFragment
    ;

field
    : alias? NAME arguments? directives? selectionSet?
    ;

alias
    : NAME ':'
    ;

arguments
    : '(' argument+ ')'
    ;

argument
    : NAME ':' valueWithVariable
    ;

fragmentSpread
    : '...' fragmentName directives?
    ;

inlineFragment
    : '...' typeCondition? directives? selectionSet
    ;

fragmentDefinition
    : 'fragment' fragmentName typeCondition directives? selectionSet
    ;

fragmentName
    :  NAME
    ;

typeCondition
    : 'on' typeName
    ;

value
    : IntValue
    | FloatValue
    | StringValue
    | BooleanValue
    | enumValue
    | arrayValue
    | objectValue;

valueWithVariable
    : variable
    | IntValue
    | FloatValue
    | StringValue
    | BooleanValue
    | enumValue
    | arrayValueWithVariable
    | objectValueWithVariable;

enumValue
    : NAME
    ;

arrayValue
    : '[' value* ']'
    ;

arrayValueWithVariable
    : '[' valueWithVariable* ']'
    ;

objectValue
    : '{' objectField* '}'
    ;

objectValueWithVariable
    : '{' objectFieldWithVariable* '}'
    ;

objectField
    : NAME ':' value
    ;

objectFieldWithVariable
    : NAME ':' valueWithVariable
    ;

directives
    : directive+
    ;

directive
    : '@' NAME arguments?
    ;

type
    : typeName
    | listType
    | nonNullType
    ;

typeName
    : NAME;

listType
    : '[' type ']'
    ;

nonNullType
    : typeName '!'
    | listType '!'
    ;

// --------------- TOKENS ---------------

BooleanValue
    : 'true'
    | 'false';

name
    : NAME
    | 'on'
    | 'fragment'
    | 'query'
    | 'mutation'
    | 'subscription'
    ;

NAME
    : [_A-Za-z][_0-9A-Za-z]*
    ;

IntValue
    : Sign? IntegerPart
    ;

FloatValue
    : Sign? IntegerPart ('.' Digit+)? ExponentPart?
    ;

Sign
    : '-'
    | '+';

IntegerPart
    : '0'
    | NonZeroDigit
    | NonZeroDigit Digit+
    ;

NonZeroDigit
    : '1'.. '9'
    ;

ExponentPart
    : ('e'|'E') Sign? Digit+
    ;

Digit
    : '0'..'9'
    ;

StringValue
    : '"' (~(["\\\n\r\u2028\u2029])|EscapedChar)* '"'
    ;

fragment EscapedChar
    :   '\\' (["\\/bfnrt] | Unicode)
    ;
fragment Unicode
    : 'u' Hex Hex Hex Hex
    ;

fragment Hex
    : [0-9a-fA-F]
    ;

// --------------- IGNORED ---------------

Ignored
    : (Whitspace|Comma|LineTerminator|Comment) -> skip
    ;

fragment Comment
    : '#' ~[\n\r\u2028\u2029]*
    ;

fragment LineTerminator
    : [\n\r\u2028\u2029]
    ;

fragment Whitspace
    : [\t\u000b\f\u0020\u00a0]
    ;

fragment Comma
    : ','
    ;
