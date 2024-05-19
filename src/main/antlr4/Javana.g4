grammar Javana;

@header {
package antlr4;
import java.util.HashMap;
import edu.yu.compilers.intermediate.symtable.SymTableEntry;
import edu.yu.compilers.intermediate.type.Typespec;
}

// Program and routines --------------------

program 
    : hdr=programHeader defs+=globalDefinitions* main=mainMethod defs+=globalDefinitions*
    ; //Done

programHeader locals [ SymTableEntry entry = null ]
    : 'Javana' name=identifier ':'
    ; //Done

mainMethod locals [ Typespec typeSpec = null, SymTableEntry entry = null ]
    : '@main' '(' args=mainArg? ')' body=blockStatement
    ; //Passes the torch to blockStataement

mainArg locals [ Typespec typeSpec = null, SymTableEntry entry = null ]
    : name=identifier ':' stringArrType
    ; //Ignoring, I always inputted the classic
    // "public static void main(String[] args)"

globalDefinitions locals [ Typespec typeSpec = null, SymTableEntry entry = null ]
    : nameDeclStatement # nameDecl
    | nameDeclDefStatement # nameDeclDef
    ; //Handled down stream, most ors are handled this way
    //will be referenced as HDS from now on

// Function Definitions and Declarations ---

funcDefinition locals [ Typespec typeSpec = null, SymTableEntry entry = null ]
    : proto=funcPrototype body=blockStatement
    ; //Done

funcPrototype locals [ Typespec typeSpec = null, SymTableEntry entry = null ]
    : 'func' name=identifier '(' argList+=funcArgList? ')' '->' return=returnType
    ; //Handled within funcDef

funcArgList locals [ Typespec typeSpec = null, SymTableEntry entry = null ]
    : args+=funcArgument (',' args+=funcArgument)*
    ; //Handled within funcDef

funcArgument locals [ Typespec typeSpec = null, SymTableEntry entry = null ]
    : typeAssoc
    ;//Handled within funcDef

returnType locals [ Typespec typeSpec = null, SymTableEntry entry = null ]
    : type
    | None
    ; //Handled within funcDef

// Name Definitions and Declarations -------

recordDecl locals [ Typespec typeSpec = null, SymTableEntry entry = null ]
    : 'record' name=identifier '{' fields+=typeAssoc* '}'
    ;//Done

variableDecl locals [ Typespec typeSpec = null, SymTableEntry entry = null ]
    : 'decl' assoc=typeAssoc
    ; //Handled DOWN stream, just by typeAssoc,
    //Since it will just visit children if no method
    //exists

typeAssoc locals [ Typespec typeSpec = null, SymTableEntry entry = null ]
    : namelst=nameList ':' t=type
    ; //Done

variableDef locals [ Typespec typeSpec = null, SymTableEntry entry = null]
    : 'var' namelst=nameList '=' expr=expression
    ; //Done

constantDef locals [ Typespec typeSpec = null, SymTableEntry entry = null]
    : 'const' namelst=nameList '=' expr=expression
    ; //Done

nameList locals [ Typespec typeSpec = null, SymTableEntry entry = null ]
    : names+=identifier (',' names+=identifier)*
    ;//Handled UP stream, means it is always dealt with
    //In places it has been called.


// Statements ------------------------------

statement
    : blockStatement
    | nameDeclStatement
    | nameDeclDefStatement
    | assignmentStatement
    | ifStatement
    | forStatement
    | whileStatement
    | expressionStatement
    | returnStatement
    | printStatement
    | printLineStatement
    ; //Handled due to "OR" statement

blockStatement
    : '{' stmts+=statement* '}'
    ; //Handled

nameDeclStatement locals [ Typespec typeSpec = null, SymTableEntry entry = null ]
    : variableDecl
    | recordDecl
    ; //Handled via OR

nameDeclDefStatement locals [ Typespec typeSpec = null, SymTableEntry entry = null]
    : variableDef
    | constantDef
    | funcDefinition
    ; //Handled via OR

assignmentStatement
    : var=variable '=' expr=expression
    ; //Done

variable locals [ SymTableEntry entry = null, Typespec typeSpec = null]
    : name=identifier modifiers+=varModifier*
    ; //Done

varModifier locals [ SymTableEntry entry = null, Typespec typeSpec = null]
    : arrIdxSpecifier
    | '.' identifier
    ; //Handled in variable

arrIdxSpecifier locals [ SymTableEntry entry = null, Typespec typeSpec = null]
    : '[' expr=expression ']'
    ; //Must be handled in other places, it is handled in variable

ifStatement
    : 'if' '(' condition=expression ')' thenStmt=blockStatement ('else' elseStmt=blockStatement)?
    ; //Done

forStatement
    : 'for' '(' init=variableDef ';' condition=expression ';' updateExpr=expression ')' body=blockStatement
    ; // Done

whileStatement
    : 'while' '(' condition=expression ')' body=blockStatement
    ; //Done

expressionStatement
    : expr=expression
    ; //Done

returnStatement locals [ Typespec typeSpec = null ]
    : 'return' expr=expression?
    ; //Done

printStatement
    : 'print' arg=printArgument
    ; //Done - I think

printLineStatement
    : 'println' arg=printArgument?
    ; //Done - I think

printArgument
    : expression        # PrintSingleValue
    | '(' exprList ')'  # FormattedPrint
    ; //Done - I think

// Expressions -----------------------------

