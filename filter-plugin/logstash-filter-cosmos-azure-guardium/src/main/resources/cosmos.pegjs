// Reference: https://docs.microsoft.com/en-us/azure/cosmos-db/sql-query-select

{
  function buildBinaryExpression(head, tail) {
    return tail.reduce((left, [, operator, , right]) => ({
      type: 'scalar_binary_expression',
      left,
      operator,
      right
    }), head)
  }
}

sql 
  = _ body:select_query _
      {
        return {
          type: 'sql',
          body
        }
      }

select_query
  = select _
    top:(top _ v:top_specification { return v })? _
    distinct:distinct? _
    select:select_specification _
    from:(from _ v:from_specification { return v })? _
    where:(where _ v:filter_condition { return v })? _
    orderBy:(order _ by _ v:sort_specification { return v })? _
    groupBy:(group _ by _ v:group_specification { return v })?
    {
      return {
        type: 'select_query',
        top,
        select,
        from,
        where,
        orderBy
      }
    }

select_specification
  = '*'
    {
      return {
        type: 'select_specification',
        '*': true
      }
    }
  / properties:object_property_list
    {
      return {
        type: 'select_specification',
        properties
      }
    }
  / value _ value:scalar_expression
    {
      return {
        type: 'select_specification',
        value
      }
    }

object_property_list
  = head:object_property
    tail:(_ "," _ v:object_property { return v })*
    {
      return {
        type: 'object_property_list',
        properties: [head, ...tail]
      }
    }

from_specification
  = source:from_source joins:(_ join _ v:(from_source) { return v })*
    {
      return {
        type: 'from_specification',
        source,
        joins
      }
    }

from_source
  = alias:identifier _ in _ expression:collection_expression
    {
      return {
        type: 'from_source',
        expression,
        alias,
        iteration: true
      }
    }
  / expression:collection_expression alias:((_ as)? _ v:identifier { return v })?
    {
      return {
        type: 'from_source',
        expression,
        alias
      }
    }

collection_expression
  = collection_member_expression
  / collection_primary_expression
  / collection_subquery_expression

filter_condition
  = condition:scalar_expression
    {
      return {
        type: 'filter_condition',
        condition
      }
    }

sort_specification
  = head:sort_expression tail:(_ "," _ v:sort_expression { return v })*
    {
      return {
        type: 'sort_specification',
        expressions: [head, ...tail]
      }
    }

sort_expression
  = expression:scalar_expression order:(_ v:(asc / desc) { return v })?
    {
      return {
        type: 'sort_expression',
        expression,
        order
      }
    }
    
group_specification
  = head:scalar_expression tail:(_ "," _ v:scalar_expression { return v })*
    {
      return {
        type: 'group_specification',
        expressions: [head, ...tail]
      }
    }


scalar_expression
  = scalar_conditional_expression

scalar_function_expression
  = udf _ "." _ name:identifier _ "(" _ args:scalar_expression_list  _ ")"
    {
      return {
        type: 'scalar_function_expression',
        name,
        arguments: args,
        udf: true
      }
    }
  / name:identifier _ "(" _ args:scalar_expression_list  _ ")"
    {
      return {
        type: 'scalar_function_expression',
        name,
        arguments: args
      }
    }

scalar_object_expression
  = "{" _
    head:scalar_object_element_property?
    tail:(_ "," _ v:scalar_object_element_property { return v })*
    _ "}"
    {
      return {
        type: "scalar_object_expression",
        properties: head ? [head, ...tail] : []
      }
    }

scalar_array_expression
  = "[" _ elements:scalar_expression_list _ "]"
    {
      return {
        type: "scalar_array_expression",
        elements
      }
    }

constant
  = undefined_constant
  / null_constant
  / boolean_constant
  / number_constant
  / string_constant
  / array_constant
  / object_constant

undefined_constant
  = "undefined"
    { return { type: 'undefined_constant' } }

null_constant
  = null
    { return { type: 'null_constant' } }

boolean_constant
  = false
    {
      return {
        type: 'boolean_constant',
        value: false
      }
    }
  / true
    {
      return {
        type: 'boolean_constant',
        value: true
      }
    }

number_constant
  = "-"? hex:"0x"? [0-9]+ ("." [0-9]+)? {
    return {
      type: "number_constant",
      // FIXME: support hex with float?
      value: hex ? parseInt(text(), 16) : parseFloat(text())
    }
  }

string_constant
  = '"' chars:double_string_character* '"'
    {
      return {
        type: "string_constant",
        value: chars.join("")
      }
    }
  / "'" chars:single_string_character* "'"
    {
      return {
        type: "string_constant",
        value: chars.join("")
      }
    }

