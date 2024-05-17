package edu.yu.compilers.backend.converter;

import antlr4.JavanaBaseVisitor;
import antlr4.JavanaParser;
import edu.yu.compilers.intermediate.symtable.Predefined;
import edu.yu.compilers.intermediate.symtable.SymTable;
import edu.yu.compilers.intermediate.symtable.SymTableEntry;
import edu.yu.compilers.intermediate.type.Typespec;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Hashtable;

import static edu.yu.compilers.intermediate.symtable.SymTableEntry.Kind.*;
import static edu.yu.compilers.intermediate.type.Typespec.Form.*;
import static edu.yu.compilers.intermediate.type.Typespec.Form.ENUMERATION;

/*
Notes for self
- I finished visitProgram and Program Header.
    Now I need to visit the defs, and main method.
    The defs are the constant and variable definitions from Pascal
    main methopd is visiting the statementlist from Pascal
 */


/**
 * Convert Javana programs to Java.
 */
public class Converter extends JavanaBaseVisitor<Object> {
    private static final Hashtable<String, String> typeNameTable;
    static {
        typeNameTable = new Hashtable<>();
        typeNameTable.put("None", "null");
        typeNameTable.put("int", "int");
        typeNameTable.put("bool", "boolean");
        typeNameTable.put("string", "String");
    }

    private CodeGenerator code;

    private String programName;

    private boolean programVariables = true;
    private boolean recordFields = false;

    public String getProgramName() {
        return programName;
    }

    @Override
    public Object visitProgram(JavanaParser.ProgramContext ctx) {
        StringWriter sw = new StringWriter();
        code = new CodeGenerator(new PrintWriter(sw));
        visit(ctx.programHeader());

        // Execution timer and runtime standard input.
        code.indent();
        code.emitLine("private static java.util.Scanner _sysin = " +
                "new java.util.Scanner(System.in);");
        code.emitLine();

        // Level 1 declarations.
        JavanaParser.IdentifierContext idCtx = ctx.programHeader().identifier();
        for(JavanaParser.GlobalDefinitionsContext globalDefinitionsContext:  ctx.globalDefinitions()) {
            visit(globalDefinitionsContext);
        }
        //emitUnnamedRecordDefinitions(idCtx.entry.getRoutineSymTable());


        // Main.
        code.emitLine();
        code.emitLine("public static void main(String[] args)");
        code.indent();

        // Allocate structured data.
        //emitAllocateStructuredVariables("", idCtx.entry.getRoutineSymTable());
        code.emitLine();

        // Main compound statement.
        visit(ctx.mainMethod());

        code.dedent();
        code.dedent();
        code.emitLine("}");

        code.close();
        return sw.toString();
    }

    @Override
    public Object visitProgramHeader(JavanaParser.ProgramHeaderContext ctx) {
        programName = ctx.name.getText();

        // Emit the Java program class.
        code.emitLine("public class " + programName);
        code.emitLine("{");

        return null;
    }

    @Override
    public Object visitConstantDef(JavanaParser.ConstantDefContext ctx) {
        JavanaParser.NameListContext namelistCtx = ctx.nameList();
        JavanaParser.ExpressionContext exprCtx = ctx.expression();
        String exprContextString = (String) visit(exprCtx); //Should be good, must ensure return statement is not null
        Typespec typespec = exprCtx.typeSpec;
        String javaType = typeNameTable.get(typespec.getIdentifier().getName());
        for (JavanaParser.IdentifierContext idCtx : namelistCtx.identifier()) {
            String name = idCtx.entry.getName();
            code.emitStart();
            code.emitEnd("final " + javaType + " " + name + " = "
                    + exprContextString + ";"); // Should be good

        }
        return null;

    }

    //Same as above, just took out "final"
    @Override
    public Object visitVariableDef(JavanaParser.VariableDefContext ctx) {
        JavanaParser.NameListContext namelistCtx = ctx.nameList();
        JavanaParser.ExpressionContext exprCtx = ctx.expression();
        String exprContextString =  (String) visit(exprCtx); //Should be good, must ensure return statement is not null
        Typespec typespec = exprCtx.typeSpec;


        for(JavanaParser.IdentifierContext idCtx: namelistCtx.identifier()) {
            String name = idCtx.getText();
            code.emitStart();
            code.emitEnd("var " + name + " = "
                    + exprContextString + ";"); // Should be good

        }
        return null;
    }


