package library.tools;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A utility class for merging multiple Java source files into a single file.
 * It performs reachability analysis to include only necessary classes and methods,
 * especially useful for competitive programming contexts or for creating
 * self-contained runnable Java applications from a larger project structure.
 * This version includes enhanced logic for identifying chained method calls
 * and debug logging for better traceability during the merging process.
 */
public final class MergeFilesFast {

    private static final Pattern PACKAGE_PATTERN = Pattern.compile("(?m)^\\s*package\\s+([\\w.]+)\\s*;");
    private static final Pattern IMPORT_PATTERN = Pattern.compile("(?m)^\\s*import\\s+([\\w.*$]+)\\s*;");
    private static final Pattern TYPE_DECL_PATTERN = Pattern.compile(
            "(?m)^\\s*(?:public\\s+)?(?:final\\s+|abstract\\s+|sealed\\s+|non-sealed\\s+)*"
                    + "(class|interface|enum|record)\\s+([A-Za-z_][A-Za-z0-9_]*)\\b");
    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("\\b[A-Z][A-Za-z0-9_]*\\b");
    private static final Pattern MAIN_PATTERN = Pattern.compile(
            "(?s)(public\\s+static\\s+void\\s+main\\s*\\(\\s*String\\s*\\[\\s*]\\s*\\w*\\s*\\)\\s*\\{)");
    private static final Pattern METHOD_HEADER_PATTERN = Pattern.compile(
            "(?m)(^[ \\t]*(?:@[^\\n]+\\n[ \\t]*)*(?:(?:public|protected|private|static|final|synchronized|native|strictfp|abstract)\\s+)*(?:<[^>]+>\\s+)?(?:[\\w<>,\\[\\].?]+\\s+)?([A-Za-z_][A-Za-z0-9_]*)\\s*\\([^;{}]*\\)\\s*(?:throws[^{]+)?\\{)");
    private static final Pattern MERGE_EXPORT_INVOCATION_PREFIX = Pattern.compile(
            "(?:(?:\\b[A-Za-z_][A-Za-z0-9_]*\\s*\\.\\s*)*)(?:MergeFiles|MergeFilesFast)\\s*\\.\\s*export\\s*\\(");
    private static final Pattern UNQUALIFIED_CALL_NAME_PATTERN = Pattern.compile("\\b([a-zA-Z_][A-Za-z0-9_]*)\\s*\\(");
    private static final Pattern MEMBER_CALL_PATTERN = Pattern.compile(
            "\\b([a-zA-Z_][A-Za-z0-9_]*)\\s*\\.\\s*([a-zA-Z_][A-Za-z0-9_]*)\\s*\\(");
    private static final Pattern DOT_CALL_NAME_PATTERN = Pattern.compile("\\.\\s*([a-zA-Z_][A-Za-z0-9_]*)\\s*\\(");
    private static final Pattern QUALIFIED_CALL_PATTERN = Pattern.compile(
            "\\b([A-Za-z_][A-Za-z0-9_]*)\\s*\\.\\s*([a-zA-Z_][A-Za-z0-9_]*)\\s*\\(");
    private static final Pattern CTOR_TYPE_PATTERN = Pattern.compile("\\bnew\\s+([A-Za-z_][A-Za-z0-9_]*)");
    private static final Pattern LOCAL_DECLARED_TYPE_PATTERN = Pattern.compile(
            "\\b([A-Z][A-Za-z0-9_]*)\\s*(?:<[^;={}()\\n]+>)?\\s+(?:\\[]\\s*)*([a-zA-Z_][A-Za-z0-9_]*)\\b");
    private static final Pattern VAR_NEW_ASSIGN_PATTERN = Pattern.compile(
            "\\bvar\\s+([a-zA-Z_][A-Za-z0-9_]*)\\s*=\\s*new\\s+([A-Z][A-Za-z0-9_]*)\\b");

    private MergeFilesFast() {
    }

    /**
     * Exports the merged code with default settings.
     * The entry class is assumed to be "Main", the main file path is "src/template/Main.java",
     * and the output file path is "C:/Users/forto/eclipse-workspace/MainMerged/Main/src/Main.java".
     *
     * @throws IOException If an I/O error occurs during file operations.
     */
    public static void export() throws IOException {
        export("Main", "src/template/Main.java", "C:/Users/forto/eclipse-workspace/MainMerged/Main/src/Main.java");
    }

    public static void export(String entryClass, String mainFilePath, String outputFilePath) throws IOException {
        Path mainFile = Paths.get(mainFilePath).toAbsolutePath().normalize();
        Path outputFile = Paths.get(outputFilePath).toAbsolutePath().normalize();

        Map<String, JavaSource> allSources = loadJavaSources(mainFile, outputFile);
        if (allSources.isEmpty()) {
            throw new IOException("No Java sources found");
        }
        JavaSource entry = findEntry(allSources, entryClass, mainFile);
        
        Set<String> projectTypeNames = collectSimpleTypeNames(allSources);
        Set<String> reachableTypeNames = resolveReachableTypes(entry, allSources, projectTypeNames);

        TreeSet<String> imports = new TreeSet<>();
        List<SourceUnit> units = new ArrayList<>();
        for (String qName : reachableTypeNames) {
            JavaSource source = allSources.get(qName);
            if (source == null) {
                continue;
            }
            imports.addAll(source.javaImports());
            String body = source.bodyWithoutPackageAndImport();
            if (source.simpleTypeName.equals(entryClass)) {
                body = injectExceptionHandlerIntoMain(body);
            } else {
                body = body.replaceAll("(?m)^\\s*public\\s+((?:final\\s+|abstract\\s+|sealed\\s+|non-sealed\\s+)*)((?:class|interface|enum|record)\\b)", "$1$2");
            }
            units.add(new SourceUnit(source.simpleTypeName, source.qualifiedTypeName, body.strip()));
        }

        units.sort(Comparator
                .comparing((SourceUnit s) -> !s.simpleTypeName.equals(entryClass))
                .thenComparing(s -> s.qualifiedTypeName));

        units = removeMergeFilesInvocations(units);
        pruneUnusedMethods(units, entryClass);
        pruneUnusedTopLevelTypes(units);
        units = pruneUnusedTypes(units, entryClass);

        String merged = renderMerged(imports, units);

        Files.writeString(outputFile, merged, StandardCharsets.UTF_8);
    }
    
    /**
     * 収集したimport文とソースユニットから、最終的なマージ済みソースコード文字列を生成します。
     *
     * @param imports 保持するimport文の集合（重複なし・ソート済み）
     * @param units   マージ対象の型ごとのソース断片リスト
     * @return 完成した単一ファイルのソースコード（文字列）
     */
    private static String renderMerged(TreeSet<String> imports, List<SourceUnit> units) {
        StringBuilder out = new StringBuilder();
        for (String imp : imports) {
            out.append("import ").append(imp).append(";\n");
        }
        if (!imports.isEmpty()) {
            out.append('\n');
        }
        for (SourceUnit u : units) {
            out.append(u.body).append("\n\n");
        }
        return out.toString();
    }

