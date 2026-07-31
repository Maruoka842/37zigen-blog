package library.util;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import library.util.game.PartisanGame;
import library.util.DyadicRational;

import java.util.Arrays;
import java.util.List;

public class PartisanGameTest {

	@Test
	public void testOutcomeClasses() {
		// A simple game where Left has 1 move and Right has none.
		PartisanGame<Integer> g = new PartisanGame<>() {
			@Override
			public Iterable<Integer> nextStatesLeft(Integer s) {
				return s > 0 ? List.of(s - 1) : List.of();
			}
			@Override
			public Iterable<Integer> nextStatesRight(Integer s) {
				return List.of();
			}
		};

		// 0: PREV (G=0)
		assertEquals(PartisanGame.Outcome.PREV, g.getOutcome(0));
		assertFalse(g.canWin(0, true));
		assertFalse(g.canWin(0, false));

		// 1: LEFT (G=1)
		assertEquals(PartisanGame.Outcome.LEFT, g.getOutcome(1));
		assertTrue(g.canWin(1, true));
		assertFalse(g.canWin(1, false));
	}

	@Test
	public void testNextOutcome() {
		// Left can move to 0 (PREV), Right can move to 0 (PREV).
		// This is * (star) in CGT, but we represent it as NEXT.
		PartisanGame<Integer> star = new PartisanGame<>() {
			@Override
			public Iterable<Integer> nextStatesLeft(Integer s) {
				return s == 1 ? List.of(0) : List.of();
			}
			@Override
			public Iterable<Integer> nextStatesRight(Integer s) {
				return s == 1 ? List.of(0) : List.of();
			}
		};

		assertEquals(PartisanGame.Outcome.NEXT, star.getOutcome(1));
		assertTrue(star.canWin(1, true));
		assertTrue(star.canWin(1, false));
	}

	@Test
	public void testRemoteness() {
		// Left: s -> s-1, Right: none
		PartisanGame<Integer> g = new PartisanGame<>() {
			@Override
			public Iterable<Integer> nextStatesLeft(Integer s) {
				return s > 0 ? List.of(s - 1) : List.of();
			}
			@Override
			public Iterable<Integer> nextStatesRight(Integer s) {
				return List.of();
			}
		};

		// n=0: L turn -> 0, R turn -> 0
		assertEquals(0, g.remoteness(0, true));
		assertEquals(0, g.remoteness(0, false));

		// n=1: L turn -> move to 0 (R turn). rem(0, false) = 0. rem(1, true) = 1.
		//      R turn -> no moves. rem(1, false) = 0.
		assertEquals(1, g.remoteness(1, true));
		assertEquals(0, g.remoteness(1, false));

		// n=2: L turn -> move to 1 (R turn). rem(1, false) = 0. rem(2, true) = 1.
		//      R turn -> no moves. rem(2, false) = 0.
		assertEquals(1, g.remoteness(2, true));
		assertEquals(0, g.remoteness(2, false));
	}

	@Test
	public void testRemotenessPath() {
		// Left: s -> s-1, Right: none
		PartisanGame<Integer> g = new PartisanGame<>() {
			@Override
			public Iterable<Integer> nextStatesLeft(Integer s) {
				return s > 0 ? List.of(s - 1) : List.of();
			}
			@Override
			public Iterable<Integer> nextStatesRight(Integer s) {
				return List.of();
			}
		};

		List<Integer> path = g.getRemotenessPath(2, true);
		// From 2, Left must move to 1, then Right has no moves, path ends.
		assertEquals(List.of(2, 1), path);
	}

	@Test
	public void testGameValue() {
		// A game representing various dyadic numbers and edge cases.
		PartisanGame<Integer> g = new PartisanGame<>() {
			@Override
			public Iterable<Integer> nextStatesLeft(Integer s) {
				if (s == 0) return List.of(); // { | } = 0
				if (s == 1) return List.of(0); // { 0 | } = 1
				if (s == 2) return List.of(1); // { 1 | } = 2
				if (s == 3) return List.of(); // { | 0 } = -1
				if (s == 4) return List.of(0); // { 0 | 1 } = 1/2
				if (s == 5) return List.of(0); // { 0 | 1/2 } = 1/4
				if (s == 6) return List.of(4); // { 1/2 | 1 } = 3/4
				if (s == 7) return List.of(0); // { 0 | 0 } = Star (non-number)
				if (s == 8) return List.of(1); // { 1 | 0 } = G_L >= G_R (non-number)
				if (s == 10) return List.of(11); // Cyclic
				if (s == 11) return List.of(10); // Cyclic
				return List.of();
			}

			@Override
			public Iterable<Integer> nextStatesRight(Integer s) {
				if (s == 3) return List.of(0);
				if (s == 4) return List.of(1);
				if (s == 5) return List.of(4);
				if (s == 6) return List.of(1);
				if (s == 7) return List.of(0);
				if (s == 8) return List.of(0);
				return List.of();
			}
		};

		// 0: { | } -> 0.0
		assertEquals(new DyadicRational(0), g.gameValue(0));

		// 1: { 0 | } -> 1.0
		assertEquals(new DyadicRational(1), g.gameValue(1));

		// 2: { 1 | } -> 2.0
		assertEquals(new DyadicRational(2), g.gameValue(2));

		// 3: { | 0 } -> -1.0
		assertEquals(new DyadicRational(-1), g.gameValue(3));

		// 4: { 0 | 1 } -> 0.5
		assertEquals(new DyadicRational(0, 1, 2), g.gameValue(4));

		// 5: { 0 | 1/2 } -> 0.25
		assertEquals(new DyadicRational(0, 1, 4), g.gameValue(5));

		// 6: { 1/2 | 1 } -> 0.75
		assertEquals(new DyadicRational(0, 3, 4), g.gameValue(6));

		// 7: { 0 | 0 } -> null (Star is not a number)
		assertNull(g.gameValue(7));

		// 8: { 1 | 0 } -> null (G_L >= G_R is not a number)
		assertNull(g.gameValue(8));

		// 10, 11: Cyclic -> null
		assertNull(g.gameValue(10));
		assertNull(g.gameValue(11));
	}

	@Test
	public void testGameValueTerminalWinners() {
		PartisanGame<Integer> leftWins = new PartisanGame<>() {
			@Override
			public Iterable<Integer> nextStatesLeft(Integer s) { return List.of(); }
			@Override
			public Iterable<Integer> nextStatesRight(Integer s) { return List.of(); }
			@Override
			protected Winner terminalWinner(Integer s) { return Winner.LEFT; }
		};

		PartisanGame<Integer> rightWins = new PartisanGame<>() {
			@Override
			public Iterable<Integer> nextStatesLeft(Integer s) { return List.of(); }
			@Override
			public Iterable<Integer> nextStatesRight(Integer s) { return List.of(); }
			@Override
			protected Winner terminalWinner(Integer s) { return Winner.RIGHT; }
		};

		assertEquals(new DyadicRational(1), leftWins.gameValue(0));
		assertEquals(new DyadicRational(-1), rightWins.gameValue(0));
	}
}
