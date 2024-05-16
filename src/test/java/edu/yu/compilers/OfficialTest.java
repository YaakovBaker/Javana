package edu.yu.compilers;

import antlr4.JavanaLexer;
import antlr4.JavanaParser;
import edu.yu.compilers.backend.converter.Converter;
import edu.yu.compilers.frontend.Semantics;
import edu.yu.compilers.frontend.SyntaxErrorHandler;
import edu.yu.compilers.intermediate.symtable.SymTableEntry;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;

import javax.tools.*;
import java.io.*;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class OfficialTest {

    private final static Logger logger = LogManager.getLogger(OfficialTest.class);

    static {
        Configurator.setLevel("edu.yu.compilers", Level.INFO);
    }

//    private File getResourceFile(String name) {
//        URL url = this.getClass().getResource(name);
//        assertNotNull(url);
//        File file = new File(url.getFile());
//        assertTrue(file.exists());
//        return file;
//    }
//
//    @Test
//    void TestHangmanConversion() {
//        logger.info("Starting testHangmanConversion");
//
//        var tree = parseProgram("hangman");
//        semanticChecks(tree, 0);
//        String objectCode = convertCode(tree);
//        var method = compileJavaSource("hangman", objectCode);
//        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        final String utf8 = StandardCharsets.UTF_8.name();
//        assertDoesNotThrow(() -> {
//            try (PrintStream ps = new PrintStream(baos, true, utf8)) {
//                System.setOut(ps);
//                method.invoke(null, new Object[] { null });
//                System.setOut(System.out);
//            }
//        }, "Error invoking java code");
//
//
//        InputStreamReader isr = new InputStreamReader(new ByteArrayInputStream(baos.toByteArray()));
//
//
//    }
//
//    private Method compileJavaSource(String className, String code) {
//        Method method = assertDoesNotThrow(() -> {
//            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
//
//            StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
//            fileManager.setLocation(StandardLocation.CLASS_OUTPUT, List.of(new File("target/classes")));
//
//            JavaFileObject source = new JavaSourceFromString(className, code);
//
//            compiler.getTask(null, fileManager, null, null,
//                            null, List.of(source))
//                    .call();
//
//            fileManager.close();
//
//            // Load the compiled class and call its main method
//            Class<?> clazz = Class.forName(className);
//            return clazz.getMethod("main", String[].class);
//        }, "Error compiling java code");
//        assertNotNull(method);
//        return method;
//    }
//
//    private static class JavaSourceFromString extends SimpleJavaFileObject {
//        final String code;
//
//        JavaSourceFromString(String name, String code) {
//            super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
//            this.code = code;
//        }
//
//        @Override
//        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
//            return code;
//        }
//    }
//
//    private String convertCode(ParseTree tree) {
//        return assertDoesNotThrow(() -> {
//            Converter pass3 = new Converter();
//            return (String) pass3.visit(tree);
//        }, "Error while running java converter");
//    }
//
//    private SymTableEntry semanticChecks(ParseTree tree, int expectedErrors) {
//        var semantics = assertDoesNotThrow(() -> {
//            var pass2 = new Semantics();
//            pass2.visit(tree);
//            return pass2;
//        }, "Error while running semantic checks");
//
//
//        return semantics.getProgramId();
//    }
//
//    private ParseTree parseProgram(String fileName) {
//        var syntaxErrorHandler = new SyntaxErrorHandler();
//        var chars = getResourceFileCharStream(fileName);
//        var ts = newTokenStream(chars, syntaxErrorHandler);
//        var parser = newParser(ts, syntaxErrorHandler);
//        var tree = parser.program();
//
//        return tree;
//    }
//
//    private JavanaParser newParser(CommonTokenStream tokens, SyntaxErrorHandler eh) {
//        JavanaParser parser = new JavanaParser(tokens);
//        parser.removeErrorListeners();
//        parser.addErrorListener(eh);
//        return parser;
//    }
//
//    private CommonTokenStream newTokenStream(CharStream cs, SyntaxErrorHandler eh) {
//        JavanaLexer lexer = new JavanaLexer(cs);
//        lexer.removeErrorListeners();
//        lexer.addErrorListener(eh);
//        return new CommonTokenStream(lexer);
//    }
//
//    private CharStream getResourceFileCharStream(String fileName) {
//        var chstrm = assertDoesNotThrow(() -> {
//            File inputFile = getResourceFile(fileName + ".jv");
//            return CharStreams.fromStream(new FileInputStream(inputFile));
//        }, "Error reading resource file");
//        assertNotNull(chstrm);
//        return chstrm;
//    }

    @BeforeEach
    void setUp() {
        logger.info("Starting test");
    }

    @AfterEach
    void tearDown() {
        logger.info("Finished test");
    }

}