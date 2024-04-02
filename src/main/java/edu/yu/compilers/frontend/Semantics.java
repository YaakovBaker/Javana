package edu.yu.compilers.frontend;

import antlr4.JavanaBaseVisitor;
import antlr4.JavanaParser;
import com.sun.source.tree.Tree;
import edu.yu.compilers.intermediate.symtable.Predefined;
import edu.yu.compilers.intermediate.symtable.SymTableEntry;
import edu.yu.compilers.intermediate.symtable.SymTableStack;
import edu.yu.compilers.intermediate.type.Typespec;
import edu.yu.compilers.intermediate.util.CrossReferencer;

import java.util.List;

import static edu.yu.compilers.intermediate.symtable.SymTableEntry.Kind.*;
import static edu.yu.compilers.intermediate.type.Typespec.Form.*;

/***
 * Check the semantics of the Javana program and populate the symbol table.
 */
public class Semantics extends JavanaBaseVisitor<Object> {
    private final SymTableStack symTableStack;
    private final SemanticErrorHandler error;
    private SymTableEntry programId;

    public Semantics() {
        // Create and initialize the symbol table stack.
        this.symTableStack = new SymTableStack();
        Predefined.initialize(symTableStack);
        this.error = new SemanticErrorHandler();
    }



    public int getErrorCount() {
        return error.getCount();
    }

    public SymTableEntry getProgramId() {
        return programId;
    }

    public void printSymbolTableStack() {
        CrossReferencer crossReferencer = new CrossReferencer();
        crossReferencer.print(symTableStack);
    }

    // Program and routines --------------------
    @Override
    public Object visitProgram(JavanaParser.ProgramContext ctx){
        visit(ctx.programHeader());
        for(JavanaParser.GlobalDefinitionsContext globalDefinitionsContext : ctx.globalDefinitions()){
            visit(globalDefinitionsContext);
        }
        visit(ctx.mainMethod());
        return null;
    }


    @Override
    public Object visitProgramHeader(JavanaParser.ProgramHeaderContext ctx){
        JavanaParser.IdentifierContext idCtx = ctx.identifier();
        String programName = idCtx.getText();
        programId = symTableStack.enterLocal(programName, PROGRAM);
        programId.setRoutineSymTable(symTableStack.push());

        symTableStack.setProgramId(programId);
        symTableStack.getLocalSymTable().setOwner(programId);
        idCtx.entry = programId;
        return null;
    }


    @Override
    public Object visitMainMethod(JavanaParser.MainMethodContext ctx){
        JavanaParser.MainArgContext mArgCtx = ctx.mainArg();
        if( mArgCtx != null){
            visit(mArgCtx);
        }
        visit(ctx.blockStatement());
        return null;
    }


    @Override
    public Object visitMainArg(JavanaParser.MainArgContext ctx){
        JavanaParser.IdentifierContext idCtx = ctx.identifier();
        JavanaParser.StringArrTypeContext strArrTypeCtx = ctx.stringArrType();
        String argName = idCtx.getText();
        SymTableEntry argId = symTableStack.enterLocal(argName, VARIABLE);
        argId.setType(new Typespec(ARRAY));
        idCtx.entry = argId;
        return null;
    }


    @Override
    public Object visitGlobalDefinitions(JavanaParser.GlobalDefinitionsContext ctx){
        JavanaParser.NameDeclStatementContext declCtx = ctx.nameDeclStatement();
        if(declCtx != null){
            visit(declCtx);
        }else{
            JavanaParser.NameDeclDefStatementContext defCtx = ctx.nameDeclDefStatement();
            visit(defCtx);
        }
        return null;
    }

