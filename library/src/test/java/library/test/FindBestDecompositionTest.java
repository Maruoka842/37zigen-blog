package library.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import org.junit.jupiter.api.Test;

import library.util.algebra.strategy.FractionFieldStrategy;
import library.util.algebra.strategy.ZStrategy;
import library.util.graph.Digraph;
import library.util.graph.TransferMatrixMethod;
import library.util.graph.ValueDigraph;
import library.util.polynomial.PolynomialFpDynamic;
import library.util.polynomial.PolynomialFpDynamic3D;
import library.util.polynomial.PolynomialFpDynamic4D;
import library.util.polynomial.PolynomialLong3D;
import library.util.polynomial.PolynomialLong4D;
import library.util.seq.AhoCorasick;

public class FindBestDecompositionTest {
	private static final PolynomialFpDynamic P1 = PolynomialFpDynamic.MOD998244353;
	private static final PolynomialFpDynamic4D P4 = new PolynomialFpDynamic4D(P1);

	@Test
	public void testABC458E() {
		Digraph g=new Digraph(3);
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				if(Math.abs(i-j)<=1)g.addEdge(i, j);
			}
		}
		var h=TransferMatrixMethod.multivariateFreeWalkGeneratingFunction(g.toLongValueDigraph());
		var p3=PolynomialFpDynamic3D.MOD998244353;
//		PolynomialLong3D.printPoly3DAsExpr("h", h.den());
		var x=p3.findBestDecomposition(h.den());
//		for (var f:x.pFactors()) {
//			PolynomialLong3D.printPoly3DAsExpr("p", f.factor);
//		}
//		for (var f:x.qFactors()) {
//			PolynomialLong3D.printPoly3DAsExpr("q", f.factor);
//		}
		assertEquals(3,x.score());
		//1 - z - y - x + x z + x y z
		//=(1-x)(1-z)-y(1-xz)
	}
	
	@Test
	public void testAGC051D() {
		
		ValueDigraph<long[][][][]>g=new ValueDigraph<>(4);
		g.addEdge(0, 1, PolynomialLong4D.x());
		g.addEdge(1, 2, PolynomialLong4D.y());
		g.addEdge(2, 3, PolynomialLong4D.z());
		g.addEdge(3, 0, PolynomialLong4D.w());
		
		g.addEdge(1, 0, PolynomialLong4D.x());
		g.addEdge(2, 1, PolynomialLong4D.y());
		g.addEdge(3, 2, PolynomialLong4D.z());
		g.addEdge(0, 3, PolynomialLong4D.w());

		var h=TransferMatrixMethod.fixedWalkGeneratingFunction(g, 0, 0, PolynomialLong4D.strategy(new ZStrategy()));
		var P4=new PolynomialFpDynamic4D(998244353);
		
		
//		PolynomialLong4D.printPoly4DAsExpr("inputNum", h.num());
//		PolynomialLong4D.printPoly4DAsExpr("inputDen", h.den());
		var x=P4.findBestDecomposition(h.den());
		/*
		for (var q:x.phFactors()) {
			PolynomialLong4D.printPolyAsExpr("ph", q.factor);
			System.out.println(q.multiplicity);
		}
		System.out.println("qconst"+x.qConst());
		for (var q:x.qFactors()) {
			PolynomialLong4D.printPolyAsExpr("q", q.factor);
			System.out.println(q.multiplicity);
		}
		*/
		assertEquals(5, x.score());
		//1 - x^2 - y^2 - z^2 - w^2 + x^2 z^2 + y^2 w^2 - 2xyzw
		//=(1 - x^2 - w^2)(1 - y^2 - z^2) - (xy + zw)^2
		//=(1-y^2)(1-w^2)-(z^2 +2x y z w + x^2 + x^2 z^2)
	}

	
	
	void agc058() {
		AhoCorasick aho=new AhoCorasick(20);
		aho.add("abc");
		aho.add("bca");
		aho.add("cab");
		aho.build();
		aho.draw();
		ValueDigraph<long[][][]> g=new ValueDigraph<>(10);
		for (int i = 0; i < aho.size(); i++) {
			for (int j = 0; j < 3; j++) {
				int next=aho.transition(i, j);
				if(aho.getCount(next)!=0)continue;
				if (j==0) g.addEdge(i, next, PolynomialLong3D.x());
				if (j==1) g.addEdge(i, next, PolynomialLong3D.y());
				if (j==2) g.addEdge(i, next, PolynomialLong3D.z());
			}
		}
//		var polyst=PolynomialLong3D.strategy(new LongZStrategy());
		var polyst=PolynomialFpDynamic3D.MOD998244353;
		FractionFieldStrategy<long[][][]> st=new FractionFieldStrategy<>(polyst);
		var f=TransferMatrixMethod.fixedStartWalkGeneratingFunction(g, 0, polyst);
		PolynomialLong3D.printPolyAsExpr("num", f.num());
		PolynomialLong3D.printPolyAsExpr("den", f.den());
		var gcd=polyst.gcd(f.num(), f.den());
		PolynomialLong3D.printPolyAsExpr("gcd", gcd);
		var x=f.den();
		PolynomialLong3D.printPolyAsExpr("den", f.den());
		// 1-z-y-x+2xyz
		//=(1-z)(1-y)-zy+2xyz
		//=(1-z)(1-y)-yz(1-2x)
	}

}