    /**
     * 到達解析を行い、実際に呼び出されている（または呼び出される可能性のある）メソッド以外を削除します。
     * <p>
     * BFSを用いた到達性解析を実施し、mainメソッドや静的イニシャライザから到達可能なメソッドのみを残します。
     * また、メソッド名の重複チェックや文字列内呼び出しの簡易推論も行います。
     * </p>
     *
     * @param units      マージ対象のソースユニット一覧（この中でbodyを書き換えます）
     * @param entryClass エントリーポイントクラス名（mainメソッドの探索起点）
     */
    private static void pruneUnusedMethods(List<SourceUnit> units, String entryClass) {
        Map<String, ParsedUnit> parsedByClass = new LinkedHashMap<>();
        for (SourceUnit unit : units) {
            parsedByClass.put(unit.simpleTypeName, ParsedUnit.parse(unit));
        }

        Map<String, MethodNode> nodeById = new LinkedHashMap<>();
        for (ParsedUnit pu : parsedByClass.values()) {
            for (MethodBlock mb : pu.methods) {
                nodeById.put(mb.id(pu.className), new MethodNode(pu.className, mb));
            }
        }

        ReachabilityIndex index = ReachabilityIndex.build(parsedByClass);

        Deque<MethodNode> queue = new ArrayDeque<>();
        Set<String> used = new HashSet<>();
        Set<String> processed = new HashSet<>();

        // mainメソッドをBFSの初期ノードとしてキューに積む
        ParsedUnit entryParsedUnit = parsedByClass.get(entryClass);
        if (entryParsedUnit != null) {
            for (MethodBlock m : entryParsedUnit.methods) {
                if ("main".equals(m.name)) {
                    enqueue(entryParsedUnit.className, m, nodeById, queue, used, "Explicitly added (main method)");
                    break;
                }
            }
        }

        for (ParsedUnit pu : parsedByClass.values()) {
            for (MethodBlock m : pu.methods) {
//                if (/*m.keepByAnnotation ||*/ /*(pu.className.equals(entryClass) && ("main".equals(m.name) || "run".equals(m.name)))*/) {
                 if(m.keepByAnnotation) {
                	 enqueue(pu.className, m, nodeById, queue, used, "override or @IncludeToMerge");
                 }
            }
            for (String initializerBody : pu.initializerBodies) {
                enqueueMethodsFromBody(initializerBody, pu.className, parsedByClass, index, nodeById, queue, used);
            }
        }

        while (!queue.isEmpty()) {
            MethodNode node = queue.poll();
            if (!processed.add(node.id())) {
                continue;
            }
            enqueueMethodsFromBody(node.block.bodyText(), node.className, parsedByClass, index, nodeById, queue, used);
        }

        for (SourceUnit unit : units) {
            ParsedUnit pu = parsedByClass.get(unit.simpleTypeName);
            if (pu == null) {
                continue;
            }
            StringBuilder usedBodiesBuilder = new StringBuilder();
            for (MethodBlock method : pu.methods) {
                if (used.contains(method.id(pu.className))) {
                    usedBodiesBuilder.append(method.bodyText()).append('\n');
                }
            }
            String cleanedUsedBodies = stripCommentsAndStrings(usedBodiesBuilder.toString());
            Map<String, Integer> methodNameCounts = new HashMap<>();
            for (MethodBlock method : pu.methods) {
                methodNameCounts.merge(method.name, 1, Integer::sum);
            }
            List<MethodBlock> remove = new ArrayList<>();
            for (MethodBlock m : pu.methods) {
                String methodChunk = pu.source.substring(m.removeStart, m.removeEnd);
                /*
                if (methodChunk.contains("org.graphstream.")) {
                    remove.add(m);
                    continue;
                }
                */
                if (used.contains(m.id(pu.className)) /*|| !m.hasExplicitAccess*/) {
                    continue;
                }
                /*
                if (methodNameCounts.getOrDefault(m.name, 0) == 1) {
                    Pattern callPattern = Pattern.compile("\\b" + Pattern.quote(m.name) + "\\s*\\(");
                    if (callPattern.matcher(cleanedUsedBodies).find()) {
                        continue;
                    }
                }
                */
                remove.add(m);
            }
            unit.body = pu.removeMethods(remove);
        }

    }
    private static void enqueueMethodsFromBody(String body, String ownerClassName, Map<String, ParsedUnit> parsedByClass,
            ReachabilityIndex index, Map<String, MethodNode> nodeById, Deque<MethodNode> queue, Set<String> used) {
        String cleaned = stripCommentsAndStrings(body);

        ParsedUnit owner = parsedByClass.get(ownerClassName);
        if (owner != null) {
            Map<String, List<MethodBlock>> ownerMethodsByName = index.methodsByClassByName.get(ownerClassName);
            if (ownerMethodsByName != null) {
                for (String callName : extractUnqualifiedCallNames(cleaned)) {
                    List<MethodBlock> candidates = ownerMethodsByName.get(callName);
                    if (candidates == null) {
                        continue;
                    }
                    for (MethodBlock cand : candidates) {
                        enqueue(owner.className, cand, nodeById, queue, used, "Unqualified call from " + owner.className + "#" + callName);
                    }
                }
            }
        }

        for (String ctorType : extractCtorTypeNames(cleaned)) {
            List<MethodBlock> constructors = index.constructorsByClass.get(ctorType);
            if (constructors == null) {
                continue;
            }
            for (MethodBlock ctor : constructors) {
                enqueue(ctorType, ctor, nodeById, queue, used, "Constructor call for " + ctorType);
            }
        }

        for (QualifiedCall call : extractQualifiedCalls(cleaned)) {
            Map<String, List<MethodBlock>> methodsByName = index.methodsByClassByName.get(call.className);
            if (methodsByName == null) {
                continue;
            }
            List<MethodBlock> methods = methodsByName.get(call.methodName);
            if (methods == null) {
                continue;
            }
            for (MethodBlock m : methods) {
                enqueue(call.className, m, nodeById, queue, used, "Qualified call from " + call.className + "#" + call.methodName);
            }
        }

        Map<String, String> localVariableTypes = inferLocalVariableTypes(cleaned, index.methodsByClassByName.keySet());
        Set<String> fallbackReceiverTypes = owner == null
                ? Set.of()
                : index.typeMentionsByClass.getOrDefault(ownerClassName, Set.of());
        for (MemberCall call : extractMemberCalls(cleaned)) {
            String receiverType = localVariableTypes.get(call.receiver);
            if (receiverType != null) {
                enqueueByClassAndMethod(receiverType, call.methodName, index, nodeById, queue, used, "Member call from variable " + call.receiver);
                continue;
            }
            if (Character.isUpperCase(call.receiver.charAt(0))) {
                enqueueByClassAndMethod(call.receiver, call.methodName, index, nodeById, queue, used, "Member call from static receiver " + call.receiver);
                continue;
            }
            if ("this".equals(call.receiver) || "super".equals(call.receiver)) {
                enqueueByClassAndMethod(ownerClassName, call.methodName, index, nodeById, queue, used, "Member call from this/super in " + ownerClassName);
                continue;
            }
            for (String candidateType : fallbackReceiverTypes) {
                enqueueByClassAndMethod(candidateType, call.methodName, index, nodeById, queue, used, "Member call from fallback receiver " + candidateType);
            }
        }

        // Add logic for chained method calls
        Set<String> chainedMethodNames = extractChainedMethodNames(cleaned);
        if (!chainedMethodNames.isEmpty()) {
            System.err.println("DEBUG: Processing chained method calls in body owned by " + ownerClassName + ". Found: " + chainedMethodNames);
        }
        for (String methodName : chainedMethodNames) {
            for (String typeName : index.methodsByClassByName.keySet()) {
                enqueueByClassAndMethod(typeName, methodName, index, nodeById, queue, used, "Chained call for " + methodName + " (speculative)");
            }
        }
    }

