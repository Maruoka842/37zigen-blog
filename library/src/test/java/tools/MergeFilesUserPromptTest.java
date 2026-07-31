package tools;

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

public class MergeFilesUserPromptTest {

    @Test
    public void testUserPromptCase() throws IOException {
        String name = "Main";
        Path solverDir = Paths.get("solver/src/main/java");
        Files.createDirectories(solverDir);
        Path input = solverDir.resolve("Main.java");

        Path tempDir = Files.createTempDirectory("merge_test_user");
        Path output = tempDir.resolve("Main.java");

        Files.writeString(input, """
import java.io.IOException; import java.util.Arrays;

import library.tools.FastScanner; import library.tools.MergeFiles; import library.tools.MyPrintWriter; import library.util.ArrayUtils; import library.util.graph.BipartiteMatching; import library.util.polynomial.PolynomialFpDynamic;

public class Main implements Runnable { static FastScanner sc = FastScanner.getInstance();

static MyPrintWriter pw = MyPrintWriter.getInstance();

public static void main(String[] args) throws IOException {
	new Main().run();
	pw.flush();
	MergeFiles.export();
}

public void run() {
	int L=sc.nextInt();
	int R=sc.nextInt();
	int M=sc.nextInt();
	BipartiteMatching g=new BipartiteMatching(L, R);
	for (int i = 0; i < M; i++) {
		int a=sc.nextInt();
		int b=sc.nextInt();
		g.addEdge(a, b);
	}
	int K=g.calc();
	pw.println(K);
	for (int i = 0; i < L; i++) {
		if(g.fromLtoR(i) != -1) {
			pw.println(i + " " + g.fromLtoR(i));
		}
	}
}

long[] powAll(long[][] f, PolynomialFpDynamic P) {
	ArrayUtils.sort(f);
	long[]ret=new long[] {1};
	for (int i = 0; i < f.length; i++) {
		int j=i;
		while(j+1<f.length && Arrays.equals(f[i], f[j+1]))j++;
		ret=P.mul(ret, P.powFull(f[i], j-i+1));
		i=j;
	}
	return ret;
}

int id(int val, int M) {
	if (val == M)
		return 2;
	if (val == M - 1)
		return 1;
	return 0;
}

void tr(Object object) {
	tr(new Object[] { object });
}

void tr(Object... objects) {
	System.out.println(Arrays.deepToString(objects));
}
}
                """);

        MergeFiles.export(name, "solver/src/main/java/Main.java", output.toString());

        String mergedContent = Files.readString(output);
        boolean ok = compileJava(output);

        if (!ok) {
            System.err.println("Compilation failed for merged file");
        }

        try {
            assertTrue(ok, "Merged Main.java failed to compile");
            assertTrue(mergedContent.contains("class BipartiteMatching"), "Merged file missing BipartiteMatching");
            assertFalse(mergedContent.contains("class PolynomialFpDynamic"), "Merged file should not contain PolynomialFpDynamic (unused)");
            assertFalse(mergedContent.contains("class Fp"), "Merged file should not contain Fp (unused)");
            assertFalse(mergedContent.contains("class Zn"), "Merged file should not contain Zn (unused)");
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
        Path compileOut = Files.createTempDirectory("compile_out_user");
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
