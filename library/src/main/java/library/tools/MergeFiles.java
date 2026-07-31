package library.tools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExecutableReferenceExpression;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtForEach;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLambda;
import spoon.reflect.code.CtThisAccess;
import spoon.reflect.declaration.CtAnonymousExecutable;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtTypeMember;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtTypeParameter;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

public class MergeFiles {

	private static final Set<String> PRIMITIVE_TYPE_NAMES = Set.of(
			"boolean", "byte", "short", "int", "long", "char", "float", "double", "void");

	/** マージ対象外メソッド用アノテーション */
	@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
	@java.lang.annotation.Target(java.lang.annotation.ElementType.METHOD)
	public static @interface IncludeToMerge {
	}

	/**
	 * Mainクラスを起点に競プロ用1ファイルを生成する。
	 *
	 * 計算量: O(N) (ただし N はソースコード全体の文字数)
	 *
	 * @throws IOException 入出力エラーが発生した場合
	 */
	// 未テスト
	public static void export() throws IOException {
		exportImpl("Main", "solver/src/main/java/Main.java", Paths.get("out","Main.java").toString(), false);
	}

	/**
	 * コメントおよび余分な空白を除去するモードで、Mainクラスを起点に競プロ用1ファイルを生成する。
	 *
	 * 計算量: O(N) (ただし N はソースコード全体の文字数)
	 *
	 * @throws IOException 入出力エラーが発生した場合
	 */
	// 未テスト
	public static void exportRemovingSpaceAndComment() throws IOException {
		exportImpl("Main", "solver/src/main/java/Main.java", Paths.get("out","Main.java").toString(), true);
	}

	private static Path projectRoot = null;
	private static Path getProjectRoot() {
		if (projectRoot != null) return projectRoot;
		Path cur = Paths.get("").toAbsolutePath();
		while (cur != null) {
			if (Files.exists(cur.resolve("src/main/java/library")) && Files.exists(cur.resolve("solver/src/main/java"))) {
				projectRoot = cur;
				return projectRoot;
			}
			cur = cur.getParent();
		}
		projectRoot = Paths.get("").toAbsolutePath();
		return projectRoot;
	}
	
	/**
	 * 指定されたクラスを起点に競プロ用1ファイルを生成する。
	 *
	 * 計算量: O(N) (ただし N はソースコード全体の文字数)
	 *
	 * @param entryClass エントリーポイントとなるクラス名
	 * @param mainFilePath メインファイルのパス
	 * @param outputFilePath 出力ファイルのパス
	 * @throws IOException 入出力エラーが発生した場合
	 */
	// 未テスト
	public static void export(String entryClass, String mainFilePath, String outputFilePath) throws IOException {
		exportImpl(entryClass, mainFilePath, outputFilePath, false);
	}

	/**
	 * コメントおよび余分な空白を除去するモードで、指定されたクラスを起点に競プロ用1ファイルを生成する。
	 *
	 * 計算量: O(N) (ただし N はソースコード全体の文字数)
	 *
	 * @param entryClass エントリーポイントとなるクラス名
	 * @param mainFilePath メインファイルのパス
	 * @param outputFilePath 出力ファイルのパス
	 * @throws IOException 入出力エラーが発生した場合
	 */
	// 未テスト
	public static void exportRemovingSpaceAndComment(String entryClass, String mainFilePath, String outputFilePath) throws IOException {
		exportImpl(entryClass, mainFilePath, outputFilePath, true);
	}

