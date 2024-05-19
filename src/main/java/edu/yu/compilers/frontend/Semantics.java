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

import static edu.yu.compilers.frontend.SemanticErrorHandler.Code.*;
import static edu.yu.compilers.intermediate.symtable.SymTableEntry.Kind.*;
import static edu.yu.compilers.intermediate.type.Typespec.Form.*;

/***
 * Check the semantics of the Javana program and populate the symbol table.
 */
public class Semantics extends JavanaBaseVisitor<Object> {
    private final SymTableStack symTableStack;
    private final SemanticErrorHandler error;
    private SymTableEntry programId;
    private int currentNestingLevel;

    public Semantics() {
        // Create and initialize the symbol table stack.
        this.symTableStack = new SymTableStack();
        Predefined.initialize(symTableStack);
        this.error = new SemanticErrorHandler();
        this.currentNestingLevel = 0;
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
    //Done
    @Override
    public Object visitProgram(JavanaParser.ProgramContext ctx){
        visit(ctx.programHeader());
        for(JavanaParser.GlobalDefinitionsContext globalDefinitionsContext : ctx.defs){
            visit(globalDefinitionsContext);
        }
        visit(ctx.mainMethod());
        return null;
    }
    //Done
    @Override
    public Object visitProgramHeader(JavanaParser.ProgramHeaderContext ctx){
        JavanaParser.IdentifierContext idCtx = ctx.identifier();
        String programName = idCtx.getText();
        programId = symTableStack.enterLocal(programName, PROGRAM);
        programId.setRoutineSymTable(symTableStack.push());

        symTableStack.setProgramId(programId);
        symTableStack.getLocalSymTable().setOwner(programId);
        ctx.entry = programId;
        return null;
    }
    //Done
    @Override
    public Object visitMainMethod(JavanaParser.MainMethodContext ctx){
        String mainName = "@main";
        //See if we have a main method
        SymTableEntry mainId = symTableStack.lookupLocal(mainName);
        if( mainId != null ){
            error.flag(SemanticErrorHandler.Code.REDECLARED_IDENTIFIER, ctx);
            return null;
        }
        //Create the main method
        mainId = symTableStack.enterLocal(mainName, FUNCTION);
        mainId.appendLineNumber(ctx.getStart().getLine());
        mainId.setRoutineCode(SymTableEntry.Routine.DECLARED);
        ctx.entry = mainId;
        //Append to the parent list
        symTableStack.getLocalSymTable().getOwner().appendSubroutine(mainId);
        mainId.setRoutineSymTable(symTableStack.push());
        ctx.entry = mainId;
        //mainId.setType(Predefined.undefinedType);
        symTableStack.getLocalSymTable().setOwner(mainId);
        JavanaParser.MainArgContext mArgCtx = ctx.mainArg();
        if( mArgCtx != null){
            visit(mArgCtx);
        }
        visit(ctx.blockStatement());
        mainId.setExecutable(ctx.blockStatement());
        symTableStack.pop();
        return null;
    }

    //Done
    @Override
    public Object visitMainArg(JavanaParser.MainArgContext ctx){
        JavanaParser.IdentifierContext idCtx = ctx.identifier();
        JavanaParser.StringArrTypeContext strArrTypeCtx = ctx.stringArrType();
        String argName = idCtx.getText();
        SymTableEntry argId = symTableStack.enterLocal(argName, VARIABLE);
        visit(strArrTypeCtx);
        argId.setType(strArrTypeCtx.typeSpec);
        idCtx.entry = argId;
        return null;
    }

    // Function Definitions and Declarations ---
    //Done
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
        this.currentNestingLevel = symTableStack.getLocalSymTable().getNestingLevel();
        funcId = symTableStack.enterLocal(funcName, FUNCTION);
        funcId.appendLineNumber(protoCtx.getStart().getLine());
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
//        this.currentNestingLevel = 0;
        return null;
    }
    //Done
    @Override
    public Object visitFuncArgList(JavanaParser.FuncArgListContext ctx){
        ArrayList<SymTableEntry> argList = new ArrayList<>();
        for(JavanaParser.FuncArgumentContext argCtx : ctx.funcArgument()){
            SymTableEntry argId = (SymTableEntry) visit(argCtx);
            argList.add(argId);
        }
        return argList;
    }
    //Done
    @Override
    public Object visitFuncArgument(JavanaParser.FuncArgumentContext ctx){
        JavanaParser.TypeAssocContext typeAssocCtx = ctx.typeAssoc();
        visit(typeAssocCtx);
        ctx.typeSpec = typeAssocCtx.t.typeSpec;
        ctx.entry = typeAssocCtx.namelst.names.get(0).entry;
        return ctx.entry;
    }
    //Done
    @Override
    public Object visitReturnType(JavanaParser.ReturnTypeContext ctx){
        String typeName = ctx.getText();
        JavanaParser.TypeContext typeCtx = ctx.type();
        if( typeCtx != null){
            visit(typeCtx);
            ctx.typeSpec = typeCtx.typeSpec;
        }else{
            //None Type
            ctx.typeSpec = Predefined.undefinedType;
        }
        return null;
    }



