package edu.yu.compilers.frontend;

import antlr4.JavanaBaseVisitor;
import antlr4.JavanaParser;
import edu.yu.compilers.intermediate.symtable.Predefined;
import edu.yu.compilers.intermediate.symtable.SymTable;
import edu.yu.compilers.intermediate.symtable.SymTableEntry;
import edu.yu.compilers.intermediate.symtable.SymTableEntry.Kind;
import edu.yu.compilers.intermediate.symtable.SymTableStack;
import edu.yu.compilers.intermediate.type.TypeChecker;
import edu.yu.compilers.intermediate.type.Typespec;
import edu.yu.compilers.intermediate.type.Typespec.Form;
import edu.yu.compilers.intermediate.util.CrossReferencer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static edu.yu.compilers.frontend.SemanticErrorHandler.Code.*;
import static edu.yu.compilers.intermediate.symtable.SymTableEntry.Kind.*;
import static edu.yu.compilers.intermediate.symtable.SymTableEntry.Routine.DECLARED;
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
        return 0;
    }

    public SymTableEntry getProgramId() {
        return null;
    }

    public void printSymbolTableStack() {

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
    public Object visitProgram(JavanaParser.ProgramContext ctx){
        visit(ctx.programHeader());
        for(JavanaParser.GlobalDefinitionsContext globalDefinitionsContext : ctx.globalDefinitions()){
            visit(globalDefinitionsContext);
        }
        visit(ctx.mainMethod());
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

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitMainMethod(JavanaParser.MainMethodContext ctx){
        JavanaParser.MainArgContext mArgCtx = ctx.mainArg();
        if( mArgCtx != null){
            visit(mArgCtx);
        }
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
    public Object visitMainArg(JavanaParser.MainArgContext ctx){
        JavanaParser.IdentifierContext idCtx = ctx.identifier();
        JavanaParser.StringArrTypeContext strArrTypeCtx = ctx.stringArrType();
        String argName = idCtx.getText();
        SymTableEntry argId = symTableStack.enterLocal(argName, VARIABLE);
        argId.setType(new Typespec(ARRAY));
        idCtx.entry = argId;
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


}