	/**
	 * 指定されたクラスを起点に競プロ用1ファイルを生成する内部実装メソッド。
	 *
	 * 計算量: O(N) (ただし N はソースコード全体の文字数)
	 *
	 * @param entryClass エントリーポイントとなるクラス名
	 * @param mainFilePath メインファイルのパス
	 * @param outputFilePath 出力ファイルのパス
	 * @param removeCommentsAndWhitespace コメントおよび余分な空白を削除するかどうか
	 * @throws IOException 入出力エラーが発生した場合
	 */
	private static void exportImpl(String entryClass, String mainFilePath, String outputFilePath, boolean removeCommentsAndWhitespace) throws IOException {
		Launcher launcher = new Launcher();
		launcher.getEnvironment().setPreserveLineNumbers(false);
		launcher.getEnvironment().setComplianceLevel(21);
		launcher.getEnvironment().setNoClasspath(true);
		if (removeCommentsAndWhitespace) {
			launcher.getEnvironment().setCommentEnabled(false);
		}

		launcher.addInputResource(getProjectRoot().resolve("solver/src/main/java").toString());
		launcher.addInputResource(getProjectRoot().resolve("src/main/java").toString());
		launcher.addInputResource(getProjectRoot().resolve(mainFilePath).toString());

		launcher.getEnvironment().setAutoImports(false);
		launcher.buildModel();
		CtModel model = launcher.getModel();

		CtType<?> mainClass = model.getAllTypes().stream().filter(t -> t.getSimpleName().equals(entryClass)).findFirst()
				.orElseThrow(() -> new RuntimeException("Entry class " + entryClass + " not found in model."));

		// --- main メソッドの先頭に例外ハンドラ設定を挿入 ---
		for (CtMethod<?> m : mainClass.getMethods()) {
			if (m.getSimpleName().equals("main")) {
				CtCodeSnippetStatement stmt = launcher.getFactory().Code().createCodeSnippetStatement(
						"Thread.setDefaultUncaughtExceptionHandler((t, e) -> System.exit(1))");
				if (m.getBody() != null) {
					m.getBody().insertBegin(stmt);
				}
			}
		}

		// --- 到達可能メソッド/コンストラクタ/フィールドをDFSで取得 ---
		Set<CtExecutable<?>> reachableMethods = new HashSet<>();
		Set<CtField<?>> reachableFields = new HashSet<>();
		Set<CtType<?>> reachableTypes = new HashSet<>();
		Deque<CtExecutable<?>> execStack = new ArrayDeque<>();
		Set<CtExecutable<?>> visitedMethods = new HashSet<>();

		// エントリーポイント main/run
		mainClass.getMethods().stream().filter(m -> m.getSimpleName().equals("main") || m.getSimpleName().equals("run"))
				.forEach(entryPoint -> {
					markVisitedAndPush(entryPoint, execStack, visitedMethods);
					for (CtConstructorCall<?> call : entryPoint.getElements(new TypeFilter<>(CtConstructorCall.class))) {
						addIfReachable(call.getExecutable(), execStack, model);
					}
				});


		for (CtAnonymousExecutable anonymousExec : mainClass.getElements(new TypeFilter<>(CtAnonymousExecutable.class))) {
			markVisitedAndPush(anonymousExec, execStack, visitedMethods);
		}

		while (!execStack.isEmpty()) {
			CtExecutable<?> exec = execStack.pop();
			if (((CtTypeMember) exec).getDeclaringType() == null) continue;

			String sig = ((CtTypeMember) exec).getDeclaringType().getQualifiedName() + "#" + exec.getSignature();
			if (sig.startsWith("spoon.") || isJdkTypeName(((CtTypeMember) exec).getDeclaringType().getQualifiedName()) || sig.startsWith("library.tools.MergeFiles"))
				continue;

			if (!reachableMethods.add(exec)) continue;

			if (exec instanceof CtMethod<?> m) {
				addOverriddenMethodsToStack(m, execStack, visitedMethods);
				markTypeReferenceReachable(m.getType(), reachableTypes, execStack, visitedMethods, model);
			}
			for (CtParameter<?> p : exec.getParameters()) {
				markTypeReferenceReachable(p.getType(), reachableTypes, execStack, visitedMethods, model);
			}

			// Define defining type as reachable
			for (CtType<?> type = exec.getParent(CtType.class); type != null; type = type.getDeclaringType()) {
				if (!isJdkTypeName(type.getQualifiedName())) {
					addTypeAndSpecialMethods(type, reachableTypes, execStack, visitedMethods, model);
				} else break;
			}

			for (CtTypeReference<?> ref : exec.getElements(new TypeFilter<>(CtTypeReference.class))) {
				markTypeReferenceReachable(ref, reachableTypes, execStack, visitedMethods, model);
			}

			for (CtExecutableReferenceExpression<?, ?> mre : exec.getElements(new TypeFilter<>(CtExecutableReferenceExpression.class))) {
				addIfReachable(mre.getExecutable(), execStack, model);
			}
			for (CtInvocation<?> inv : exec.getElements(new TypeFilter<>(CtInvocation.class))) {
				addIfReachable(inv.getExecutable(), execStack, model);
			}
			for (CtConstructorCall<?> call : exec.getElements(new TypeFilter<>(CtConstructorCall.class))) {
				addIfReachable(call.getExecutable(), execStack, model);
				markTypeReferenceReachable(call.getType(), reachableTypes, execStack, visitedMethods, model);
			}
			for (CtFieldAccess<?> fa : exec.getElements(new TypeFilter<>(CtFieldAccess.class))) {
				if (fa == null || fa.getVariable() == null) continue;
				try {
					CtField<?> field = fa.getVariable().getDeclaration();
					if (field != null && field.getDeclaringType() != null) {
						markFieldAndInitializerReachable(field, reachableFields, reachableTypes, execStack, visitedMethods, model);
					}
				} catch (Exception e) {}
			}
			for (CtLambda<?> lambda : exec.getElements(new TypeFilter<>(CtLambda.class))) {
				if (lambda.getBody() != null) {
					for (CtInvocation<?> inv2 : lambda.getBody().getElements(new TypeFilter<>(CtInvocation.class))) {
						addIfReachable(inv2.getExecutable(), execStack, model);
					}
				}
				if (lambda.getExpression() != null) {
					for (CtInvocation<?> inv2 : lambda.getExpression().getElements(new TypeFilter<>(CtInvocation.class))) {
						addIfReachable(inv2.getExecutable(), execStack, model);
					}
				}
			}
		}

		for (CtField<?> field : reachableFields) {
			CtType<?> declType = field.getDeclaringType();
			if (declType != null && !isJdkTypeName(declType.getQualifiedName())) {
				addTypeAndSpecialMethods(declType, reachableTypes, execStack, visitedMethods, model);
			}
		}
		expandReachableTypeDependencies(reachableTypes, reachableFields, reachableMethods);

		// --- Simple Name Collision Resolution ---
		Map<String, List<CtType<?>>> simpleNameToTypes = new HashMap<>();
		for (CtType<?> type : reachableTypes) {
			if (type.getDeclaringType() == null && !isInvalidMergeType(type)) {
				simpleNameToTypes.computeIfAbsent(type.getSimpleName(), k -> new ArrayList<>()).add(type);
			}
		}
		for (var entry : simpleNameToTypes.entrySet()) {
			List<CtType<?>> types = entry.getValue();
			if (types.size() > 1) {
				types.sort((a, b) -> a.getQualifiedName().compareTo(b.getQualifiedName()));
				CtType<?> entryType = types.stream().filter(t -> t.getSimpleName().equals(entryClass)).findFirst().orElse(null);
				for (CtType<?> t : types) {
					if (t == entryType || (entryType == null && t == types.get(0))) continue;
					String qName = t.getQualifiedName();
					String[] parts = qName.split("\\.");
					StringBuilder newName = new StringBuilder();
					for (String part : parts) {
						if (part.isEmpty()) continue;
						newName.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
					}
					tr("Resolving collision: renaming " + qName + " to " + newName);
					t.setSimpleName(newName.toString());
				}
			}
		}

		// Convert to static nested if possible
		for (CtType<?> type : reachableTypes) {
			for (CtType<?> nested : type.getNestedTypes()) {
				if (nested instanceof CtClass<?> clazz && !clazz.hasModifier(ModifierKind.STATIC)) {
					if (canBeStatic(clazz)) {
						clazz.addModifier(ModifierKind.STATIC);
					}
				}
			}
		}

		// --- Move all to unnamed package ---
		Set<String> modelTypeNames = new HashSet<>();
		for (CtType<?> type : model.getAllTypes()) {
			if (type.getDeclaringType() == null) modelTypeNames.add(type.getQualifiedName());
		}
		for (CtTypeReference<?> ref : model.getElements(new TypeFilter<>(CtTypeReference.class))) {
			CtTypeReference<?> topRef = ref;
			while (topRef.getDeclaringType() != null) topRef = topRef.getDeclaringType();
			if (topRef.getPackage() != null && modelTypeNames.contains(topRef.getQualifiedName())) {
				topRef.setPackage(null);
			}
		}
		for (CtType<?> type : new ArrayList<>(model.getAllTypes())) {
			if (type.getDeclaringType() == null && type.getPackage() != null && !type.getPackage().isUnnamedPackage()) {
				type.delete();
				launcher.getFactory().Package().getRootPackage().addType(type);
			}
		}

		launcher.getEnvironment().setAutoImports(true);
		Set<String> imports = new TreeSet<>();
		for (CtType<?> type : reachableTypes) {
			if (isInvalidMergeType(type)) continue;
			type.getElements(new TypeFilter<>(CtTypeReference.class)).forEach(ref -> {
				String qName = ref.getQualifiedName();
				if (qName == null || qName.endsWith("[]") || qName.contains("<")) return;
				if (qName.startsWith("java.lang.") && !qName.substring(10).contains(".")) return;
				if (qName.startsWith("java.") || qName.startsWith("javax.")) {
					imports.add("import " + qName.replace('$', '.') + ";");
				}
			});
		}

		StringBuilder merged = new StringBuilder();
		for (String imp : imports) merged.append(imp).append("\n");
		if (!imports.isEmpty()) merged.append("\n");

		Map<String, String> externalNestedTypeNames = collectNestedTypeNames(reachableTypes);
		List<CtType<?>> sortedTypes = new ArrayList<>(reachableTypes);
		sortedTypes.sort((a, b) -> {
			if (a.getSimpleName().equals(entryClass)) return -1;
			if (b.getSimpleName().equals(entryClass)) return 1;
			return a.getQualifiedName().compareTo(b.getQualifiedName());
		});

		for (CtType<?> type : sortedTypes) {
			if (isInvalidMergeType(type) || type.isLocalType() || type.isAnonymous() || type.getDeclaringType() != null) continue;
			processTypeRecursively(type, reachableMethods, reachableFields, reachableTypes);
			if (!type.getSimpleName().equals(entryClass)) type.removeModifier(ModifierKind.PUBLIC);
			String code = type.toString();
			code = qualifyExternalNestedTypeNames(code, type, externalNestedTypeNames);
			code = code.replaceAll("\\b(library|solver|template)\\.[\\w.]+\\.", "");
			merged.append(code).append("\n\n");
		}

		String outputStr;
		if (removeCommentsAndWhitespace) {
			outputStr = removeCommentsAndWhitespace(merged.toString().replaceAll("(?m)^\\s*@MergeFiles\\.IncludeToMerge\\s*$", ""));
		} else {
			String originalCode = Files.readString(getProjectRoot().resolve(mainFilePath));
			StringBuilder commented = new StringBuilder("\n// --- Original Code ---\n");
			for (String line : originalCode.split("\n", -1)) commented.append("// ").append(line).append("\n");
			outputStr = merged.toString().replaceAll("(?m)^\\s*@MergeFiles\\.IncludeToMerge\\s*$", "") + commented;
		}
		Path outPath = getProjectRoot().resolve(outputFilePath);
		if (outPath.getParent() != null) Files.createDirectories(outPath.getParent());
		Files.writeString(outPath, outputStr);
	}

