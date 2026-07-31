# -*- coding: utf-8 -*-
import subprocess
import sys
import os
import argparse
import time

# Windowsでの標準出力エンコーディングエラー回避 (未テスト, 計算量: O(1))
if sys.platform == 'win32':
    try:
        sys.stdout.reconfigure(encoding='utf-8')
        sys.stderr.reconfigure(encoding='utf-8')
    except Exception:
        pass

# 未テスト
# 計算量: O(F) (F is the number of files in search_dir)
def detect_file(patterns, search_dir='.'):
    """
    指定ディレクトリから指定されたパターンのいずれかにマッチするファイルを探します。
    大文字小文字を区別せずにチェックします。
    """
    try:
        files = os.listdir(search_dir)
    except Exception:
        return None
    for pattern in patterns:
        for f in files:
            if f.lower() == pattern.lower():
                return os.path.join(search_dir, f)
    return None

# 未テスト
# 計算量: O(D) (D is the depth of the directory structure to resolve paths)
def get_commands_for_java_maven(filepath):
    """
    src/main/java または src/test/java に配置された Java ファイルに対して、
    Maven を使ったコンパイルコマンドと実行コマンドを生成します。
    """
    abs_path = os.path.abspath(filepath)
    base = os.path.basename(abs_path)
    classname, _ = os.path.splitext(base)
    norm_path = abs_path.replace('\\', '/')

    # Maven を環境変数 PATH に追加するプレフィックス
    maven_path = "C:\\apache-maven-3.9.15\\bin"
    if sys.platform == 'win32' and os.path.exists(maven_path):
        # cmd.exeで実行されるため、set PATH を使用
        maven_prefix = f'set PATH={maven_path};%PATH% && '
    else:
        maven_prefix = ""
    cp_sep = ';' if sys.platform == 'win32' else ':'

    # --- src/test/java のケース (library プロジェクトのテストソース) ---
    test_pattern = "/src/test/java/"
    if test_pattern in norm_path:
        idx = norm_path.rfind(test_pattern)
        source_root = os.path.abspath(abs_path[:idx + len(test_pattern) - 1])
        proj_root = os.path.dirname(os.path.dirname(os.path.dirname(source_root)))
        pom_path = os.path.join(proj_root, 'pom.xml')
        if os.path.exists(pom_path):
            pkg = get_java_package(filepath)
            fqcn = f"{pkg}.{classname}" if pkg else classname
            compile_cmd = f'{maven_prefix}mvn -f "{pom_path}" test-compile dependency:copy-dependencies -DskipTests'
            test_classes = os.path.join(proj_root, 'target', 'test-classes')
            main_classes = os.path.join(proj_root, 'target', 'classes')
            deps = os.path.join(proj_root, 'target', 'dependency', '*')
            classpath_str = cp_sep.join([test_classes, main_classes, deps])
            run_cmd = f'java -cp "{classpath_str}" {fqcn}'
            return compile_cmd, run_cmd

    # --- src/main/java のケース ---
    main_pattern = "/src/main/java/"
    if main_pattern in norm_path:
        idx = norm_path.rfind(main_pattern)
        source_root = os.path.abspath(abs_path[:idx + len(main_pattern) - 1])
        # source_root の3つ上がプロジェクトルート (xxx/src/main/java -> xxx)
        proj_root = os.path.dirname(os.path.dirname(os.path.dirname(source_root)))
        pom_path = os.path.join(proj_root, 'pom.xml')

        if not os.path.exists(pom_path):
            # pom.xml がない (solver/ のような子ディレクトリ): 親ディレクトリを探す
            parent_root = os.path.dirname(proj_root)
            parent_pom = os.path.join(parent_root, 'pom.xml')
            if os.path.exists(parent_pom):
                library_classes = os.path.join(parent_root, 'target', 'classes')
                library_deps = os.path.join(parent_root, 'target', 'dependency', '*')
                # Maven で library をビルドしてから javac で Main.java をコンパイル
                # Main.class は Main.java と同じ source_root に出力
                compile_cmd = (
                    f'{maven_prefix}mvn -f "{parent_pom}" package dependency:copy-dependencies -DskipTests'
                    f' && javac -cp "{library_classes}{cp_sep}{library_deps}" "{abs_path}"'
                )
                # 実行時クラスパス: Main.class のあるディレクトリ + library
                classpath_str = cp_sep.join([source_root, library_classes, library_deps])
                run_cmd = f'java -cp "{classpath_str}" {classname}'
                return compile_cmd, run_cmd
            return None, None

        # pom.xml がある: 通常の Maven プロジェクト
        compile_cmd = f'{maven_prefix}mvn -f "{pom_path}" package dependency:copy-dependencies -DskipTests'
        main_classes = os.path.join(proj_root, 'target', 'classes')
        deps = os.path.join(proj_root, 'target', 'dependency', '*')
        pkg = get_java_package(filepath)
        fqcn = f"{pkg}.{classname}" if pkg else classname
        classpath_str = cp_sep.join([main_classes, deps])
        run_cmd = f'java -cp "{classpath_str}" {fqcn}'
        return compile_cmd, run_cmd

    return None, None
