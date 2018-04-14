/*
   The MIT License (MIT)

   Copyright (c) 2016 Yannick Scherer
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
    GraphQL schema grammar derived from:

       * GraphQL Draft Specification, April 2016
       * http://graphql.org/learn/schema/
*/

grammar GraphQLSchema;

schema
    : definition+
    ;

definition
    : typeDefinition
    | interfaceDefinition
    | schemaDefinition
    | enumDefinition
    | unionDefinition
    | inputTypeDefinition
    | directiveDefinition
    | scalarDefinition
    | typeExtensionDefinition
    ;

// --------------- OUTPUT TYPE ---------------

typeDefinition
    : K_TYPE typeName typeImplements? directives? typeDefinitionFields
    ;

typeDefinitionFields
    : '{' typeDefinitionField+ '}'
    ;

typeImplements
    : K_IMPLEMENTS typeImplementsTypes
    ;

typeImplementsTypes
    : typeName+
    ;

typeDefinitionField
    : fieldName arguments? directives? ':' typeDefinitionFieldType directives?
    ;

typeDefinitionFieldType
    : type
    ;

fieldName
    : anyName
    ;

arguments
    : '(' argument+ ')'
    ;

argument
    : argumentName directives? ':' argumentType defaultValue?
    ;

argumentName
    : anyName
    ;

argumentType
    : type
    ;

defaultValue
    : '=' value
    ;

// --------------- INTERFACE ---------------

interfaceDefinition
    : K_INTERFACE typeName directives? typeDefinitionFields
    ;

// --------------- SCHEMA ---------------

schemaDefinition
    : K_SCHEMA directives? '{' schemaTypes '}'
    ;

schemaTypes
    : schemaType+
    ;

schemaType
    : (K_MUTATION | K_QUERY | K_SUBSCRIPTION) ':' typeName
    ;

// --------------- ENUM ---------------

enumDefinition
    : K_ENUM typeName directives? enumDefinitionFields
    ;

enumDefinitionFields
    : '{' enumDefinitionField+ '}'
    ;

enumDefinitionField
    : enumDefinitionFieldName enumDefinitionType?
    ;

enumDefinitionFieldName
    : anyName
    ;

enumDefinitionType
    : '@' K_ENUM K_ENUM_INT  '(' K_VALUE ':' enumDefinitionIntValue ')'
    ;

enumDefinitionIntValue
    : intValue
    ;

// --------------- UNION ---------------

unionDefinition
    : K_UNION typeName '=' unionDefinitionTypes directives?
    ;

unionDefinitionTypes
    : typeName ( '|' typeName )*
    ;

// --------------- INPUT TYPE ---------------

inputTypeDefinition
    : K_INPUT typeName directives? inputTypeDefinitionFields
    ;

inputTypeDefinitionFields
    : '{' inputTypeDefinitionField+ '}'
    ;

inputTypeDefinitionField
    : fieldName directives? ':' inputTypeDefinitionFieldType directives?
    ;

inputTypeDefinitionFieldType
    : type
    ;

// --------------- DIRECTIVES ---------------

directiveDefinition
    : K_DIRECTIVE directiveName arguments? 'on' directiveLocations
    ;

directiveName
    : '@' anyName
    ;

directiveLocations
    : directiveLocation+
    ;

directiveLocation
    : DIRECTIVE_LOCATION
    ;

// --------------- EXTEND TYPE ---------------

typeExtensionDefinition
    : K_EXTEND K_TYPE typeName typeImplements? typeDefinitionFields
    ;

// --------------- SCALAR ---------------

scalarDefinition
    : K_SCALAR typeName
    ;

// --------------- TYPES ---------------

type
    : namedType
    | listType
    | nonNullType
    ;

namedType
    : typeName
    ;

typeName
    : anyName;

listType
    : '[' type ']'
    ;

nonNullType
    : namedType '!'
    | listType '!'
    ;

typeCondition
    : K_ON typeName
    ;

// --------------- VALUES ---------------

value
    : intValue
    | floatValue
    | booleanValue
    | stringValue
    | enumValue
    | arrayValue
    | objectValue
    | nullValue
    ;

intValue
    : IntValue
    ;

floatValue
    : FloatValue
    ;

booleanValue
    : BooleanValue
    ;

stringValue
    : StringValue
    ;

enumValue
    : enumValueName
    ;

arrayValue
    : '[' value* ']'
    ;

objectValue
    : '{' objectField* '}'
    ;

objectField
    : fieldName ':' objectFieldValue
    ;

objectFieldValue
    : value
    ;

nullValue
    : NullValue
    ;

// ------------- DIRECTIVES ------------

directives
    : directive+
    ;

directive
    : directiveName directiveArguments?
    ;

directiveArguments
    : '(' directiveArgument+ ')'
    ;

directiveArgument
    : argumentName ':' directiveArgumentValue
    ;

directiveArgumentValue
    : value
    ;

// --------------- BOOLEAN---------------

BooleanValue
    : K_TRUE
    | K_FALSE;

// --------------- NULL ---------------

NullValue
    : K_NULL
    ;

// --------------- NAMES ---------------

anyName
    : nameTokens
    | K_TRUE
    | K_FALSE
    | K_NULL
    ;

enumValueName
    : nameTokens
    ;

nameTokens
    : NAME
    | DIRECTIVE_LOCATION
    | K_TYPE
    | K_IMPLEMENTS
    | K_INTERFACE
    | K_SCHEMA
    | K_ENUM
    | K_ENUM_INT
    | K_UNION
    | K_INPUT
    | K_DIRECTIVE
    | K_EXTEND
    | K_SCALAR
    | K_ON
    | K_FRAGMENT
    | K_QUERY
    | K_MUTATION
    | K_SUBSCRIPTION
    | K_VALUE
    ;

DIRECTIVE_LOCATION
    : 'QUERY'
    | 'MUTATION'
    | 'SUBSCRIPTION'
    | 'FIELD'
    | 'FRAGMENT_DEFINITION'
    | 'FRAGMENT_SPREAD'
    | 'INLINE_FRAGMENT'
    | 'SCHEMA'
    | 'SCALAR'
    | 'OBJECT'
    | 'FIELD_DEFINITION'
    | 'ARGUMENT_DEFINITION'
    | 'INTERFACE'
    | 'UNION'
    | 'ENUM'
    | 'ENUM_VALUE'
    | 'INPUT_OBJECT'
    | 'INPUT_FIELD_DEFINITION'
    ;

K_TYPE         : 'type'         ;
K_IMPLEMENTS   : 'implements'   ;
K_INTERFACE    : 'interface'    ;
K_SCHEMA       : 'schema'       ;
K_ENUM         : 'enum'         ;
K_ENUM_INT     : 'Int'          ;
K_UNION        : 'union'        ;
K_INPUT        : 'input'        ;
K_DIRECTIVE    : 'directive'    ;
K_EXTEND       : 'extend'       ;
K_SCALAR       : 'scalar'       ;
K_ON           : 'on'           ;
K_FRAGMENT     : 'fragment'     ;
K_QUERY        : 'query'        ;
K_MUTATION     : 'mutation'     ;
K_SUBSCRIPTION : 'subscription' ;
K_VALUE        : 'value'        ;
K_TRUE         : 'true'         ;
K_FALSE        : 'false'        ;
K_NULL         : 'null'         ;

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
    : (Whitspace|Comma|LineTerminator|Comment) -> channel(HIDDEN)
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