	/**
	 * Javaソースコードからすべてのコメントおよび余分な空白を除去する。
	 *
	 * 計算量: O(N) (ただし N は入力文字列の長さ)
	 *
	 * @param code 変換元のJavaソースコード文字列
	 * @return コメントと余分な空白が除去されたJavaソースコード文字列
	 */
	// 未テスト
	public static String removeCommentsAndWhitespace(String code) {
		StringBuilder sb = new StringBuilder();
		int len = code.length();
		int i = 0;
		boolean lastWasSpace = false;
		boolean beginningOfLine = true;

		final int NORMAL = 0;
		final int STRING = 1;
		final int CHAR = 2;
		final int TEXT_BLOCK = 3;
		final int BLOCK_COMMENT = 4;
		final int LINE_COMMENT = 5;

		int state = NORMAL;

		while (i < len) {
			char c = code.charAt(i);

			if (state == BLOCK_COMMENT) {
				if (c == '*' && i + 1 < len && code.charAt(i + 1) == '/') {
					state = NORMAL;
					i += 2;
					if (!beginningOfLine && !lastWasSpace) {
						sb.append(' ');
						lastWasSpace = true;
					}
				} else {
					i++;
				}
				continue;
			}

			if (state == LINE_COMMENT) {
				if (c == '\n' || c == '\r') {
					state = NORMAL;
				} else {
					i++;
					continue;
				}
			}

			if (state == TEXT_BLOCK) {
				if (c == '"' && i + 2 < len && code.substring(i, i + 3).equals("\"\"\"")) {
					sb.append("\"\"\"");
					state = NORMAL;
					i += 3;
					lastWasSpace = false;
					beginningOfLine = false;
				} else if (c == '\\' && i + 1 < len) {
					sb.append('\\').append(code.charAt(i + 1));
					i += 2;
				} else {
					sb.append(c);
					i++;
				}
				continue;
			}

			if (state == STRING) {
				if (c == '"') {
					sb.append('"');
					state = NORMAL;
					i++;
					lastWasSpace = false;
					beginningOfLine = false;
				} else if (c == '\\' && i + 1 < len) {
					sb.append('\\').append(code.charAt(i + 1));
					i += 2;
				} else {
					sb.append(c);
					i++;
				}
				continue;
			}

			if (state == CHAR) {
				if (c == '\'') {
					sb.append('\'');
					state = NORMAL;
					i++;
					lastWasSpace = false;
					beginningOfLine = false;
				} else if (c == '\\' && i + 1 < len) {
					sb.append('\\').append(code.charAt(i + 1));
					i += 2;
				} else {
					sb.append(c);
					i++;
				}
				continue;
			}

			if (c == '/' && i + 1 < len && code.charAt(i + 1) == '*') {
				state = BLOCK_COMMENT;
				i += 2;
				continue;
			}
			if (c == '/' && i + 1 < len && code.charAt(i + 1) == '/') {
				state = LINE_COMMENT;
				i += 2;
				continue;
			}
			if (c == '"' && i + 2 < len && code.substring(i, i + 3).equals("\"\"\"")) {
				state = TEXT_BLOCK;
				sb.append("\"\"\"");
				i += 3;
				continue;
			}
			if (c == '"') {
				state = STRING;
				sb.append('"');
				i++;
				continue;
			}
			if (c == '\'') {
				state = CHAR;
				sb.append('\'');
				i++;
				continue;
			}

			if (Character.isWhitespace(c)) {
				if (!beginningOfLine && !lastWasSpace) {
					sb.append(' ');
					lastWasSpace = true;
				}
				i++;
				continue;
			}

			sb.append(c);
			lastWasSpace = false;
			beginningOfLine = false;
			i++;
		}

		if (lastWasSpace && sb.length() > 0 && sb.charAt(sb.length() - 1) == ' ') {
			sb.setLength(sb.length() - 1);
		}

		return sb.toString().trim();
	}

