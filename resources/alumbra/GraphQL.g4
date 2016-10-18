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
    | operationType  operationName? variableDefinitions? directives? selectionSet
    ;

operationType
    : K_QUERY
    | K_MUTATION
    | K_SUBSCRIPTION
    ;

operationName
    : NAME
    ;

variableDefinitions
    : '(' variableDefinition+ ')'
    ;

variableDefinition
    : variable ':' type defaultValue?
    ;

variable
    : '$' anyName
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
    : fieldAlias? fieldName arguments? directives? selectionSet?
    ;

fieldAlias
    : anyName  ':'
    ;

fieldName
    : anyName
    ;

arguments
    : '(' argument+ ')'
    ;

argument
    : anyName ':' valueWithVariable
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

typeCondition
    : 'on' typeName
    ;

value
    : intValue
    | floatValue
    | StringValue
    | booleanValue
    | enumValue
    | arrayValue
    | objectValue;

valueWithVariable
    : variable
    | intValue
    | floatValue
    | StringValue
    | booleanValue
    | enumValue
    | arrayValueWithVariable
    | objectValueWithVariable;

intValue
    : IntValue
    ;

floatValue
    : FloatValue
    ;

booleanValue
    : BooleanValue
    ;

enumValue
    : anyName
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
    : anyName ':' value
    ;

objectFieldWithVariable
    : anyName ':' valueWithVariable
    ;

directives
    : directive+
    ;

directive
    : '@' anyName arguments?
    ;

type
    : typeName
    | listType
    | nonNullType
    ;

typeName
    : anyName;

listType
    : '[' type ']'
    ;

nonNullType
    : typeName '!'
    | listType '!'
    ;

// --------------- BOOLEAN---------------

BooleanValue
    : 'true'
    | 'false';

// --------------- NAMES ---------------

anyName
    : NAME
    | K_ON
    | K_FRAGMENT
    | K_QUERY
    | K_MUTATION
    | K_SUBSCRIPTION
    ;

fragmentName
    : NAME
    | K_FRAGMENT
    | K_QUERY
    | K_MUTATION
    | K_SUBSCRIPTION
    ;

K_ON : 'on' ;
K_FRAGMENT : 'fragment' ;
K_QUERY : 'query' ;
K_MUTATION : 'mutation' ;
K_SUBSCRIPTION : 'subscription' ;

NAME : [_A-Za-z][_0-9A-Za-z]* ;

// --------------- INTEGER ---------------

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

// --------------- STRING ---------------

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
