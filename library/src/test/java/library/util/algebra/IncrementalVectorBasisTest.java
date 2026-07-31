package library.util.algebra;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import library.util.algebra.instance.VectorSpaceElement;
import library.util.algebra.strategy.FpStrategy;
import library.util.algebra.strategy.VectorSpaceStrategy;
import library.util.linalg.IncrementalVectorBasis;

class IncrementalVectorBasisTest {
	@Test
	void addMaintainsRrefAndRejectsDependentVectors() {
		FpStrategy field = new FpStrategy(7);
		VectorSpaceStrategy<Long, String> strategy = new VectorSpaceStrategy<>(field);
		IncrementalVectorBasis<Long, String> basis = new IncrementalVectorBasis<>(strategy, Comparator.naturalOrder());

		VectorSpaceElement<Long, String> xy = vector(strategy, Map.of("x", 1L, "y", 1L));
		VectorSpaceElement<Long, String> yz = vector(strategy, Map.of("y", 1L, "z", 1L));
		VectorSpaceElement<Long, String> xz = vector(strategy, Map.of("x", 1L, "z", 6L));

		assertTrue(basis.add(xy));
		assertTrue(basis.add(yz));
		assertFalse(basis.add(xz));
		assertEquals(2, basis.dimension());
		assertEquals(List.of("x", "y"), basis.pivots());
		assertEquals(Map.of("x", 1L, "z", 6L), basis.basis().get(0).val());
		assertEquals(Map.of("y", 1L, "z", 1L), basis.basis().get(1).val());
		assertTrue(basis.contains(xz));
		assertEquals(Map.of(), basis.reduce(xz).val());
	}

	@Test
	void reduceReturnsRemainderAndBasisReturnsCopies() {
		FpStrategy field = new FpStrategy(7);
		VectorSpaceStrategy<Long, Integer> strategy = new VectorSpaceStrategy<>(field);
		IncrementalVectorBasis<Long, Integer> basis = new IncrementalVectorBasis<>(strategy);

		assertTrue(basis.add(vector(strategy, Map.of(2, 2L, 4, 6L))));
		assertEquals(Map.of(2, 1L, 4, 3L), basis.basis().get(0).val());
		assertEquals(Map.of(1, 5L, 4, 6L), basis.reduce(vector(strategy, Map.of(1, 5L, 2, 3L, 4, 1L))).val());
		assertFalse(basis.contains(vector(strategy, Map.of(1, 1L))));

		VectorSpaceElement<Long, Integer> exposed = basis.basis().get(0);
		exposed.val().clear();
		assertNotSame(exposed, basis.basis().get(0));
		assertEquals(Map.of(2, 1L, 4, 3L), basis.basis().get(0).val());
	}

	private static <B> VectorSpaceElement<Long, B> vector(VectorSpaceStrategy<Long, B> strategy, Map<B, Long> value) {
		return new VectorSpaceElement<>(value, strategy);
	}
}