    @Override
    public Object visitTypeAssoc(JavanaParser.TypeAssocContext ctx) {
        JavanaParser.NameListContext nameListContext = ctx.nameList();
        JavanaParser.TypeContext typeSpecContext = ctx.type();
        String javaType = typeNameTable.get(typeSpecContext.getText());

        for(JavanaParser.IdentifierContext idCtx: nameListContext.identifier()) {
            String name = idCtx.entry.getName();
            code.emitStart();

            code.emitEnd(javaType + " " + name + ";"); // Should be good

        }
        return null;
    }

    private String visitTypeAssocForRecord(JavanaParser.TypeAssocContext ctx){
        JavanaParser.NameListContext nameListContext = ctx.nameList();
        JavanaParser.TypeContext typeSpecContext = ctx.type();
        String javaType = typeNameTable.get(typeSpecContext.getText());
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i< ctx.namelst.names.size(); i++) {
            if(i!=0){
                code.emit(", ");
            }
            JavanaParser.IdentifierContext idCtx = ctx.namelst.identifier(i);
            String name = idCtx.entry.getName();
            code.emitEnd(javaType + " " + name); // Should be good
        }

        return stringBuilder.toString();
    }

    @Override
    public Object visitRecordDecl(JavanaParser.RecordDeclContext ctx) {
        JavanaParser.IdentifierContext identifierContext = ctx.identifier();
        code.emitStart();
        code.emit("public record " + identifierContext.getText());
        code.emit("(");
        for(int i = 0; i< ctx.fields.size(); i++){
            if(i != 0){
                code.emit(", ");
            }
            code.emit(visitTypeAssocForRecord(ctx.typeAssoc(i)));
        }
        code.emitEnd("){}");
        code.emitLine();

        return null;
    }

    @Override
    public Object visitMainMethod(JavanaParser.MainMethodContext ctx) {
        programVariables = false;
        visit(ctx.blockStatement());
        return null;
    }

    @Override
    public Object visitFuncDefinition(JavanaParser.FuncDefinitionContext ctx) {
        code.emit("public static ");
        if(ctx.funcPrototype().returnType().type() != null){
            code.emit((String) visit(ctx.funcPrototype().returnType().type()));
        }else{
            code.emit("void");
        }
        code.emit(" " + ctx.funcPrototype().identifier().getText() + "(");
        //Get Params
        if(ctx.funcPrototype().funcArgList!=null){
            for(int i = 0; i<ctx.funcPrototype().funcArgList.args.size(); i++){
                JavanaParser.FuncArgumentContext funcArgumentContext = ctx.funcPrototype().funcArgList.args.get(i);
                JavanaParser.TypeAssocContext typeAssocContext = funcArgumentContext.typeAssoc();
                Typespec typespec = typeAssocContext.type().typeSpec;

                String javaType;
                //I never Check if it's an array here
                if(typespec.getForm()==ARRAY){
                    javaType = typespec.getArrayElementType().getIdentifier().getName();
                    javaType = javaType + "[]";
                }else{
                    javaType = typeNameTable.get(typespec.getIdentifier().getName());
                }

                JavanaParser.NameListContext nameListContext = typeAssocContext.nameList();
                for(int j = 0; j<nameListContext.identifier().size(); j++){
                    code.emit(javaType);
                    JavanaParser.IdentifierContext identifierContext = nameListContext.identifier(j);
                    if(i == ctx.funcPrototype().funcArgList.args.size()-1 && j == nameListContext.identifier().size() -1){
                        //Do nothing
                        code.emit(" " + identifierContext.getText());
                    }else{
                        code.emit(" " + identifierContext.getText() + ", ");
                    }
                }
            }
        }

        code.emit("){");


        visit(ctx.blockStatement());


        code.emitLine("}");
        return null;
    }

    @Override
    public Object visitBlockStatement(JavanaParser.BlockStatementContext ctx) {
        code.emitLine("{");

        for(JavanaParser.StatementContext statementContext : ctx.statement()){
            visit(statementContext);
        }

        code.emitLine("}");

        return null;
    }

    @Override
    public Object visitAssignmentStatement(JavanaParser.AssignmentStatementContext ctx) {
        String lhs = (String) visit(ctx.variable());
        String expr = (String) visit(ctx.expression());
        code.emit(lhs + " = " + expr);
        code.emitEnd(";");

        return null;
    }

    @Override
    public Object visitVariable(JavanaParser.VariableContext ctx) {

        return ctx.getText();


        //Need to return a string here
//        JavanaParser.IdentifierContext idCtx = ctx.identifier();
//        SymTableEntry variableId = idCtx.entry;
//        StringBuilder variableName = new StringBuilder(variableId.getName());
//        Typespec type = idCtx.typeSpec;
//
//        if ((type != Predefined.booleanType)
//                && (variableId.getKind() == ENUMERATION_CONSTANT)) {
//            variableName.insert(0, type.getIdentifier().getName() + ".");
//        }
//
//        // Loop over any subscript and field modifiers.
//        for (JavanaParser.VarModifierContext modCtx : ctx.modifiers) {
//            // Subscripts.
//            if (modCtx.arrIdxSpecifier() != null) {
//                //There used to be a for each loop here, but in Javana, there is only one expr, not multiple
//                Typespec indexType = type.getArrayIndexType();
//                int minIndex = 0;
//
//                if (indexType.getForm() == SUBRANGE) {
//                    minIndex = indexType.getSubrangeMinValue();
//                }
//
//                JavanaParser.ExpressionContext exprCtx = modCtx.arrIdxSpecifier().expression();
//                String expr = (String) visit(exprCtx);
//                String subscript =
//                        (minIndex == 0) ? expr
//                                : (minIndex < 0) ? "(" + expr + ")+" + (-minIndex)
//                                : "(" + expr + ")-" + minIndex;
//
//                variableName.append("[").append(subscript).append("]");
//
//                type = type.getArrayElementType();
//            }
//
//            // Record field.
//            else {
//                JavanaParser.IdentifierContext fieldCtx = ctx.identifier();
//                String fieldName = fieldCtx.entry.getName();
//                variableName.append(".").append(fieldName);
//                type = fieldCtx.typeSpec;
//            }
//        }
//
//
//        return variableName.toString();
    }

    @Override
    public Object visitIfStatement(JavanaParser.IfStatementContext ctx) {
        code.emit("if(");
        code.emit((String) visit(ctx.expression()));
        code.emit("){");
        visit(ctx.thenStmt); //Make sure all statements are emitting code, and not returning string
        code.emitLine("}");
        if(ctx.elseStmt!=null){
            code.emit("else{");
            visit(ctx.elseStmt);
            code.emit("}");
        }

        return null;
    }

    @Override
    public Object visitForStatement(JavanaParser.ForStatementContext ctx) {
        code.emit("for(");

        //Includes ;
        // 0   1 2 3
        // var i = 0;
        JavanaParser.NameListContext namelistCtx = ctx.init.nameList();
        JavanaParser.ExpressionContext exprCtx = ctx.init.expression();
        String exprContextString =  (String) visit(exprCtx);

        JavanaParser.IdentifierContext idCtx = namelistCtx.identifier(0);
        String name = idCtx.getText();
        code.emit("var " + name + " = "
                + exprContextString + ";"); // Should be good

        code.emit((String) visit(ctx.condition));
        code.emit("; ");
        //Need to get the variable and the = sign here

        code.emit(name + " " + "=");
        code.emit((String) visit(ctx.updateExpr));

        code.emit(")");

        visit(ctx.body); //Includes { }
        code.emitLine();

        return null;
    }

    @Override
    public Object visitWhileStatement(JavanaParser.WhileStatementContext ctx) {
        code.emit("do");
        code.emitLine("{");
        code.indent();

        visit(ctx.body);

        code.dedent();
        code.emitLine("}");

        code.emitStart("while ((");
        code.emit((String) visit(ctx.expression()));
        code.emitEnd("));");

        return null;
    }

    @Override
    public Object visitExpressionStatement(JavanaParser.ExpressionStatementContext ctx) {
        code.emit((String) visit(ctx.expression()));
        code.emit(";");
        return null;
    }

    @Override
    public Object visitReturnStatement(JavanaParser.ReturnStatementContext ctx) {
        code.emitLine();
        code.emit("return ");
        code.emit((String) visit(ctx.expr));
        code.emit(";");
        return null;
    }

    //I'm not 100% on these prints, but lets see what happens
    @Override
    public Object visitPrintStatement(JavanaParser.PrintStatementContext ctx) {
        code.emit("System.out.printf");
        visit(ctx.arg);
        code.emitEnd(";");
        return null;
    }

    @Override
    public Object visitPrintLineStatement(JavanaParser.PrintLineStatementContext ctx) {
        if (ctx.arg != null) {
            code.emit("System.out.printf");
            visit(ctx.arg);
            code.emitEnd(";");
        } else {
            code.emitEnd("System.out.println();");
        }

        return null;
    }

    @Override
    public Object visitPrintSingleValue(JavanaParser.PrintSingleValueContext ctx) {
        code.emit("(String.valueOf" + ctx.getText() + ")");
        return null;
    }

    @Override
    public Object visitFormattedPrint(JavanaParser.FormattedPrintContext ctx) {
        code.emit(ctx.exprList().getText());
        return null;
    }

    @Override
    public Object visitExprArrayElement(JavanaParser.ExprArrayElementContext ctx) {
        return ctx.getText();
    }

    @Override
    public Object visitExprArrayLength(JavanaParser.ExprArrayLengthContext ctx) {
        return ctx.getText();
    }

    @Override
    public Object visitExprRecordField(JavanaParser.ExprRecordFieldContext ctx) {
        return ctx.getText();
    }

    @Override
    public Object visitExprHigherArith(JavanaParser.ExprHigherArithContext ctx) {
        return ctx.getText();
    }

    @Override
    public Object visitExprArith(JavanaParser.ExprArithContext ctx) {
        return ctx.getText();
    }

    @Override
    public Object visitExprRelational(JavanaParser.ExprRelationalContext ctx) {
        return ctx.getText();
    }

    @Override
    public Object visitExprEquality(JavanaParser.ExprEqualityContext ctx) {
        return ctx.getText();
    }

    @Override
    public Object visitExprHighLogical(JavanaParser.ExprHighLogicalContext ctx) {
        return ctx.getText();
    }

    @Override
    public Object visitExprLowLogical(JavanaParser.ExprLowLogicalContext ctx) {
        return ctx.getText();
    }

    @Override
    public Object visitExprNot(JavanaParser.ExprNotContext ctx) {
        return ctx.getText();
    }

    @Override
    public Object visitExprGroup(JavanaParser.ExprGroupContext ctx) {
        return ctx.getText();
    }

