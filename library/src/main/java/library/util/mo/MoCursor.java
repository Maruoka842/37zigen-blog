package library.util.mo;

public abstract class MoCursor<T>{
	int l=0;
	int r=0;
	
	/**
	 * (i, r) を [i, r) に
	 * @param i
	 */
	public abstract void addLeft(int i);

	/**
	 * [i, r) を (i, r) に
	 * @param i
	 */
	public abstract void popLeft(int i);

	/**
	 * [l, r) を [l, i) に
	 * @param i
	 */
	public abstract void addRight(int i);
	
	/**
	 * [l, r] を [l, r) に
	 * @param i
	 */
	public abstract void popRight(int i);

	public final void internal_addLeft() {
		addLeft(--l);
	}
	public final void internal_popLeft() {
		popLeft(l++);
	}
	public final void internal_addRight() {
		addRight(r++);
	}
	public final void internal_popRight() {
		popRight(--r);
	}

	public abstract T getValue();
}