    private static Set<String> extractUnqualifiedCallNames(String code) {
        LinkedHashSet<String> names = new LinkedHashSet<>();
        Matcher matcher = UNQUALIFIED_CALL_NAME_PATTERN.matcher(code);
        while (matcher.find()) {
            String name = matcher.group(1);
            if (!ParsedUnit.isControlKeyword(name)) {
                names.add(name);
            }
        }
        return names;
    }

    private static Set<String> extractChainedMethodNames(String code) {
        LinkedHashSet<String> names = new LinkedHashSet<>();
        Matcher matcher = DOT_CALL_NAME_PATTERN.matcher(code);
        while (matcher.find()) {
            String name = matcher.group(1);
            names.add(name);
            System.err.println("DEBUG: Extracted chained method name: " + name + " from code: " + code.replace('\n', ' ').substring(0, Math.min(code.length(), 100)) + "...");
        }
        return names;
    }

    private static List<MemberCall> extractMemberCalls(String code) {
        List<MemberCall> calls = new ArrayList<>();
        Matcher matcher = MEMBER_CALL_PATTERN.matcher(code);
        while (matcher.find()) {
            calls.add(new MemberCall(matcher.group(1), matcher.group(2)));
        }
        return calls;
    }

    private static Map<String, String> inferLocalVariableTypes(String code, Set<String> knownTypeNames) {
        Map<String, String> types = new HashMap<>();
        Matcher declaredMatcher = LOCAL_DECLARED_TYPE_PATTERN.matcher(code);
        while (declaredMatcher.find()) {
            String typeName = declaredMatcher.group(1);
            String variableName = declaredMatcher.group(2);
            if (knownTypeNames.contains(typeName)) {
                types.put(variableName, typeName);
            }
        }

        Matcher varMatcher = VAR_NEW_ASSIGN_PATTERN.matcher(code);
        while (varMatcher.find()) {
            types.put(varMatcher.group(1), varMatcher.group(2));
        }
        return types;
    }

    private static Set<String> extractCtorTypeNames(String code) {
        LinkedHashSet<String> names = new LinkedHashSet<>();
        Matcher matcher = CTOR_TYPE_PATTERN.matcher(code);
        while (matcher.find()) {
            names.add(matcher.group(1));
        }
        return names;
    }

    private static List<QualifiedCall> extractQualifiedCalls(String code) {
        List<QualifiedCall> calls = new ArrayList<>();
        Matcher matcher = QUALIFIED_CALL_PATTERN.matcher(code);
        while (matcher.find()) {
            calls.add(new QualifiedCall(matcher.group(1), matcher.group(2)));
        }
        return calls;
    }

    private static final class QualifiedCall {
        final String className;
        final String methodName;

        QualifiedCall(String className, String methodName) {
            this.className = className;
            this.methodName = methodName;
        }
    }

    private static void enqueueByClassAndMethod(String className, String methodName, ReachabilityIndex index,
            Map<String, MethodNode> nodeById, Deque<MethodNode> queue, Set<String> used, String reason) {
        Map<String, List<MethodBlock>> methodsByName = index.methodsByClassByName.get(className);
        if (methodsByName == null) {
            return;
        }
        List<MethodBlock> methods = methodsByName.get(methodName);
        if (methods == null) {
            return;
        }
                    for (MethodBlock m : methods) {
                        enqueue(className, m, nodeById, queue, used, reason);
                    }    }

    private static final class MemberCall {
        final String receiver;
        final String methodName;

        MemberCall(String receiver, String methodName) {
            this.receiver = receiver;
            this.methodName = methodName;
        }
    }

    private static final class ReachabilityIndex {
        final Map<String, Map<String, List<MethodBlock>>> methodsByClassByName;
        final Map<String, List<MethodBlock>> constructorsByClass;
        final Map<String, Set<String>> typeMentionsByClass;

        ReachabilityIndex(Map<String, Map<String, List<MethodBlock>>> methodsByClassByName,
                Map<String, List<MethodBlock>> constructorsByClass, Map<String, Set<String>> typeMentionsByClass) {
            this.methodsByClassByName = methodsByClassByName;
            this.constructorsByClass = constructorsByClass;
            this.typeMentionsByClass = typeMentionsByClass;
        }

        static ReachabilityIndex build(Map<String, ParsedUnit> parsedByClass) {
            Map<String, Map<String, List<MethodBlock>>> methodsByClassByName = new HashMap<>();
            Map<String, List<MethodBlock>> constructorsByClass = new HashMap<>();
            Map<String, Set<String>> typeMentionsByClass = new HashMap<>();
            Set<String> classNames = parsedByClass.keySet();

            for (ParsedUnit pu : parsedByClass.values()) {
                Map<String, List<MethodBlock>> methodsByName = new HashMap<>();
                List<MethodBlock> constructors = new ArrayList<>();
                for (MethodBlock mb : pu.methods) {
                    methodsByName.computeIfAbsent(mb.name, k -> new ArrayList<>()).add(mb);
                    if (mb.isConstructor) {
                        constructors.add(mb);
                    }
                }
                methodsByClassByName.put(pu.className, methodsByName);
                constructorsByClass.put(pu.className, constructors);

                Set<String> mentions = new HashSet<>();
                for (String id : referencedTypeLikeIdentifiers(pu.source)) {
                    if (classNames.contains(id)) {
                        mentions.add(id);
                    }
                }
                typeMentionsByClass.put(pu.className, mentions);
            }
            return new ReachabilityIndex(methodsByClassByName, constructorsByClass, typeMentionsByClass);
        }
    }


    private static List<SourceUnit> removeMergeFilesInvocations(List<SourceUnit> units) {
        for (SourceUnit unit : units) {
            unit.body = removeMergeExportInvocations(unit.body);
        }
        return units;
    }

    private static String removeMergeExportInvocations(String source) {
        List<int[]> replaceRanges = new ArrayList<>();
        boolean[] codeMask = computeCodeMask(source);
        Matcher matcher = MERGE_EXPORT_INVOCATION_PREFIX.matcher(source);
        while (matcher.find()) {
            int start = matcher.start();
            int openParen = matcher.end() - 1;
            if (!isCodeRange(source, codeMask, start, openParen + 1)) {
                continue;
            }
            int closeParen = findMatchingParen(source, openParen);
            if (closeParen < 0) {
                continue;
            }
            int semicolon = closeParen + 1;
            while (semicolon < source.length() && Character.isWhitespace(source.charAt(semicolon))) {
                semicolon++;
            }
            if (semicolon >= source.length() || source.charAt(semicolon) != ';' || !codeMask[semicolon]) {
                continue;
            }
            replaceRanges.add(new int[] { start, semicolon + 1 });
        }

        if (replaceRanges.isEmpty()) {
            return source;
        }

        StringBuilder sb = new StringBuilder(source);
        replaceRanges.sort((a, b) -> Integer.compare(b[0], a[0]));
        for (int[] range : replaceRanges) {
            sb.replace(range[0], range[1], ";");
        }
        return sb.toString();
    }

