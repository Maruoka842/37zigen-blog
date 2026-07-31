package library.util;

public class Chars{
	public static char max(char x, char y) {
		return x >= y ? x : y;
	}
	
	public static char min(char x, char y) {
		return x <= y ? x : y;
	}
	
	public static boolean isUpperCaseLetter(char c) {
		return 'A'<=c&&c<='Z';
	}
	
	public static boolean isLowerCaseLetter(char c) {
		return 'a'<=c&&c<='z';
	}
	
	public static int[] digitToIntArray(char[] cs) {
		int[] a = new int[cs.length];
		for (int i = 0; i < cs.length; i++) {
			a[i] = cs[i]-'0';
		}
		return a;
	}
	
	public static int[] lowercaseLettersToInts(char[] cs) {
		int[] a = new int[cs.length];
		for (int i = 0; i < cs.length; i++) {
			a[i] = cs[i]-'a';
		}
		return a;
	}

	public static int[] uppercaseLettersToInts(char[] cs) {
		int[] a = new int[cs.length];
		for (int i = 0; i < cs.length; i++) {
			a[i] = cs[i]-'A';
		}
		return a;
	}	
	
}

