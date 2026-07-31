package library.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.TreeMap;
import java.util.Map;

public class IntervalMapAggregationTest {

	@Test
	public void testSumLength() {
		// Sum of (r - l) * x
		var sumAggregator = new RangeAbelianGroupAggregator<Long, Long>() {
			long sum = 0;
			@Override
			public void add(Long l, Long r, Long x) {
				sum += (r - l) * x;
			}
			@Override
			public void remove(Long l, Long r, Long x) {
				sum -= (r - l) * x;
			}

			public Long getSum() {
				return sum;
			}
		};

		IntervalMapWithAdjacentMergingGroupAggregation<Long, Long> map =
				new IntervalMapWithAdjacentMergingGroupAggregation<>(0L, 10L, 0L, sumAggregator);

		assertEquals(0L, sumAggregator.getSum());

		map.put(2L, 5L, 3L); // [0, 2):0, [2, 5):3, [5, 10):0 -> sum = 3*3 = 9
		assertEquals(9L, sumAggregator.getSum());

		map.put(4L, 7L, 2L); // [0, 2):0, [2, 4):3, [4, 7):2, [7, 10):0 -> sum = 3*2 + 2*3 + 0 = 6+6=12
		assertEquals(12L, sumAggregator.getSum());

		map.put(0L, 10L, 5L); // [0, 10):5 -> sum = 50
		assertEquals(50L, sumAggregator.getSum());
	}

	@Test
	public void testMaxAggregation() {
		// Maintain max of values assigned to intervals
		var maxAggregator = new RangeAbelianGroupAggregator<Long, Integer>() {
			TreeMap<Integer, Integer> counts = new TreeMap<>();
			@Override
			public void add(Long l, Long r, Integer x) {
				counts.put(x, counts.getOrDefault(x, 0) + 1);
			}
			@Override
			public void remove(Long l, Long r, Integer x) {
				int c = counts.get(x);
				if (c == 1) counts.remove(x);
				else counts.put(x, c - 1);
			}

			public Integer getSum() {
				return counts.isEmpty() ? null : counts.lastKey();
			}
		};

		IntervalMapWithAdjacentMergingGroupAggregation<Long, Integer> map =
				new IntervalMapWithAdjacentMergingGroupAggregation<>(0L, 100L, 0, maxAggregator);

		assertEquals(0, maxAggregator.getSum());

		map.put(10L, 20L, 50); // [0,10):0, [10,20):50, [20,100):0 -> max = 50
		assertEquals(50, maxAggregator.getSum());

		map.put(15L, 25L, 30); // [0,10):0, [10,15):50, [15,25):30, [25,100):0 -> max = 50
		assertEquals(50, maxAggregator.getSum());

		map.put(5L, 30L, 10); // [0,5):0, [5,30):10, [30,100):0 -> max = 10
		assertEquals(10, maxAggregator.getSum());
	}

	@Test
	public void testPairCountAggregation() {
		// Count pairs (i, j) of intervals such that v_i = v_j (i < j)
		var pairAggregator = new RangeAbelianGroupAggregator<Long, Integer>() {
			Map<Integer, Long> counts = new TreeMap<>();
			long pairs = 0;
			@Override
			public void add(Long l, Long r, Integer x) {
				long c = counts.getOrDefault(x, 0L);
				pairs += c; // Adding (c+1)-th element adds c pairs
				counts.put(x, c + 1);
			}
			@Override
			public void remove(Long l, Long r, Integer x) {
				long c = counts.get(x);
				pairs -= (c - 1); // Removing c-th element removes c-1 pairs
				if (c == 1) counts.remove(x);
				else counts.put(x, c - 1);
			}

			public Long getSum() {
				return pairs;
			}
		};

		// Domain [0, 10), initial value 0.
		// Initial state: one interval [0, 10) with value 0.
		// Pairs: 0.
		IntervalMapWithAdjacentMergingGroupAggregation<Long, Integer> map =
				new IntervalMapWithAdjacentMergingGroupAggregation<>(0L, 10L, 0, pairAggregator);

		assertEquals(0L, pairAggregator.getSum());

		// Put 1 at [2, 4)
		// Intervals: [0, 2):0, [2, 4):1, [4, 10):0
		// Values: 0, 1, 0
		// Two intervals have value 0 -> 1 pair.
		map.put(2L, 4L, 1);
		assertEquals(1L, pairAggregator.getSum());

		// Put 1 at [6, 8)
		// Intervals: [0, 2):0, [2, 4):1, [4, 6):0, [6, 8):1, [8, 10):0
		// Values: 0, 1, 0, 1, 0
		// Value 0: 3 intervals -> 3 pairs
		// Value 1: 2 intervals -> 1 pair
		// Total pairs: 4
		map.put(6L, 8L, 1);
		assertEquals(4L, pairAggregator.getSum());

		// Put 1 at [4, 6) -> merge [2, 4), [4, 6), [6, 8)
		// Intervals: [0, 2):0, [2, 8):1, [8, 10):0
		// Values: 0, 1, 0
		// Value 0: 2 intervals -> 1 pair
		// Value 1: 1 interval -> 0 pairs
		// Total pairs: 1
		map.put(4L, 6L, 1);
		assertEquals(1L, pairAggregator.getSum());
	}
}
