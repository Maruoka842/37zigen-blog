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

public class MergeFilesCompileCheck {

    @Test
    public void testUserBug() throws IOException {
        String name = "Main";
        Path solverDir = Paths.get("solver/src/main/java");
        Files.createDirectories(solverDir);
        Path input = solverDir.resolve("Main.java");

        Path tempDir = Files.createTempDirectory("merge_test");
        Path output = tempDir.resolve("Main.java");

        Files.writeString(input, """
                import java.io.IOException;
                import java.util.ArrayDeque;
                import java.util.Arrays;
                import java.util.Queue;
                import java.util.Random;

                import library.tools.FastScanner;
                import library.tools.MergeFiles;
                import library.tools.MyPrintWriter;
                import library.util.ArrayUtils;
                import library.util.graph.Graph;

                public class Main {
			static MyPrintWriter pw = MyPrintWriter.getInstance();
			static FastScanner sc = FastScanner.getInstance();


			public static void main(String[] args) throws IOException {
				new Main().run();
				pw.flush();
				MergeFiles.export();
			}

			void run() {
				Random rnd=new Random();

				int N=sc.nextInt();
				int M=sc.nextInt();
				long[]W=sc.nextLongs(N);
				Graph g=Graph.read(N, M);
				long INF=Long.MAX_VALUE/3;
				long[]ans=new long[N];
				Arrays.fill(ans, INF);
				ans[0]=0;
				long[][]dp=new long[N][N];
				ArrayUtils.fill(dp, INF);
				Arrays.fill(dp[0], 0);
				for (int d = N-1; d >=1; d--) {
					for (int i = 0; i < N; i++) {
						if(dp[i][d]==INF)continue;
						for (int v : g.adj[i]) {
							dp[v][d-1]=Math.min(dp[v][d-1], dp[i][d]+W[i]*d);
						}
					}
				}
				for (int i = 0; i < N; i++) {
					long min=ArrayUtils.min(dp[i]);
					pw.println(min);
				}

			}


			void tr(Object... objects) {
				System.out.println(Arrays.deepToString(objects));
			}
                }
                """);

        System.out.println("=== MergeFilesBugFixTest start ===");

        MergeFiles.export(name, "solver/src/main/java/Main.java", output.toString());

        String mergedContent = Files.readString(output);
        boolean ok = compileJava(output);

        if (!ok) {
            System.out.println("--- Generated code (excerpt) ---");
            String[] lines = mergedContent.split("\n");
            for (int i = 0; i < Math.min(lines.length, 100); i++) {
                System.out.println((i+1) + ": " + lines[i]);
            }
        }

        try {
            assertTrue(ok, "Merged Main.java failed to compile");
            assertTrue(mergedContent.contains("class IntArrayList"), "Merged file missing IntArrayList");
            assertTrue(!mergedContent.contains("class MaxFlow"), "Merged file should not contain MaxFlow (unused)");
            System.out.println("✅ MergeFilesBugFixTest passed");
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
                        // ignore
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

        Path compileOut = Files.createTempDirectory("compile_out");
        List<String> options = List.of("-d", compileOut.toString(), "--release", "21");

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