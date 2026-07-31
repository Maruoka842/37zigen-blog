package library.util.polynomial;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import library.util.algebra.instance.FractionFieldElement;
import library.util.algebra.strategy.FractionFieldStrategy;

/**
 * 文字列形式の数式を FractionFieldElement<MultivariatePolynomial<Long>> に変換するパーサー。
 * 多変数多項式の分数（有理関数）を扱い、自動的に通分・約分を行う。
 */
public class MultivariateRationalFunctionParser {
    private final long mod;
    private final Map<String, Integer> varMap;
    private final int totalVars;
    private final FractionFieldStrategy<MultivariatePolynomial<Long>> field;
    private final MultivariatePolynomialOverFpStrategy polyStrategy;

    /**
     * MultivariateRationalFunctionParser のコンストラクタ。
     * @param mod 有限体の標数。
     * @param varMap 変数名からインデックスへのマップ。
     * @param totalVars 扱う全変数の数。
     */
    public MultivariateRationalFunctionParser(long mod, Map<String, Integer> varMap, int totalVars) {
        this.mod = mod;
        this.varMap = varMap;
        this.totalVars = totalVars;
        this.polyStrategy = new MultivariatePolynomialOverFpStrategy(mod);
        this.field = new FractionFieldStrategy<>(polyStrategy);
    }

    /**
     * MultivariateRationalFunctionParser のファクトリメソッド。
     * @param mod 有限体の標数。
     * @param vars 変数名のリスト。
     * @return 生成されたパーサー。
     */
    public static MultivariateRationalFunctionParser of(long mod, String... vars) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < vars.length; i++) {
            map.put(vars[i], i);
        }
        return new MultivariateRationalFunctionParser(mod, map, vars.length);
    }

    private enum TokenType {
        NUMBER, VARIABLE, PLUS, MINUS, STAR, SLASH, CARET, LPAREN, RPAREN, EOF
    }

    private static class Token {
        final TokenType type;
        final String value;
        Token(TokenType type, String value) { this.type = type; this.value = value; }
    }

    private static class Tokenizer {
        private final String input;
        private int pos = 0;
        private static final Pattern VAR_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9]*");
        private static final Pattern NUM_PATTERN = Pattern.compile("^[0-9]+");

        Tokenizer(String input) { this.input = input; }

        Token nextToken() {
            while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) pos++;
            if (pos >= input.length()) return new Token(TokenType.EOF, "");
            char c = input.charAt(pos);
            switch (c) {
                case '+': pos++; return new Token(TokenType.PLUS, "+");
                case '-': pos++; return new Token(TokenType.MINUS, "-");
                case '*': pos++; return new Token(TokenType.STAR, "*");
                case '/': pos++; return new Token(TokenType.SLASH, "/");
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
    private void consume() { currentToken = tokenizer.nextToken(); }

    /**
     * 文字列を解析して有理関数を生成する。
     * @param input 解析対象の文字列。
     * @return 生成された有理関数。
     */
    public MultivariateRationalFunctionOverFp parse(String input) {
        tokenizer = new Tokenizer(input);
        consume();
        FractionFieldElement<MultivariatePolynomial<Long>> res = expr();
        if (currentToken.type != TokenType.EOF) throw new RuntimeException("Unexpected token at end: " + currentToken.value);
        return new MultivariateRationalFunctionOverFp(res.num, res.den, mod);
    }

    private FractionFieldElement<MultivariatePolynomial<Long>> expr() {
        FractionFieldElement<MultivariatePolynomial<Long>> res = term();
        while (currentToken.type == TokenType.PLUS || currentToken.type == TokenType.MINUS) {
            TokenType type = currentToken.type;
            consume();
            FractionFieldElement<MultivariatePolynomial<Long>> t = term();
            if (type == TokenType.PLUS) res = field.add(res, t);
            else res = field.add(res, field.neg(t));
        }
        return res;
    }

    private FractionFieldElement<MultivariatePolynomial<Long>> term() {
        FractionFieldElement<MultivariatePolynomial<Long>> res = factor();
        while (currentToken.type == TokenType.STAR || currentToken.type == TokenType.SLASH || currentToken.type == TokenType.VARIABLE || currentToken.type == TokenType.LPAREN || currentToken.type == TokenType.NUMBER) {
            TokenType type = currentToken.type;
            if (type == TokenType.STAR || type == TokenType.SLASH) consume();
            else type = TokenType.STAR;
            FractionFieldElement<MultivariatePolynomial<Long>> f = factor();
            if (type == TokenType.STAR) res = field.mul(res, f);
            else res = field.mul(res, field.inv(f));
        }
        return res;
    }

    private FractionFieldElement<MultivariatePolynomial<Long>> factor() { return power(); }

    private FractionFieldElement<MultivariatePolynomial<Long>> power() {
        FractionFieldElement<MultivariatePolynomial<Long>> res = primary();
        if (currentToken.type == TokenType.CARET) {
            consume();
            int exponent = integer();
            int absExponent = Math.abs(exponent);
            res = field.of(res.num().pow(absExponent), res.den().pow(absExponent));
            if (exponent < 0) {
                res = field.inv(res);
            }
        }
        return res;
    }

    private int integer() {
        boolean lparen = false;
        if (currentToken.type == TokenType.LPAREN) {
            consume();
            lparen = true;
        }
        int sign = 1;
        if (currentToken.type == TokenType.PLUS) {
            consume();
        } else if (currentToken.type == TokenType.MINUS) {
            consume();
            sign = -1;
        }
        if (currentToken.type != TokenType.NUMBER) throw new RuntimeException("Expected number");
        int val = Integer.parseInt(currentToken.value);
        consume();
        if (lparen) {
            if (currentToken.type != TokenType.RPAREN) throw new RuntimeException("Expected )");
            consume();
        }
        return sign * val;
    }

    private FractionFieldElement<MultivariatePolynomial<Long>> primary() {
        if (currentToken.type == TokenType.NUMBER) {
            long val = Long.parseLong(currentToken.value);
            consume();
            return field.from(new MultivariatePolynomialOverFp(mod).one().multiply(val % mod));
        } else if (currentToken.type == TokenType.VARIABLE) {
            String name = currentToken.value;
            if (!varMap.containsKey(name)) throw new RuntimeException("Unknown variable: " + name);
            int idx = varMap.get(name);
            consume();
            int[] exps = new int[totalVars];
            exps[idx] = 1;
            return field.from(MultivariatePolynomialOverFp.singleTerm(mod, new Monomial(exps), 1L));
        } else if (currentToken.type == TokenType.LPAREN) {
            consume();
            FractionFieldElement<MultivariatePolynomial<Long>> res = expr();
            if (currentToken.type != TokenType.RPAREN) throw new RuntimeException("Expected )");
            consume();
            return res;
        } else if (currentToken.type == TokenType.MINUS) {
            consume();
            return field.neg(primary());
        }
        throw new RuntimeException("Unexpected token: " + currentToken.value);
    }
}
