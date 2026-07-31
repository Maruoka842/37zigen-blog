package library.tools;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

public class MergeFiles2 {
    public static void main(String[] args) throws IOException {
        export();
    }
    
    public static void export() throws IOException {
		export("Main", "src/template/Main.java", "C:/Users/forto/eclipse-workspace/MainMerged/Main/src/Main.java");
    }

    /**
     * Mainクラスを起点に競プロ用1ファイルを生成
     */
    public static void export(String entryClass, String mainFilePath, String outputFilePath) throws IOException {

    }
    
    class Class {
    	String className;
    	Set<String> methodNames;
    }
    
    
    
    
    static boolean DEBUG = true;

    static void tr(Object... objects) {
        if (DEBUG)
            System.out.println(Arrays.deepToString(objects));
    }
}