	private static boolean canBeStatic(CtClass<?> clazz) {
		CtType<?> outer = clazz.getDeclaringType();
		if (outer == null) return false;

		List<CtThisAccess<?>> thisAccesses = clazz.getElements(new TypeFilter<>(CtThisAccess.class));
		for (CtThisAccess<?> access : thisAccesses) {
			if (access.getType() != null && !access.getType().getQualifiedName().equals(clazz.getQualifiedName())) return false;
		}
		List<CtFieldAccess<?>> fieldAccesses = clazz.getElements(new TypeFilter<>(CtFieldAccess.class));
		for (CtFieldAccess<?> fa : fieldAccesses) {
			try {
				CtField<?> f = fa.getVariable().getDeclaration();
				if (f != null && !f.hasModifier(ModifierKind.STATIC) && f.getDeclaringType() != null && f.getDeclaringType().getQualifiedName().equals(outer.getQualifiedName())) return false;
			} catch (Exception e) {}
		}
		List<CtInvocation<?>> invocations = clazz.getElements(new TypeFilter<>(CtInvocation.class));
		for (CtInvocation<?> inv : invocations) {
			try {
				CtExecutable<?> exec = inv.getExecutable().getDeclaration();
				if (exec instanceof CtMethod<?> m && !m.hasModifier(ModifierKind.STATIC) && m.getDeclaringType() != null && m.getDeclaringType().getQualifiedName().equals(outer.getQualifiedName())) return false;
			} catch (Exception e) {}
		}
		List<CtTypeReference<?>> typeRefs = clazz.getElements(new TypeFilter<>(CtTypeReference.class));
		for (CtTypeReference<?> ref : typeRefs) {
			try {
				CtType<?> decl = ref.getTypeDeclaration();
				if (decl instanceof CtTypeParameter tp) {
					var declarer = tp.getTypeParameterDeclarer();
					if (declarer != null && declarer != clazz) {
						CtType<?> current = clazz.getDeclaringType();
						while (current != null) {
							if (current == declarer) return false;
							current = current.getDeclaringType();
						}
					}
				}
			} catch (Exception e) {}
		}

		return true;
	}

