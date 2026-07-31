import os
import sys
import subprocess
import argparse

def get_maven_prefix():
    maven_path = "C:\\apache-maven-3.9.15\\bin"
    if sys.platform == 'win32' and os.path.exists(maven_path):
        return f'set PATH={maven_path};%PATH% && '
    return ""

HARNESS_SOURCE = r'''
import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class StressHarness {
    static class TaskResult {
        String stdout;
        String stderr;
        boolean timeout;
        Throwable error;

        TaskResult(String stdout, String stderr, boolean timeout, Throwable error) {
            this.stdout = stdout;
            this.stderr = stderr;
            this.timeout = timeout;
            this.error = error;
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 5) {
            System.err.println("Usage: java StressHarness <maxTests> <timeoutMs> <genClass> <naiveClass> <targetClass> <cp...>");
            System.exit(1);
        }

        int maxTests = Integer.parseInt(args[0]);
        long timeoutMs = Long.parseLong(args[1]);
        String genClass = args[2];
        String naiveClass = args[3];
        String targetClass = args[4];

        List<URL> urls = new ArrayList<>();
        for (int i = 5; i < args.length; i++) {
            String path = args[i];
            if (path.endsWith("*")) {
                Path dir = Paths.get(path.substring(0, path.length() - 1));
                if (Files.exists(dir) && Files.isDirectory(dir)) {
                    try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.jar")) {
                        for (Path entry : stream) {
                            urls.add(entry.toUri().toURL());
                        }
                    }
                }
            } else {
                Path p = Paths.get(path);
                if (Files.exists(p)) {
                    urls.add(p.toUri().toURL());
                }
            }
        }
        URL[] urlArray = urls.toArray(new URL[0]);

        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        InputStream originalIn = System.in;

        ExecutorService executor = Executors.newSingleThreadExecutor();

        try {
            for (int i = 1; i <= maxTests; i++) {
                TaskResult genRes = runTask(executor, urlArray, genClass, null, timeoutMs);
                if (genRes.timeout) {
                    originalOut.println("\n[!] Generator TLE at test case #" + i);
                    saveFailed(null, null, null);
                    return;
                }
                if (genRes.error != null) {
                    originalOut.println("\n[!] Generator crashed at test case #" + i);
                    genRes.error.printStackTrace(originalErr);
                    saveFailed(null, null, null);
                    return;
                }
                String input = genRes.stdout;

                TaskResult naiveRes = runTask(executor, urlArray, naiveClass, input, timeoutMs);
                if (naiveRes.timeout) {
                    originalOut.println("\n[!] Naive TLE at test case #" + i);
                    saveFailed(input, null, null);
                    return;
                }
                if (naiveRes.error != null) {
                    originalOut.println("\n[!] Naive crashed at test case #" + i);
                    originalOut.println("Stderr: " + naiveRes.stderr);
                    naiveRes.error.printStackTrace(originalErr);
                    saveFailed(input, null, null);
                    return;
                }

                TaskResult targetRes = runTask(executor, urlArray, targetClass, input, timeoutMs);
                if (targetRes.timeout) {
                    originalOut.println("\n[!] Target TLE at test case #" + i);
                    saveFailed(input, naiveRes.stdout, null);
                    return;
                }
                if (targetRes.error != null) {
                    originalOut.println("\n[!] Target crashed at test case #" + i);
                    originalOut.println("Stderr: " + targetRes.stderr);
                    targetRes.error.printStackTrace(originalErr);
                    saveFailed(input, naiveRes.stdout, null);
                    return;
                }

                if (!compare(naiveRes.stdout, targetRes.stdout)) {
                    originalOut.println("\n[!] FAIL detected at test case #" + i + "! Reason: Wrong Answer");
                    originalOut.println("============================================================");
                    originalOut.println("--- INPUT ---");
                    originalOut.println(input);
                    originalOut.println("--- NAIVE OUTPUT ---");
                    originalOut.println(naiveRes.stdout);
                    if (naiveRes.stderr != null && !naiveRes.stderr.isEmpty()) {
                        originalOut.println("--- NAIVE STDERR ---");
                        originalOut.println(naiveRes.stderr);
                    }
                    originalOut.println("--- TARGET OUTPUT ---");
                    originalOut.println(targetRes.stdout);
                    if (targetRes.stderr != null && !targetRes.stderr.isEmpty()) {
                        originalOut.println("--- TARGET STDERR ---");
                        originalOut.println(targetRes.stderr);
                    }
                    originalOut.println("============================================================");
                    saveFailed(input, naiveRes.stdout, targetRes.stdout);
                    return;
                }

                if (i % 10 == 0 || i == maxTests) {
                    originalOut.println("[+] Passed " + i + "/" + maxTests + " cases...");
                }
            }
            originalOut.println("[+] All tests passed successfully!");
        } finally {
            executor.shutdownNow();
            System.setOut(originalOut);
            System.setErr(originalErr);
            System.setIn(originalIn);
        }
    }

    private static TaskResult runTask(ExecutorService executor, URL[] urls, String className, String input, long timeoutMs) {
        Future<TaskResult> future = executor.submit(() -> {
            PrintStream oldOut = System.out;
            PrintStream oldErr = System.err;
            InputStream oldIn = System.in;

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayOutputStream err = new ByteArrayOutputStream();
            try (PrintStream psOut = new PrintStream(out);
                 PrintStream psErr = new PrintStream(err);
                 URLClassLoader loader = new URLClassLoader(urls, ClassLoader.getPlatformClassLoader())) {

                System.setOut(psOut);
                System.setErr(psErr);
                if (input != null) {
                    System.setIn(new ByteArrayInputStream(input.getBytes()));
                }

                Class<?> clazz = loader.loadClass(className);
                Method main = clazz.getMethod("main", String[].class);
                main.invoke(null, (Object) new String[0]);

                psOut.flush();
                psErr.flush();
                return new TaskResult(out.toString(), err.toString(), false, null);
            } catch (InvocationTargetException e) {
                return new TaskResult(out.toString(), err.toString(), false, e.getCause());
            } catch (Throwable t) {
                return new TaskResult(out.toString(), err.toString(), false, t);
            } finally {
                System.setOut(oldOut);
                System.setErr(oldErr);
                System.setIn(oldIn);
            }
        });

        try {
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            return new TaskResult(null, null, true, null);
        } catch (ExecutionException e) {
            return new TaskResult(null, null, false, e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new TaskResult(null, null, false, e);
        }
    }

    private static boolean compare(String s1, String s2) {
        List<String> l1 = normalize(s1);
        List<String> l2 = normalize(s2);
        return l1.equals(l2);
    }

    private static List<String> normalize(String s) {
        if (s == null) return Collections.emptyList();
        return Arrays.stream(s.split("\\r?\\n"))
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .collect(Collectors.toList());
    }

    private static void saveFailed(String input, String naive, String target) {
        try {
            if (input != null) Files.writeString(Paths.get("failed_input.txt"), input);
            if (naive != null) Files.writeString(Paths.get("failed_naive_out.txt"), naive);
            if (target != null) Files.writeString(Paths.get("failed_target_out.txt"), target);
            System.out.println("[*] Saved failed cases to failed_*.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
'''

