package library.util.mo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import library.util.MathUtils;

public class Mo {
	int queryNumber=0;
	ArrayList<Query>queries=new ArrayList<>();
	
	public Mo() {
		
	}
	
	record Query(int l, int r, int id) {};
	
	/**
	 * クエリ [l, r) を追加
	 * @param l
	 * @param r
	 */
	public void addQuery(int l, int r) {
		queries.add(new Query(l, r, queryNumber++));
	}
	
	/**
	 * クエリの結果を、クエリ順に並べたリストを返す
	 * @param <T>
	 * @param node
	 * @return
	 */
	public <T> List<T> run(MoCursor<T> node) {
		int Q=queries.size();
	   if (Q == 0) {
	        return Collections.emptyList();
	    }
		int N=0;
		for (int i = 0; i < queries.size(); i++) {
			N=Math.max(N, queries.get(i).r());
		}
		int B = Math.max(1, (int) (N / MathUtils.sqrt(2 * Q)));
		Collections.sort(queries, (p, q)->{
			int pl=p.l/B;
			int ql=q.l/B;
			if (pl != ql) {
				return Integer.compare(pl, ql);
			} else {
				return Integer.compare(p.r(), q.r()) * (pl%2==0?1:-1);
			}
		});
		int l=0;
		int r=0;
		Object[] ans=new Object[Q];
		for (var query : queries) {
			while(query.l()<l) {
				node.internal_addLeft();
				--l;
			}
			while(query.r()>r) {
				node.internal_addRight();
				++r;
			}
			while(query.l()>l) {
				node.internal_popLeft();
				++l;
			}
			while(query.r()<r) {
				node.internal_popRight();
				--r;
			}
			ans[query.id()] = node.getValue();
		}
	    @SuppressWarnings("unchecked")
	    List<T> result = (List<T>)(List<?>) Arrays.asList(ans);
	    return result;
	}
	

}