	private static CtType<?> resolveType(CtTypeReference<?> ref, CtModel model) {
		CtType<?> type = ref.getTypeDeclaration();
		if (type != null) return type;
		String qName = ref.getQualifiedName();
		return model.getAllTypes().stream().filter(t -> qName.equals(t.getQualifiedName())).findFirst().orElse(null);
	}

	private static CtExecutable<?> resolveExecutable(CtExecutableReference<?> ref, CtModel model) {
		if (ref == null) return null;
		CtExecutable<?> decl = ref.getDeclaration();
		if (decl != null) return decl;
		CtTypeReference<?> declaringTypeRef = ref.getDeclaringType();
		if (declaringTypeRef == null) return null;
		CtType<?> clazz = resolveType(declaringTypeRef, model);
		if (clazz != null) {
			for (CtMethod<?> m : clazz.getAllMethods()) {
				if (m.getSimpleName().equals(ref.getSimpleName()) && m.getParameters().size() == ref.getParameters().size()) return m;
			}
			for (CtConstructor<?> c : clazz.getElements(new TypeFilter<>(CtConstructor.class))) {
				if (c.getParameters().size() == ref.getParameters().size()) return c;
			}
		}
		return null;
	}

	private static void expandReachableTypeDependencies(Set<CtType<?>> reachableTypes, Set<CtField<?>> reachableFields, Set<CtExecutable<?>> reachableMethods) {
		Queue<CtType<?>> queue = new ArrayDeque<>(reachableTypes);
		while (!queue.isEmpty()) {
			CtType<?> type = queue.poll();
			if (type == null || isInvalidMergeType(type)) continue;
			addTypeReferenceDependency(type.getSuperclass(), reachableTypes, queue, "superclass");
			for (CtTypeReference<?> iface : type.getSuperInterfaces()) addTypeReferenceDependency(iface, reachableTypes, queue, "superinterface");
			for (CtField<?> field : type.getFields()) {
				if (reachableFields.contains(field)) addTypeReferenceDependency(field.getType(), reachableTypes, queue, "field type");
			}
			if (type.isInterface()) {
				for (CtMethod<?> m : type.getMethods()) {
					reachableMethods.add(m);
					for (CtTypeReference<?> ref : m.getElements(new TypeFilter<>(CtTypeReference.class))) addTypeReferenceDependency(ref, reachableTypes, queue, "interface method reference");
				}
			}
		}
	}

	private static void addTypeReferenceDependency(CtTypeReference<?> ref, Set<CtType<?>> reachableTypes, Queue<CtType<?>> queue, String reason) {
		if (ref == null || isInvalidTypeReference(ref)) return;
		CtType<?> refType = ref.getTypeDeclaration();
		if (refType == null) return;
		String qName = refType.getQualifiedName();
		if (isJdkTypeName(qName) || isInvalidMergeType(refType)) return;
		if (reachableTypes.add(refType)) queue.add(refType);
	}