array_constant
  = "[" _ head:constant tail:(_ "," _ v:constant { return v })* _ "]"
    {
      return {
        type: "array_constant",
        elements: [head, ...tail]
      }
    }

object_constant
  = "{" _
    head:object_constant_property
    tail:(_ "," _ v:object_constant_property { return v })*
    _ "}"
    {
      return {
        type: "object_constant",
        properties: [head, ...tail]
      }
    }

// by us
_ = (whitespace / comment)*

whitespace
  = [ \t\n\r]

comment
  = "--" (![\n\r] source_character)*

select = "SELECT"i !identifier_start
top = "TOP"i !identifier_start
distinct = "DISTINCT"i !identifier_start
from = "FROM"i !identifier_start
where = "WHERE"i !identifier_start
order = "ORDER"i !identifier_start
by = "BY"i !identifier_start
group = "GROUP"i !identifier_start
as = "AS"i !identifier_start
join = "JOIN"i !identifier_start
in = "IN"i !identifier_start
value = "VALUE"i !identifier_start
asc = "ASC"i !identifier_start { return "ASC" }
desc = "DESC"i !identifier_start { return "DESC" }
and = "AND"i !identifier_start { return "AND" }
or = "OR"i !identifier_start { return "OR" }
not = "NOT"i !identifier_start { return "NOT" }
between = "BETWEEN"i !identifier_start
exists = "EXISTS"i !identifier_start
array = "ARRAY"i !identifier_start
null = "null" !identifier_start
true = "true" !identifier_start
false = "false" !identifier_start
udf = "udf" !identifier_start

reserved
  = select
  / top
  / distinct
  / from
  / where
  / order
  / by
  / group
  / as
  / join
  / in
  / value
  / asc
  / desc
  / and
  / or
  / not
  / between
  / exists
  / array
  / null
  / true
  / false
  / udf

identifier
  = !reserved name:identifier_name
    {
      return {
        type: 'identifier',
        name
      }
    }

identifier_start
  = [a-zA-Z_]

identifier_name
  = head:identifier_start tail:[a-zA-Z0-9_]*
    { return head + tail.join('') }

parameter_name
  = "@" identifier_name
    {
      return {
        type: 'parameter_name',
        name: text()
      }
    }

array_index
  = unsigned_integer

unary_operator
  = "+"
  / "-"
  / "~"
  / not

double_string_character
  = !('"' / "\\") source_character { return text(); }
  / "\\" seq:escape_sequence { return seq }

single_string_character
  = !("'" / "\\") source_character { return text(); }
  / "\\" seq:escape_sequence { return seq }

source_character
  = .

escape_sequence
  = charactor_escape_sequence
  / unicode_escape_sequence

charactor_escape_sequence
  = single_escape_character
  / non_escape_character

single_escape_character
  = "'"
  / '"'
  / "\\"
  / "b" { return "\b" }
  / "f" { return "\f" }
  / "n" { return "\n" }
  / "r" { return "\r" }
  / "t" { return "\t" }

non_escape_character
  =  !(escape_character) source_character
     { return text() }

escape_character
  = single_escape_character
  / "u"

unicode_escape_sequence
  = "u" digits:$(hex_digit hex_digit hex_digit hex_digit)
    { return String.fromCharCode(parseInt(digits, 16)) }

hex_digit
  = [0-9a-f]i

object_property
  = property:scalar_expression alias:((_ as)? _ v:identifier { return v })?
    { return { property, alias } }

scalar_primary_expression
  = identifier
  / parameter_name
  / constant
  / scalar_array_expression
  / scalar_object_expression
  / subquery_expression
  / "(" _ expression:scalar_expression _ ")"
    { return expression }

subquery_expression
  = array_subquery_expression
  / exists_subquery_expression
  / scalar_subquery_expression

array_subquery_expression
  = array _ expression:subquery
    {
      return {
        type: "array_subquery_expression",
        expression
      }
    }

exists_subquery_expression
  = exists _ expression:subquery
    {
      return {
        type: 'exists_subquery_expression',
        expression
      }
    }

scalar_subquery_expression
  = expression:subquery
    {
      return {
        type: "scalar_subquery_expression",
        expression
      }
    }

scalar_member_expression
  = head:scalar_primary_expression
    tail:(
      _ "." _ property:identifier _
      { return { property, computed: false } }
    / _ "[" _ property:(string_constant / array_index / parameter_name) _ "]"
      { return { property, computed: true } }
    )*
    {
      return tail.reduce((object, { property, computed }) => ({
        type: 'scalar_member_expression',
        object,
        property,
        computed
      }), head)	
    }