# 未テスト
# 計算量: O(L) (L is the number of characters in the first few lines of the file)
def get_java_package(filepath):
    """
    Javaファイルからパッケージ名を取得します。
    """
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            for _ in range(50):  # 最初の50行を確認
                line = f.readline()
                if not line:
                    break
                line = line.strip()
                if line.startswith('package '):
                    # 'package a.b.c;' -> 'a.b.c'
                    parts = line.split()
                    if len(parts) >= 2:
                        pkg = parts[1].rstrip(';')
                        return pkg
    except Exception:
        pass
    return None

# 未テスト
# 計算量: O(D) (D is the depth of the directory structure to resolve paths)
def get_commands_for_java_fast(filepath):
    """
    Maven を使わず javac で直接コンパイルするコマンドを生成する。
    事前条件: library の target/classes および target/dependency/* が存在すること。
    戻り値: (compile_cmd, run_cmd)。library のビルド済み成果物が見つからない場合は (None, None)。
    """
    abs_path = os.path.abspath(filepath)
    base = os.path.basename(abs_path)
    classname, _ = os.path.splitext(base)
    norm_path = abs_path.replace('\\', '/')
    cp_sep = ';' if sys.platform == 'win32' else ':'

    main_pattern = "/src/main/java/"
    if main_pattern in norm_path:
        idx = norm_path.rfind(main_pattern)
        source_root = os.path.abspath(abs_path[:idx + len(main_pattern) - 1])
        proj_root = os.path.dirname(os.path.dirname(os.path.dirname(source_root)))
        pom_path = os.path.join(proj_root, 'pom.xml')

        if not os.path.exists(pom_path):
            parent_root = os.path.dirname(proj_root)
            library_classes = os.path.join(parent_root, 'target', 'classes')
            library_deps = os.path.join(parent_root, 'target', 'dependency', '*')
            if os.path.exists(library_classes):
                compile_cmd = f'javac -cp "{library_classes}{cp_sep}{library_deps}" "{abs_path}"'
                classpath_str = cp_sep.join([source_root, library_classes, library_deps])
                run_cmd = f'java -cp "{classpath_str}" {classname}'
                return compile_cmd, run_cmd

    test_pattern = "/src/test/java/"
    if test_pattern in norm_path:
        idx = norm_path.rfind(test_pattern)
        source_root = os.path.abspath(abs_path[:idx + len(test_pattern) - 1])
        proj_root = os.path.dirname(os.path.dirname(os.path.dirname(source_root)))
        library_classes = os.path.join(proj_root, 'target', 'classes')
        library_deps = os.path.join(proj_root, 'target', 'dependency', '*')
        if os.path.exists(library_classes):
            pkg = get_java_package(filepath)
            fqcn = f"{pkg}.{classname}" if pkg else classname
            test_classes = os.path.join(proj_root, 'target', 'test-classes')
            compile_cmd = f'javac -cp "{library_classes}{cp_sep}{library_deps}" -d "{test_classes}" "{abs_path}"'
            classpath_str = cp_sep.join([test_classes, library_classes, library_deps])
            run_cmd = f'java -cp "{classpath_str}" {fqcn}'
            return compile_cmd, run_cmd

    return None, None