    // Name Definitions and Declarations -------
    //Done
    @Override
    public Object visitRecordDecl(JavanaParser.RecordDeclContext ctx){
        //Get the fields and name for the record
        List<JavanaParser.TypeAssocContext> recordFields = ctx.fields;
        JavanaParser.IdentifierContext idCtx = ctx.name;
        String recordTypeName = idCtx.getText();
        //Look up our record
        SymTableEntry recordTypeId = symTableStack.lookupLocal(recordTypeName);
        if( recordTypeId == null ){
            recordTypeId = createRecordType(recordFields, recordTypeName);
        }else{
            error.flag(REDECLARED_IDENTIFIER, ctx);
        }
        //Set the context
        ctx.entry = recordTypeId;
        ctx.typeSpec = recordTypeId.getType();
        //assign line number(s)
        recordTypeId.appendLineNumber(ctx.getStart().getLine());
        return null;
    }

    //Done
    private SymTableEntry createRecordType( List<JavanaParser.TypeAssocContext> recordFields, String recordTypeName) {
        Typespec recordType = new Typespec(RECORD);
        //Enter the record type into the symbol table
        SymTableEntry recordTypeId = symTableStack.enterLocal(recordTypeName, TYPE);
        recordTypeId.setType(recordType);
        recordType.setIdentifier(recordTypeId);
        //Create the record type path
        String recordTypePath = createRecordTypePath(recordType);
        recordType.setRecordTypePath(recordTypePath);
        // Enter the record fields into the record type's symbol table.
        SymTable recordSymTable = createRecordSymTable(recordFields, recordTypeId);
        recordType.setRecordSymTable(recordSymTable);
        return recordTypeId;
    }
    