scalar_unary_expression
  = scalar_function_expression
  / scalar_member_expression
  / operator:unary_operator _ argument:scalar_unary_expression
    {
      return {
        type: 'scalar_unary_expression',
        operator,
        argument
      }
    }

scalar_conditional_expression
  = test:(scalar_binary_or_expression) _ "?" _
    consequent:(scalar_conditional_expression) _ ":" _
    alternate:(scalar_conditional_expression)
    {
      return {
        type: 'scalar_conditional_expression',
        test,
        consequent,
        alternate
      }
    }
  / scalar_binary_or_expression

scalar_binary_or_expression
  = head:(scalar_binary_and_expression)
    tail:(_ (or / "??") _ scalar_binary_and_expression)*
    { return buildBinaryExpression(head, tail) }

scalar_binary_and_expression
  = head:(scalar_binary_equality_expression)
    tail:(_ and _ scalar_binary_equality_expression)*
    { return buildBinaryExpression(head, tail) }

scalar_binary_equality_expression
  = head:(scalar_binary_relational_expression)
    tail:(_ ("=" / "!=" / "<>") _ scalar_binary_relational_expression)*
    { return buildBinaryExpression(head, tail) }

scalar_binary_relational_expression
  = head:(scalar_in_expression)
    tail:(_ ("<=" / ">=" / "<" / ">") _ scalar_in_expression)*
    { return buildBinaryExpression(head, tail) }

scalar_in_expression
  = value:scalar_between_expression _ in _ "(" _ list:scalar_expression_list _")"
    {
      return {
        type: 'scalar_in_expression',
        value,
        list
      }
    }
  / scalar_between_expression

scalar_between_expression
  = value:scalar_binary_bitwise_or_expression _ between _ begin:scalar_binary_bitwise_or_expression _ and _ end:scalar_binary_bitwise_or_expression
    {
      return {
        type: 'scalar_between_expression',
        value,
        begin,
        end
      }
    }
  / scalar_binary_bitwise_or_expression

scalar_binary_bitwise_or_expression
  = head:(scalar_binary_bitwise_xor_expression)
    tail:(_ "|" _ scalar_binary_bitwise_xor_expression)*
    { return buildBinaryExpression(head, tail) }

scalar_binary_bitwise_xor_expression
  = head:(scalar_binary_bitwise_and_expression)
    tail:(_ "^" _ scalar_binary_bitwise_and_expression)*
    { return buildBinaryExpression(head, tail) }

scalar_binary_bitwise_and_expression
  = head:(scalar_binary_shift_expression)
    tail:(_ "&" _ scalar_binary_shift_expression)*
    { return buildBinaryExpression(head, tail) }

scalar_binary_shift_expression
  = head:(scalar_binary_additive_expression)
    tail:(_ ("<<" / ">>>" / ">>") _ scalar_binary_additive_expression)*
    { return buildBinaryExpression(head, tail) }

scalar_binary_additive_expression
  = head:(scalar_binary_multiplicative_expression)
    tail:(_ ("+" / "-" / "||") _ scalar_binary_multiplicative_expression)*
    { return buildBinaryExpression(head, tail) }

scalar_binary_multiplicative_expression
  = head:(scalar_unary_expression)
    tail:(_ ("*" / "/" / "%") _ scalar_unary_expression)*
    { return buildBinaryExpression(head, tail) }

scalar_object_element_property
  = key:(identifier / string_constant) _ ":" _ value:scalar_expression
    { return { key, value } }

object_constant_property
  = key:(identifier / string_constant) _ ":" _ value:constant
    { return { key, value } }

collection_primary_expression
  = expression:identifier
    {
      return {
        type: 'collection_expression',
        expression
      }
    }

collection_member_expression
  = head:collection_primary_expression
    tail:(
      _ "." _ property:identifier _ { return { property, computed: false } }
    / _ "[" _ property:(string_constant / array_index / parameter_name) _ "]"  { return { property, computed: true } }
    )+
    {
      return tail.reduce((object, { property, computed }) => ({
        type: 'collection_member_expression',
        object,
        property,
        computed
      }), head)
    }

collection_subquery_expression
  = expression:subquery
    {
      return {
        type: "collection_subquery_expression",
        expression
      }
    }

top_specification
  = value:(unsigned_integer / parameter_name)
    {
      return {
        type: 'top_specification',
        value
      }
    }

unsigned_integer
  = [0-9]+
    {
      return {
        type: 'number_constant',
        value: Number(text())
      }
    }

scalar_expression_list
  = head:scalar_expression? tail:(_ "," _ v:scalar_expression { return v })*
    { return head ? [head, ...tail] : [] }

subquery
  = "(" _ subquery:select_query _ ")"
    { return subquery }