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
    ;

programHeader
    : 'Javana' name=identifier ':'
    ;

mainMethod
    : '@main' '(' args=mainArg? ')' body=blockStatement
    ;

mainArg
    : name=identifier ':' stringArrType
    ;

globalDefinitions
    : nameDeclStatement # nameDecl
    | nameDeclDefStatement # nameDeclDef
    ;

// Function Definitions and Declarations ---

funcDefinition
    : proto=funcPrototype body=blockStatement
    ;

funcPrototype
    : 'func' name=identifier '(' argList+=funcArgList? ')' '->' return=returnType
    ;

funcArgList
    : args+=funcArgument (',' args+=funcArgument)*
    ;

funcArgument
    : typeAssoc
    ;

returnType locals [ Typespec typeSpec = null, SymTableEntry entry = null ]
    : type
    | None
    ;

// Name Definitions and Declarations -------

recordDecl locals [ Typespec typeSpec = null, SymTableEntry entry = null ]
    : 'record' name=identifier '{' fields+=typeAssoc* '}'
    ;

variableDecl
    : 'decl' assoc=typeAssoc
    ;

typeAssoc
    : namelst=nameList ':' t=type
    ;

variableDef locals [ Typespec typeSpec = null ]
    : 'var' namelst=nameList '=' expr=expression
    ;

constantDef
    : 'const' namelst=nameList '=' expr=expression
    ;

nameList
    : names+=identifier (',' names+=identifier)*
    ;


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
    ;

blockStatement
    : '{' stmts+=statement* '}'
    ;

nameDeclStatement
    : variableDecl
    | recordDecl
    ;

nameDeclDefStatement
    : variableDef
    | constantDef
    | funcDefinition
    ;

assignmentStatement
    : var=variable '=' expr=expression
    ;

variable locals [ SymTableEntry entry = null, Typespec typeSpec = null]
    : name=identifier modifiers+=varModifier*
    ;

varModifier
    : arrIdxSpecifier   # varArrayIndexModfier
    | '.' identifier    # varRecordFieldModifier
    ;

arrIdxSpecifier
    : '[' expr=expression ']'
    ;

ifStatement
    : 'if' '(' condition=expression ')' thenStmt=blockStatement ('else' elseStmt=blockStatement)?
    ;

forStatement
    : 'for' '(' init=variableDef? ';' condition=expression ';' updateExpr=expression ')' body=blockStatement
    ;

whileStatement
    : 'while' '(' condition=expression ')' body=blockStatement
    ;

expressionStatement
    : expr=expression
    ;

returnStatement
    : 'return' expr=expression?
    ;

printStatement
    : 'print' arg=printArgument
    ;

printLineStatement
    : 'println' arg=printArgument?
    ;

printArgument
    : expression        # PrintSingleValue
    | '(' exprList ')'  # FormattedPrint
    ;

// Expressions -----------------------------

expression locals [ Typespec typeSpec = null, SymTableEntry entry = null]
    : expr=expression arrIdx=arrIdxSpecifier            # ExprArrayElement
    | expr=expression '.' 'length'                      # ExprArrayLength
    | expr=expression '.' name=identifier               # ExprRecordField
    | lhs=expression op=HIGHER_ARITH_OP rhs=expression  # ExprHigherArith
    | lhs=expression op=ARITH_OP rhs=expression         # ExprArith
    | lhs=expression op=REL_OP rhs=expression           # ExprRelational
    | lhs=expression op=EQ_OP rhs=expression            # ExprEquality
    | lhs=expression op=HIGH_LOGIC_OP rhs=expression    # ExprHighLogical
    | lhs=expression op=LOW_LOGIC_OP rhs=expression     # ExprLowLogical
    | '!' expression                                    # ExprNot
    | '(' expression ')'                                # ExprGroup
    | readCharCall                                      # ExprReadChar
    | readLineCall                                      # ExprReadLine
    | functionCall                                      # ExprFunctionCall
    | variable                                          # ExprVariable
    | literal                                           # ExprLiteral
    | newArray                                          # ExprNewArray
    | newRecord                                         # ExprNewRecord
    ;

exprList
    : exprs+=expression (',' exprs+=expression)*
    ;

readCharCall
    : 'readch' '(' ')'
    ;

readLineCall
    : 'readln' '(' ')'
    ;

functionCall
    : name=identifier '(' args=exprList? ')'
    ;

newArray locals [Typespec typeSpec= null ]
    : '@' t=arrayElemType arrIdxSpecifier
    ;

arrayElemType locals [Typespec typeSpec= null ]
    : scalarType
    | identifier
    ;

newRecord locals [Typespec typeSpec= null ]
    : '@' identifier '{' init=fieldInitList? '}'
    ;

fieldInitList
    : init+=fieldInit (',' init+=fieldInit)*
    ;

fieldInit
    : field=identifier '=' expr=expression
    ;

literal locals [ Typespec typeSpec = null ]
    : INTEGER   # IntegerLiteral
    | BOOL      # BooleanLiteral
    | STRING    # StringLiteral
    | None      # NoneValue
    ;

// Types -----------------------------------

type locals [ Typespec typeSpec = null ]
    : scalarType    # TypeScalar
    | compositeType # TypeComposite
    ;

scalarType locals [ Typespec typeSpec = null ]
    : integerType
    | booleanType
    | stringType
    ;

compositeType locals [ Typespec typeSpec = null ]
    : recordType
    | integerArrType
    | booleanArrType
    | stringArrType
    | recordArrType
    ;

integerType locals [ Typespec typeSpec = null ] : INT_TYPE ;
booleanType locals [ Typespec typeSpec = null ] : BOOL_TYPE ;
stringType locals [ Typespec typeSpec = null ] : STR_TYPE ;
recordType locals [ Typespec typeSpec = null ] : identifier ;

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
