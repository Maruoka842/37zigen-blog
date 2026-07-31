package library.test;

import library.util.seq.StringUtils;
import java.util.Arrays;

public class StringUtilsTest {
    public static void main(String[] args) {
        testLyndon();
    }

    private static void testLyndon() {
        {
            char[] s = "banana".toCharArray();
            int[] res = StringUtils.lyndonDecomposition(s);
            int[] expected = {0, 1, 3, 5, 6};
            if (!Arrays.equals(res, expected)) {
                throw new RuntimeException("banana failed: " + Arrays.toString(res));
            }
        }
        {
            char[] s = "abacaba".toCharArray();
            int[] res = StringUtils.lyndonDecomposition(s);
            int[] expected = {0, 4, 6, 7};
            if (!Arrays.equals(res, expected)) {
                throw new RuntimeException("abacaba failed: " + Arrays.toString(res));
            }
        }
        {
            char[] s = "aaaaa".toCharArray();
            int[] res = StringUtils.lyndonDecomposition(s);
            int[] expected = {0, 1, 2, 3, 4, 5};
            if (!Arrays.equals(res, expected)) {
                throw new RuntimeException("aaaaa failed: " + Arrays.toString(res));
            }
        }
        System.out.println("Lyndon decomposition tests passed!");
    }
}