expression locals [ Typespec typeSpec = null, SymTableEntry entry = null]
    : expr=expression arrIdx=arrIdxSpecifier                # ExprArrayElement
    | expr=expression '.' 'length()'                        # ExprStringLength
    | expr=expression '.' 'length'                          # ExprArrayLength
    | str=expression '.' 'charAt' '(' index=expression ')'  # ExprCharAt
    | 'Integer.parseInt(' expression ')'                    # ExprIntegerParseInt
    | lhs=expression '.equals(' rhs=expression ')'          # ExprDotEquals
    | expr=expression '.' name=identifier                   # ExprRecordField
    | lhs=expression op=HIGHER_ARITH_OP rhs=expression      # ExprHigherArith
    | lhs=expression op=ARITH_OP rhs=expression             # ExprArith
    | lhs=expression op=REL_OP rhs=expression               # ExprRelational
    | lhs=expression op=EQ_OP rhs=expression                # ExprEquality
    | lhs=expression op=HIGH_LOGIC_OP rhs=expression        # ExprHighLogical
    | lhs=expression op=LOW_LOGIC_OP rhs=expression         # ExprLowLogical
    | '!' expression                                        # ExprNot
    | '(' expression ')'                                    # ExprGroup
    | readCharCall                                          # ExprReadChar
    | readLineCall                                          # ExprReadLine
    | functionCall                                          # ExprFunctionCall //Done til here
    | variable                                              # ExprVariable //Will just visitVariable, which returns a String
    | literal                                               # ExprLiteral //Handled by literal
    | newArray                                              # ExprNewArray //Handled by newArray
    | newRecord                                             # ExprNewRecord //Handled by newArray
    ; //Make sure to return String instead of return null

exprList
    : exprs+=expression (',' exprs+=expression)*
    ; //Will be handled upStream, like name list

readCharCall
    : 'readch' '(' ')'
    ; //Done I think, semi-colon?

readLineCall
    : 'readln' '(' ')'
    ; //Done I think, semi-colon?

functionCall
    : name=identifier '(' args=exprList? ')'
    ; //Done

newArray locals [Typespec typeSpec= null ]
    : '@' t=arrayElemType arrId=arrIdxSpecifier
    ; //Done

arrayElemType locals [Typespec typeSpec= null ]
    : scalarType
    | identifier
    ; //Done

newRecord locals [Typespec typeSpec= null ]
    : '@' identifier '{' init=fieldInitList? '}'
    ; //Done

fieldInitList
    : init+=fieldInit (',' init+=fieldInit)*
    ; //Done

fieldInit
    : field=identifier '=' expr=expression
    ; //Done

literal locals [ Typespec typeSpec = null ]
    : INTEGER   # IntegerLiteral //Done
    | BOOL      # BooleanLiteral //Done
    | STRING    # StringLiteral //Done
    | None      # NoneValue //Done
    ;

// Types -----------------------------------

type locals [ Typespec typeSpec = null ]
    : scalarType    # TypeScalar //Done
    | compositeType # TypeComposite //Done
    ;

scalarType locals [ Typespec typeSpec = null ]
    : integerType
    | booleanType
    | stringType
    ; //Done

compositeType locals [ Typespec typeSpec = null ]
    : recordType
    | integerArrType
    | booleanArrType
    | stringArrType
    | recordArrType
    ; //Done

//Done
integerType locals [ Typespec typeSpec = null ] : INT_TYPE ;
booleanType locals [ Typespec typeSpec = null ] : BOOL_TYPE ;
stringType locals [ Typespec typeSpec = null ] : STR_TYPE ;
recordType locals [ Typespec typeSpec = null ] : identifier ;

//Done
integerArrType locals [ Typespec typeSpec = null ] : INT_ARR_TYPE ;
booleanArrType locals [ Typespec typeSpec = null ] : BOOL_ARR_TYPE ;
stringArrType locals [ Typespec typeSpec = null ] : STR_ARR_TYPE ;
recordArrType locals [ Typespec typeSpec = null ] : REC_ARR_TYPE ;

// Misc Rules

identifier locals [ SymTableEntry entry = null, Typespec typeSpec = null ]
    : IDENT
    ;

None
    : NULL_VALUE
    ;

// Lexer tokens

INT_ARR_TYPE  : INT_TYPE '[' ']' ;
BOOL_ARR_TYPE : BOOL_TYPE '[' ']' ;
STR_ARR_TYPE  : STR_TYPE '[' ']' ;
REC_ARR_TYPE  : IDENT '[' ']' ;

INT_TYPE  : 'int' ;
BOOL_TYPE : 'bool' ;
STR_TYPE  : 'string' ;

HIGHER_ARITH_OP : '*' | '/' | '%' ;
ARITH_OP        : '+' | '-' ;
REL_OP          : '<' | '>' | '<=' | '>=' ;
EQ_OP           : '==' | '!=' ;
HIGH_LOGIC_OP   : '&&' ;
LOW_LOGIC_OP    : '||' ;

BOOL : 'true' | 'false' ;
NULL_VALUE : 'None';
IDENT  : [a-zA-Z_] [a-zA-Z_0-9]* ;
STRING : '"' ( '\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '"' | '\\' ) | ~('\\' | '"') )* '"' ;
INTEGER : '-'? [0-9]+ ;

NEWLINE : '\r'? '\n' -> skip ;
WS : [ \t]+ -> skip ;
COMMENT : '/*' .*? '*/' -> skip ;
LINE_COMMENT : '//' ~[\r\n]* -> skip ;