	private static void addIfReachable(CtExecutableReference<?> execRef, Deque<CtExecutable<?>> execStack, CtModel model) {
		if (execRef == null) return;
		CtExecutable<?> exec = resolveExecutable(execRef, model);
		if (exec != null) execStack.push(exec);
	}

	private static void markVisitedAndPush(CtExecutable<?> exec, Deque<CtExecutable<?>> execStack, Set<CtExecutable<?>> visitedMethods) {
		if (visitedMethods.add(exec)) execStack.push(exec);
	}

	private static void markFieldAndInitializerReachable(CtField<?> field, Set<CtField<?>> reachableFields, Set<CtType<?>> reachableTypes, Deque<CtExecutable<?>> execStack, Set<CtExecutable<?>> visitedMethods, CtModel model) {
		if (field == null || field.getDeclaringType() == null) return;
		if (!reachableFields.add(field)) return;
		markTypeReferenceReachable(field.getType(), reachableTypes, execStack, visitedMethods, model);
		CtExpression<?> init = field.getDefaultExpression();
		if (init == null) return;
		for (CtTypeReference<?> ref : init.getElements(new TypeFilter<>(CtTypeReference.class))) markTypeReferenceReachable(ref, reachableTypes, execStack, visitedMethods, model);
		for (CtFieldAccess<?> fa : init.getElements(new TypeFilter<>(CtFieldAccess.class))) {
			if (fa == null || fa.getVariable() == null) continue;
			try { markFieldAndInitializerReachable(fa.getVariable().getDeclaration(), reachableFields, reachableTypes, execStack, visitedMethods, model); } catch (Exception e) {}
		}
		for (CtInvocation<?> inv : init.getElements(new TypeFilter<>(CtInvocation.class))) addIfReachable(inv.getExecutable(), execStack, model);
		for (CtConstructorCall<?> cc : init.getElements(new TypeFilter<>(CtConstructorCall.class))) addIfReachable(cc.getExecutable(), execStack, model);
	}

	private static void addTypeAndSpecialMethods(CtType<?> type, Set<CtType<?>> reachableTypes, Deque<CtExecutable<?>> execStack, Set<CtExecutable<?>> visitedMethods, CtModel model) {
		if (type == null || isJdkTypeName(type.getQualifiedName()) || isInvalidMergeType(type)) return;
		if (reachableTypes.add(type)) {
			for (CtMethod<?> m : type.getMethods()) {
				if (m.getAnnotation(Override.class) != null || implementsAbstractMethod(type, m) || isSpecialMethod(type, m) || overridesNonJdkMethod(type, m)) {
					if (visitedMethods.add(m)) execStack.push(m);
				}
			}
			for (CtAnonymousExecutable anonymousExec : type.getElements(new TypeFilter<>(CtAnonymousExecutable.class))) {
				if (visitedMethods.add(anonymousExec)) execStack.push(anonymousExec);
			}
			if (type.isInterface()) {
				for (CtMethod<?> m : type.getMethods()) {
					if (visitedMethods.add(m)) execStack.push(m);
				}
			}
		}
	}

	private static void markTypeReferenceReachable(CtTypeReference<?> ref, Set<CtType<?>> reachableTypes, Deque<CtExecutable<?>> execStack, Set<CtExecutable<?>> visitedMethods, CtModel model) {
		if (ref == null) return;
		while (ref instanceof CtArrayTypeReference<?> arrayRef) ref = arrayRef.getComponentType();
		for (CtTypeReference<?> typeArg : ref.getActualTypeArguments()) markTypeReferenceReachable(typeArg, reachableTypes, execStack, visitedMethods, model);
		if (isInvalidTypeReference(ref)) return;
		CtType<?> refType = resolveType(ref, model);
		if (refType != null) {
			if (!isJdkTypeName(refType.getQualifiedName()) && !isInvalidMergeType(refType)) addTypeAndSpecialMethods(refType, reachableTypes, execStack, visitedMethods, model);
		}
	}

	private static List<CtType<?>> getAllSuperTypes(CtType<?> type) {
		List<CtType<?>> res = new ArrayList<>();
		Deque<CtTypeReference<?>> stack = new ArrayDeque<>();
		if (type.getSuperclass() != null) stack.push(type.getSuperclass());
		stack.addAll(type.getSuperInterfaces());
		Set<String> visited = new HashSet<>();
		while (!stack.isEmpty()) {
			CtTypeReference<?> ref = stack.pop();
			if (!visited.add(ref.getQualifiedName())) continue;
			CtType<?> t = ref.getTypeDeclaration();
			if (t == null) continue;
			res.add(t);
			if (t.getSuperclass() != null) stack.push(t.getSuperclass());
			stack.addAll(t.getSuperInterfaces());
		}
		return res;
	}

	static boolean implementsAbstractMethod(CtType<?> type, CtMethod<?> m) {
		if (m.isAbstract() || m.hasModifier(ModifierKind.PRIVATE)) return false;
		for (CtType<?> superType : getAllSuperTypes(type)) {
			for (CtMethod<?> sm : superType.getMethods()) {
				if (sm.isAbstract() && isOverriding(m, sm)) return true;
			}
		}
		return false;
	}

