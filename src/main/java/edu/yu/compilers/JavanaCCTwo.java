package edu.yu.compilers;

import antlr4.JavanaLexer;
import antlr4.JavanaParser;
import edu.yu.compilers.frontend.Semantics;
import edu.yu.compilers.frontend.SyntaxErrorHandler;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.IOException;
import java.util.List;

public class JavanaCCTwo {

    public static void main(String[] args) throws Exception {
        String desktop = "C:\\Users\\yyb20\\Downloads\\YUCompSci\\JavanaCompilerProject\\Javana\\";
        String laptop = "C:\\Users\\yyb20\\Downloads\\YUCompSci\\Javana\\";
        String pakage = "src\\main\\java\\edu\\yu\\compilers\\jvPrograms\\";
        String folderExample = "examplePrograms\\";
        String folderHangMan = "hangman\\";
        String folderFinished = "finished\\";
        String errorChecking = "ErrorsInputs\\errors\\";
        String typeMismatch = "TYPE_MISMATCH\\";
        String sourceFileName = laptop + pakage + folderExample + "ExampleArrayAccess.jv";
        String hangMan = laptop + pakage + folderHangMan + "hangman.jv";
        String finished = laptop + pakage + folderFinished + "TestAssignmentStatements.jv";
        String testingPath = laptop + pakage + errorChecking + typeMismatch + "typecheck_expr_relational.jv";
        String errorCheckingPath = laptop + pakage + errorChecking + "FunctionErrors.jv";
        sourceFileName = hangMan;
//        sourceFileName = finished;
//        sourceFileName = testingPath;
//        sourceFileName = errorCheckingPath;
        SyntaxErrorHandler syntaxErrorHandler = new SyntaxErrorHandler();

        var lexer = createLexer(sourceFileName, syntaxErrorHandler);
        if (lexer == null)
            System.exit(-1);

        // Pass 1: Parse the Pascal source file.

        var parser = createParser(lexer, syntaxErrorHandler);
        ParseTree tree = parser.program();
        int errorCount = syntaxErrorHandler.getCount();

        // Pass 2: Semantic operations.

        Semantics pass2 = new Semantics();
        pass2.visit(tree);
        errorCount = pass2.getErrorCount();


        if (errorCount > 0)
            System.err.printf("There were %d semantic errors.\n", errorCount);
        pass2.printSymbolTableStack();
        System.exit(errorCount);
    }

    private static JavanaLexer createLexer(String sourceFileName, SyntaxErrorHandler syntaxErrorHandler) {
        try {
            var lexer = new JavanaLexer(CharStreams.fromFileName(sourceFileName));
            lexer.addErrorListener(syntaxErrorHandler);
            return lexer;
        } catch (IOException e) {
            System.out.println("Source file error: " + sourceFileName);
            return null;
        }
    }

    private static JavanaParser createParser(JavanaLexer lexer, SyntaxErrorHandler syntaxErrorHandler) {
        var parser = new JavanaParser(new CommonTokenStream(lexer));
        parser.addErrorListener(syntaxErrorHandler);
        return parser;
    }

    protected static void printTokens(List<Token> tokens, Vocabulary vocabulary) {
        System.out.println("Tokens:");
        System.out.println();

        for (var token : tokens) {
            var symbolicName = vocabulary.getSymbolicName(token.getType());
            var tokenText = token.getText();

            if (symbolicName == null) {
                System.out.printf("%14s : '%s'\n", "", tokenText);
            } else if (symbolicName.equals("STRING")) {
                // Strip the single quotes from the string.
                var string = tokenText.substring(1, tokenText.length() - 1);

                // Unescape embedded single quotes
                string = string.replace("''", "'");

                // print out the token with double quotes around the strings
                System.out.printf("%14s : \"%s\"\n", symbolicName, string);
            } else if (symbolicName.equals("CHARACTER")) {
                // Length will be 3, unless it is an escaped single quote
                if (tokenText.length() == 3) {
                    System.out.printf("%14s : '%s'\n", symbolicName, tokenText.charAt(1));
                } else {
                    System.out.printf("%14s : '\\%s'\n", symbolicName, tokenText.charAt(2));
                }
            } else {
                if (symbolicName.equals("ERROR"))
                    System.out.printf("TOKEN ERROR at line %d: Invalid token at '%s'\n", token.getLine(), tokenText);

                System.out.printf("%14s : %s\n", symbolicName, tokenText);
            }
        }
    }
}