    //Done
    private SymTable createRecordSymTable(List<JavanaParser.TypeAssocContext> fields, SymTableEntry ownerId) {
        SymTable recordSymTable = symTableStack.push();
        recordSymTable.setOwner(ownerId);
        //visit each field
        for(JavanaParser.TypeAssocContext typeAssocCtx : fields){
            //Deal with TypeAssoc here since can't deal with it there
            for(JavanaParser.IdentifierContext idCtx : typeAssocCtx.namelst.names){
                String fieldName = idCtx.getText();
                SymTableEntry fieldId = recordSymTable.lookup(fieldName);
                if( fieldId != null ){
                    error.flag(REDECLARED_IDENTIFIER, idCtx);
                }

                fieldId = recordSymTable.enter(fieldName, RECORD_FIELD);
                fieldId.setType(typeAssocCtx.t.typeSpec);
                fieldId.appendLineNumber(idCtx.getStart().getLine());
            }
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
    //Done
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

    //Should be done?
    @Override
    public Object visitTypeAssoc(JavanaParser.TypeAssocContext ctx) {
        JavanaParser.TypeContext typeCtx = ctx.t;
        JavanaParser.NameListContext nameListCtx = ctx.namelst;
        List<JavanaParser.IdentifierContext> names = nameListCtx.names;
        visit(typeCtx);
        Typespec typespec = typeCtx.typeSpec;
        //For each Identifier in the NameList
        for(JavanaParser.IdentifierContext name : names){
            //Get the name
            String typeAssocName = name.getText();
            //Look up the name in the current stack
            SymTableEntry typeId = symTableStack.lookupLocal(typeAssocName);

            //If the typeId is null then lets create it
            if(typeId == null){
                typeId = symTableStack.enterLocal(typeAssocName, TYPE);
                typeId.setType(typeCtx.typeSpec);
//                typeCtx.typeSpec.setIdentifier(typeId);
            }else{//Else we already declared this ident
                error.flag(REDECLARED_IDENTIFIER, ctx);
            }
            name.entry = typeId;
            name.typeSpec = typeCtx.typeSpec;

            typeId.appendLineNumber(ctx.getStart().getLine());
        }
        return null;
    }
    //Done
    @Override
    public Object visitVariableDecl(JavanaParser.VariableDeclContext ctx){
        JavanaParser.TypeAssocContext typeAssocCtx = ctx.assoc;
        JavanaParser.TypeContext typeCtx = typeAssocCtx.t;
        visit(typeCtx);

        JavanaParser.NameListContext nameListCtx = typeAssocCtx.namelst;

        for(JavanaParser.IdentifierContext idCtx : nameListCtx.names){
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
    //Done but var's in main are not being added under main
    @Override
    public Object visitVariableDef(JavanaParser.VariableDefContext ctx){
        JavanaParser.NameListContext nameListCtx = ctx.namelst;
        //The names of the nameList
        List<JavanaParser.IdentifierContext> names = nameListCtx.names;
        //The Expression being evaluated
        JavanaParser.ExpressionContext exprCtx = ctx.expr;
        visit(exprCtx);
        //The type of the expression
        Typespec exprType = exprCtx.typeSpec;

        //For Each name in the name List
        for(JavanaParser.IdentifierContext idCtx : names){
            int lineNumber = idCtx.getStart().getLine();
            //Get the name
            String varName = idCtx.getText();
            //Look up the name in the current stack
            SymTableEntry varId = symTableStack.lookupLocal(varName);
            //If the varId is null then lets create it
            if( varId == null ){
                //Create the variable
                varId = symTableStack.enterLocal(varName, VARIABLE);
                //Verify the variable got entered
                if( varId == null ){
                    System.err.println("Error: varId is null");
                    return null;
                }
                //Set the type of the variable to the expression's type
                varId.setType(exprType);
                //Assign slot numbers to local variables.
                SymTable symTable = varId.getSymTable();
                if( symTable.getNestingLevel() > 1 ){
                    varId.setSlotNumber(symTable.nextSlotNumber());
                }
                idCtx.entry = varId;
                idCtx.typeSpec = exprType;
            }else{//Else we already declared this ident
                error.flag(REDECLARED_IDENTIFIER, idCtx);
                //Set the type of the variable to the expression's type
                varId.setType(exprType);
                idCtx.entry = varId;
            }
            //Append the line number
            varId.appendLineNumber(lineNumber);
        }
        return null;
    }
    //Done
    @Override
    public Object visitConstantDef(JavanaParser.ConstantDefContext ctx){
        JavanaParser.NameListContext nameListCtx = ctx.namelst;
        JavanaParser.ExpressionContext exprCtx = ctx.expr;
        visit(exprCtx);
        Typespec exprType = exprCtx.typeSpec;
        //For Each name in the name List
        for(JavanaParser.IdentifierContext idCtx : nameListCtx.names){
            //Get the name
            String varName = idCtx.getText();
            //Look up the name in the current stack
            SymTableEntry constId = symTableStack.lookupLocal(varName);
            //If the varId is null then lets create it
            if( constId == null ){
                constId = symTableStack.enterLocal(varName, CONSTANT);
                constId.setType(exprType);
                constId.setValue(exprCtx);
                // Assign slot numbers to local variables.
                SymTable symTable = constId.getSymTable();
                if (symTable.getNestingLevel() > 1) {
                    constId.setSlotNumber(symTable.nextSlotNumber());
                }
                idCtx.entry = constId;
                idCtx.typeSpec = exprType;
                ctx.entry = constId;
            }else{//Else we already declared this ident
                error.flag(REDECLARED_IDENTIFIER, ctx);
            }
            //Append the line number
            constId.appendLineNumber(ctx.getStart().getLine());
        }
        return null;
    }

    // Statements ------------------------------
    //Done
    @Override
    public Object visitBlockStatement(JavanaParser.BlockStatementContext ctx){
        symTableStack.push();
        for(JavanaParser.StatementContext stmtCtx : ctx.statement()){
            visit(stmtCtx);
        }
        symTableStack.pop();
        return null;
    }
    //Done
    @Override
    public Object visitNameDecl(JavanaParser.NameDeclContext ctx){
        JavanaParser.NameDeclStatementContext nameDeclCtx = ctx.nameDeclStatement();
        visit(nameDeclCtx);
        ctx.typeSpec = nameDeclCtx.typeSpec;
        return null;
    }
    //Done
    @Override
    public Object visitNameDeclDef(JavanaParser.NameDeclDefContext ctx){
        JavanaParser.NameDeclDefStatementContext nameDeclDefCtx = ctx.nameDeclDefStatement();
        JavanaParser.ConstantDefContext constDefCtx = nameDeclDefCtx.constantDef();
        JavanaParser.VariableDefContext varDefCtx = nameDeclDefCtx.variableDef();
        JavanaParser.FuncDefinitionContext funcDefCtx = nameDeclDefCtx.funcDefinition();
        if( constDefCtx != null ){
            visit(constDefCtx);
            ctx.typeSpec = constDefCtx.typeSpec;
        }else if( varDefCtx != null ){
            visit(varDefCtx);
            ctx.typeSpec = varDefCtx.typeSpec;
        }else if( funcDefCtx != null ){
            visit(funcDefCtx);
            ctx.typeSpec = funcDefCtx.entry.getType();
        }
        return null;
    }
    //Done
    @Override
    public Object visitAssignmentStatement(JavanaParser.AssignmentStatementContext ctx){
        JavanaParser.VariableContext lhs = ctx.var;
        JavanaParser.ExpressionContext rhs = ctx.expr;
        visit(lhs);
        visit(rhs);

        Typespec lhsType = lhs.typeSpec;
        Typespec rhsType = rhs.typeSpec;
        //If the lhs is null then it's an undeclared identifier
//        if( lhs.entry == null ){
//            error.flag(SemanticErrorHandler.Code.UNDECLARED_IDENTIFIER, lhs);
//        }
        //If LHS is an array element then we need to get the array element type
        if( lhsType.getForm() != null && lhsType.getForm() == ARRAY ){
            lhsType = lhsType.getArrayElementType();
        }
        //if the rhs is an array element then we need to get the array element type
        if( rhsType.getForm() != null && rhsType.getForm() == ARRAY ){
            rhsType = rhsType.getArrayElementType();
        }
        //Check if the type of the variable of the lhs can have the expression assigned to it
        if( !TypeChecker.areAssignmentCompatible(lhsType, rhsType) ){
            error.flag(SemanticErrorHandler.Code.ILLEGAL_ASSIGNMENT, rhs);
        }
        return null;
    }
    //Should be Done
    @Override
    public Object visitVariable(JavanaParser.VariableContext ctx) {
        JavanaParser.IdentifierContext idCtx = ctx.identifier();
        String varName = idCtx.getText();
        //Look up the name in the current stack
        SymTableEntry varId = symTableStack.lookup(varName);
        if( varId != null ){//If the varId is not null then we found the variable
            int lineNumber = ctx.getStart().getLine();
            //If the varId's type is Array then we need to get the array element type
            Typespec varType = varId.getType();
            if( varType != null && varType.getForm() != null && varType.getForm() == ARRAY ){
                ctx.typeSpec = varId.getType().getArrayElementType();
            }else{
                ctx.typeSpec = varId.getType();
            }
            ctx.entry = varId;
            varId.appendLineNumber(lineNumber);
            //Now deal with the varModifiers
            for(JavanaParser.VarModifierContext varModCtx : ctx.modifiers){
                visit(varModCtx);
            }
        }else{//Else we didn't find the variable
            error.flag(SemanticErrorHandler.Code.UNDECLARED_IDENTIFIER, ctx);
            ctx.typeSpec = Predefined.integerType;
//            ctx.typeSpec = Predefined.undefinedType;
        }
        return null;
    }

    //Done
    @Override
    public Object visitVarModifier(JavanaParser.VarModifierContext ctx) {
        //Have to deal with the modifier
        JavanaParser.ArrIdxSpecifierContext arrIdxCtx = ctx.arrIdxSpecifier();
        JavanaParser.IdentifierContext idCtx = ctx.identifier();
        if( arrIdxCtx != null ){
            visit(arrIdxCtx);
        }else if( idCtx != null ){
            visit(idCtx);
        }
        return null;
    }

    //Done
    @Override
    public Object visitArrIdxSpecifier(JavanaParser.ArrIdxSpecifierContext ctx){
        JavanaParser.ExpressionContext exprCtx = ctx.expr;
        visit(exprCtx);
        if( !TypeChecker.isInteger(exprCtx.typeSpec) ){
            error.flag(SemanticErrorHandler.Code.TYPE_MUST_BE_INTEGER, exprCtx);
        }
        ctx.typeSpec = exprCtx.typeSpec;
        return null;
    }

    //Done
    @Override
    public Object visitIfStatement(JavanaParser.IfStatementContext ctx){
        JavanaParser.ExpressionContext exprCtx = ctx.condition;
        JavanaParser.BlockStatementContext trueCtx = ctx.thenStmt;
        JavanaParser.BlockStatementContext falseCtx = ctx.elseStmt;
        visit(exprCtx);
        //Make sure the expression is a boolean
        if( exprCtx.typeSpec != Predefined.booleanType){
            error.flag(SemanticErrorHandler.Code.TYPE_MUST_BE_BOOLEAN, exprCtx);
        }

        visit(trueCtx);
        if (falseCtx != null) visit(falseCtx);

        return null;
    }

    //Done
    @Override
    public Object visitForStatement(JavanaParser.ForStatementContext ctx){
        symTableStack.push();
        JavanaParser.VariableDefContext varDefCtx = ctx.init;
        JavanaParser.ExpressionContext conditionCtx = ctx.condition;
        JavanaParser.ExpressionContext updateCtx = ctx.updateExpr;
        JavanaParser.BlockStatementContext blockCtx = ctx.body;
        //Optional variableDef
        if( varDefCtx != null ){
            visit(varDefCtx);
            //Then lookup local the var's name to get varId
            String varName = varDefCtx.namelst.names.get(0).getText();
            SymTableEntry varId = symTableStack.lookupLocal(varName);
            if( varId.getType() != Predefined.integerType){
                error.flag(SemanticErrorHandler.Code.TYPE_MUST_BE_INTEGER, varDefCtx.namelst.names.get(0));
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
        symTableStack.pop();
        return null;
    }

    //Check
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

    //Done
    @Override
    public Object visitReturnStatement(JavanaParser.ReturnStatementContext ctx){
        JavanaParser.ExpressionContext exprCtx = ctx.expr;
        if( exprCtx != null ){
            visit(exprCtx);
        }
        //Get the returnType
        Typespec returnType = exprCtx != null ? exprCtx.typeSpec : Predefined.undefinedType;
        //Check the current stack frame's routineIdType
        SymTable stack = symTableStack.get(this.currentNestingLevel + 1);
        SymTableEntry routineId = stack.getOwner();

        Typespec routineType = routineId.getType();
        //Compare and see if we have a type mismatch or not
        //If our form is array then we need to check the array element type
        if( routineType.getForm() == ARRAY ){
            if( !TypeChecker.areAssignmentCompatible(routineType.getArrayElementType(), returnType.getArrayElementType()) ){
                error.flag(SemanticErrorHandler.Code.INVALID_RETURN_TYPE, exprCtx);
            }
        }else{
            if( !TypeChecker.areAssignmentCompatible(routineType, returnType) ){
                error.flag(SemanticErrorHandler.Code.INVALID_RETURN_TYPE, exprCtx);
            }
        }
        return null;
    }

    //TODO
    @Override
    public Object visitPrintStatement(JavanaParser.PrintStatementContext ctx){
        JavanaParser.PrintArgumentContext printArgCtx = ctx.printArgument();
        visit(printArgCtx);
        return null;
    }

    //TODO
    @Override
    public Object visitPrintLineStatement(JavanaParser.PrintLineStatementContext ctx){
        JavanaParser.PrintArgumentContext printArgCtx = ctx.printArgument();
        if( printArgCtx != null ){
            visit(printArgCtx);
        }
        return null;
    }

    //
    @Override
    public Object visitPrintSingleValue(JavanaParser.PrintSingleValueContext ctx){
        JavanaParser.ExpressionContext exprCtx = ctx.expression();
        visit(exprCtx);
        return null;
    }

    //
    @Override
    public Object visitFormattedPrint(JavanaParser.FormattedPrintContext ctx) {
        JavanaParser.ExprListContext exprListCtx = ctx.exprList();
        visit(exprListCtx);
        return null;
    }




    // Expressions -----------------------------

    //Done
    @Override
    public Object visitExprArrayElement(JavanaParser.ExprArrayElementContext ctx) {
        JavanaParser.ExpressionContext exprCtx = ctx.expr;
        JavanaParser.ArrIdxSpecifierContext arrIdxCtx = ctx.arrIdx;
        visit(arrIdxCtx);
        //Index must be int
        if( arrIdxCtx.expr.typeSpec != Predefined.integerType){
            error.flag(SemanticErrorHandler.Code.TYPE_MUST_BE_INTEGER, arrIdxCtx);
        }
        visit(exprCtx);//This should be calling the variable
        //Expression should be an array type
        if( exprCtx.typeSpec.getForm() != ARRAY){
            error.flag(SemanticErrorHandler.Code.INVALID_TYPE, exprCtx);
        }
        //Set the type of the expression
        ctx.typeSpec = exprCtx.typeSpec.getArrayElementType();
        return null;
    }


    //Done
    @Override
    public Object visitExprArrayLength(JavanaParser.ExprArrayLengthContext ctx) {
        JavanaParser.ExpressionContext exprCtx = ctx.expr;
        visit(exprCtx);
        //This expression should return an array
        Typespec exprType = exprCtx.typeSpec;
        //Java is fine with calling.length on a string, so lets keep that functionality too for hangman
        if( exprType != null && exprType.getForm() != ARRAY){
            error.flag(SemanticErrorHandler.Code.TYPE_MUST_BE_ARRAY, exprCtx);
        }
        ctx.typeSpec = Predefined.integerType;
        return null;
    }

    @Override
    public Object visitExprStringLength(JavanaParser.ExprStringLengthContext ctx) {
        JavanaParser.ExpressionContext exprCtx = ctx.expr;
        visit(exprCtx);
        ctx.typeSpec = Predefined.integerType;
        return null;
    }

    //Done-ish
    @Override
    public Object visitExprRecordField(JavanaParser.ExprRecordFieldContext ctx) {
        JavanaParser.ExpressionContext exprCtx = ctx.expr;
        JavanaParser.IdentifierContext idCtx = ctx.identifier();
        visit(exprCtx);
        //This expression should return a record
        Typespec exprType = exprCtx.typeSpec;
        //THis should be a record
        if( exprType.getForm() != RECORD){
            error.flag(SemanticErrorHandler.Code.INVALID_TYPE, exprCtx);
        }else{
            //Check if the field exists
//            SymTable recordSymTable = exprType.getRecordSymTable();
            SymTableEntry recordId = exprType.getIdentifier();
            SymTable recordSymTable = recordId.getSymTable();
            SymTableEntry fieldId = recordSymTable.lookup(idCtx.getText());
            if( fieldId == null ){
                error.flag(SemanticErrorHandler.Code.UNDECLARED_IDENTIFIER, ctx);
                ctx.typeSpec = Predefined.undefinedType;
            }else{
                ctx.typeSpec = fieldId.getType();
            }
        }
        return null;
    }

    //Done
    @Override
    public Object visitExprHigherArith(JavanaParser.ExprHigherArithContext ctx) {
        JavanaParser.ExpressionContext leftCtx = ctx.lhs;
        JavanaParser.ExpressionContext rightCtx = ctx.rhs;
        visit(leftCtx);
        visit(rightCtx);
        //Get the types
        Typespec leftType = leftCtx.typeSpec;
        Typespec rightType = rightCtx.typeSpec;
        // Both operands integer ==> integer result
        if( TypeChecker.areBothInteger(leftType, rightType) ){
            ctx.typeSpec = Predefined.integerType;
        }
        // Both real operands ==> real result
        // One real and one integer operand ==> real result
        else if( TypeChecker.isAtLeastOneReal(leftType, rightType) ){
            ctx.typeSpec = Predefined.realType;
        }
        // Type mismatch.
        else {
            error.flag(TYPE_MISMATCH, ctx);
            ctx.typeSpec = Predefined.integerType;
        }
        return null;
    }

    //Done
    @Override
    public Object visitExprArith(JavanaParser.ExprArithContext ctx) {
        JavanaParser.ExpressionContext leftCtx = ctx.lhs;
        JavanaParser.ExpressionContext rightCtx = ctx.rhs;
        visit(leftCtx);
        visit(rightCtx);
        Typespec leftType = leftCtx.typeSpec;
        Typespec rightType = rightCtx.typeSpec;
            // Both operands integer ==> integer result
        if (TypeChecker.areBothInteger(leftType, rightType)) {
            ctx.typeSpec = Predefined.integerType;
        }
            // Both real operands ==> real result
            // One real and one integer operand ==> real result
        else if (TypeChecker.isAtLeastOneReal(leftType, rightType)) {
            ctx.typeSpec = Predefined.realType;
        }
            // Both operands string ==> string result
        else if (TypeChecker.areBothString(leftType, rightType)) {
            ctx.typeSpec = Predefined.stringType;
        }
        // Type mismatch.
        else {
            error.flag(TYPE_MISMATCH, ctx);
            ctx.typeSpec = Predefined.integerType;
        }
        return null;
    }
    //Done
    @Override
    public Object visitExprRelational(JavanaParser.ExprRelationalContext ctx) {
        evaluateComparison(ctx.lhs, ctx.rhs, ctx);
        return null;
    }
    //Done
    @Override
    public Object visitExprEquality(JavanaParser.ExprEqualityContext ctx) {
        evaluateComparison(ctx.lhs, ctx.rhs, ctx);
        return null;
    }

    private void evaluateComparison(JavanaParser.ExpressionContext leftCtx, JavanaParser.ExpressionContext rightCtx, JavanaParser.ExpressionContext ctx){
        visit(leftCtx);
        visit(rightCtx);
        Typespec leftType = leftCtx.typeSpec;
        Typespec rightType = rightCtx.typeSpec;
        //Both operands must be the same type
        if( !TypeChecker.areComparisonCompatible(leftType, rightType) ){
            error.flag(TYPE_MISMATCH, ctx);
        }
        //Set the type of the expression
        ctx.typeSpec = Predefined.booleanType;
    }
    //Done
    @Override
    public Object visitExprHighLogical(JavanaParser.ExprHighLogicalContext ctx) {
        evaluateBooleanLogical(ctx.lhs, ctx.rhs, ctx);
        return null;
    }
    //Done
    @Override
    public Object visitExprLowLogical(JavanaParser.ExprLowLogicalContext ctx) {
        evaluateBooleanLogical(ctx.lhs, ctx.rhs, ctx);
        return null;
    }

    private void evaluateBooleanLogical(JavanaParser.ExpressionContext leftCtx, JavanaParser.ExpressionContext rightCtx, JavanaParser.ExpressionContext ctx){
        visit(leftCtx);
        visit(rightCtx);
        Typespec leftType = leftCtx.typeSpec;
        Typespec rightType = rightCtx.typeSpec;
        if( !TypeChecker.areBothBoolean(leftType, rightType)){
            error.flag(SemanticErrorHandler.Code.TYPE_MUST_BE_BOOLEAN, ctx);
        }
        //Set the type of the expression
        ctx.typeSpec = Predefined.booleanType;
    }

    @Override
    public Object visitExprNot(JavanaParser.ExprNotContext ctx) {
        JavanaParser.ExpressionContext exprCtx = ctx.expression();
        visit(exprCtx);
        if( exprCtx.typeSpec != Predefined.booleanType){
            error.flag(SemanticErrorHandler.Code.TYPE_MUST_BE_BOOLEAN, ctx);
        }
        ctx.typeSpec = Predefined.booleanType;
        return null;
    }

    @Override
    public Object visitExprGroup(JavanaParser.ExprGroupContext ctx) {
        JavanaParser.ExpressionContext exprCtx = ctx.expression();
        visit(exprCtx);
        ctx.typeSpec = exprCtx.typeSpec;
        //We can group anything so we need to make sure it's just not undefined
        if( exprCtx.typeSpec == Predefined.undefinedType){
            error.flag(SemanticErrorHandler.Code.INVALID_TYPE, ctx);
        }
        return null;
    }

    //Done
    @Override
    public Object visitExprReadChar(JavanaParser.ExprReadCharContext ctx) {
        ctx.typeSpec = Predefined.stringType;
        return null;
    }
    //Done
    @Override
    public Object visitExprReadLine(JavanaParser.ExprReadLineContext ctx) {
        ctx.typeSpec = Predefined.stringType;
        return null;
    }

    @Override
    public Object visitExprFunctionCall(JavanaParser.ExprFunctionCallContext ctx) {
        JavanaParser.FunctionCallContext fcCtx = ctx.functionCall();
        JavanaParser.IdentifierContext idCtx = fcCtx.name;
        JavanaParser.ExprListContext exprListCtx = fcCtx.args;
        String funcName = idCtx.getText();
        SymTableEntry funcId = symTableStack.lookup(funcName);
        if( funcId == null ){
            error.flag(SemanticErrorHandler.Code.UNDECLARED_IDENTIFIER, idCtx);
            ctx.typeSpec = Predefined.undefinedType;
        }else{
            ctx.typeSpec = funcId.getType();
            idCtx.entry = funcId;
        }
        if( exprListCtx != null){
            visit(exprListCtx);
        }
        return null;
    }

    //Done?
    @Override
    public Object visitExprVariable(JavanaParser.ExprVariableContext ctx) {
        JavanaParser.VariableContext varCtx = ctx.variable();
        visit(varCtx);
        ctx.typeSpec = varCtx.typeSpec;
        return null;
    }

    //Done
    @Override
    public Object visitExprLiteral(JavanaParser.ExprLiteralContext ctx) {
        JavanaParser.LiteralContext litCtx = ctx.literal();
        visit(litCtx);
        ctx.typeSpec = litCtx.typeSpec;
        return null;
    }

    //Literals
    //Done
    @Override
    public Object visitIntegerLiteral(JavanaParser.IntegerLiteralContext ctx) {
        ctx.typeSpec = Predefined.integerType;
        return null;
    }

    //Done
    @Override
    public Object visitBooleanLiteral(JavanaParser.BooleanLiteralContext ctx) {
        ctx.typeSpec = Predefined.booleanType;
        return null;
    }
    //Done
    @Override
    public Object visitStringLiteral(JavanaParser.StringLiteralContext ctx) {
        ctx.typeSpec = Predefined.stringType;
        return null;
    }
    //Done
    @Override
    public Object visitNoneValue(JavanaParser.NoneValueContext ctx) {
        ctx.typeSpec = Predefined.undefinedType;
        return null;
    }
    //End Literals

    @Override
    public Object visitExprNewArray(JavanaParser.ExprNewArrayContext ctx) {
        //Visit The Array Element type and set the typeSpec accordingly
        JavanaParser.ArrayElemTypeContext arrayElemTypeCtx = ctx.newArray().t;
        visit(ctx.newArray().t);
        //Evaluate the array index specifier
        visit(ctx.newArray().arrId);
        ctx.typeSpec = arrayElemTypeCtx.typeSpec;
        return null;
    }

    @Override
    public Object visitExprNewRecord(JavanaParser.ExprNewRecordContext ctx) {
        JavanaParser.NewRecordContext newRecordCtx = ctx.newRecord();
        JavanaParser.IdentifierContext idCtx = newRecordCtx.identifier();
        JavanaParser.FieldInitListContext fieldInitListCtx = newRecordCtx.fieldInitList();
        String recordTypeName = idCtx.getText();
        SymTableEntry recordTypeId = symTableStack.lookup(recordTypeName);
        if( recordTypeId == null ){
            error.flag(SemanticErrorHandler.Code.UNDECLARED_IDENTIFIER, idCtx);
            newRecordCtx.typeSpec = Predefined.undefinedType;
        }else{
            newRecordCtx.typeSpec = recordTypeId.getType();
            idCtx.entry = recordTypeId;
            if( fieldInitListCtx != null){
                visit(fieldInitListCtx);
            }
        }
        return null;
    }

    //More

    @Override
    public Object visitExprList(JavanaParser.ExprListContext ctx){
        for(JavanaParser.ExpressionContext exprCtx : ctx.expression()){
            visit(exprCtx);
        }
        return null;
    }


    //Types

    @Override
    public Object visitTypeScalar(JavanaParser.TypeScalarContext ctx) {
        JavanaParser.ScalarTypeContext scalarTypeCtx = ctx.scalarType();
        visit(scalarTypeCtx);
        ctx.typeSpec = scalarTypeCtx.typeSpec;
        return null;
    }

    @Override
    public Object visitTypeComposite(JavanaParser.TypeCompositeContext ctx) {
        JavanaParser.CompositeTypeContext compositeTypeCtx = ctx.compositeType();
        visit(compositeTypeCtx);
        ctx.typeSpec = compositeTypeCtx.typeSpec;
        return null;
    }

    @Override
    public Object visitScalarType(JavanaParser.ScalarTypeContext ctx) {
        JavanaParser.IntegerTypeContext intTypeCtx = ctx.integerType();
        JavanaParser.BooleanTypeContext boolTypeCtx = ctx.booleanType();
        JavanaParser.StringTypeContext strTypeCtx = ctx.stringType();
        if( intTypeCtx != null) {
            visit(intTypeCtx);
            ctx.typeSpec = intTypeCtx.typeSpec;
        }else if( boolTypeCtx != null){
            visit(boolTypeCtx);
            ctx.typeSpec = boolTypeCtx.typeSpec;
        }else if( strTypeCtx != null){
            visit(strTypeCtx);
            ctx.typeSpec = strTypeCtx.typeSpec;
        }else {
            ctx.typeSpec = Predefined.undefinedType;//Shouldn't happen
        }
        return null;
    }

    @Override
    public Object visitCompositeType(JavanaParser.CompositeTypeContext ctx) {
        JavanaParser.RecordTypeContext recordTypeCtx = ctx.recordType();
        JavanaParser.IntegerArrTypeContext intArrTypeCtx = ctx.integerArrType();
        JavanaParser.BooleanArrTypeContext boolArrTypeCtx = ctx.booleanArrType();
        JavanaParser.StringArrTypeContext strArrTypeCtx = ctx.stringArrType();
        JavanaParser.RecordArrTypeContext recordArrTypeCtx = ctx.recordArrType();
        if( recordTypeCtx != null ){
            visit(recordTypeCtx);
            ctx.typeSpec = recordTypeCtx.typeSpec;
        }else if( intArrTypeCtx != null ){
            visit(intArrTypeCtx);
            ctx.typeSpec = intArrTypeCtx.typeSpec;
        }else if( boolArrTypeCtx != null ){
            visit(boolArrTypeCtx);
            ctx.typeSpec = boolArrTypeCtx.typeSpec;
        }else if( strArrTypeCtx != null ){
            visit(strArrTypeCtx);
            ctx.typeSpec = strArrTypeCtx.typeSpec;
        }else if( recordArrTypeCtx != null ){
            visit(recordArrTypeCtx);
            ctx.typeSpec = recordArrTypeCtx.typeSpec;
        }else{
            ctx.typeSpec = Predefined.undefinedType;
        }
        return null;
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

    @Override
    public Object visitRecordType(JavanaParser.RecordTypeContext ctx){
        ctx.typeSpec = Predefined.undefinedType;
        return null;
    }

    @Override
    public Object visitArrayElemType(JavanaParser.ArrayElemTypeContext ctx) {
        JavanaParser.ScalarTypeContext scalarTypeCtx = ctx.scalarType();
        JavanaParser.IdentifierContext idCtx = ctx.identifier();
        //Set the array index type to integer
        ctx.typeSpec = new Typespec(ARRAY);
        ctx.typeSpec.setArrayIndexType(Predefined.integerType);
        //If the scalarTypeCtx is not null then we have a scalar type to visit
        if( scalarTypeCtx != null ){
            visit(scalarTypeCtx);
            ctx.typeSpec.setArrayElementType(scalarTypeCtx.typeSpec);
        }else if( idCtx != null ){
            //Search symbol table for the identifier that is the type in question
            SymTableEntry idEntry = symTableStack.lookup(idCtx.getText());
            if( idEntry != null ){
                if( idEntry.getKind() == TYPE ){
                    ctx.typeSpec.setArrayElementType(idEntry.getType());
                }else{
                    error.flag(SemanticErrorHandler.Code.INVALID_TYPE, idCtx);
                    ctx.typeSpec.setArrayElementType(Predefined.undefinedType);
                }
            }else{
                error.flag(SemanticErrorHandler.Code.INVALID_TYPE, idCtx);
                ctx.typeSpec.setArrayElementType(Predefined.undefinedType);
            }
        }
        return null;
    }

    @Override
    public Object visitIntegerArrType(JavanaParser.IntegerArrTypeContext ctx){
        ctx.typeSpec = new Typespec(ARRAY);
        ctx.typeSpec.setArrayIndexType(Predefined.integerType);
        ctx.typeSpec.setArrayElementType(Predefined.integerType);
        return null;
    }

    
    @Override
    public Object visitBooleanArrType(JavanaParser.BooleanArrTypeContext ctx){
        ctx.typeSpec = new Typespec(ARRAY);
        ctx.typeSpec.setArrayIndexType(Predefined.integerType);
        ctx.typeSpec.setArrayElementType(Predefined.booleanType);
        return null;
    }

    
    @Override
    public Object visitStringArrType(JavanaParser.StringArrTypeContext ctx){
        ctx.typeSpec = new Typespec(ARRAY);
        ctx.typeSpec.setArrayIndexType(Predefined.integerType);
        ctx.typeSpec.setArrayElementType(Predefined.stringType);
        return null;
    }

    
    @Override
    public Object visitRecordArrType(JavanaParser.RecordArrTypeContext ctx){
        ctx.typeSpec = new Typespec(ARRAY);
        ctx.typeSpec.setArrayIndexType(Predefined.integerType);
        ctx.typeSpec.setArrayElementType(Predefined.undefinedType);
        return null;
    }

    // Misc Rules
    
    @Override
    public Object visitIdentifier(JavanaParser.IdentifierContext ctx){
        SymTableEntry idEntry = symTableStack.lookup(ctx.getText());
        if( idEntry == null ){
            error.flag(SemanticErrorHandler.Code.UNDECLARED_IDENTIFIER, ctx);
            ctx.typeSpec = Predefined.undefinedType;
        }else{
            ctx.typeSpec = idEntry.getType();
            ctx.entry = idEntry;
        }
        return null;
    }

    @Override
    public Object visitExprCharAt(JavanaParser.ExprCharAtContext ctx) {
        JavanaParser.ExpressionContext strCtx = ctx.str;
        JavanaParser.ExpressionContext indexCtx = ctx.index;
        visit(strCtx);
        //Make sure it is a string type
        if( strCtx.typeSpec != Predefined.stringType){
            error.flag(SemanticErrorHandler.Code.TYPE_MUST_BE_STRING, strCtx);
        }
        visit(indexCtx);
        //Must be a integer type
        if( indexCtx.typeSpec != Predefined.integerType){
            error.flag(SemanticErrorHandler.Code.TYPE_MUST_BE_INTEGER, indexCtx);
        }
        ctx.typeSpec = Predefined.stringType;
        return null;
    }

    @Override
    public Object visitExprDotEquals(JavanaParser.ExprDotEqualsContext ctx){
        JavanaParser.ExpressionContext lhs = ctx.lhs;
        JavanaParser.ExpressionContext rhs = ctx.rhs;
        //lhs type should be the same as rhs type
        visit(lhs);
        visit(rhs);
        if( lhs.typeSpec != rhs.typeSpec ){
            error.flag(TYPE_MISMATCH, ctx);
        }
        //Set the type of the expression to boolean
        ctx.typeSpec = Predefined.booleanType;
        return null;
    }

    @Override
    public Object visitExprIntegerParseInt(JavanaParser.ExprIntegerParseIntContext ctx){
        JavanaParser.ExpressionContext strCtx = ctx.expression();
        visit(strCtx);
        //The expression should be a string
        if( strCtx.typeSpec != Predefined.stringType){
            error.flag(SemanticErrorHandler.Code.TYPE_MUST_BE_STRING, strCtx);
        }
        ctx.typeSpec = Predefined.integerType;
        return null;
    }
}
