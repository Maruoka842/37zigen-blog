package tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.Test;

import library.tools.MergeFiles;

public class MergeFilesCommentsWhitespaceTest {

    @Test
    public void testRemoveCommentsAndWhitespaceDirectly() {
        String input = """
                package tools;

                /**
                 * This is a Javadoc comment
                 */
                public class Main {
                    // This is a line comment
                    public static void main(String[] args) {
                        /* This is a block comment */
                        String s = "hello // not comment /* still not comment */ world";
                        char c = '/';
                        String block = \"\"\"
                                This is a text block
                                // nested comment? No!
                                /* nested block? No! */
                                \"\"\";
                        int a = 123;  // trailing comment
                    }
                }
                """;

        String expected = "package tools; public class Main { public static void main(String[] args) { String s = \"hello // not comment /* still not comment */ world\"; char c = '/'; String block = \"\"\"\n" +
                "                This is a text block\n" +
                "                // nested comment? No!\n" +
                "                /* nested block? No! */\n" +
                "                \"\"\"; int a = 123; } }";

        String result = MergeFiles.removeCommentsAndWhitespace(input);
        assertEquals(expected, result);
    }

    @Test
    public void testNoNewlinesOutsideLiterals() {
        String input = """
                package tools;
                import java.util.List;

                public class Sample {
                    // line comment
                    public static void main(String[] args) {
                        int x = 5;
                        /* block
                           comment */
                        int y = 10;
                    }
                }
                """;
        String result = MergeFiles.removeCommentsAndWhitespace(input);
        assertFalse(result.contains("\n"), "Result should not contain any newlines: " + result);
        assertFalse(result.contains("\r"), "Result should not contain any carriage returns: " + result);
    }

    @Test
    public void testMergeWithRemoval() throws IOException {
        String name = "Main";
        Path solverDir = Paths.get("solver/src/main/java");
        Files.createDirectories(solverDir);
        Path input = solverDir.resolve("Main.java");

        Path tempDir = Files.createTempDirectory("merge_test_comments");
        Path output = tempDir.resolve("Main.java");

        Files.writeString(input, """
                import java.io.IOException;
                import java.util.Arrays;
                import library.tools.FastScanner;
                import library.tools.MergeFiles;
                import library.tools.MyPrintWriter;

                /**
                 * Main Javadoc
                 */
                public class Main {
                    // Static field with trailing comment
                    static MyPrintWriter pw = MyPrintWriter.getInstance(); // print writer
                    static FastScanner sc = FastScanner.getInstance();

                    /*
                     * Multi-line block comment
                     */
                    public static void main(String[] args) throws IOException {
                        new Main().run();
                        pw.flush();
                    }

                    void run() {
                        // Inline comment inside run
                        int N = sc.nextInt();
                        int[] arr = sc.nextInts(N);
                        /* Another block comment */
                        pw.println(Arrays.toString(arr));
                    }
                }
                """);

        try {
            MergeFiles.exportRemovingSpaceAndComment(name, "solver/src/main/java/Main.java", output.toString());

            String mergedContent = Files.readString(output);

            boolean ok = compileJava(output);
            assertTrue(ok, "Merged and minified Main.java failed to compile");

            // Verify that output does NOT contain Javadocs or Comments
            assertFalse(mergedContent.contains("Main Javadoc"), "Output still contains Main Javadoc comment");
            assertFalse(mergedContent.contains("print writer"), "Output still contains trailing print writer comment");
            assertFalse(mergedContent.contains("Multi-line block comment"), "Output still contains Multi-line block comment");
            assertFalse(mergedContent.contains("Inline comment inside run"), "Output still contains Inline comment inside run");
            assertFalse(mergedContent.contains("Another block comment"), "Output still contains Another block comment");
            assertFalse(mergedContent.contains("Original Code"), "Output should not contain Original Code block at the end");

            // Verify that it is formatted (e.g. no empty lines)
            assertFalse(mergedContent.contains("\n\n\n"), "Output contains multiple redundant empty lines");

        } finally {
            deleteDirectory(tempDir);
        }
    }

    private void deleteDirectory(Path path) throws IOException {
        try (Stream<Path> walk = Files.walk(path)) {
            walk.sorted(Comparator.reverseOrder())
                .forEach(p -> {
                    try {
                        Files.delete(p);
                    } catch (IOException e) {
                    }
                });
        }
    }

    private static boolean compileJava(Path javaFile) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) throw new IllegalStateException("JDK required");
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
        Iterable<? extends JavaFileObject> units = fileManager.getJavaFileObjects(javaFile.toFile());
        Path compileOut = Files.createTempDirectory("compile_out_comments");
        List<String> options = List.of("-d", compileOut.toString(), "--release", "21");
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, options, null, units);
        boolean success = task.call();
        fileManager.close();
        if (!success) {
            for (Diagnostic<?> d : diagnostics.getDiagnostics()) {
                System.err.printf("  [%s] line %d: %s%n", d.getKind(), d.getLineNumber(), d.getMessage(null));
            }
        }
        return success;
    }
}