# 未テスト
# 計算量: O(D + L) (D is the depth of the directory structure, L is package search time)
def get_commands_for_file(filepath, fast=False):
    """
    ファイルの拡張子に基づいて、コンパイルコマンドと実行コマンドを返します。
    戻り値: (compile_cmd, run_cmd)
    """
    if not filepath:
        return None, None
    
    base, ext = os.path.splitext(filepath)
    ext = ext.lower()
    
    if ext == '.java':
        # Maven構成用（依存関係の解決が必要な場合）を優先して試す
        if fast:
            comp, run = get_commands_for_java_fast(filepath)
        else:
            comp, run = get_commands_for_java_maven(filepath)
        if comp and run:
            return comp, run
            
        compile_cmd = f"javac {filepath}"
        
        pkg = get_java_package(filepath)
        if pkg:
            # パッケージがある場合、階層数分だけ親に遡るパスをクラスパスに設定
            depth = len(pkg.split('.'))
            cp_dir = "/".join([".."] * depth)
            run_cmd = f"java -cp {cp_dir} {pkg}.{base}"
        else:
            run_cmd = f"java {base}"
            
        return compile_cmd, run_cmd
    elif ext == '.cpp':
        if sys.platform == 'win32':
            exe_name = f"{base}.exe"
            compile_cmd = f"g++ -O3 -std=c++17 {filepath} -o {exe_name}"
            run_cmd = f".\\{exe_name}"
        else:
            exe_name = f"./{base}"
            compile_cmd = f"g++ -O3 -std=c++17 {filepath} -o {exe_name}"
            run_cmd = exe_name
        return compile_cmd, run_cmd
    elif ext == '.py':
        run_cmd = f'"{sys.executable}" {filepath}'
        return None, run_cmd
    
    return None, None

# 未テスト
# 計算量: O(N) (N is the number of compilation commands)
def combine_compile_commands(comp_cmds_dict):
    """
    各言語のコンパイルコマンドを最適化して結合します。
    オプションのない単純な javac コマンドは1つにまとめ、
    特殊なオプションを持つコマンドは個別に実行します。
    """
    simple_java_files = []
    other_cmds = []
    for filepath, comp in comp_cmds_dict.items():
        if not comp:
            continue
        base, ext = os.path.splitext(filepath)
        if ext.lower() == '.java' and comp == f"javac {filepath}":
            simple_java_files.append(filepath)
        else:
            if comp not in other_cmds:  # 重複除去
                other_cmds.append(comp)

    combined = []
    if simple_java_files:
        combined.append(f"javac {' '.join(simple_java_files)}")
    combined.extend(other_cmds)
    
    if combined:
        return " && ".join(combined)
    return ""