    // Function Definitions and Declarations ---
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitFuncDefinition(JavanaParser.FuncDefinitionContext ctx){
        JavanaParser.FuncPrototypeContext protoCtx = ctx.funcPrototype();
        visit(protoCtx);
        visit(ctx.blockStatement());
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitFuncPrototype(JavanaParser.FuncPrototypeContext ctx){
        JavanaParser.IdentifierContext idCtx = ctx.identifier();
        String funcName = idCtx.getText();
        SymTableEntry funcId = symTableStack.enterLocal(funcName, FUNCTION);
        funcId.setRoutineSymTable(symTableStack.push());
        symTableStack.getLocalSymTable().setOwner(funcId);
        idCtx.entry = funcId;
        visit(ctx.funcArgList());
        visit(ctx.returnType());
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitFuncArgList(JavanaParser.FuncArgListContext ctx){
        for(JavanaParser.FuncArgumentContext argCtx : ctx.funcArgument()){
            visit(argCtx);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitFuncArgument(JavanaParser.FuncArgumentContext ctx){
        JavanaParser.TypeAssocContext typeAssocCtx = ctx.typeAssoc();
        visit(typeAssocCtx);
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitReturnType(JavanaParser.ReturnTypeContext ctx){
        if( ctx.type() != null){
            visit(ctx.type());
        }
        return null;
    }

    // Name Definitions and Declarations -------

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitRecordDecl(JavanaParser.RecordDeclContext ctx){
        JavanaParser.IdentifierContext idCtx = ctx.identifier();
        String recordName = idCtx.getText();
        SymTableEntry recordId = symTableStack.enterLocal(recordName, RECORD_FIELD);
        recordId.setRoutineSymTable(symTableStack.push());
        symTableStack.getLocalSymTable().setOwner(recordId);
        idCtx.entry = recordId;
        List<JavanaParser.TypeAssocContext> typeAssocCtxs = ctx.typeAssoc();
        for(JavanaParser.TypeAssocContext typeAssocCtx : typeAssocCtxs){
            visit(typeAssocCtx);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitVariableDecl(JavanaParser.VariableDeclContext ctx){
        visit(ctx.typeAssoc());
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitTypeAssoc(JavanaParser.TypeAssocContext ctx){
        JavanaParser.NameListContext nameListCtx = ctx.nameList();
        JavanaParser.TypeContext typeCtx = ctx.type();
        visit(nameListCtx);
        visit(typeCtx);
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitVariableDef(JavanaParser.VariableDefContext ctx){
        visit(ctx.nameList());
        visit(ctx.expression());
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitConstantDef(JavanaParser.ConstantDefContext ctx){
        visit(ctx.nameList());
        visit(ctx.expression());
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitNameList(JavanaParser.NameListContext ctx){
        List<JavanaParser.IdentifierContext> idCtxs = ctx.identifier();
        for(JavanaParser.IdentifierContext idCtx : idCtxs){
            String name = idCtx.getText();
            SymTableEntry id = symTableStack.enterLocal(name, VARIABLE);
            idCtx.entry = id;
        }
        return null;
    }

    // Statements ------------------------------

//    /**
//     * {@inheritDoc}
//     *
//     * <p>The default implementation returns the result of calling
//     * {@link #visitChildren} on {@code ctx}.</p>
//     *
//     * @param ctx
//     */
//    @Override
//    public Object visitStatement(JavanaParser.StatementContext ctx){
//        //He doesn't do this for his Pascal one?
//        return null;
//    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitBlockStatement(JavanaParser.BlockStatementContext ctx){
        symTableStack.push();
        for(JavanaParser.StatementContext stmtCtx : ctx.statement()){
            visit(stmtCtx);
        }
        symTableStack.pop();
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitNameDeclStatement(JavanaParser.NameDeclStatementContext ctx){
        if( ctx.variableDecl() != null){
            visit(ctx.variableDecl());
        }else{
            visit(ctx.recordDecl());
        }
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitNameDeclDefStatement(JavanaParser.NameDeclDefStatementContext ctx){
        if( ctx.variableDef() != null){
            visit(ctx.variableDef());
        }else if( ctx.constantDef() != null ){
            visit(ctx.constantDef());
        }else{
            visit(ctx.funcDefinition());
        }
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitAssignmentStatement(JavanaParser.AssignmentStatementContext ctx){
        JavanaParser.IdentifierContext idCtx = ctx.identifier();
        visit(idCtx);
        if( ctx.identModifier() != null){
            visit(ctx.identModifier());
        }
        visit(ctx.expression());
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitIdentModifier(JavanaParser.IdentModifierContext ctx){
        if( ctx.arrIdxSpecifier() != null){
            visit(ctx.arrIdxSpecifier());
        }else{
            visit(ctx.identifier());
        }
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitArrIdxSpecifier(JavanaParser.ArrIdxSpecifierContext ctx){
        visit(ctx.expression());
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitIfStatement(JavanaParser.IfStatementContext ctx){
        JavanaParser.ExpressionContext exprCtx = ctx.expression();
        JavanaParser.BlockStatementContext trueCtx = ctx.blockStatement(0);
        JavanaParser.BlockStatementContext falseCtx = ctx.blockStatement(1);

        visit(exprCtx);
        //Make sure the expression is a boolean
        if( exprCtx.typeSpec != Predefined.booleanType){
            error.flag(SemanticErrorHandler.Code.TYPE_MUST_BE_BOOLEAN, ctx);
        }

        visit(trueCtx);
        if (falseCtx != null) visit(falseCtx);

        return null;
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitForStatement(JavanaParser.ForStatementContext ctx){
        //Optional variableDef
        //Expression 1
        //Expression 2
        //Block
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitWhileStatement(JavanaParser.WhileStatementContext ctx){
        //Expression
        //Block
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitExpressionStatement(JavanaParser.ExpressionStatementContext ctx){
        visit(ctx.expression());
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitReturnStatement(JavanaParser.ReturnStatementContext ctx){
        visit(ctx.expression());
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitPrintStatement(JavanaParser.PrintStatementContext ctx){
        visit(ctx.printArgument());
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitPrintLineStatement(JavanaParser.PrintLineStatementContext ctx){
        JavanaParser.PrintArgumentContext printArgCtx = ctx.printArgument();
        visit(printArgCtx);
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitPrintArgument(JavanaParser.PrintArgumentContext ctx){
        JavanaParser.ExpressionContext exprCtx = ctx.expression();
        if( exprCtx != null) {
            visit(exprCtx);
        }else{
            visit(ctx.exprList());
        }
        return null;
    }

    // Expressions -----------------------------

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitExpression(JavanaParser.ExpressionContext ctx){
        return super.visitExpression(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitExprList(JavanaParser.ExprListContext ctx){
        return super.visitExprList(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitReadCharCall(JavanaParser.ReadCharCallContext ctx){
        return super.visitReadCharCall(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitReadLineCall(JavanaParser.ReadLineCallContext ctx){
        return super.visitReadLineCall(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitFunctionCall(JavanaParser.FunctionCallContext ctx){
        return super.visitFunctionCall(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitNewArray(JavanaParser.NewArrayContext ctx){
        return super.visitNewArray(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitNewRecord(JavanaParser.NewRecordContext ctx){
        return super.visitNewRecord(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitVarInitList(JavanaParser.VarInitListContext ctx){
        return super.visitVarInitList(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitLiteral(JavanaParser.LiteralContext ctx){
        return super.visitLiteral(ctx);
    }

    // Types -----------------------------------

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitType(JavanaParser.TypeContext ctx){
        return super.visitType(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitScalarType(JavanaParser.ScalarTypeContext ctx){
        return super.visitScalarType(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitCompositeType(JavanaParser.CompositeTypeContext ctx){
        return super.visitCompositeType(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitIntegerType(JavanaParser.IntegerTypeContext ctx){
        return super.visitIntegerType(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitBooleanType(JavanaParser.BooleanTypeContext ctx){
        return super.visitBooleanType(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitStringType(JavanaParser.StringTypeContext ctx){
        return super.visitStringType(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitRecordType(JavanaParser.RecordTypeContext ctx){
        return super.visitRecordType(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitIntegerArrType(JavanaParser.IntegerArrTypeContext ctx){
        return super.visitIntegerArrType(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitBooleanArrType(JavanaParser.BooleanArrTypeContext ctx){
        return super.visitBooleanArrType(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitStringArrType(JavanaParser.StringArrTypeContext ctx){
        return super.visitStringArrType(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitRecordArrType(JavanaParser.RecordArrTypeContext ctx){
        return super.visitRecordArrType(ctx);
    }

    // Misc Rules
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitIdentifier(JavanaParser.IdentifierContext ctx){
        return super.visitIdentifier(ctx);
    }
}
