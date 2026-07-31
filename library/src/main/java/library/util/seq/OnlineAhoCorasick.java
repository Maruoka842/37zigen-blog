package library.util.seq;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;

import library.util.collections.IntArrayList;

/**
 * オンラインでキーワードを追加可能な Aho-Corasick オートマトン。
 * バイナリマージング手法により、キーワード追加の償却計算量を O(log(N) * |keyword|) に抑える。
 *
 * <p>計算量:
 * <ul>
 *   <li>追加: O(log(N) * |keyword|) 償却</li>
 *   <li>検索: O(log(N) * |text| + Σ|keyword|)</li>
 * </ul>
 * </p>
 */
public class OnlineAhoCorasick {
    /** 追加されたキーワードの総数 */
    private int nKeywords = 0;
    /** アルファベットの種類数 */
    private final int alphabetSize;
    /** 文字からアルファベットインデックスへのマッピング関数 */
    private final ToIntFunction<Character> charToIndex;
    /** 追加された各キーワードの整数配列表現のリスト */
    private final List<int[]> keywordArrays = new ArrayList<>();
    /** 各バケット（automata[i]）に含まれるキーワードの元のIDリスト */
    private final IntArrayList[] kwdIds = new IntArrayList[30];
    /** バイナリマージング用の Aho-Corasick オートマトンの配列 */
    private final AhoCorasick[] automata = new AhoCorasick[30];

    /**
     * デフォルト設定（アルファベットサイズ26, 'a'-'z'）で初期化する。
     */
    public OnlineAhoCorasick() {
        this(26, c -> c - 'a');
    }

    /**
     * 指定されたアルファベットサイズと文字マッピングで初期化する。
     * @param alphabetSize アルファベットサイズ
     * @param charToIndex 文字からインデックスへのマッピング
     */
    public OnlineAhoCorasick(int alphabetSize, ToIntFunction<Character> charToIndex) {
        this.alphabetSize = alphabetSize;
        this.charToIndex = charToIndex;
    }

    /**
     * キーワードを追加する。
     * @param keyword 追加する文字列
     */
    public void add(String keyword) {
        int[] a = new int[keyword.length()];
        for (int i = 0; i < keyword.length(); i++) {
            a[i] = charToIndex.applyAsInt(keyword.charAt(i));
        }
        add(a);
    }

    /**
     * 整数配列として表現されたキーワードを追加する。
     * @param keyword 追加する配列
     */
    public void add(int[] keyword) {
        int id = nKeywords++;
        keywordArrays.add(keyword);

        int bucket = 0;
        while (bucket < 30 && automata[bucket] != null) {
            bucket++;
        }

        IntArrayList ids = new IntArrayList();
        int totalNodes = 1; // 根
        for (int i = 0; i < bucket; i++) {
            ids.addAll(kwdIds[i]);
            kwdIds[i] = null;
            automata[i] = null;
        }
        ids.add(id);
        for (int i = 0; i < ids.size(); i++) {
            totalNodes += keywordArrays.get(ids.get(i)).length;
        }

        AhoCorasick ac = new AhoCorasick(totalNodes, alphabetSize);
        for (int i = 0; i < ids.size(); i++) {
            ac.add(keywordArrays.get(ids.get(i)));
        }
        ac.build();
        automata[bucket] = ac;
        kwdIds[bucket] = ids;
    }

    /**
     * テキスト中の各キーワードの出現回数を取得する。
     * @param text 検索対象の文字列
     * @return 追加された順序での各キーワードの出現回数
     */
    public long[] match(String text) {
        int[] t = new int[text.length()];
        for (int i = 0; i < text.length(); i++) {
            t[i] = charToIndex.applyAsInt(text.charAt(i));
        }
        return match(t);
    }

    /**
     * 整数配列として表現されたテキスト中の各キーワードの出現回数を取得する。
     * @param text 検索対象の配列
     * @return 追加された順序での各キーワードの出現回数
     */
    public long[] match(int[] text) {
        long[] res = new long[nKeywords];
        for (int i = 0; i < 30; i++) {
            if (automata[i] != null) {
                long[] sub = automata[i].matchEach(text);
                for (int j = 0; j < sub.length; j++) {
                    res[kwdIds[i].get(j)] = sub[j];
                }
            }
        }
        return res;
    }

    /**
     * テキストの接頭辞とマッチするパターンの最大長を返す。
     * @param text 検索対象の文字列
     * @return 最大マッチ長。マッチしない場合は -1。
     */
    public int maxPrefixMatchLength(String text) {
        int[] t = new int[text.length()];
        for (int i = 0; i < text.length(); i++) {
            t[i] = charToIndex.applyAsInt(text.charAt(i));
        }
        return maxPrefixMatchLength(t);
    }

    /**
     * テキストの接頭辞とマッチするパターンの最大長を返す。
     * @param text 検索対象の配列
     * @return 最大マッチ長。マッチしない場合は -1。
     */
    public int maxPrefixMatchLength(int[] text) {
        int res = -1;
        for (int i = 0; i < 30; i++) {
            if (automata[i] != null) {
                res = Math.max(res, automata[i].maxPrefixMatchLength(text));
            }
        }
        return res;
    }

    /**
     * 追加されたキーワードの総数を返す。
     * @return キーワード数
     */
    public int numKeywords() {
        return nKeywords;
    }

	/**
	 * 内部のオートマトンの状態を文字列として表す。
	 * <ul>
	 *   <li>計算量: $O(\sum \text{size}_i \times \Sigma)$</li>
	 * </ul>
	 * @return 内部のオートマトンの状態を表す文字列
	 */
	// 未テスト
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("OnlineAhoCorasick numKeywords=").append(nKeywords);
		for (int i = 0; i < 30; i++) {
			if (automata[i] != null) {
				sb.append("\nBucket ").append(i).append(":\n");
				sb.append(automata[i].toString());
			}
		}
		return sb.toString();
	}

	/**
	 * 内部のオートマトンの状態を標準出力に出力する。
	 * <ul>
	 *   <li>計算量: $O(\sum \text{size}_i \times \Sigma)$</li>
	 * </ul>
	 */
	// 未テスト
	public void dump() {
		System.out.println(toString());
	}
}