# 未テスト
# 計算量: O(F) (F is the number of files in the current directory, plus path existence checks)
def auto_detect_commands(fast=False):
    """
    カレントディレクトリのファイルを自動検出し、対応するコンパイルコマンドと実行コマンドを生成します。
    """
    # スクリプトの場所からプロジェクトルートを特定 (stress.py は library/ 直下)
    script_dir = os.path.dirname(os.path.abspath(__file__))
    project_root = script_dir  # library/ 自体がプロジェクトルート

    # Generator/Naive の固定検索ディレクトリ (src/test/java/library/tools/test/)
    test_dir = os.path.join(script_dir, 'src', 'test', 'java', 'library', 'tools', 'test')

    gen_patterns = ['Generator.java', 'generator.py', 'generator.cpp', 'gen.py', 'gen.cpp', 'Generator.cpp']
    naive_patterns = ['Naive.java', 'naive.py', 'naive.cpp', 'Naive.cpp']
    target_patterns = ['Target.java', 'target.py', 'target.cpp', 'Target.cpp', 'main.cpp', 'Main.java']

    # まずカレントディレクトリ、次に src/test/java/library/tools/test/ を探す
    gen_file = detect_file(gen_patterns) or detect_file(gen_patterns, test_dir)
    naive_file = detect_file(naive_patterns) or detect_file(naive_patterns, test_dir)

    # プロジェクト内の solver/src/main/java/Main.java を最優先の target ファイル候補とする
    solver_main = os.path.join(project_root, 'solver', 'src', 'main', 'java', 'Main.java')
    if os.path.exists(solver_main):
        target_file = solver_main
    else:
        target_file = detect_file(target_patterns)

    gen_comp, gen_run = get_commands_for_file(gen_file, fast=fast)
    naive_comp, naive_run = get_commands_for_file(naive_file, fast=fast)
    target_comp, target_run = get_commands_for_file(target_file, fast=fast)

    comp_dict = {}
    if gen_file and gen_comp:
        comp_dict[gen_file] = gen_comp
    if naive_file and naive_comp:
        comp_dict[naive_file] = naive_comp
    if target_file and target_comp:
        comp_dict[target_file] = target_comp

    compile_cmd = combine_compile_commands(comp_dict)

    return gen_run, naive_run, target_run, compile_cmd

