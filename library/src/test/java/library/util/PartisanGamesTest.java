package library.util;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import library.util.game.PartisanGame;
import library.util.game.PartisanGames;
import library.util.DyadicRational;

import java.util.Arrays;
import java.util.List;

public class PartisanGamesTest {

	@Test
	public void testPartisanSubtractionGame() {
		// Left: {1}, Right: {2}
		PartisanGames.PartisanSubtractionGame game = new PartisanGames.PartisanSubtractionGame(List.of(1L), List.of(2L));

		assertEquals(PartisanGame.Outcome.PREV, game.getOutcome(0L));
		assertEquals(PartisanGame.Outcome.LEFT, game.getOutcome(1L));

		// n=2: NEXT
		assertEquals(PartisanGame.Outcome.NEXT, game.getOutcome(2L));

		// n=3: PREV
		assertEquals(PartisanGame.Outcome.PREV, game.getOutcome(3L));
	}

	@Test
	public void testDomineering() {
		// 2x2 Domineering
		PartisanGames.Domineering game = new PartisanGames.Domineering(2, 2);
		assertEquals(PartisanGame.Outcome.NEXT, game.getOutcome(0L));
	}

	@Test
	public void testSum() {
		PartisanGame<Integer> plus1 = new PartisanGame<>() {
			@Override public Iterable<Integer> nextStatesLeft(Integer s) { return s > 0 ? List.of(s - 1) : List.of(); }
			@Override public Iterable<Integer> nextStatesRight(Integer s) { return List.of(); }
		};
		PartisanGame<Integer> minus1 = new PartisanGame<>() {
			@Override public Iterable<Integer> nextStatesLeft(Integer s) { return List.of(); }
			@Override public Iterable<Integer> nextStatesRight(Integer s) { return s > 0 ? List.of(s - 1) : List.of(); }
		};

		PartisanGames.Sum<Integer> sum = new PartisanGames.Sum<>(List.of(plus1, minus1));

		// (1, 1) -> PREV
		assertEquals(PartisanGame.Outcome.PREV, sum.getOutcome(Arrays.asList(1, 1)));

		// (2, 1) -> LEFT
		assertEquals(PartisanGame.Outcome.LEFT, sum.getOutcome(Arrays.asList(2, 1)));
	}

	@Test
	public void testAliceBobGridGame() {
		int n = 2;
		boolean[][] isWhite = {
				{false, true},
				{true, false}
		};
		PartisanGames.AliceBobGridGame game = new PartisanGames.AliceBobGridGame(isWhite);

		// k = 0, at (0, 0) [Black]
		// Alice starts: black -> Bob wins -> Alice loses
		assertFalse(game.canWin(new PartisanGames.AliceBobGridGame.State(0, 0, 0), true));
		// Bob starts: black -> Bob wins -> Bob wins
		assertTrue(game.canWin(new PartisanGames.AliceBobGridGame.State(0, 0, 0), false));

		// k = 0, at (0, 1) [White]
		// Alice starts: white -> Alice wins -> Alice wins
		assertTrue(game.canWin(new PartisanGames.AliceBobGridGame.State(0, 1, 0), true));
		// Bob starts: white -> Alice wins -> Bob loses
		assertFalse(game.canWin(new PartisanGames.AliceBobGridGame.State(0, 1, 0), false));

		// k = 1, start (0, 0)
		// Alice starts: must move to (0, 1) [White]. After move, k=0, Bob's turn, Bob at (0, 1) [White].
		// Bob at (0, 1) with k=0 is Alice's win. So Alice wins.
		assertTrue(game.canWin(new PartisanGames.AliceBobGridGame.State(0, 0, 1), true));

		// Bob starts: can move to (0, 1) [White] or (1, 0) [White].
		// After move, k=0, Alice's turn. Alice at (0, 1) or (1, 0) with k=0 is Alice's win.
		// So Bob loses.
		assertFalse(game.canWin(new PartisanGames.AliceBobGridGame.State(0, 0, 1), false));

		// k = 2, start (0, 0)
		// Alice starts -> Alice moves to (0, 1) [k=1, Bob starts].
		// Bob starts at (0, 1) with k=1.
		// Bob can move to (0, 0) [k=0, Alice starts, (0, 0) is Black] -> Bob wins.
		// Bob can move to (1, 1) [k=0, Alice starts, (1, 1) is Black] -> Bob wins.
		// So Alice loses.
		assertFalse(game.canWin(new PartisanGames.AliceBobGridGame.State(0, 0, 2), true));
	}

	@Test
	public void testSumGameValue() {
		PartisanGame<Integer> g1 = new PartisanGame<>() {
			@Override
			public Iterable<Integer> nextStatesLeft(Integer s) {
				return s == 1 ? List.of(0) : List.of();
			}
			@Override
			public Iterable<Integer> nextStatesRight(Integer s) {
				return List.of();
			}
		};

		PartisanGame<Integer> g2 = new PartisanGame<>() {
			@Override
			public Iterable<Integer> nextStatesLeft(Integer s) {
				if (s == 2) return List.of(0);
				if (s == 1) return List.of(0);
				return List.of();
			}
			@Override
			public Iterable<Integer> nextStatesRight(Integer s) {
				if (s == 1) return List.of(2);
				return List.of();
			}
		};

		PartisanGames.Sum<Integer> sumGame = new PartisanGames.Sum<>(List.of(g1, g2));

		DyadicRational val1 = g1.gameValue(1); // 1.0
		DyadicRational val2 = g2.gameValue(1); // 0.5

		assertEquals(new DyadicRational(1), val1);
		assertEquals(new DyadicRational(0, 1, 2), val2);

		DyadicRational sumVal = sumGame.gameValue(List.of(1, 1)); // 1.5
		assertEquals(new DyadicRational(1, 1, 2), sumVal);
	}

	@Test
	public void testSumWinningMove() {
		PartisanGame<Integer> g1 = new PartisanGame<>() {
			@Override
			public Iterable<Integer> nextStatesLeft(Integer s) {
				return s > 0 ? List.of(s - 1) : List.of();
			}
			@Override
			public Iterable<Integer> nextStatesRight(Integer s) {
				return List.of();
			}
		};

		PartisanGame<Integer> g2 = new PartisanGame<>() {
			@Override
			public Iterable<Integer> nextStatesLeft(Integer s) {
				return List.of();
			}
			@Override
			public Iterable<Integer> nextStatesRight(Integer s) {
				return s > 0 ? List.of(s - 1) : List.of();
			}
		};

		PartisanGames.Sum<Integer> sumGame = new PartisanGames.Sum<>(List.of(g1, g2));

		// At (1, 1), the game is a draw/P-position under normal play because Left moves -> (0, 1) and then Right moves -> (0, 0) and Left loses.
		// So canWin(List.of(1, 1), true) is false. winningMove should return null.
		assertNull(sumGame.winningMove(List.of(1, 1), true));

		// At (2, 0), Left has a winning move to (1, 0).
		Object[] move = sumGame.winningMove(List.of(2, 0), true);
		assertNotNull(move);
		assertEquals(0, move[0]);
		assertEquals(1, move[1]);
	}
}