	private static boolean overridesNonJdkMethod(CtType<?> type, CtMethod<?> m) {
		if (m.hasModifier(ModifierKind.PRIVATE) || m.hasModifier(ModifierKind.STATIC)) return false;
		for (CtType<?> superType : getAllSuperTypes(type)) {
			if (isJdkTypeName(superType.getQualifiedName())) continue;
			for (CtMethod<?> sm : superType.getMethods()) {
				if (isOverriding(m, sm)) return true;
			}
		}
		return false;
	}

	private static boolean isSpecialMethod(CtType<?> type, CtMethod<?> m) {
		String name = m.getSimpleName();
		if (name.equals("iterator") && m.getParameters().isEmpty()) return isSubtypeOf(type, Iterable.class);
		if (name.equals("compareTo") && m.getParameters().size() == 1) return isSubtypeOf(type, Comparable.class);
		if ((name.equals("hasNext") || name.equals("next") || name.equals("nextInt")) && m.getParameters().isEmpty()) {
			return isSubtypeOf(type, java.util.Iterator.class) || isSubtypeOf(type, java.util.PrimitiveIterator.class);
		}
		return false;
	}

	private static boolean isSubtypeOf(CtType<?> type, Class<?> superId) {
		try {
			return type.getReference().isSubtypeOf(type.getFactory().Type().createReference(superId));
		} catch (Exception e) { return false; }
	}

	private static Map<String, String> collectNestedTypeNames(Set<CtType<?>> reachableTypes) {
		Map<String, String> ret = new HashMap<>();
		for (CtType<?> type : reachableTypes) {
			if (type == null || type.getDeclaringType() != null || isInvalidMergeType(type)) continue;
			collectNestedTypeNames(type, type.getSimpleName(), ret);
		}
		return ret;
	}

	private static void collectNestedTypeNames(CtType<?> type, String ownerName, Map<String, String> ret) {
		for (CtType<?> nested : type.getNestedTypes()) {
			String simpleName = nested.getSimpleName();
			String qualifiedName = ownerName + "." + simpleName;
			if (ret.containsKey(simpleName) && !ret.get(simpleName).equals(qualifiedName)) ret.put(simpleName, "");
			else ret.put(simpleName, qualifiedName);
			collectNestedTypeNames(nested, qualifiedName, ret);
		}
	}

	private static String qualifyExternalNestedTypeNames(String code, CtType<?> currentType, Map<String, String> nestedTypeNames) {
		String currentTypeSimpleName = currentType.getSimpleName();
		for (var entry : nestedTypeNames.entrySet()) {
			String simpleName = entry.getKey();
			String qualifiedName = entry.getValue();
			if (qualifiedName.isEmpty() || qualifiedName.startsWith(currentTypeSimpleName + ".") || simpleName.equals(currentTypeSimpleName)) continue;
			code = code.replaceAll("(?<![\\w$.])(?<!class\\s)(?<!interface\\s)(?<!enum\\s)(?<!record\\s)" + Pattern.quote(simpleName) + "(?![\\w$])(?![\\w$.]*\\s*\\.new)", qualifiedName);
		}
		return code;
	}

	private static boolean containsOptionalDependency(spoon.reflect.declaration.CtElement element) {
		for (CtTypeReference<?> ref : element.getElements(new TypeFilter<>(CtTypeReference.class))) {
			String qName = ref.getQualifiedName();
			if (qName != null && (qName.contains("org.graphstream") || qName.contains("org.knowm.xchart") || qName.contains("spoon") || qName.contains("cc.redberry.rings") || qName.contains("java.awt") || qName.contains("javax.swing"))) return true;
		}
		return false;
	}

	private static void processTypeRecursively(CtType<?> type, Set<CtExecutable<?>> reachableMethods, Set<CtField<?>> reachableFields, Set<CtType<?>> reachableTypes) {
		for (CtMethod<?> method : new HashSet<>(type.getMethods())) {
			if (containsOptionalDependency(method)) method.delete();
			else deleteIfUnusedMethod(type, method, reachableMethods);
		}
		if (type instanceof CtClass<?> clazz) {
			for (CtConstructor<?> ctor : new HashSet<>(clazz.getConstructors())) {
				if (!reachableMethods.contains(ctor)) ctor.delete();
			}
		}
		for (CtMethod<?> method : type.getMethods()) {
			method.getElements(new TypeFilter<>(CtInvocation.class)).forEach(inv -> {
				CtExecutableReference<?> exec = inv.getExecutable();
				if (exec != null && exec.getDeclaringType() != null && (exec.getDeclaringType().getQualifiedName().equals("library.tools.MergeFiles") || exec.getDeclaringType().getSimpleName().equals("MergeFiles"))) inv.delete();
			});
		}
		for (CtField<?> field : new HashSet<>(type.getFields())) {
			if (containsOptionalDependency(field)) {
				field.delete();
				reachableFields.remove(field);
			}
		}
		deleteUnusedFieldsRecursively(type, reachableFields);
		for (CtType<?> nested : new HashSet<>(type.getNestedTypes())) {
			processTypeRecursively(nested, reachableMethods, reachableFields, reachableTypes);
			if (!reachableTypes.contains(nested)) nested.delete();
		}
	}