# 未テスト
# 計算量: O(N) (N is the length of command and input_data size, plus process execution time)
def run_command(cmd, input_data=None, timeout=5.0):
    """
    指定されたコマンドを実行し、(stdout, stderr, returncode, is_timeout) を返します。
    """
    try:
        proc = subprocess.Popen(
            cmd,
            shell=True,
            stdin=subprocess.PIPE,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
            encoding='utf-8',
            errors='ignore'
        )
        stdout, stderr = proc.communicate(input=input_data, timeout=timeout)
        return stdout, stderr, proc.returncode, False
    except subprocess.TimeoutExpired as e:
        # プロセスを強制終了する
        if sys.platform == 'win32':
            subprocess.call(['taskkill', '/F', '/T', '/PID', str(proc.pid)], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
        else:
            proc.kill()
        stdout, stderr = proc.communicate()
        return stdout, stderr, -1, True
    except Exception as e:
        return "", str(e), -1, False

# 未テスト
# 計算量: O(T * (G + N + C)) (T: max_tests, G/N/C: execution times of generator, naive, and target)
def start_stress_test(args):
    """
    ストレスチェックのメインループを実行します。
    """
    # 事前コンパイルコマンドがあれば実行
    if args.compile_cmd:
        print(f"[*] Running compilation command: {args.compile_cmd}")
        stdout, stderr, code, is_timeout = run_command(args.compile_cmd, timeout=180.0)
        if code != 0:
            print("[-] Compilation failed!")
            print(f"Stdout:\n{stdout}")
            print(f"Stderr:\n{stderr}")
            sys.exit(1)
        print("[+] Compilation successful.")

    print(f"[*] Starting stress test (Max tests: {args.max_tests})...")
    print(f"    Generator command: {args.gen_cmd}")
    print(f"    Naive solver command: {args.naive_cmd}")
    print(f"    Target solver command: {args.target_cmd}")
    print("-" * 60)

    for i in range(1, args.max_tests + 1):
        # 1. ジェネレーターを動かしてテストケースを生成
        gen_out, gen_err, gen_code, gen_timeout = run_command(args.gen_cmd)
        if gen_code != 0 or gen_timeout:
            print(f"[-] Generator failed at test case #{i}!")
            print(f"Stdout: {gen_out}")
            print(f"Stderr: {gen_err}")
            sys.exit(1)

        # 2. 愚直解を動かす
        naive_out, naive_err, naive_code, naive_timeout = run_command(args.naive_cmd, input_data=gen_out, timeout=args.timeout)
        # 3. テスト対象コードを動かす
        target_out, target_err, target_code, target_timeout = run_command(args.target_cmd, input_data=gen_out, timeout=args.timeout)

        # 判定
        failed = False
        reason = ""

        if target_timeout:
            failed = True
            reason = "Target solver TLE (Timeout)"
        elif target_code != 0:
            failed = True
            reason = f"Target solver crashed (exit code {target_code})"
        elif naive_timeout:
            failed = True
            reason = "Naive solver TLE (Timeout)"
        elif naive_code != 0:
            failed = True
            reason = f"Naive solver crashed (exit code {naive_code})"
        else:
            # 出力比較 (トリムして空白行の違いなどを緩和)
            naive_lines = [line.strip() for line in naive_out.strip().splitlines() if line.strip()]
            target_lines = [line.strip() for line in target_out.strip().splitlines() if line.strip()]
            if naive_lines != target_lines:
                failed = True
                reason = "Wrong Answer (Output mismatch)"

        if failed:
            print(f"\n[!] FAIL detected at test case #{i}! Reason: {reason}")
            print("=" * 60)
            print("--- INPUT ---")
            print(gen_out)
            print("--- NAIVE OUTPUT ---")
            print(naive_out)
            if naive_err:
                print(f"(Stderr: {naive_err})")
            print("--- TARGET OUTPUT ---")
            print(target_out)
            if target_err:
                print(f"(Stderr: {target_err})")
            print("=" * 60)

            # ファイルに保存
            with open("failed_input.txt", "w", encoding="utf-8") as f:
                f.write(gen_out)
            with open("failed_naive_out.txt", "w", encoding="utf-8") as f:
                f.write(naive_out)
            with open("failed_target_out.txt", "w", encoding="utf-8") as f:
                f.write(target_out)
            print("[*] Saved input to 'failed_input.txt'")
            print("[*] Saved naive output to 'failed_naive_out.txt'")
            print("[*] Saved target output to 'failed_target_out.txt'")
            sys.exit(0)

        if i % 10 == 0 or i == args.max_tests:
            print(f"[+] Passed {i}/{args.max_tests} cases...")

    print("[+] All tests passed successfully!")

# 未テスト
# 計算量: O(F) (F is the number of files in the current directory, plus argument parsing and dispatching)
def main():
    parser = argparse.ArgumentParser(description="Stress checker for competitive programming solvers.")
    parser.add_argument("--gen_cmd", default=None, help="Command to run the generator (e.g. 'python gen.py')")
    parser.add_argument("--naive_cmd", default=None, help="Command to run the naive/correct solver (e.g. 'python naive.py')")
    parser.add_argument("--target_cmd", default=None, help="Command to run the target solver to test")
    parser.add_argument("--compile_cmd", default=None, help="Optional compilation command run once before testing")
    parser.add_argument("--max_tests", type=int, default=100, help="Maximum number of test iterations")
    parser.add_argument("--timeout", type=float, default=2.0, help="Time limit for each run in seconds")
    parser.add_argument("--fast", action="store_true", help="Skip Maven, use javac directly (requires library already built)")
    parser.add_argument("--no-compile", action="store_true", help="Skip compilation entirely")

    args = parser.parse_args()

    # 自動検出を実行
    auto_gen, auto_naive, auto_target, auto_compile = auto_detect_commands(fast=args.fast)

    # 引数が指定されていなければ自動検出したコマンドを使用
    if args.gen_cmd is None:
        args.gen_cmd = auto_gen
    if args.naive_cmd is None:
        args.naive_cmd = auto_naive
    if args.target_cmd is None:
        args.target_cmd = auto_target
    if args.compile_cmd is None:
        args.compile_cmd = auto_compile

    # --no-compile ならコンパイルをスキップ
    if args.no_compile:
        args.compile_cmd = None

    # 必須コマンドが設定されているか確認
    missing = []
    if not args.gen_cmd:
        missing.append("--gen_cmd (or Generator file)")
    if not args.naive_cmd:
        missing.append("--naive_cmd (or Naive file)")
    if not args.target_cmd:
        missing.append("--target_cmd (or Target file)")

    if missing:
        parser.print_help()
        print(f"\n[-] Error: Missing required commands: {', '.join(missing)}")
        sys.exit(1)

    start_stress_test(args)

if __name__ == "__main__":
    main()
