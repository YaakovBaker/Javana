// Generated from c:/Users/yyb20/Downloads/YUCompSci/Baker_Yaakov_800615623/Javana/src/main/antlr4/Javana.g4 by ANTLR 4.13.1

package antlr4;

import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link JavanaParser}.
 */
public interface JavanaListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link JavanaParser#program}.
	 * @param ctx the parse tree
	 */
	void enterProgram(JavanaParser.ProgramContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavanaParser#program}.
	 * @param ctx the parse tree
	 */
	void exitProgram(JavanaParser.ProgramContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavanaParser#programHeader}.
	 * @param ctx the parse tree
	 */
	void enterProgramHeader(JavanaParser.ProgramHeaderContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavanaParser#programHeader}.
	 * @param ctx the parse tree
	 */
	void exitProgramHeader(JavanaParser.ProgramHeaderContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavanaParser#mainMethod}.
	 * @param ctx the parse tree
	 */
	void enterMainMethod(JavanaParser.MainMethodContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavanaParser#mainMethod}.
	 * @param ctx the parse tree
	 */
	void exitMainMethod(JavanaParser.MainMethodContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavanaParser#mainArg}.
	 * @param ctx the parse tree
	 */
	void enterMainArg(JavanaParser.MainArgContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavanaParser#mainArg}.
	 * @param ctx the parse tree
	 */
	void exitMainArg(JavanaParser.MainArgContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavanaParser#globalDefinitions}.
	 * @param ctx the parse tree
	 */
	void enterGlobalDefinitions(JavanaParser.GlobalDefinitionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavanaParser#globalDefinitions}.
	 * @param ctx the parse tree
	 */
	void exitGlobalDefinitions(JavanaParser.GlobalDefinitionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavanaParser#funcDefinition}.
	 * @param ctx the parse tree
	 */
	void enterFuncDefinition(JavanaParser.FuncDefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavanaParser#funcDefinition}.
	 * @param ctx the parse tree
	 */
	void exitFuncDefinition(JavanaParser.FuncDefinitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavanaParser#funcPrototype}.
	 * @param ctx the parse tree
	 */
	void enterFuncPrototype(JavanaParser.FuncPrototypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavanaParser#funcPrototype}.
	 * @param ctx the parse tree
	 */
	void exitFuncPrototype(JavanaParser.FuncPrototypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavanaParser#funcArgList}.
	 * @param ctx the parse tree
	 */
	void enterFuncArgList(JavanaParser.FuncArgListContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavanaParser#funcArgList}.
	 * @param ctx the parse tree
	 */
	void exitFuncArgList(JavanaParser.FuncArgListContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavanaParser#funcArgument}.
	 * @param ctx the parse tree
	 */
	void enterFuncArgument(JavanaParser.FuncArgumentContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavanaParser#funcArgument}.
	 * @param ctx the parse tree
	 */
	void exitFuncArgument(JavanaParser.FuncArgumentContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavanaParser#returnType}.
	 * @param ctx the parse tree
	 */
	void enterReturnType(JavanaParser.ReturnTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavanaParser#returnType}.
	 * @param ctx the parse tree
	 */
	void exitReturnType(JavanaParser.ReturnTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavanaParser#recordDecl}.
	 * @param ctx the parse tree
	 */
	void enterRecordDecl(JavanaParser.RecordDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavanaParser#recordDecl}.
	 * @param ctx the parse tree
	 */
	void exitRecordDecl(JavanaParser.RecordDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavanaParser#variableDecl}.
	 * @param ctx the parse tree
	 */
	void enterVariableDecl(JavanaParser.VariableDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavanaParser#variableDecl}.
	 * @param ctx the parse tree
	 */
	void exitVariableDecl(JavanaParser.VariableDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavanaParser#typeAssoc}.
	 * @param ctx the parse tree
	 */
	void enterTypeAssoc(JavanaParser.TypeAssocContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavanaParser#typeAssoc}.
	 * @param ctx the parse tree
	 */
	void exitTypeAssoc(JavanaParser.TypeAssocContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavanaParser#variableDef}.
	 * @param ctx the parse tree
	 */
	void enterVariableDef(JavanaParser.VariableDefContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavanaParser#variableDef}.
	 * @param ctx the parse tree
	 */
	void exitVariableDef(JavanaParser.VariableDefContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavanaParser#constantDef}.
	 * @param ctx the parse tree
	 */
	void enterConstantDef(JavanaParser.ConstantDefContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavanaParser#constantDef}.
	 * @param ctx the parse tree
	 */
	void exitConstantDef(JavanaParser.ConstantDefContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavanaParser#nameList}.
	 * @param ctx the parse tree
	 */
	void enterNameList(JavanaParser.NameListContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavanaParser#nameList}.
	 * @param ctx the parse tree
	 */
	void exitNameList(JavanaParser.NameListContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavanaParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(JavanaParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavanaParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(JavanaParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavanaParser#blockStatement}.
	 * @param ctx the parse tree
	 */
	void enterBlockStatement(JavanaParser.BlockStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavanaParser#blockStatement}.
	 * @param ctx the parse tree
	 */
	void exitBlockStatement(JavanaParser.BlockStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavanaParser#nameDeclStatement}.
	 * @param ctx the parse tree
	 */
	void enterNameDeclStatement(JavanaParser.NameDeclStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavanaParser#nameDeclStatement}.
	 * @param ctx the parse tree
	 */
	void exitNameDeclStatement(JavanaParser.NameDeclStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavanaParser#nameDeclDefStatement}.
	 * @param ctx the parse tree
	 */
	void enterNameDeclDefStatement(JavanaParser.NameDeclDefStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavanaParser#nameDeclDefStatement}.
	 * @param ctx the parse tree
	 */
	void exitNameDeclDefStatement(JavanaParser.NameDeclDefStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavanaParser#assignmentStatement}.
	 * @param ctx the parse tree
	 */
	void enterAssignmentStatement(JavanaParser.AssignmentStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavanaParser#assignmentStatement}.
	 * @param ctx the parse tree
	 */
	void exitAssignmentStatement(JavanaParser.AssignmentStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavanaParser#identModifier}.
	 * @param ctx the parse tree
	 */
	void enterIdentModifier(JavanaParser.IdentModifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavanaParser#identModifier}.
	 * @param ctx the parse tree
	 */
	void exitIdentModifier(JavanaParser.IdentModifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavanaParser#arrIdxSpecifier}.
	 * @param ctx the parse tree
	 */
	void enterArrIdxSpecifier(JavanaParser.ArrIdxSpecifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavanaParser#arrIdxSpecifier}.
	 * @param ctx the parse tree
	 */
	void exitArrIdxSpecifier(JavanaParser.ArrIdxSpecifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavanaParser#ifStatement}.
	 * @param ctx the parse tree
	 */
	void enterIfStatement(JavanaParser.IfStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavanaParser#ifStatement}.
	 * @param ctx the parse tree
	 */
	void exitIfStatement(JavanaParser.IfStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavanaParser#forStatement}.
	 * @param ctx the parse tree
	 */
	void enterForStatement(JavanaParser.ForStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavanaParser#forStatement}.
	 * @param ctx the parse tree
	 */
	void exitForStatement(JavanaParser.ForStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavanaParser#whileStatement}.
	 * @param ctx the parse tree
	 */
	void enterWhileStatement(JavanaParser.WhileStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavanaParser#whileStatement}.
	 * @param ctx the parse tree
	 */
	void exitWhileStatement(JavanaParser.WhileStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavanaParser#expressionStatement}.
	 * @param ctx the parse tree
	 */
	void enterExpressionStatement(JavanaParser.ExpressionStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavanaParser#expressionStatement}.
	 * @param ctx the parse tree
	 */
	void exitExpressionStatement(JavanaParser.ExpressionStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavanaParser#returnStatement}.
	 * @param ctx the parse tree
	 */
	void enterReturnStatement(JavanaParser.ReturnStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavanaParser#returnStatement}.
	 * @param ctx the parse tree
	 */
	void exitReturnStatement(JavanaParser.ReturnStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavanaParser#printStatement}.
	 * @param ctx the parse tree
	 */
	void enterPrintStatement(JavanaParser.PrintStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavanaParser#printStatement}.
	 * @param ctx the parse tree
	 */
	void exitPrintStatement(JavanaParser.PrintStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavanaParser#printLineStatement}.
	 * @param ctx the parse tree
	 */
	void enterPrintLineStatement(JavanaParser.PrintLineStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavanaParser#printLineStatement}.
	 * @param ctx the parse tree
	 */
	void exitPrintLineStatement(JavanaParser.PrintLineStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavanaParser#printArgument}.
	 * @param ctx the parse tree
	 */
	void enterPrintArgument(JavanaParser.PrintArgumentContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavanaParser#printArgument}.
	 * @param ctx the parse tree
	 */
	void exitPrintArgument(JavanaParser.PrintArgumentContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavanaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(JavanaParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavanaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(JavanaParser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavanaParser#exprList}.
	 * @param ctx the parse tree
	 */
	void enterExprList(JavanaParser.ExprListContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavanaParser#exprList}.
	 * @param ctx the parse tree
	 */
	void exitExprList(JavanaParser.ExprListContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavanaParser#readCharCall}.
	 * @param ctx the parse tree
	 */
	void enterReadCharCall(JavanaParser.ReadCharCallContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavanaParser#readCharCall}.
	 * @param ctx the parse tree
	 */
	void exitReadCharCall(JavanaParser.ReadCharCallContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavanaParser#readLineCall}.
	 * @param ctx the parse tree
	 */
	void enterReadLineCall(JavanaParser.ReadLineCallContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavanaParser#readLineCall}.
	 * @param ctx the parse tree
	 */
	void exitReadLineCall(JavanaParser.ReadLineCallContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavanaParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void enterFunctionCall(JavanaParser.FunctionCallContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavanaParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void exitFunctionCall(JavanaParser.FunctionCallContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavanaParser#newArray}.
	 * @param ctx the parse tree
	 */
	void enterNewArray(JavanaParser.NewArrayContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavanaParser#newArray}.
	 * @param ctx the parse tree
	 */
	void exitNewArray(JavanaParser.NewArrayContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavanaParser#newRecord}.
	 * @param ctx the parse tree
	 */
	void enterNewRecord(JavanaParser.NewRecordContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavanaParser#newRecord}.
	 * @param ctx the parse tree
	 */
	void exitNewRecord(JavanaParser.NewRecordContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavanaParser#varInitList}.
	 * @param ctx the parse tree
	 */
	void enterVarInitList(JavanaParser.VarInitListContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavanaParser#varInitList}.
	 * @param ctx the parse tree
	 */
	void exitVarInitList(JavanaParser.VarInitListContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavanaParser#literal}.
	 * @param ctx the parse tree
	 */
	void enterLiteral(JavanaParser.LiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavanaParser#literal}.
	 * @param ctx the parse tree
	 */
	void exitLiteral(JavanaParser.LiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavanaParser#type}.
	 * @param ctx the parse tree
	 */
	void enterType(JavanaParser.TypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavanaParser#type}.
	 * @param ctx the parse tree
	 */
	void exitType(JavanaParser.TypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavanaParser#scalarType}.
	 * @param ctx the parse tree
	 */
	void enterScalarType(JavanaParser.ScalarTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavanaParser#scalarType}.
	 * @param ctx the parse tree
	 */
	void exitScalarType(JavanaParser.ScalarTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavanaParser#compositeType}.
	 * @param ctx the parse tree
	 */
	void enterCompositeType(JavanaParser.CompositeTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavanaParser#compositeType}.
	 * @param ctx the parse tree
	 */
	void exitCompositeType(JavanaParser.CompositeTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavanaParser#integerType}.
	 * @param ctx the parse tree
	 */
	void enterIntegerType(JavanaParser.IntegerTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavanaParser#integerType}.
	 * @param ctx the parse tree
	 */
	void exitIntegerType(JavanaParser.IntegerTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavanaParser#booleanType}.
	 * @param ctx the parse tree
	 */
	void enterBooleanType(JavanaParser.BooleanTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavanaParser#booleanType}.
	 * @param ctx the parse tree
	 */
	void exitBooleanType(JavanaParser.BooleanTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavanaParser#stringType}.
	 * @param ctx the parse tree
	 */
	void enterStringType(JavanaParser.StringTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavanaParser#stringType}.
	 * @param ctx the parse tree
	 */
	void exitStringType(JavanaParser.StringTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavanaParser#recordType}.
	 * @param ctx the parse tree
	 */
	void enterRecordType(JavanaParser.RecordTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavanaParser#recordType}.
	 * @param ctx the parse tree
	 */
	void exitRecordType(JavanaParser.RecordTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavanaParser#integerArrType}.
	 * @param ctx the parse tree
	 */
	void enterIntegerArrType(JavanaParser.IntegerArrTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavanaParser#integerArrType}.
	 * @param ctx the parse tree
	 */
	void exitIntegerArrType(JavanaParser.IntegerArrTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavanaParser#booleanArrType}.
	 * @param ctx the parse tree
	 */
	void enterBooleanArrType(JavanaParser.BooleanArrTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavanaParser#booleanArrType}.
	 * @param ctx the parse tree
	 */
	void exitBooleanArrType(JavanaParser.BooleanArrTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavanaParser#stringArrType}.
	 * @param ctx the parse tree
	 */
	void enterStringArrType(JavanaParser.StringArrTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavanaParser#stringArrType}.
	 * @param ctx the parse tree
	 */
	void exitStringArrType(JavanaParser.StringArrTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavanaParser#recordArrType}.
	 * @param ctx the parse tree
	 */
	void enterRecordArrType(JavanaParser.RecordArrTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavanaParser#recordArrType}.
	 * @param ctx the parse tree
	 */
	void exitRecordArrType(JavanaParser.RecordArrTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavanaParser#identifier}.
	 * @param ctx the parse tree
	 */
	void enterIdentifier(JavanaParser.IdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavanaParser#identifier}.
	 * @param ctx the parse tree
	 */
	void exitIdentifier(JavanaParser.IdentifierContext ctx);
}