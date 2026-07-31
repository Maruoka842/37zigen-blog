package library.tools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public class MergeFilesTest {

    private static final String SRC_DIR = "C:\\Users\\forto\\eclipse-workspace\\Main\\src\\test\\mergeTests\\in"; // テスト用Main群を置くフォルダ
    private static final String OUTPUT_DIR = "C:\\Users\\forto\\eclipse-workspace\\Main\\src\\test\\mergeTests\\out";

    public static void test() throws IOException {
        Files.createDirectories(Paths.get(OUTPUT_DIR));

        List<Path> testFiles = Files.walk(Paths.get(SRC_DIR))
                .filter(p -> p.toString().endsWith(".java"))
                .toList();

        System.out.println("=== MergeFiles test start ===");
        int success = 0;

        for (Path input : testFiles) {
            String name = input.getFileName().toString().replace(".java", "");
            Path output = Paths.get(OUTPUT_DIR, name + "_merged.java");
            System.out.println("[TEST] " + name);
            System.out.println(output.getFileName());
            try {
                // --- 1. マージ実行 ---
                MergeFiles.export(name, input.toString(), output.toString());

                // --- 2. コンパイルチェック ---
                boolean ok = compileJava(output);
                if (ok) {
                    System.out.println("✅ " + name + " → compile OK\n");
                    success++;
                } else {
                    System.out.println("❌ " + name + " → compile FAILED\n");
                }

            } catch (Exception e) {
                System.out.println("❌ Exception in " + name + ": " + e);
                e.printStackTrace();
            }
        }

        System.out.printf("=== Result: %d / %d succeeded ===%n", success, testFiles.size());
    }

    /** javac API を用いて単一ファイルをコンパイルする */
    private static boolean compileJava(Path javaFile) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) throw new IllegalStateException("JDKで実行してください（JREではなく）");

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

        Iterable<? extends JavaFileObject> units = fileManager.getJavaFileObjects(javaFile.toFile());

        List<String> options = List.of("-d", "out/compiled", "--release", "17"); // 必要に応じて変更
        Files.createDirectories(Paths.get("out/compiled"));

        JavaCompiler.CompilationTask task =
                compiler.getTask(null, fileManager, diagnostics, options, null, units);

        boolean success = task.call();
        fileManager.close();

        if (!success) {
            System.err.println("Compilation errors for " + javaFile + ":");
            for (Diagnostic<?> d : diagnostics.getDiagnostics()) {
                System.err.printf("  [%s] line %d: %s%n", d.getKind(), d.getLineNumber(), d.getMessage(null));
            }
        }
        return success;
    }
}
