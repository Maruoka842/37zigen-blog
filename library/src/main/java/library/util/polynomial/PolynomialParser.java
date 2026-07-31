package library.util.polynomial;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文字列形式の数式を MultivariatePolynomial に変換するパーサー。
 * 四則演算 (+, -, *), べき乗 (^), 括弧, 変数名に対応する。
 */
public class PolynomialParser {
    /** 有限体の標数。 */
    private final long mod;
    /** 変数名からインデックスへの対応マップ。 */
    private final Map<String, Integer> varMap;
    /** Monomial の全変数サイズ。 */
    private final int totalVars;

    /**
     * Javadoc: PolynomialParser のコンストラクタ。
     * Pre-condition: varMap != null, totalVars >= 0.
     * Calculation complexity: O(1).
     *
     * @param mod 有限体の標数。
     * @param varMap 変数名からインデックスへのマップ。
     * @param totalVars 扱う全変数の数（Monomial のサイズ）。
     */
    public PolynomialParser(long mod, Map<String, Integer> varMap, int totalVars) {
        this.mod = mod;
        this.varMap = varMap;
        this.totalVars = totalVars;
    }

    /**
     * Javadoc: PolynomialParser のファクトリメソッド。
     * Pre-condition: vars != null, vars の各要素が有効な変数名であること。
     * Post-condition: 指定された変数順序（vars[i] -> インデックス i）を持つパーサーを返す。
     * Calculation complexity: O(vars.length).
     *
     * @param mod 有限体の標数。
     * @param vars 変数名のリスト。
     * @return 生成されたパーサー。
     */
    public static PolynomialParser of(long mod, String... vars) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < vars.length; i++) {
            map.put(vars[i], i);
        }
        return new PolynomialParser(mod, map, vars.length);
    }

    private enum TokenType {
        NUMBER, VARIABLE, PLUS, MINUS, STAR, CARET, LPAREN, RPAREN, EOF
    }

    private static class Token {
        final TokenType type;
        final String value;

        Token(TokenType type, String value) {
            this.type = type;
            this.value = value;
        }
    }

    private static class Tokenizer {
        private final String input;
        private int pos = 0;
        private static final Pattern VAR_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9]*");
        private static final Pattern NUM_PATTERN = Pattern.compile("^[0-9]+");

        Tokenizer(String input) {
            this.input = input;
        }

        Token nextToken() {
            while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) {
                pos++;
            }

            if (pos >= input.length()) {
                return new Token(TokenType.EOF, "");
            }

            char c = input.charAt(pos);
            switch (c) {
                case '+': pos++; return new Token(TokenType.PLUS, "+");
                case '-': pos++; return new Token(TokenType.MINUS, "-");
                case '*': pos++; return new Token(TokenType.STAR, "*");
                case '^': pos++; return new Token(TokenType.CARET, "^");
                case '(': pos++; return new Token(TokenType.LPAREN, "(");
                case ')': pos++; return new Token(TokenType.RPAREN, ")");
            }

            Matcher varMatcher = VAR_PATTERN.matcher(input.substring(pos));
            if (varMatcher.find()) {
                String val = varMatcher.group();
                pos += val.length();
                return new Token(TokenType.VARIABLE, val);
            }

            Matcher numMatcher = NUM_PATTERN.matcher(input.substring(pos));
            if (numMatcher.find()) {
                String val = numMatcher.group();
                pos += val.length();
                return new Token(TokenType.NUMBER, val);
            }

            throw new RuntimeException("Unexpected character: " + c);
        }
    }

    private Tokenizer tokenizer;
    private Token currentToken;

    private void consume() {
        currentToken = tokenizer.nextToken();
    }

    /**
     * Javadoc: 文字列を解析して MultivariatePolynomial を生成する。
     * Mathematical notation: string S -> f in Fp[x0, ..., xn].
     * Pre-condition: input != null.
     * Post-condition: 入力文字列に対応する多項式を返す。
     * Calculation complexity: O(length(input) * polynomial_ops).
     *
     * @param input 解析対象の文字列。
     * @return 生成された多項式。
     */
    public MultivariatePolynomial<Long> parse(String input) {
        tokenizer = new Tokenizer(input);
        consume();
        MultivariatePolynomial<Long> res = expr();
        if (currentToken.type != TokenType.EOF) {
            throw new RuntimeException("Unexpected token at end: " + currentToken.value);
        }
        return res;
    }

    // expr = term ( (PLUS | MINUS) term )*
    private MultivariatePolynomial<Long> expr() {
        MultivariatePolynomial<Long> res = term();
        while (currentToken.type == TokenType.PLUS || currentToken.type == TokenType.MINUS) {
            TokenType type = currentToken.type;
            consume();
            MultivariatePolynomial<Long> t = term();
            if (type == TokenType.PLUS) res = res.add(t);
            else res = res.sub(t);
        }
        return res;
    }

    // term = factor ( (STAR | epsilon) factor )*
    private MultivariatePolynomial<Long> term() {
        MultivariatePolynomial<Long> res = factor();
        while (currentToken.type == TokenType.STAR || currentToken.type == TokenType.VARIABLE || currentToken.type == TokenType.LPAREN || currentToken.type == TokenType.NUMBER) {
            if (currentToken.type == TokenType.STAR) {
                consume();
            }
            MultivariatePolynomial<Long> f = factor();
            res = res.mul(f);
        }
        return res;
    }

    // factor = power
    private MultivariatePolynomial<Long> factor() {
        return power();
    }

    // power = primary ( CARET NUMBER )?
    private MultivariatePolynomial<Long> power() {
        MultivariatePolynomial<Long> res = primary();
        if (currentToken.type == TokenType.CARET) {
            consume();
            if (currentToken.type != TokenType.NUMBER) {
                throw new RuntimeException("Expected number after ^");
            }
            int exponent = Integer.parseInt(currentToken.value);
            consume();
            MultivariatePolynomial<Long> base = res;
            res = new MultivariatePolynomialOverFp(mod).one();
            for (int i = 0; i < exponent; i++) {
                res = res.mul(base);
            }
        }
        return res;
    }

    // primary = NUMBER | VARIABLE | LPAREN expr RPAREN | MINUS primary
    private MultivariatePolynomial<Long> primary() {
        if (currentToken.type == TokenType.NUMBER) {
            long val = Long.parseLong(currentToken.value);
            consume();
            if (val == 0) return new MultivariatePolynomialOverFp(mod);
            return new MultivariatePolynomialOverFp(mod).one().multiply(val);
        } else if (currentToken.type == TokenType.VARIABLE) {
            String name = currentToken.value;
            if (!varMap.containsKey(name)) {
                throw new RuntimeException("Unknown variable: " + name);
            }
            int idx = varMap.get(name);
            consume();
            int[] exps = new int[totalVars];
            exps[idx] = 1;
            return new MultivariatePolynomialOverFp(mod).add(MultivariatePolynomialOverFp.singleTerm(mod, new Monomial(exps), 1));
        } else if (currentToken.type == TokenType.LPAREN) {
            consume();
            MultivariatePolynomial<Long> res = expr();
            if (currentToken.type != TokenType.RPAREN) {
                throw new RuntimeException("Expected )");
            }
            consume();
            return res;
        } else if (currentToken.type == TokenType.MINUS) {
            consume();
            return primary().multiply(-1L);
        }
        throw new RuntimeException("Unexpected token: " + currentToken.value);
    }
}
