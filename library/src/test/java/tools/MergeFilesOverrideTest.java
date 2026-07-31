package tools;

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

public class MergeFilesOverrideTest {

    @Test
    public void testOverrideRetention() throws IOException {
        String name = "Main";

        // solver path (used by MergeFiles.export as relative from project root)
        Path solverDir = Paths.get("solver/src/main/java");
        Files.createDirectories(solverDir);
        Path solverMain = solverDir.resolve("Main.java");

        // temporary library path (we'll put it in src/main/java/library/tmp_test so MergeFiles finds it)
        Path libDir = Paths.get("src/main/java/library/tmp_test");
        Files.createDirectories(libDir);
        Path libA = libDir.resolve("A.java");
        Path libB = libDir.resolve("B.java");

        Path tempDir = Files.createTempDirectory("merge_test_override");
        Path output = tempDir.resolve("Main.java");

        Files.writeString(solverMain, """
                import java.io.IOException;
                import library.tmp_test.B;

                public class Main {
                    public static void main(String[] args) {
                        new B().x();
                        System.out.println("Success");
                    }
                }
                """);

        Files.writeString(libA, """
                package library.tmp_test;
                public class A {
                    public void x() {
                        throw new RuntimeException("A.x called");
                    }
                }
                """);

        Files.writeString(libB, """
                package library.tmp_test;
                public class B extends A {
                    public void x() {
                        // Correctly overrides A.x
                    }
                }
                """);

        try {
            MergeFiles.export(name, "solver/src/main/java/Main.java", output.toString());

            String mergedContent = Files.readString(output);

            assertTrue(mergedContent.contains("class A"), "Merged file missing class A");
            assertTrue(mergedContent.contains("class B"), "Merged file missing class B");

            boolean ok = compileAndRun(output);
            assertTrue(ok, "Merged code failed to run correctly (likely B.x was pruned and A.x was called)");

        } finally {
            deleteDirectory(tempDir);
            Files.deleteIfExists(libA);
            Files.deleteIfExists(libB);
            Files.deleteIfExists(libDir);
        }
    }

    private void deleteDirectory(Path path) throws IOException {
        if (!Files.exists(path)) return;
        try (Stream<Path> walk = Files.walk(path)) {
            walk.sorted(Comparator.reverseOrder())
                .forEach(p -> {
                    try {
                        Files.delete(p);
                    } catch (IOException e) {
                        // ignore
                    }
                });
        }
    }

    private static boolean compileAndRun(Path javaFile) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
        Iterable<? extends JavaFileObject> units = fileManager.getJavaFileObjects(javaFile.toFile());

        Path compileOut = Files.createTempDirectory("compile_run_out");
        List<String> options = List.of("-d", compileOut.toString(), "--release", "21");

        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, options, null, units);
        boolean success = task.call();
        fileManager.close();

        if (!success) {
            for (Diagnostic<?> d : diagnostics.getDiagnostics()) {
                System.err.println(d.getMessage(null));
            }
            return false;
        }

        try {
            Process p = Runtime.getRuntime().exec(new String[]{"java", "-cp", compileOut.toString(), "Main"});
            int exitCode = p.waitFor();
            // Clean up compileOut
            Files.walk(compileOut).sorted(Comparator.reverseOrder()).forEach(path -> {
                try { Files.delete(path); } catch (Exception e) {}
            });
            return exitCode == 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