//    @Override
//    public Object visitReadCharCall(JavanaParser.ReadCharCallContext ctx) {
//        code.emit("(String) _sysin.next().charAt(0)");
//        return null;
//    }

    @Override
    public Object visitExprReadChar(JavanaParser.ExprReadCharContext ctx) {
        return "String.valueOf(_sysin.next().charAt(0))"; //Semi-colon here?
    }

//    @Override
//    public Object visitReadLineCall(JavanaParser.ReadLineCallContext ctx) {
//        code.emit("(String) _sysin.nextLine()");
//        return null;
//    }

    @Override
    public Object visitExprReadLine(JavanaParser.ExprReadLineContext ctx) {
        return "(String) _sysin.nextLine()"; //Semi-colon here?
    }

//    @Override
//    public Object visitFunctionCall(JavanaParser.FunctionCallContext ctx) {
//        code.emit(ctx.getText());
//        return null;
//    }

    @Override
    public Object visitExprFunctionCall(JavanaParser.ExprFunctionCallContext ctx) {
        return ctx.getText(); //name(arg, arg)
    }

    @Override
    public Object visitIntegerLiteral(JavanaParser.IntegerLiteralContext ctx) {
        return ctx.getText();
    }

    @Override
    public Object visitBooleanLiteral(JavanaParser.BooleanLiteralContext ctx) {
        return ctx.getText();
    }

    @Override
    public Object visitStringLiteral(JavanaParser.StringLiteralContext ctx) {
        return ctx.getText();
    }

    @Override
    public Object visitNoneValue(JavanaParser.NoneValueContext ctx) {
        return "null";
    }

    @Override
    public Object visitExprCharAt(JavanaParser.ExprCharAtContext ctx) {
        return "String.valueOf(" + ctx.getText() + ")" ;
    }

    @Override
    public Object visitExprDotEquals(JavanaParser.ExprDotEqualsContext ctx) {
        return ctx.getText();
    }

    @Override
    public Object visitExprIntegerParseInt(JavanaParser.ExprIntegerParseIntContext ctx) {
        return ctx.getText();
    }

    @Override
    public Object visitNewArray(JavanaParser.NewArrayContext ctx) { //return a string
        // @type[expr] -> new int[10]
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("new ");
        String arrayElemType = (String) visit(ctx.arrayElemType());
        stringBuilder.append(arrayElemType);
        stringBuilder.append("[");
        String exprString = (String) visit(ctx.arrIdxSpecifier().expr); //All expressions return a string
        stringBuilder.append(exprString);
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    @Override
    public Object visitArrayElemType(JavanaParser.ArrayElemTypeContext ctx) {
        if(ctx.identifier()!=null){
            return ctx.identifier().getText();
        }else{
            return typeNameTable.get(ctx.scalarType().getText()); //Else convert the typeName
        }
    }

    @Override
    public Object visitNewRecord(JavanaParser.NewRecordContext ctx) { //return a string
        // @Point{x=10, y=20} -> new Point(10, 20)
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("new ");
        String recordName = ctx.identifier().getText();
        stringBuilder.append(recordName);
        stringBuilder.append("(");
        if(ctx.init!=null){
            for(int i = 0; i< ctx.init.fieldInit().size(); i++){
                if(i==0){
                    stringBuilder.append(visit(ctx.init.fieldInit().get(i).expr));
                }else{
                    stringBuilder.append(", ");
                    stringBuilder.append(visit(ctx.init.fieldInit().get(i).expr));
                }
            }
        }
        stringBuilder.append(")");
        return stringBuilder.toString();

    }

    @Override
    public Object visitScalarType(JavanaParser.ScalarTypeContext ctx) {
        return typeNameTable.get(ctx.getText());
    }

    @Override
    public Object visitCompositeType(JavanaParser.CompositeTypeContext ctx) {
        if(ctx.recordType()!=null){
            return ctx.recordType().getText();
        }else if(ctx.recordArrType()!= null){
            return ctx.recordArrType().getText();
        }else if(ctx.integerArrType()!= null){
            return "int[]";
        }else if(ctx.booleanArrType()!= null){
            return "boolean[]";
        }else if(ctx.stringArrType()!= null){
            return "String[]";
        }

        return null;
    }

    @Override
    public Object visitIdentifier(JavanaParser.IdentifierContext ctx) {
        return ctx.getText();
    }



    /**
     * Emit a record type definition for an unnamed record.
     *
     * @param symTable the symbol table that can contain unnamed records.
     */
    private void emitUnnamedRecordDefinitions(SymTable symTable) {
        // Loop to look for names of unnamed record types.
        for (SymTableEntry id : symTable.sortedEntries()) {
            if ((id.getKind() == TYPE)
                    && (id.getType().getForm() == RECORD)
                    && (id.getName().startsWith(SymTable.UNNAMED_PREFIX))) {
                code.emitStart();
                if (programVariables) code.emit("public static ");
                code.emitEnd("class " + id.getName());
                code.emitLine("{");
                code.indent();
                emitRecordFields(id.getType().getRecordSymTable());
                code.dedent();
                code.emitLine("}");
                code.emitLine();
            }
        }
    }

    /**
     * Copied from Pascal HW7
     *
     * Emit the record fields of a record.
     *
     * @param symTable the symbol table of the unnamed record.
     */
    private void emitRecordFields(SymTable symTable) {
        emitUnnamedRecordDefinitions(symTable);

        // Loop over the entries of the symbol table.
        for (SymTableEntry fieldId : symTable.sortedEntries()) {
            if (fieldId.getKind() == RECORD_FIELD) {
                code.emitStart(typeName(fieldId.getType()));
                code.emit(" " + fieldId.getName());
                code.emitEnd(";");
            }
        }
    }

    /**
     * Copied from Pascal HW7
     *
     * Convert a Pascal type name to the equivalent Java type name.
     *
     * @param pascalType the datatype name.
     * @return the Java type name.
     */
    private String typeName(Typespec pascalType) {
        Typespec.Form form = pascalType.getForm();
        SymTableEntry typeId = pascalType.getIdentifier();
        String pascalTypeName = typeId != null ? typeId.getName() : null;

        if (form == ARRAY) {
            Typespec elemType = pascalType.getArrayBaseType();
            pascalTypeName = elemType.getIdentifier().getName();
            String javaTypeName = typeNameTable.get(pascalTypeName);
            return javaTypeName != null ? javaTypeName : pascalTypeName;
        } else if (form == SUBRANGE) {
            Typespec baseType = pascalType.baseType();
            pascalTypeName = baseType.getIdentifier().getName();
            return typeNameTable.get(pascalTypeName);
        } else if (form == ENUMERATION) {
            return pascalTypeName != null ? pascalTypeName : "int";
        } else if (form == RECORD) return pascalTypeName;
        else return typeNameTable.get(pascalTypeName);
    }

    /**
     * Emit code to allocate data for structured (array or record) variables.
     *
     * @param lhsPrefix the prefix for the target variable name.
     * @param symTable  the symbol table containing the variable names.
     */
    private void emitAllocateStructuredVariables(String lhsPrefix, SymTable symTable) {
        // Loop over all the symbol table's identifiers to emit
        // code to allocate array and record variables.
        for (SymTableEntry id : symTable.sortedEntries()) {
            if (id.getKind() == VARIABLE) {
                emitAllocateStructuredData(lhsPrefix, id);
            }
        }
    }

    /**
     * Emit code to allocate structured (array or record) data.
     *
     * @param lhsPrefix  the prefix for the target variable name.
     * @param variableId the symbol table entry of the target variable.
     */
    private void emitAllocateStructuredData(String lhsPrefix,
                                            SymTableEntry variableId) {
        Typespec variableType = variableId.getType();
        Typespec.Form form = variableType.getForm();
        String variableName = variableId.getName();

        if (form == ARRAY) {
            code.emitStart(lhsPrefix + variableName + " = ");
            Typespec elemType = emitNewArray(variableType);
            code.emitEnd(";");

            if (elemType.isStructured()) {
                emitNewArrayElement(lhsPrefix, variableName, variableType);
            }
        } else if (form == RECORD) {
            code.emitStart(lhsPrefix + variableName + " = ");
            emitNewRecord(lhsPrefix, variableName, variableType);
        }
    }

    private Typespec emitNewArray(Typespec type) {
        StringBuilder sizes = new StringBuilder();

        while (type.getForm() == ARRAY) {
            sizes.append("[").append(type.getArrayElementCount()).append("]");
            type = type.getArrayElementType();
        }

        type = type.baseType();
        String pascalTypeName = type.getIdentifier().getName();
        String javaTypeName = typeNameTable.get(pascalTypeName);

        if (javaTypeName == null) javaTypeName = pascalTypeName;
        code.emit("new " + javaTypeName + sizes);

        return type;
    }

    /**
     * Emit code to allocate an array element.
     *
     * @param lhsPrefix    the prefix for the target variable name.
     * @param variableName the name of the target variable.
     * @param elemType     the element's datatype.
     */
    private void emitNewArrayElement(String lhsPrefix, String variableName,
                                     Typespec elemType) {
        int dimensionCount = 0;

        StringBuilder variableNameBuilder = new StringBuilder(variableName);
        do {
            int elemCount = elemType.getArrayElementCount();
            ++dimensionCount;
            String subscript = "_i" + dimensionCount;
            variableNameBuilder.append("[").append(subscript).append("]");

            code.emitLine("for (int " + subscript + " = 0; " +
                    subscript + " < " + elemCount +
                    "; " + subscript + "++)");
            code.emitStart("{");
            code.indent();

            elemType = elemType.getArrayElementType();
        } while (elemType.getForm() == ARRAY);
        variableName = variableNameBuilder.toString();

        String typeName = elemType.getIdentifier().getName();
        code.emitStart(lhsPrefix + variableName + " = new " + typeName + "()");
        code.emitEnd(";");

        emitNewRecordFields(lhsPrefix + variableName + ".", elemType);

        while (--dimensionCount >= 0) {
            code.dedent();
            code.emitLine("}");
        }
    }

    /**
     * Emit code to allocate a new record.
     *
     * @param lhsPrefix    the prefix for the target variable name.
     * @param variableName the name of the target variable.
     * @param recordType   the record's datatype.
     */
    private void emitNewRecord(String lhsPrefix, String variableName,
                               Typespec recordType) {
        String typePath = recordType.getRecordTypePath();
        int index = typePath.indexOf('$');

        // Don't include the program name in the record type path.
        // Replace each $ with a period.
        typePath = typePath.substring(index + 1).replace('$', '.');
        code.emit("new " + typePath + "();");

        emitNewRecordFields(lhsPrefix + variableName + ".", recordType);
    }

    /**
     * Emit code to allocate a record's fields.
     *
     * @param lhsPrefix  the prefix for the target variable name.
     * @param recordType the record's datatype.
     */
    private void emitNewRecordFields(String lhsPrefix, Typespec recordType) {
        for (SymTableEntry fieldId : recordType.getRecordSymTable().sortedEntries()) {
            if (fieldId.getKind() == RECORD_FIELD) {
                Typespec type = fieldId.getType();

                if (type.isStructured()) {
                    emitAllocateStructuredData(lhsPrefix, fieldId);
                }
            }
        }
    }


}