def main():
    parser = argparse.ArgumentParser(description="In-JVM Stress Checker for Java Solvers")
    parser.add_argument("--max_tests", type=int, default=100, help="Maximum number of test cases")
    parser.add_argument("--timeout", type=float, default=2.0, help="Timeout in seconds for each task")
    parser.add_argument("--fast", action="store_true", help="Skip 'mvn test-compile' and use existing classes")
    parser.add_argument("--no-compile", action="store_true", help="Skip all compilation steps")
    args = parser.parse_args()

    print("Note: in-JVM mode does not support System.exit() or Runtime.halt() in tested code.")

    cp_sep = ';' if sys.platform == 'win32' else ':'
    maven_prefix = get_maven_prefix()

    proj_root = os.getcwd()
    lib_classes = os.path.join(proj_root, "target", "classes")
    lib_test_classes = os.path.join(proj_root, "target", "test-classes")
    lib_deps_dir = os.path.join(proj_root, "target", "dependency")
    solver_src = os.path.join(proj_root, "solver", "src", "main", "java")

    if not args.no_compile:
        if not args.fast:
            cmd = f"{maven_prefix}mvn test-compile dependency:copy-dependencies -DskipTests"
            print(f"[*] Running: {cmd}", flush=True)
            subprocess.run(cmd, shell=True, check=True)

        # Compile Main.java
        main_java = os.path.join(solver_src, "Main.java")
        classpath = cp_sep.join([lib_classes, lib_test_classes, os.path.join(lib_deps_dir, "*")])
        cmd = f'javac -cp "{classpath}" "{main_java}"'
        print(f"[*] Running: {cmd}", flush=True)
        subprocess.run(cmd, shell=True, check=True)

        # Generate and compile StressHarness
        with open("StressHarness.java", "w", encoding="utf-8") as f:
            f.write(HARNESS_SOURCE)
        cmd = f"javac StressHarness.java"
        print(f"[*] Running: {cmd}", flush=True)
        subprocess.run(cmd, shell=True, check=True)

    # Run StressHarness
    harness_cp = cp_sep.join([".", solver_src, lib_classes, lib_test_classes, os.path.join(lib_deps_dir, "*")])

    gen_class = "library.tools.test.Generator"
    naive_class = "library.tools.test.Naive"
    target_class = "Main"

    timeout_ms = int(args.timeout * 1000)

    cmd = [
        "java", "-cp", harness_cp, "StressHarness",
        str(args.max_tests), str(timeout_ms),
        gen_class, naive_class, target_class,
        solver_src, lib_classes, lib_test_classes, os.path.join(lib_deps_dir, "*"), "."
    ]

    print(f"[*] Starting StressHarness (In-JVM)...", flush=True)
    subprocess.run(cmd)

if __name__ == "__main__":
    main()
