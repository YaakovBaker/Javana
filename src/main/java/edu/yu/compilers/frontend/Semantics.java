package edu.yu.compilers.frontend;

import antlr4.JavanaBaseVisitor;
import antlr4.JavanaParser;
import edu.yu.compilers.intermediate.symtable.Predefined;
import edu.yu.compilers.intermediate.symtable.SymTable;
import edu.yu.compilers.intermediate.symtable.SymTableEntry;
import edu.yu.compilers.intermediate.symtable.SymTableStack;
import edu.yu.compilers.intermediate.type.TypeChecker;
import edu.yu.compilers.intermediate.type.Typespec;
import edu.yu.compilers.intermediate.util.CrossReferencer;

import java.util.ArrayList;
import java.util.List;

import static edu.yu.compilers.frontend.SemanticErrorHandler.Code.REDECLARED_IDENTIFIER;
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
        for(JavanaParser.GlobalDefinitionsContext globalDefinitionsContext : ctx.defs){
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

    // Function Definitions and Declarations ---
    
    @Override
    public Object visitFuncDefinition(JavanaParser.FuncDefinitionContext ctx){
        JavanaParser.FuncPrototypeContext protoCtx = ctx.funcPrototype();
        //FunctionProtype
        //Get our contexts
        JavanaParser.IdentifierContext idCtx = protoCtx.identifier();
        JavanaParser.FuncArgListContext argListCtx = protoCtx.funcArgList();
        //Grab the function name
        String funcName = idCtx.getText();
        //We need to create this function as an entity of the stack
        SymTableEntry funcId = symTableStack.lookupLocal(funcName);
        if( funcId != null ){
            error.flag(SemanticErrorHandler.Code.REDECLARED_IDENTIFIER, protoCtx.getStart().getLine(), funcName);
            return null;
        }
        funcId = symTableStack.enterLocal(funcName, FUNCTION);
        funcId.setRoutineCode(SymTableEntry.Routine.DECLARED);
        idCtx.entry = funcId;

        //Append to the parent list
        symTableStack.getLocalSymTable().getOwner().appendSubroutine(funcId);

        funcId.setRoutineSymTable(symTableStack.push());
        idCtx.entry = funcId;

        SymTable symTable = symTableStack.getLocalSymTable();
        symTable.setOwner(funcId);

        if( argListCtx != null){
            ArrayList<SymTableEntry> argIds = (ArrayList<SymTableEntry>) visit(argListCtx);
            funcId.setRoutineParameters(argIds);

            for( SymTableEntry argId : argIds ){
                argId.setSlotNumber(symTable.nextSlotNumber());
            }
        }
        JavanaParser.ReturnTypeContext returnTypeCtx = protoCtx.returnType();
        visit(returnTypeCtx);
        Typespec returnType = returnTypeCtx.typeSpec;
        funcId.setType(returnType);
        idCtx.typeSpec = returnType;

        SymTableEntry assocVarId = symTableStack.enterLocal(funcName, VARIABLE);
        assocVarId.setSlotNumber(symTable.nextSlotNumber());
        assocVarId.setType(returnType);
        //Visit Block
        visit(ctx.blockStatement());
        funcId.setExecutable(ctx.blockStatement());
        symTableStack.pop();
        return null;
    }
    
    @Override
    public Object visitFuncArgList(JavanaParser.FuncArgListContext ctx){
        ArrayList<SymTableEntry> argList = new ArrayList<>();
        for(JavanaParser.FuncArgumentContext argCtx : ctx.funcArgument()){
            SymTableEntry argId = (SymTableEntry) visit(argCtx);
            argList.add(argId);
        }
        return argList;
    }

    // TODO
    @Override
    public Object visitFuncArgument(JavanaParser.FuncArgumentContext ctx){
        JavanaParser.TypeAssocContext typeAssocCtx = ctx.typeAssoc();

        return null;
    }

    
    @Override
    public Object visitReturnType(JavanaParser.ReturnTypeContext ctx){
        String typeName = ctx.getText();
        SymTableEntry typeId = symTableStack.lookup(typeName);
        if( typeId != null){
            if( typeId.getKind() != TYPE ){
                error.flag(SemanticErrorHandler.Code.INVALID_TYPE, ctx);
                ctx.typeSpec = Predefined.undefinedType;//Or IntegerType
            }else{
                ctx.typeSpec = typeId.getType();
            }
            typeId.appendLineNumber(ctx.getStart().getLine());
        }else{
            error.flag(SemanticErrorHandler.Code.UNDECLARED_IDENTIFIER, ctx);
            ctx.typeSpec = Predefined.undefinedType;//Or IntegerType
        }
        ctx.entry = typeId;
        if( ctx.type() != null){
            visit(ctx.type());
        }
        return null;
    }

    // Name Definitions and Declarations -------
    @Override
    public Object visitRecordDecl(JavanaParser.RecordDeclContext ctx){
        String recordTypeName = SymTable.generateUnnamedName();
        Typespec recordType = new Typespec(RECORD);

        SymTableEntry recordTypeId = symTableStack.enterLocal(recordTypeName, TYPE);
        recordTypeId.setType(recordType);
        recordType.setIdentifier(recordTypeId);

        String recordTypePath = createRecordTypePath(recordType);
        recordType.setRecordTypePath(recordTypePath);

        // Enter the record fields into the record type's symbol table.
        SymTable recordSymTable = createRecordSymTable(ctx.fields, recordTypeId);
        recordType.setRecordSymTable(recordSymTable);

        ctx.entry = recordTypeId;
        ctx.typeSpec = recordType;
        return null;
    }

    //TODO
    private SymTable createRecordSymTable(List<JavanaParser.TypeAssocContext> fields, SymTableEntry ownerId) {

        SymTable recordSymTable = symTableStack.push();

        recordSymTable.setOwner(ownerId);
        //visit each field
        for(JavanaParser.TypeAssocContext typeAssocCtx : fields){
            //TODO
        }

        recordSymTable.resetVariables(RECORD_FIELD);
        symTableStack.pop();

        return recordSymTable;

    }

    /**
     * Create the fully qualified type pathname of a record type.
     *
     * @param recordType the record type.
     * @return the pathname.
     */
    private String createRecordTypePath(Typespec recordType) {
        SymTableEntry recordId = recordType.getIdentifier();
        SymTableEntry parentId = recordId.getSymTable().getOwner();
        String path = recordId.getName();

        while ((parentId.getKind() == TYPE) && (parentId.getType().getForm() == RECORD)) {
            path = parentId.getName() + "$" + path;
            parentId = parentId.getSymTable().getOwner();
        }

        path = parentId.getName() + "$" + path;
        return path;
    }


    //Did record Decl
    
    @Override
    public Object visitVariableDecl(JavanaParser.VariableDeclContext ctx){
        JavanaParser.TypeAssocContext typeAssocCtx = ctx.typeAssoc();

        JavanaParser.TypeContext typeCtx = typeAssocCtx.type();
        visit(typeCtx);

        JavanaParser.NameListContext nameListCtx = typeAssocCtx.nameList();

        for(JavanaParser.IdentifierContext idCtx : nameListCtx.identifier()){
            int lineNumber = idCtx.getStart().getLine();
            String varName = idCtx.getText();

            SymTableEntry variableId = symTableStack.lookupLocal(varName);

            if (variableId == null) {
                variableId = symTableStack.enterLocal(varName, VARIABLE);
                variableId.setType(typeCtx.typeSpec);

                // Assign slot numbers to local variables.
                SymTable symTable = variableId.getSymTable();
                if (symTable.getNestingLevel() > 1) {
                    variableId.setSlotNumber(symTable.nextSlotNumber());
                }

                idCtx.entry = variableId;
            }else{
                error.flag(REDECLARED_IDENTIFIER, ctx);
                variableId.setType(typeCtx.typeSpec);
                idCtx.entry = variableId;
            }

            variableId.appendLineNumber(lineNumber);
        }
        return null;
    }

    //TODO
    @Override
    public Object visitVariableDef(JavanaParser.VariableDefContext ctx){
        visit(ctx.nameList());
        visit(ctx.expression());
        return null;
    }

    //TODO
    @Override
    public Object visitConstantDef(JavanaParser.ConstantDefContext ctx){
        visit(ctx.nameList());
        visit(ctx.expression());
        return null;
    }

    // Statements ------------------------------
    
    @Override
    public Object visitBlockStatement(JavanaParser.BlockStatementContext ctx){
        symTableStack.push();
        for(JavanaParser.StatementContext stmtCtx : ctx.statement()){
            visit(stmtCtx);
        }
        symTableStack.pop();
        return null;
    }

    @Override
    public Object visitAssignmentStatement(JavanaParser.AssignmentStatementContext ctx){
        JavanaParser.VariableContext varCtx = ctx.var;
        JavanaParser.ExpressionContext exprCtx = ctx.expr;
        visit(varCtx);
        visit(exprCtx);

        Typespec varType = varCtx.typeSpec;
        Typespec exprType = exprCtx.typeSpec;

        if( varCtx.entry == null ){
            error.flag(SemanticErrorHandler.Code.UNDECLARED_IDENTIFIER, varCtx);
        }

        if( !TypeChecker.areAssignmentCompatible(varType, exprType)){
            error.flag(SemanticErrorHandler.Code.INCOMPATIBLE_ASSIGNMENT, exprCtx);
        }
        return null;
    }

    @Override
    public Object visitVariable(JavanaParser.VariableContext ctx) {
        JavanaParser.IdentifierContext idCtx = ctx.identifier();
        String varName = idCtx.getText();
        SymTableEntry varId = symTableStack.lookup(varName);
        if( varId != null ){
            int lineNumber = ctx.getStart().getLine();
            ctx.typeSpec = varId.getType();
            ctx.entry = varId;
            varId.appendLineNumber(lineNumber);
        }else{
            ctx.typeSpec = Predefined.undefinedType;
        }
        return null;
    }

    @Override
    public Object visitArrIdxSpecifier(JavanaParser.ArrIdxSpecifierContext ctx){
        JavanaParser.ExpressionContext exprCtx = ctx.expr;
        visit(exprCtx);
        if( exprCtx.typeSpec != Predefined.integerType){
            error.flag(SemanticErrorHandler.Code.TYPE_MUST_BE_INTEGER, ctx);
        }
        return null;
    }

    
    @Override
    public Object visitIfStatement(JavanaParser.IfStatementContext ctx){
        JavanaParser.ExpressionContext exprCtx = ctx.condition;
        JavanaParser.BlockStatementContext trueCtx = ctx.thenStmt;
        JavanaParser.BlockStatementContext falseCtx = ctx.elseStmt;
        visit(exprCtx);
        //Make sure the expression is a boolean
        if( exprCtx.typeSpec != Predefined.booleanType){
            error.flag(SemanticErrorHandler.Code.TYPE_MUST_BE_BOOLEAN, ctx);
        }

        visit(trueCtx);
        if (falseCtx != null) visit(falseCtx);

        return null;
    }

    //TODO - double check update Expressions
    @Override
    public Object visitForStatement(JavanaParser.ForStatementContext ctx){
        JavanaParser.VariableDefContext varDefCtx = ctx.init;
        JavanaParser.ExpressionContext conditionCtx = ctx.condition;
        JavanaParser.ExpressionContext updateCtx = ctx.updateExpr;
        JavanaParser.BlockStatementContext blockCtx = ctx.body;
        //Optional variableDef
        if( varDefCtx != null ){
            visit(varDefCtx);
            if( varDefCtx.typeSpec != Predefined.integerType){
                error.flag(SemanticErrorHandler.Code.TYPE_MUST_BE_INTEGER, varDefCtx);
            }
        }
        //Expression 1
        visit(conditionCtx);
        if( conditionCtx.typeSpec != Predefined.booleanType){
            error.flag(SemanticErrorHandler.Code.TYPE_MUST_BE_BOOLEAN, conditionCtx);
        }
        //Expression 2
        visit(updateCtx);
        if( updateCtx.typeSpec != Predefined.integerType){
            error.flag(SemanticErrorHandler.Code.TYPE_MUST_BE_INTEGER, updateCtx);
        }
        //Block
        visit(blockCtx);
        return null;
    }

    
    @Override
    public Object visitWhileStatement(JavanaParser.WhileStatementContext ctx){
        JavanaParser.ExpressionContext conditionCtx = ctx.condition;
        JavanaParser.BlockStatementContext blockCtx = ctx.body;
        visit(conditionCtx);
        if( conditionCtx.typeSpec != Predefined.booleanType){
            error.flag(SemanticErrorHandler.Code.TYPE_MUST_BE_BOOLEAN, conditionCtx);
        }
        visit(blockCtx);
        return null;
    }
    
    @Override
    public Object visitReturnStatement(JavanaParser.ReturnStatementContext ctx){
        JavanaParser.ExpressionContext exprCtx = ctx.expr;
        if( exprCtx != null ){
            visit(exprCtx);
        }
        Typespec returnType = exprCtx != null ? exprCtx.typeSpec : Predefined.undefinedType;
        //Check the current stack frame's returnType
        SymTableEntry routineId = symTableStack.getLocalSymTable().getOwner();
        Typespec routineType = routineId.getType();
        //Compare and see if we have a type mismatch or not
        if( !TypeChecker.areAssignmentCompatible(routineType, returnType)){
            error.flag(SemanticErrorHandler.Code.TYPE_MISMATCH, ctx);
        }
        return null;
    }

    //TODO
    @Override
    public Object visitFormattedPrint(JavanaParser.FormattedPrintContext ctx) {
        return super.visitFormattedPrint(ctx);
    }


    // Expressions -----------------------------

    //Currently working on
    @Override
    public Object visitExprArrayElement(JavanaParser.ExprArrayElementContext ctx) {
        JavanaParser.ExpressionContext exprCtx = ctx.expr;
        JavanaParser.ArrIdxSpecifierContext arrIdxCtx = ctx.arrIdx;
        visit(exprCtx);
        //This expression should return an array c = arrayReturn()[5]

        visit(arrIdxCtx);
        //I feel like there is more to this. This expression should return an array
        return null;
    }

    @Override
    public Object visitExprList(JavanaParser.ExprListContext ctx){
        return super.visitExprList(ctx);
    }

    
    @Override
    public Object visitReadCharCall(JavanaParser.ReadCharCallContext ctx){
        return super.visitReadCharCall(ctx);
    }

    
    @Override
    public Object visitReadLineCall(JavanaParser.ReadLineCallContext ctx){
        return super.visitReadLineCall(ctx);
    }

    
    @Override
    public Object visitFunctionCall(JavanaParser.FunctionCallContext ctx){
        return super.visitFunctionCall(ctx);
    }

    
    @Override
    public Object visitNewArray(JavanaParser.NewArrayContext ctx){
        return super.visitNewArray(ctx);
    }

    
    @Override
    public Object visitNewRecord(JavanaParser.NewRecordContext ctx){
        return super.visitNewRecord(ctx);
    }

    @Override
    public Object visitIntegerType(JavanaParser.IntegerTypeContext ctx){
        ctx.typeSpec = Predefined.integerType;
        return null;
    }

    @Override
    public Object visitBooleanType(JavanaParser.BooleanTypeContext ctx){
        ctx.typeSpec = Predefined.booleanType;
        return null;
    }

    
    @Override
    public Object visitStringType(JavanaParser.StringTypeContext ctx){
        ctx.typeSpec = Predefined.stringType;
        return null;
    }

    //DOn't need?
//    @Override
//    public Object visitRecordType(JavanaParser.RecordTypeContext ctx){
//
//    }

    
    @Override
    public Object visitIntegerArrType(JavanaParser.IntegerArrTypeContext ctx){
        ctx.typeSpec = Predefined.integerType;
        return null;
    }

    
    @Override
    public Object visitBooleanArrType(JavanaParser.BooleanArrTypeContext ctx){
        ctx.typeSpec = Predefined.booleanType;
        return null;
    }

    
    @Override
    public Object visitStringArrType(JavanaParser.StringArrTypeContext ctx){
        ctx.typeSpec = Predefined.stringType;
        return null;
    }

    
    @Override
    public Object visitRecordArrType(JavanaParser.RecordArrTypeContext ctx){
        ctx.typeSpec = Predefined.undefinedType;
        return null;
    }

    // Misc Rules
    
    @Override
    public Object visitIdentifier(JavanaParser.IdentifierContext ctx){
        ctx.typeSpec = Predefined.undefinedType;
        ctx.entry = symTableStack.lookup(ctx.getText());
        return null;
    }
}
