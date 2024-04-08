package edu.yu.compilers.frontend;

import antlr4.JavanaBaseVisitor;
import antlr4.JavanaParser;
import com.sun.source.tree.Tree;
import edu.yu.compilers.intermediate.symtable.Predefined;
import edu.yu.compilers.intermediate.symtable.SymTable;
import edu.yu.compilers.intermediate.symtable.SymTableEntry;
import edu.yu.compilers.intermediate.symtable.SymTableStack;
import edu.yu.compilers.intermediate.type.TypeChecker;
import edu.yu.compilers.intermediate.type.Typespec;
import edu.yu.compilers.intermediate.util.CrossReferencer;

import java.util.ArrayList;
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

    
    @Override
    public Object visitFuncArgument(JavanaParser.FuncArgumentContext ctx){
        JavanaParser.TypeAssocContext typeAssocCtx = ctx.typeAssoc();
        visit(typeAssocCtx);
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


        /*
        JavanaParser.IdentifierContext idCtx = ctx.identifier();
        String recordName = idCtx.getText();
        SymTableEntry recordId = symTableStack.enterLocal(recordName, RECORD_FIELD);
        recordId.setRoutineSymTable(symTableStack.push());
        symTableStack.getLocalSymTable().setOwner(recordId);
        idCtx.entry = recordId;
        for(JavanaParser.TypeAssocContext typeAssocCtx : ctx.fields){
            visit(typeAssocCtx);
        }

         */
        return null;


    }

    private SymTable createRecordSymTable(List<JavanaParser.TypeAssocContext> fields, SymTableEntry ownerId) {

        SymTable recordSymTable = symTableStack.push();

        recordSymTable.setOwner(ownerId);
        //visit each field
        for(JavanaParser.TypeAssocContext typeAssocCtx : fields){
            visit(typeAssocCtx);
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
        visit(ctx.typeAssoc());
        return null;
    }

    
    @Override
    public Object visitTypeAssoc(JavanaParser.TypeAssocContext ctx){
        JavanaParser.NameListContext nameListCtx = ctx.nameList();
        JavanaParser.TypeContext typeCtx = ctx.type();
        visit(nameListCtx);
        visit(typeCtx);
        return null;
    }

    
    @Override
    public Object visitVariableDef(JavanaParser.VariableDefContext ctx){
        visit(ctx.nameList());
        visit(ctx.expression());
        return null;
    }

    
    @Override
    public Object visitConstantDef(JavanaParser.ConstantDefContext ctx){
        visit(ctx.nameList());
        visit(ctx.expression());
        return null;
    }

    
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
    public Object visitNameDeclStatement(JavanaParser.NameDeclStatementContext ctx){
        if( ctx.variableDecl() != null){
            visit(ctx.variableDecl());
        }else{
            visit(ctx.recordDecl());
        }
        return null;
    }

    
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
        visit(ctx.expression());
        return null;
    }

    
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

    
    @Override
    public Object visitForStatement(JavanaParser.ForStatementContext ctx){
        //Optional variableDef
        //Expression 1
        //Expression 2
        //Block
        return null;
    }

    
    @Override
    public Object visitWhileStatement(JavanaParser.WhileStatementContext ctx){
        //Expression
        //Block
        return null;
    }

    
    @Override
    public Object visitExpressionStatement(JavanaParser.ExpressionStatementContext ctx){
        visit(ctx.expression());
        return null;
    }

    
    @Override
    public Object visitReturnStatement(JavanaParser.ReturnStatementContext ctx){
        visit(ctx.expression());
        return null;
    }

    
    @Override
    public Object visitPrintStatement(JavanaParser.PrintStatementContext ctx){
        visit(ctx.printArgument());
        return null;
    }

    
    @Override
    public Object visitPrintLineStatement(JavanaParser.PrintLineStatementContext ctx){
        JavanaParser.PrintArgumentContext printArgCtx = ctx.printArgument();
        visit(printArgCtx);
        return null;
    }


    @Override
    public Object visitPrintSingleValue(JavanaParser.PrintSingleValueContext ctx) {
        return super.visitPrintSingleValue(ctx);
    }

    @Override
    public Object visitFormattedPrint(JavanaParser.FormattedPrintContext ctx) {
        return super.visitFormattedPrint(ctx);
    }


    // Expressions -----------------------------
    
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
    public Object visitScalarType(JavanaParser.ScalarTypeContext ctx){
        return super.visitScalarType(ctx);
    }

    
    @Override
    public Object visitCompositeType(JavanaParser.CompositeTypeContext ctx){
        return super.visitCompositeType(ctx);
    }

    
    @Override
    public Object visitIntegerType(JavanaParser.IntegerTypeContext ctx){
        return super.visitIntegerType(ctx);
    }

    
    @Override
    public Object visitBooleanType(JavanaParser.BooleanTypeContext ctx){
        return super.visitBooleanType(ctx);
    }

    
    @Override
    public Object visitStringType(JavanaParser.StringTypeContext ctx){
        return super.visitStringType(ctx);
    }

    
    @Override
    public Object visitRecordType(JavanaParser.RecordTypeContext ctx){
        return super.visitRecordType(ctx);
    }

    
    @Override
    public Object visitIntegerArrType(JavanaParser.IntegerArrTypeContext ctx){
        return super.visitIntegerArrType(ctx);
    }

    
    @Override
    public Object visitBooleanArrType(JavanaParser.BooleanArrTypeContext ctx){
        return super.visitBooleanArrType(ctx);
    }

    
    @Override
    public Object visitStringArrType(JavanaParser.StringArrTypeContext ctx){
        return super.visitStringArrType(ctx);
    }

    
    @Override
    public Object visitRecordArrType(JavanaParser.RecordArrTypeContext ctx){
        return super.visitRecordArrType(ctx);
    }

    // Misc Rules
    
    @Override
    public Object visitIdentifier(JavanaParser.IdentifierContext ctx){
        return super.visitIdentifier(ctx);
    }
}