	private static void deleteIfUnusedMethod(CtType<?> type, CtMethod<?> method, Set<CtExecutable<?>> reachableMethods) {
		if (type.isInterface() || method.getAnnotation(MergeFiles.IncludeToMerge.class) != null || method.getSimpleName().equals("main") || method.getSimpleName().equals("run") || reachableMethods.contains(method)) return;
		if (method.getSimpleName().equals("compareTo") && method.getParameters().size() == 1) {
			if (isSubtypeOf(type, Comparable.class)) return;
		}
		if (method.getSimpleName().equals("iterator") && method.getParameters().isEmpty()) {
			if (isSubtypeOf(type, Iterable.class)) return;
		}
		method.delete();
	}

	private static void deleteUnusedFieldsRecursively(CtType<?> type, Set<CtField<?>> reachableFields) {
		for (CtField<?> field : new HashSet<>(type.getFields())) {
			if (!reachableFields.contains(field)) field.delete();
		}
		for (CtType<?> nested : new HashSet<>(type.getNestedTypes())) deleteUnusedFieldsRecursively(nested, reachableFields);
	}

	static boolean DEBUG = false;
	static void tr(Object... objects) {
		if (DEBUG) System.out.println(Arrays.deepToString(objects));
	}

	private static boolean isInvalidMergeType(CtType<?> type) {
		if (type == null || type instanceof CtTypeParameter) return true;
		String qName = type.getQualifiedName();
		if (qName == null || qName.equals("library.tools.MergeFiles") || isJdkTypeName(qName)) return true;
		return isInvalidTypeName(qName) || isInvalidTypeName(type.getSimpleName());
	}

	private static boolean isOverriding(CtMethod<?> m, CtMethod<?> superMethod) {
		if (m == superMethod) return false;
		try {
			if (m.isOverriding(superMethod)) return true;
		} catch (Exception e) {}
		if (!m.getSimpleName().equals(superMethod.getSimpleName())) return false;
		if (m.getParameters().size() != superMethod.getParameters().size()) return false;
		for (int i = 0; i < m.getParameters().size(); i++) {
			CtTypeReference<?> t1 = m.getParameters().get(i).getType();
			CtTypeReference<?> t2 = superMethod.getParameters().get(i).getType();
			if (t1 == null || t2 == null || !t1.getQualifiedName().equals(t2.getQualifiedName())) return false;
		}
		if (superMethod.hasModifier(ModifierKind.PRIVATE) || superMethod.hasModifier(ModifierKind.STATIC)) return false;
		if (superMethod.hasModifier(ModifierKind.PUBLIC) || superMethod.hasModifier(ModifierKind.PROTECTED)) return true;
		CtType<?> type1 = m.getDeclaringType();
		CtType<?> type2 = superMethod.getDeclaringType();
		if (type1 == null || type2 == null) return false;
		String p1 = type1.getPackage() != null ? type1.getPackage().getQualifiedName() : "";
		String p2 = type2.getPackage() != null ? type2.getPackage().getQualifiedName() : "";
		return p1.equals(p2);
	}

	private static void addOverriddenMethodsToStack(CtMethod<?> m, Deque<CtExecutable<?>> execStack, Set<CtExecutable<?>> visitedMethods) {
		CtType<?> type = m.getDeclaringType();
		if (type == null) return;
		for (CtType<?> t : getAllSuperTypes(type)) {
			for (CtMethod<?> sm : t.getMethods()) {
				if (isOverriding(m, sm) && !isJdkTypeName(sm.getDeclaringType().getQualifiedName())) {
					if (visitedMethods.add(sm)) execStack.push(sm);
				}
			}
		}
	}

	private static boolean isInvalidTypeReference(CtTypeReference<?> ref) {
		if (ref == null) return true;
		CtTypeReference<?> comp = ref;
		while (comp instanceof CtArrayTypeReference<?> arr) comp = arr.getComponentType();
		return isInvalidTypeName(comp.getQualifiedName());
	}

	private static boolean isInvalidTypeName(String name) {
		return name == null || PRIMITIVE_TYPE_NAMES.contains(name) || name.equals("null") || name.contains("[") || name.contains("]");
	}

	private static boolean isJdkTypeName(String qName) {
		if (qName == null) return true;
		return qName.startsWith("java.") || qName.startsWith("javax.") || qName.startsWith("sun.") || qName.contains("org.graphstream") || qName.contains("org.knowm.xchart") || qName.contains("spoon") || qName.contains("cc.redberry.rings");
	}
}