    private static int findMatchingParen(String source, int openParen) {
        int depth = 0;
        boolean inLineComment = false;
        boolean inBlockComment = false;
        boolean inString = false;
        boolean inTextBlock = false;
        boolean inChar = false;
        for (int i = openParen; i < source.length(); i++) {
            char c = source.charAt(i);
            char next = i + 1 < source.length() ? source.charAt(i + 1) : '\0';

            if (inLineComment) {
                if (c == '\n') {
                    inLineComment = false;
                }
                continue;
            }
            if (inBlockComment) {
                if (c == '*' && next == '/') {
                    inBlockComment = false;
                    i++;
                }
                continue;
            }
            if (inString) {
                if (c == '\\') {
                    i++;
                } else if (c == '"') {
                    inString = false;
                }
                continue;
            }
            if (inTextBlock) {
                if (c == '"' && next == '"' && i + 2 < source.length() && source.charAt(i + 2) == '"') {
                    inTextBlock = false;
                    i += 2;
                }
                continue;
            }
            if (inChar) {
                if (c == '\\') {
                    i++;
                } else if (c == '\'') {
                    inChar = false;
                }
                continue;
            }

            if (c == '/' && next == '/') {
                inLineComment = true;
                i++;
                continue;
            }
            if (c == '/' && next == '*') {
                inBlockComment = true;
                i++;
                continue;
            }
            if (c == '"' && next == '"' && i + 2 < source.length() && source.charAt(i + 2) == '"') {
                inTextBlock = true;
                i += 2;
                continue;
            }
            if (c == '"') {
                inString = true;
                continue;
            }
            if (c == '\'') {
                inChar = true;
                continue;
            }

            if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    private static boolean[] computeCodeMask(String source) {
        boolean[] mask = new boolean[source.length()];
        boolean inLineComment = false;
        boolean inBlockComment = false;
        boolean inString = false;
        boolean inTextBlock = false;
        boolean inChar = false;
        for (int i = 0; i < source.length(); i++) {
            char c = source.charAt(i);
            char next = i + 1 < source.length() ? source.charAt(i + 1) : '\0';

            if (inLineComment) {
                if (c == '\n') {
                    inLineComment = false;
                }
                continue;
            }
            if (inBlockComment) {
                if (c == '*' && next == '/') {
                    i++;
                    inBlockComment = false;
                }
                continue;
            }
            if (inString) {
                if (c == '\\') {
                    i++;
                } else if (c == '"') {
                    inString = false;
                }
                continue;
            }
            if (inTextBlock) {
                if (c == '"' && next == '"' && i + 2 < source.length() && source.charAt(i + 2) == '"') {
                    inTextBlock = false;
                    i += 2;
                }
                continue;
            }
            if (inChar) {
                if (c == '\\') {
                    i++;
                } else if (c == '\'') {
                    inChar = false;
                }
                continue;
            }

            if (c == '/' && next == '/') {
                inLineComment = true;
                i++;
                continue;
            }
            if (c == '/' && next == '*') {
                inBlockComment = true;
                i++;
                continue;
            }
            if (c == '"' && next == '"' && i + 2 < source.length() && source.charAt(i + 2) == '"') {
                inTextBlock = true;
                i += 2;
                continue;
            }
            if (c == '"') {
                inString = true;
                continue;
            }
            if (c == '\'') {
                inChar = true;
                continue;
            }

            mask[i] = true;
        }
        return mask;
    }

    private static boolean isCodeRange(String source, boolean[] codeMask, int startInclusive, int endExclusive) {
        for (int i = startInclusive; i < endExclusive; i++) {
            if (!codeMask[i] && !Character.isWhitespace(source.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static List<SourceUnit> pruneUnusedTypes(List<SourceUnit> units, String entryClass) {
        Map<String, SourceUnit> byName = new LinkedHashMap<>();
        Set<String> projectTypeNames = new HashSet<>();
        for (SourceUnit unit : units) {
            if ("MergeFiles".equals(unit.simpleTypeName) || "MergeFilesFast".equals(unit.simpleTypeName)) {
                continue;
            }
            byName.put(unit.simpleTypeName, unit);
            projectTypeNames.add(unit.simpleTypeName);
        }

        LinkedHashSet<String> reachable = new LinkedHashSet<>();
        Deque<SourceUnit> queue = new ArrayDeque<>();
        SourceUnit entry = byName.get(entryClass);
        if (entry == null) {
            return units;
        }
        queue.add(entry);

        while (!queue.isEmpty()) {
            SourceUnit current = queue.poll();
            if (!reachable.add(current.simpleTypeName)) {
                continue;
            }
            for (String name : referencedTypeLikeIdentifiers(current.body)) {
                if (projectTypeNames.contains(name) && !reachable.contains(name)) {
                    SourceUnit next = byName.get(name);
                    if (next != null) {
                        queue.add(next);
                    }
                }
            }
        }

        List<SourceUnit> pruned = new ArrayList<>();
        for (SourceUnit unit : units) {
            if (reachable.contains(unit.simpleTypeName)) {
                pruned.add(unit);
            }
        }
        return pruned;
    }

    /**
     * クラス内で未使用と思われるトップレベル型（static nested classなど）を削除します。
     *
     * @param body         クラスのボディ部分（{ }の中身）
     * @param rootTypeName このクラスのシンプル名（削除の基準）
     * @return 不要なトップレベル型を削除した後のボディ文字列
     */
    private static void pruneUnusedTopLevelTypes(List<SourceUnit> units) {
        for (SourceUnit unit : units) {
            unit.body = pruneUnusedTopLevelTypesInBody(unit.body, unit.simpleTypeName);
        }
    }

    private static String pruneUnusedTopLevelTypesInBody(String body, String rootTypeName) {
        List<TopLevelTypeBlock> blocks = extractTopLevelTypeBlocks(body);
        if (blocks.size() <= 1) {
            return body;
        }

        Map<String, TopLevelTypeBlock> byName = new LinkedHashMap<>();
        for (TopLevelTypeBlock block : blocks) {
            byName.put(block.name, block);
        }

        LinkedHashSet<String> keep = new LinkedHashSet<>();
        if (byName.containsKey(rootTypeName)) {
            keep.add(rootTypeName);
        } else {
            keep.add(blocks.get(0).name);
        }

        Deque<String> queue = new ArrayDeque<>(keep);
        while (!queue.isEmpty()) {
            String current = queue.poll();
            TopLevelTypeBlock block = byName.get(current);
            if (block == null) {
                continue;
            }
            for (String ref : referencedTypeLikeIdentifiers(block.text(body))) {
                if (byName.containsKey(ref) && keep.add(ref)) {
                    queue.add(ref);
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        for (TopLevelTypeBlock block : blocks) {
            if (!keep.contains(block.name)) {
                continue;
            }
            if (!sb.isEmpty()) {
                sb.append("\n\n");
            }
            sb.append(body, block.start, block.end);
        }
        return sb.isEmpty() ? body : sb.toString().strip();
    }

    private static List<TopLevelTypeBlock> extractTopLevelTypeBlocks(String body) {
        List<TopLevelTypeBlock> blocks = new ArrayList<>();
        boolean[] codeMask = computeCodeMask(body);
        int depth = 0;
        for (int i = 0; i < body.length(); i++) {
            char c = body.charAt(i);
            if (!codeMask[i]) {
                continue;
            }
            if (c == '{') {
                depth++;
                continue;
            }
            if (c == '}') {
                depth = Math.max(0, depth - 1);
                continue;
            }
            if (depth != 0) {
                continue;
            }

            Matcher m = TYPE_DECL_PATTERN.matcher(body);
            m.region(i, body.length());
            if (!m.lookingAt()) {
                continue;
            }
            String name = m.group(2);
            int open = findNextCodeChar(body, codeMask, m.end(), '{');
            if (open < 0) {
                continue;
            }
            int close = ParsedUnit.findMatchingBrace(body, open);
            if (close < 0) {
                continue;
            }
            blocks.add(new TopLevelTypeBlock(name, m.start(), close + 1));
            System.err.println("DEBUG: Found TopLevelTypeBlock: " + name + ", start: " + m.start() + ", end: " + (close + 1));
            i = close;
        }
        return blocks;
    }

    private static int findNextCodeChar(String source, boolean[] codeMask, int fromIndex, char target) {
        for (int i = Math.max(0, fromIndex); i < source.length(); i++) {
            if (codeMask[i] && source.charAt(i) == target) {
                return i;
            }
        }
        return -1;
    }

    private static void enqueue(String className, MethodBlock block, Map<String, MethodNode> nodeById, Deque<MethodNode> q,
            Set<String> used, String reason) {
        String id = block.id(className);
        MethodNode node = nodeById.get(id);
        if (className.equals("MergeFilesFast"))return; // Avoid enqueuing MergeFilesFast's own methods
        if (node != null && used.add(id)) {
            q.add(node);
            System.err.println("DEBUG: Enqueued method: " + className + "#" + block.name + " (Reason: " + reason + ")");
        }
    }


    private static Set<String> referencedTypeLikeIdentifiers(String text) {
        String cleaned = stripCommentsAndStrings(text);
        Matcher matcher = IDENTIFIER_PATTERN.matcher(cleaned);
        LinkedHashSet<String> ids = new LinkedHashSet<>();
        while (matcher.find()) {
            String id = matcher.group();
            if (!isKeywordLike(id)) {
                ids.add(id);
            }
        }
        return ids;
    }

    private static String joinBodies(List<SourceUnit> units) {
        StringBuilder sb = new StringBuilder();
        for (SourceUnit u : units) {
            sb.append(u.body).append('\n');
        }
        return sb.toString();
    }

    /**
     * メインソースファイルを起点に、プロジェクト内の関連するすべてのJavaソースファイルを読み込み、
     * 完全修飾名をキーとしたマップとして返します。
     * <p>
     * このメソッドはマージ処理の最初のステップであり、以下の流れで動作します：
     * <ol>
     *   <li>指定されたメインソースファイル（mainFile）を解析して読み込む</li>
     *   <li>そのファイル内で参照されている型（クラス・インタフェースなど）を探索</li>
     *   <li>同一パッケージ・インポート文・ファイル名一致などのルールに基づいて関連ファイルを芋づる式に発見</li>
     *   <li>BFS（幅優先探索）を使用して、参照先のソースを順次読み込みマップに蓄積</li>
     *   <li>出力先ファイル（outputFile）は除外して無限ループを防ぐ</li>
     * </ol>
     * </p>
     * <p>
     * 結果として返されるマップには、プロジェクト内で「自作と思われる」ほぼすべてのJavaクラスが含まれます。
     * これを基に後続の到達解析（resolveReachableTypes）が行われ、実際に必要な型だけが抽出されます。
     * </p>
     *
     * @param mainFile    解析の起点となるメインのJavaソースファイルのパス（通常Main.java）
     * @param outputFile  出力先ファイルのパス（このパスに該当するファイルは読み込み対象から除外）
     * @return            完全修飾名（qualified name）をキーとし、JavaSourceオブジェクトを値とするマップ
     *                    （プロジェクト内で発見された自作クラスのほぼ全情報）
     * @throws IOException ファイルの読み込みに失敗した場合（存在しない、権限エラーなど）
     */
    private static Map<String, JavaSource> loadJavaSources(Path mainFile, Path outputFile) throws IOException {
        Map<String, JavaSource> map = new LinkedHashMap<>();
        Path projectRoot = Paths.get("").toAbsolutePath().normalize();
        Map<String, List<Path>> pathIndex = buildJavaPathIndex(projectRoot, outputFile);
        Map<Path, JavaSource> parsedCache = new HashMap<>();

        if (!Files.exists(mainFile)) {
            return map;
        }

        JavaSource entry = parseSourceCached(mainFile, parsedCache);
        if (entry == null || entry.simpleTypeName == null || entry.simpleTypeName.isEmpty()) {
            return map;
        }
        map.put(entry.qualifiedTypeName, entry);

        Deque<JavaSource> queue = new ArrayDeque<>();
        queue.add(entry);
        while (!queue.isEmpty()) {
            JavaSource current = queue.poll();
            for (String id : current.referencedTypeLikeIdentifiers()) {
                if (map.values().stream().anyMatch(s -> s.simpleTypeName.equals(id))) {
                    continue;
                }
                JavaSource next = resolveAndLoadCandidate(current, id, projectRoot, pathIndex, parsedCache);
                if (next != null && !map.containsKey(next.qualifiedTypeName)) {
                    map.put(next.qualifiedTypeName, next);
                    queue.add(next);
                }
            }
        }
        return map;
    }

    private static Map<String, List<Path>> buildJavaPathIndex(Path projectRoot, Path outputFile) throws IOException {
        Map<String, List<Path>> index = new HashMap<>();
        try (var stream = Files.walk(projectRoot)) {
            stream.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .filter(p -> !p.toAbsolutePath().normalize().equals(outputFile))
                    .filter(p -> !isInHiddenGitDir(projectRoot, p))
                    .forEach(path -> {
                        String fileName = path.getFileName().toString();
                        if (!fileName.endsWith(".java")) {
                            return;
                        }
                        String simple = fileName.substring(0, fileName.length() - ".java".length());
                        index.computeIfAbsent(simple, k -> new ArrayList<>()).add(path.toAbsolutePath().normalize());
                    });
        }
        return index;
    }

    private static boolean isInHiddenGitDir(Path root, Path path) {
        Path rel;
        try {
            rel = root.relativize(path.toAbsolutePath().normalize());
        } catch (IllegalArgumentException e) {
            return false;
        }
        for (Path part : rel) {
            if (".git".equals(part.toString())) {
                return true;
            }
        }
        return false;
    }

    private static void putSource(Path path, Map<String, JavaSource> map) {
        try {
            JavaSource source = JavaSource.parse(path);
            if (source.simpleTypeName != null && !source.simpleTypeName.isEmpty()) {
                map.put(source.qualifiedTypeName, source);
            }
        } catch (IOException ignored) {
        }
    }

    private static JavaSource resolveAndLoadCandidate(JavaSource current,
            String simpleTypeName,
            Path projectRoot,
            Map<String, List<Path>> pathIndex,
            Map<Path, JavaSource> parsedCache) {

        LinkedHashSet<Path> candidatePaths = new LinkedHashSet<>();

        Path samePackage = packageToPath(projectRoot, current.packageName).resolve(simpleTypeName + ".java");
        if (Files.exists(samePackage)) {
            candidatePaths.add(samePackage.toAbsolutePath().normalize());
        }

        for (String imp : current.importedQualifiedNames) {
            if (imp.endsWith("." + simpleTypeName)) {
                Path importedPath = qualifiedToPath(projectRoot, imp);
                if (Files.exists(importedPath)) {
                    candidatePaths.add(importedPath.toAbsolutePath().normalize());
                }
            }
        }

        for (String imp : current.importedQualifiedNames) {
            if (!imp.endsWith(".*")) {
                continue;
            }
            String pkg = imp.substring(0, imp.length() - 2);
            if (pkg.startsWith("java.") || pkg.startsWith("javax.")) {
                continue;
            }
            Path wildcardPath = packageToPath(projectRoot, pkg).resolve(simpleTypeName + ".java");
            if (Files.exists(wildcardPath)) {
                candidatePaths.add(wildcardPath.toAbsolutePath().normalize());
            }
        }

        for (Path p : pathIndex.getOrDefault(simpleTypeName, List.of())) {
            candidatePaths.add(p);
        }

        List<JavaSource> candidates = new ArrayList<>();
        for (Path p : candidatePaths) {
            JavaSource src = parseSourceCached(p, parsedCache);
            if (src == null) {
                continue;
            }
            if (simpleTypeName.equals(src.simpleTypeName)) {
                candidates.add(src);
            }
        }

        return chooseCandidate(current, candidates);
    }

    private static JavaSource parseSourceCached(Path path, Map<Path, JavaSource> parsedCache) {
        Path normalized = path.toAbsolutePath().normalize();
        JavaSource cached = parsedCache.get(normalized);
        if (cached != null) {
            return cached;
        }
        try {
            JavaSource src = JavaSource.parse(normalized);
            parsedCache.put(normalized, src);
            return src;
        } catch (IOException e) {
            return null;
        }
    }

    private static Path packageToPath(Path projectRoot, String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            return projectRoot;
        }
        Path p = projectRoot;
        for (String part : packageName.split("\\.")) {
            p = p.resolve(part);
        }
        return p;
    }

    private static Path qualifiedToPath(Path projectRoot, String qualifiedName) {
        String rel = qualifiedName.replace('.', '/') + ".java";
        return projectRoot.resolve(rel);
    }

    private static JavaSource findEntry(Map<String, JavaSource> allSources, String entryClass, Path mainFile) {
        Path normalizedMain = mainFile.toAbsolutePath().normalize();
        for (JavaSource src : allSources.values()) {
            if (src.simpleTypeName.equals(entryClass) && src.sourcePath.equals(normalizedMain)) {
                return src;
            }
        }
        for (JavaSource src : allSources.values()) {
            if (src.simpleTypeName.equals(entryClass)) {
                return src;
            }
        }
        throw new IllegalArgumentException("Entry class not found: " + entryClass + " (main file: " + mainFile + ")");
    }
    

    /**
     * プロジェクト内で読み込んだすべてのJavaソースから、登場する型の単純名（simple name）を収集します。
     * <p>
     * 返されるセットには、各クラスの {@code simpleTypeName}（パッケージ名を除いた名前）のみが含まれます。
     * 主に到達解析や名前解決の候補絞り込みに使用されます。
     * </p>
     *
     * @param allSources プロジェクト内で読み込んだすべてのJavaソースのマップ
     * @return           プロジェクト内に存在するすべての型の単純名の集合
     */
    private static Set<String> collectSimpleTypeNames(Map<String, JavaSource> allSources) {
        Set<String> names = new HashSet<>();
        for (JavaSource src : allSources.values()) {
            names.add(src.simpleTypeName);
        }
        return names;
    }
    
    /**
     * エントリーポイント（通常Mainクラス）から到達可能な型の完全修飾名（qualified name）を収集します。
     * <p>
     * BFS（幅優先探索）を使用して、参照されている型の単純名を起点に、インポート状況や同一パッケージの
     * ルールに基づいて最も適切な候補を選択しながら到達可能な型を特定します。
     * </p>
     * <p>
     * このメソッドの結果は、実際にマージ対象とする型（クラス・インタフェースなど）を決定するための
     * 基盤となります。不要な型はここで除外されるため、後の処理で大幅に削減されます。
     * </p>
     *
     * @param entry           到達解析の起点となるJavaSource（通常Mainクラス）
     * @param allSources      プロジェクト内で読み込んだすべてのJavaソースのマップ
     * @param projectTypeNames プロジェクト内に定義されている型の単純名の集合
     *                         （{@link #collectSimpleTypeNames} の結果）
     * @return                エントリーポイントから到達可能な型の完全修飾名の集合
     */
    private static Set<String> resolveReachableTypes(JavaSource entry,
            Map<String, JavaSource> allSources,
            Set<String> projectTypeNames) {

        Map<String, List<JavaSource>> bySimpleName = new HashMap<>();
        for (JavaSource src : allSources.values()) {
            bySimpleName.computeIfAbsent(src.simpleTypeName, k -> new ArrayList<>()).add(src);
        }

        LinkedHashSet<String> reachable = new LinkedHashSet<>();
        Deque<JavaSource> queue = new ArrayDeque<>();
        queue.add(entry);

        while (!queue.isEmpty()) {
            JavaSource current = queue.poll();
            if (!reachable.add(current.qualifiedTypeName)) {
                continue;
            }

            Set<String> identifiers = current.referencedTypeLikeIdentifiers();
            for (String id : identifiers) {
                if (!projectTypeNames.contains(id)) {
                    continue;
                }
                List<JavaSource> candidates = bySimpleName.getOrDefault(id, List.of());
                JavaSource picked = chooseCandidate(current, candidates);
                if (picked != null && !reachable.contains(picked.qualifiedTypeName)) {
                    queue.add(picked);
                }
            }
        }

        return reachable;
    }

    private static JavaSource chooseCandidate(JavaSource from, List<JavaSource> candidates) {
        if (candidates.isEmpty()) {
            return null;
        }

        for (JavaSource c : candidates) {
            if (c.packageName.equals(from.packageName)) {
                return c;
            }
        }

        Set<String> imported = from.importedQualifiedNames;
        for (JavaSource c : candidates) {
            if (imported.contains(c.qualifiedTypeName)) {
                return c;
            }
        }

        return candidates.get(0);
    }

    private static String injectExceptionHandlerIntoMain(String src) {
        if (src.contains("Thread.setDefaultUncaughtExceptionHandler")) {
            return src;
        }
        Matcher m = MAIN_PATTERN.matcher(src);
        if (!m.find()) {
            return src;
        }
        String insertion = m.group(1) + "\n        Thread.setDefaultUncaughtExceptionHandler((t, e) -> System.exit(1));";
        return m.replaceFirst(Matcher.quoteReplacement(insertion));
    }

    /**
     * マージ対象の1つの型（クラス・インタフェース・enum・record）を表す軽量なデータクラス
     */
    private static final class SourceUnit {
        final String simpleTypeName;
        final String qualifiedTypeName;//完全修飾名（パッケージ名＋クラス名）

        String body;

        SourceUnit(String simpleTypeName, String qualifiedTypeName, String body) {
            this.simpleTypeName = simpleTypeName;
            this.qualifiedTypeName = qualifiedTypeName;
            this.body = body;
        }
    }

    private static final class TopLevelTypeBlock {
        final String name;
        final int start;
        final int end;

        TopLevelTypeBlock(String name, int start, int end) {
            this.name = name;
            this.start = start;
            this.end = end;
        }

        String text(String source) {
            return source.substring(start, end);
        }
    }

    private static final class MethodNode {
        final String className;
        final MethodBlock block;

        MethodNode(String className, MethodBlock block) {
            this.className = className;
            this.block = block;
        }

        String id() {
            return block.id(className);
        }
    }

    private static final class ParsedUnit {
        final String className;
        final String source;
        final List<MethodBlock> methods;
        final List<String> initializerBodies;

        ParsedUnit(String className, String source, List<MethodBlock> methods, List<String> initializerBodies) {
            this.className = className;
            this.source = source;
            this.methods = methods;
            this.initializerBodies = initializerBodies;
        }

        /**
         * ファイルからパッケージ名、クラス名、import文を解析してJavaSourceオブジェクトを生成します。
         *
         * @param path 解析対象の.javaファイルパス
         * @return 解析結果。失敗した場合は空のsimpleTypeNameを持つオブジェクト
         * @throws IOException ファイル読み込みに失敗した場合
         */
        static ParsedUnit parse(SourceUnit unit) {
            List<MethodBlock> methods = new ArrayList<>();
            int classOpenBraceIdx = unit.body.indexOf('{');
            if (classOpenBraceIdx == -1) { // No class body
                return new ParsedUnit(unit.simpleTypeName, unit.body, methods, List.of());
            }

            Matcher m = METHOD_HEADER_PATTERN.matcher(unit.body);
            boolean[] codeMask = computeCodeMask(unit.body); // Compute mask once
            while (m.find()) {
                int methodOpenBraceIdx = m.end() - 1;

                // Calculate depth of methodOpenBraceIdx
                int currentDepth = 0;
                for (int i = classOpenBraceIdx; i <= methodOpenBraceIdx; i++) {
                    if (!codeMask[i]) continue;
                    char c = unit.body.charAt(i);
                    if (c == '{') currentDepth++;
                    else if (c == '}') currentDepth--;
                }

                if (currentDepth != 2) { // Method is nested, skip it
                    continue;
                }
                
                String name = m.group(2);
                if (isControlKeyword(name)) {
                    continue;
                }
                int open = m.end() - 1;
                int close = findMatchingBrace(unit.body, open);
                if (close < 0) {
                    continue;
                }
                String header = m.group(1);
                boolean constructor = name.equals(unit.simpleTypeName);
                boolean keepByAnnotation = header.contains("@IncludeToMerge") || header.contains("@Override");
                boolean hasExplicitAccess = Pattern.compile("\\b(public|protected|private)\\b").matcher(header).find();
                int removeStart = findMethodRemoveStart(unit.body, m.start());
                methods.add(new MethodBlock(name, open + 1, close, removeStart, close + 1, constructor, keepByAnnotation,
                        hasExplicitAccess, unit.body));
                System.err.println("DEBUG: Created MethodBlock: " + name + ", removeStart: " + removeStart + ", removeEnd: " + (close + 1));
            }
            return new ParsedUnit(unit.simpleTypeName, unit.body, methods, parseInitializerBodies(unit.body));
        }

        private static List<String> parseInitializerBodies(String source) {
            List<String> bodies = new ArrayList<>();
            int classOpen = source.indexOf('{');
            if (classOpen < 0) {
                return bodies;
            }
            int classClose = findMatchingBrace(source, classOpen);
            if (classClose < 0) {
                return bodies;
            }
            boolean[] codeMask = computeCodeMask(source);
            int depth = 1;
            for (int i = classOpen + 1; i < classClose; i++) {
                if (!codeMask[i]) {
                    continue;
                }
                char c = source.charAt(i);
                if (c == '{') {
                    if (depth == 1 && isInitializerBlockStart(source, codeMask, i)) {
                        int close = findMatchingBrace(source, i);
                        if (close > i) {
                            bodies.add(source.substring(i + 1, close));
                            i = close;
                            continue;
                        }
                    }
                    depth++;
                } else if (c == '}') {
                    depth--;
                }
            }
            return bodies;
        }

        private static boolean isInitializerBlockStart(String source, boolean[] codeMask, int blockOpenIndex) {
            int i = blockOpenIndex - 1;
            while (i >= 0) {
                if (!codeMask[i]) {
                    i--;
                    continue;
                }
                char c = source.charAt(i);
                if (Character.isWhitespace(c)) {
                    i--;
                    continue;
                }
                if (c == ')') {
                    return false;
                }
                break;
            }
            return true;
        }

        String removeMethods(List<MethodBlock> remove) {
            System.err.println("DEBUG: Entering removeMethods for class: " + className);
            System.err.println("DEBUG: Original source length: " + source.length());
            // System.err.println("DEBUG: Original source body:\n" + source); // Only if absolutely necessary, can be very verbose
            if (remove.isEmpty()) {
                System.err.println("DEBUG: No methods to remove.");
                return source;
            }
            remove.sort(Comparator.comparingInt(m -> -m.removeStart));
            StringBuilder sb = new StringBuilder(source);
            for (MethodBlock m : remove) {
                System.err.println("DEBUG: Removing MethodBlock: " + m.name + ", removeStart: " + m.removeStart + ", removeEnd: " + m.removeEnd);
                sb.delete(m.removeStart, m.removeEnd);
            }
            String result = sb.toString().replaceAll("\\n{3,}", "\n\n").strip();
            System.err.println("DEBUG: Exiting removeMethods for class: " + className + ", result length: " + result.length());
            // System.err.println("DEBUG: Result body after removal:\n" + result); // Only if absolutely necessary
            return result;
        }

        private static int findMethodRemoveStart(String src, int methodStart) {
            int start = methodStart;
            int cursor = methodStart;
            while (cursor > 0) {
                int lineStart = src.lastIndexOf('\n', cursor - 1) + 1;
                String line = src.substring(lineStart, cursor).trim();
                if (line.isEmpty()
                        || line.startsWith("//")
                        || line.startsWith("/*")
                        || line.startsWith("*")
                        || line.startsWith("*/")
                        || line.startsWith("@")) {
                    start = lineStart;
                    if (lineStart == 0) {
                        break;
                    }
                    cursor = lineStart - 1;
                    continue;
                }
                break;
            }

            return start;
        }

        private static boolean isControlKeyword(String s) {
            return s.equals("if") || s.equals("for") || s.equals("while") || s.equals("switch") || s.equals("catch")
                    || s.equals("try") || s.equals("do") || s.equals("synchronized");
        }
        static int findMatchingBrace(String s, int open) {
            boolean[] codeMask = computeCodeMask(s);
            int depth = 0;
            for (int i = open; i < s.length(); i++) {
                if (!codeMask[i]) {
                    continue;
                }
                char c = s.charAt(i);
                if (c == '{') {
                    depth++;
                } else if (c == '}') {
                    depth--;
                    if (depth == 0) {
                        return i;
                    }
                }
            }
            return -1;
        }
    }

    private static final class MethodBlock {
        final String name;
        final int bodyStart;
        final int bodyEnd;
        final int removeStart;
        final int removeEnd;
        final boolean isConstructor;
        final boolean keepByAnnotation;
        final boolean hasExplicitAccess;
        final String source;

        MethodBlock(String name, int bodyStart, int bodyEnd, int removeStart, int removeEnd, boolean isConstructor,
                boolean keepByAnnotation, boolean hasExplicitAccess, String source) {
            this.name = name;
            this.bodyStart = bodyStart;
            this.bodyEnd = bodyEnd;
            this.removeStart = removeStart;
            this.removeEnd = removeEnd;
            this.isConstructor = isConstructor;
            this.keepByAnnotation = keepByAnnotation;
            this.hasExplicitAccess = hasExplicitAccess;
            this.source = source;
        }

        String id(String className) {
            return className + "#" + name + "@" + removeStart;
        }

        String bodyText() {
            return source.substring(bodyStart, bodyEnd);
        }
    }



    private static boolean isKeywordLike(String s) {
        String k = s.toLowerCase(Locale.ROOT);
        return k.equals("public") || k.equals("private") || k.equals("protected") || k.equals("class")
                || k.equals("interface") || k.equals("enum") || k.equals("record") || k.equals("extends")
                || k.equals("implements") || k.equals("return") || k.equals("static") || k.equals("final")
                || k.equals("abstract") || k.equals("void") || k.equals("new") || k.equals("null")
                || k.equals("true") || k.equals("false") || k.equals("main") || k.equals("string");
    }

    private static String stripCommentsAndStrings(String src) {
        StringBuilder sb = new StringBuilder(src.length());
        boolean inLineComment = false;
        boolean inBlockComment = false;
        boolean inString = false;
        boolean inTextBlock = false;
        boolean inChar = false;
        for (int i = 0; i < src.length(); i++) {
            char c = src.charAt(i);
            char next = i + 1 < src.length() ? src.charAt(i + 1) : '\0';

            if (inLineComment) {
                if (c == '\n') {
                    inLineComment = false;
                    sb.append('\n');
                } else {
                    sb.append(' ');
                }
                continue;
            }
            if (inBlockComment) {
                if (c == '*' && next == '/') {
                    sb.append(' ').append(' ');
                    i++;
                    inBlockComment = false;
                } else {
                    sb.append(c == '\n' ? '\n' : ' ');
                }
                continue;
            }
            if (inString) {
                if (c == '\\') {
                    sb.append(' ');
                    if (i + 1 < src.length()) {
                        sb.append(' ');
                        i++;
                    }
                } else if (c == '"') {
                    sb.append(' ');
                    inString = false;
                } else {
                    sb.append(c == '\n' ? '\n' : ' ');
                }
                continue;
            }
            if (inTextBlock) {
                if (c == '"' && next == '"' && i + 2 < src.length() && src.charAt(i + 2) == '"') {
                    sb.append(' ').append(' ').append(' ');
                    i += 2;
                    inTextBlock = false;
                } else {
                    sb.append(c == '\n' ? '\n' : ' ');
                }
                continue;
            }
            if (inChar) {
                if (c == '\\') {
                    sb.append(' ');
                    if (i + 1 < src.length()) {
                        sb.append(' ');
                        i++;
                    }
                } else if (c == '\'') {
                    sb.append(' ');
                    inChar = false;
                } else {
                    sb.append(c == '\n' ? '\n' : ' ');
                }
                continue;
            }

            if (c == '/' && next == '/') {
                sb.append(' ').append(' ');
                i++;
                inLineComment = true;
                continue;
            }
            if (c == '/' && next == '*') {
                sb.append(' ').append(' ');
                i++;
                inBlockComment = true;
                continue;
            }
            if (c == '"' && next == '"' && i + 2 < src.length() && src.charAt(i + 2) == '"') {
                sb.append(' ').append(' ').append(' ');
                i += 2;
                inTextBlock = true;
                continue;
            }
            if (c == '"') {
                sb.append(' ');
                inString = true;
                continue;
            }
            if (c == '\'') {
                sb.append(' ');
                inChar = true;
                continue;
            }

            sb.append(c);
        }
        return sb.toString();
    }

    private static final class JavaSource {
        final String raw;
        final String packageName;
        final String simpleTypeName;
        final String qualifiedTypeName;
        final Set<String> importedQualifiedNames;
        final Path sourcePath;

        private JavaSource(String raw, String packageName, String simpleTypeName, Set<String> imports, Path sourcePath) {
            this.raw = raw;
            this.packageName = packageName;
            this.simpleTypeName = simpleTypeName;
            this.qualifiedTypeName = packageName.isEmpty() ? simpleTypeName : packageName + "." + simpleTypeName;
            this.importedQualifiedNames = imports;
            this.sourcePath = sourcePath;
        }

        static JavaSource parse(Path path) throws IOException {
            String raw = Files.readString(path, StandardCharsets.UTF_8);
            String packageName = "";
            Matcher pkg = PACKAGE_PATTERN.matcher(raw);
            if (pkg.find()) {
                packageName = pkg.group(1);
            }

            Matcher decl = TYPE_DECL_PATTERN.matcher(raw);
            if (!decl.find()) {
                return new JavaSource(raw, packageName, "", Set.of(), path.toAbsolutePath().normalize());
            }
            String simpleTypeName = decl.group(2);

            LinkedHashSet<String> imports = new LinkedHashSet<>();
            Matcher imp = IMPORT_PATTERN.matcher(raw);
            while (imp.find()) {
                imports.add(imp.group(1));
            }

            return new JavaSource(raw, packageName, simpleTypeName, imports, path.toAbsolutePath().normalize());
        }

        Set<String> javaImports() {
            Set<String> out = new LinkedHashSet<>();
            for (String imp : importedQualifiedNames) {
                if (imp.startsWith("java.") || imp.startsWith("javax.")) {
                    out.add(imp);
                }
            }
            return out;
        }

        String bodyWithoutPackageAndImport() {
            String noPkg = raw.replaceAll("(?m)^\\s*package\\s+[\\w.]+\\s*;\\s*", "");
            return noPkg.replaceAll("(?m)^\\s*import\\s+[\\w.*$]+\\s*;\\s*", "");
        }

        Set<String> referencedTypeLikeIdentifiers() {
            String cleaned = stripCommentsAndStrings(raw);
            Matcher matcher = IDENTIFIER_PATTERN.matcher(cleaned);
            LinkedHashSet<String> ids = new LinkedHashSet<>();
            while (matcher.find()) {
                String id = matcher.group();
                if (id.equals(simpleTypeName)) {
                    continue;
                }
                if (isKeywordLike(id)) {
                    continue;
                }
                ids.add(id);
            }
            return ids;
        }


    }
}