package library.util;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.BitSet;

import library.util.game.ImpartialGame;
import library.util.game.ImpartialGames;
import library.util.graph.Graph;

public class ImpartialGameTest {

	@Test
	public void testNim() {
		ImpartialGames.Nim nim = new ImpartialGames.Nim();
		assertEquals(0, nim.grundy(0L));
		assertEquals(5, nim.grundy(5L));
		assertEquals(100, nim.grundy(100L));
		assertTrue(nim.isWin(5L));
		assertFalse(nim.isWin(0L));

		assertEquals(3, nim.grundy(3L));
	}

	@Test
	public void testWythoff() {
		ImpartialGames.WythoffGame game = new ImpartialGames.WythoffGame();

		assertFalse(game.isWin(new ImpartialGames.WythoffGame.State(1, 2)));
		assertFalse(game.isWin(new ImpartialGames.WythoffGame.State(3, 5)));
		assertTrue(game.isWin(new ImpartialGames.WythoffGame.State(1, 1)));

		assertEquals(0, game.grundy(new ImpartialGames.WythoffGame.State(1, 2)));
		assertNotEquals(0, game.grundy(new ImpartialGames.WythoffGame.State(1, 1)));
	}

	@Test
	public void testGreenHackenbush() {
		ImpartialGames.GreenHackenbush game = new ImpartialGames.GreenHackenbush();
		List<int[]> edges = Arrays.asList(new int[]{0, 1});
		Graph graph = new Graph(2);
		graph.addEdge(0, 1);
		int[] ground = {1};
		assertEquals(1, ImpartialGames.GreenHackenbush.grundy(2, edges, ground));
		assertEquals(1, ImpartialGames.GreenHackenbush.grundy(graph, ground));
		assertEquals(1, game.grundy(new ImpartialGames.GreenHackenbush.State(graph, ground)));

		edges = Arrays.asList(new int[]{0, 1}, new int[]{1, 2}, new int[]{2, 0});
		ground = new int[]{0};
		assertEquals(1, ImpartialGames.GreenHackenbush.grundy(3, edges, ground));

		edges = Arrays.asList(new int[]{0, 1}, new int[]{0, 2});
		ground = new int[]{1, 2};
		assertEquals(0, ImpartialGames.GreenHackenbush.grundy(3, edges, ground));
	}

	@Test
	public void testNodeGeography() {
		List<Integer>[] adj = new List[3];
		for (int i = 0; i < 3; i++) adj[i] = new ArrayList<>();
		adj[0].add(1);
		adj[1].add(2);

		ImpartialGames.NodeGeography game = new ImpartialGames.NodeGeography(adj);
		assertEquals(0, game.grundy(new ImpartialGames.NodeGeography.State(0, 1L << 0)));
	}

	@Test
	public void testEdgeGeography() {
		Graph g = new Graph(3);
		g.addEdge(0, 1);
		g.addEdge(1, 2);
		ImpartialGames.EdgeGeography game = new ImpartialGames.EdgeGeography();

		assertEquals(0, game.grundy(new ImpartialGames.EdgeGeography.State(0, g)));
	}

	@Test
	public void testNimWithPassTracksBothPlayersPassRights() {
		ImpartialGames.NimWithPass game = new ImpartialGames.NimWithPass();
		List<Long> empty = Arrays.asList(0L);

		assertEquals(0, game.grundy(new ImpartialGames.NimWithPass.State(empty, false, false)));
		assertEquals(1, game.grundy(new ImpartialGames.NimWithPass.State(empty, true, false)));
		assertEquals(0, game.grundy(new ImpartialGames.NimWithPass.State(empty, false, true)));
		assertEquals(0, game.grundy(new ImpartialGames.NimWithPass.State(empty, true, true)));
	}

	@Test
	public void testNimWithPassGrundyFormula() {
		ImpartialGames.NimWithPass game = new ImpartialGames.NimWithPass();
		List<Long> piles = Arrays.asList(1L, 2L, 4L);

		assertEquals(7, game.grundy(new ImpartialGames.NimWithPass.State(piles, false, false)));
		assertEquals(7, game.grundy(new ImpartialGames.NimWithPass.State(piles, true, true)));
		assertEquals(1, game.grundy(new ImpartialGames.NimWithPass.State(piles, true, false)));
		assertEquals(0, game.grundy(new ImpartialGames.NimWithPass.State(piles, false, true)));
		assertEquals(2, game.grundy(new ImpartialGames.NimWithPass.State(Arrays.asList(1L), true, false)));
	}

	@Test
	public void testNimWithPassNextStatesSwapPassRightsAfterEveryMove() {
		ImpartialGames.NimWithPass game = new ImpartialGames.NimWithPass();
		ImpartialGames.NimWithPass.State state = new ImpartialGames.NimWithPass.State(Arrays.asList(1L), true, false);

		Set<ImpartialGames.NimWithPass.State> nexts = new HashSet<>();
		for (ImpartialGames.NimWithPass.State next : game.nextStates(state)) {
			nexts.add(next);
		}

		assertEquals(2, nexts.size());
		assertTrue(nexts.contains(new ImpartialGames.NimWithPass.State(Arrays.asList(0L), false, true)));
		assertTrue(nexts.contains(new ImpartialGames.NimWithPass.State(Arrays.asList(1L), false, false)));
	}

	@Test
	public void testNimSquare() {
		ImpartialGames.NimSquare game = new ImpartialGames.NimSquare();
		assertEquals(0, game.grundy(0L));
		assertEquals(1, game.grundy(1L));
		assertEquals(0, game.grundy(2L));
		assertEquals(1, game.grundy(3L));
		assertEquals(2, game.grundy(4L));
	}

	@Test
	public void testWhiteKnight() {
		ImpartialGames.WhiteKnight game = new ImpartialGames.WhiteKnight();
		assertEquals(0, game.grundy(new ImpartialGames.WhiteKnight.Pos(0, 0)));
		assertEquals(2, game.grundy(new ImpartialGames.WhiteKnight.Pos(2, 1)));
	}

	@Test
	public void testFibonacciNim() {
		ImpartialGames.FibonacciNim game = new ImpartialGames.FibonacciNim();
		// n=3, maxTake=2. f(3)=3. 3 > 2. Lose.
		assertFalse(game.isWin(new ImpartialGames.FibonacciNim.State(3, 2)));
		// n=4, maxTake=1. f(4)=1. 1 <= 1. Win.
		assertTrue(game.isWin(new ImpartialGames.FibonacciNim.State(4, 1)));
		// n=5, maxTake=2. f(5)=5. 5 > 2. Lose.
		assertFalse(game.isWin(new ImpartialGames.FibonacciNim.State(5, 2)));
	}

	@Test
	public void testMooreNim() {
		ImpartialGames.MooreNim game = new ImpartialGames.MooreNim(2);
		// For k=2, any triple (x, x, x) is a P-position because bit sums will be 3 or 0.
		List<Long> pPos = Arrays.asList(1L, 1L, 1L, 2L, 2L, 2L, 3L, 3L, 3L);
		assertFalse(game.isWin(pPos));
		assertEquals(0, game.grundy(pPos));

		List<Long> nPos = Arrays.asList(1L, 2L, 4L);
		assertTrue(game.isWin(nPos));
		assertNotEquals(0, game.grundy(nPos));

		List<Long> winMove = game.winningMove(nPos);
		assertNotNull(winMove);
		assertFalse(game.isWin(winMove));

		// Verify winMove is valid
		int changed = 0;
		for (int i = 0; i < 3; i++) {
			if (!nPos.get(i).equals(winMove.get(i))) {
				changed++;
				assertTrue(winMove.get(i) < nPos.get(i));
			}
		}
		assertTrue(changed >= 1 && changed <= 2);
	}

	@Test
	public void testChomp() {
		ImpartialGames.Chomp game = new ImpartialGames.Chomp();
		ImpartialGames.Chomp.State state1x1 = ImpartialGames.Chomp.initialState(1, 1);
		assertEquals(0, game.grundy(state1x1));
		assertFalse(game.isWin(state1x1));

		ImpartialGames.Chomp.State state1x2 = ImpartialGames.Chomp.initialState(1, 2);
		assertEquals(1, game.grundy(state1x2));
		assertTrue(game.isWin(state1x2));

		ImpartialGames.Chomp.State state2x2 = ImpartialGames.Chomp.initialState(2, 2);
		// 2x2 Chomp is known to be a win for the first player.
		assertTrue(game.isWin(state2x2));
		assertNotEquals(0, game.grundy(state2x2));
	}

	@Test
	public void testNimSumInstance() {
		// test as instance
		ImpartialGames.NimSum game = new ImpartialGames.NimSum(3);
		List<Long> state = Arrays.asList(1L, 2L, 7L);
		assertEquals(4, game.grundy(state));
		Object[] res = game.winningMove(state);
		assertNotNull(res);
		assertEquals(2, res[0]);
		assertEquals(3L, res[1]);
	}

	@Test
	public void testNimSumRemoteness() {
		ImpartialGames.NimSum game = new ImpartialGames.NimSum(3);

		// P-position: (1, 2, 3) -> 1^2^3 = 0. rem = 1+2+3 = 6
		assertEquals(6, game.remoteness(Arrays.asList(1L, 2L, 3L)));
		// P-position: (0, 0, 0) -> rem = 0
		assertEquals(0, game.remoteness(Arrays.asList(0L, 0L, 0L)));
		// N-position: (1, 1, 1) -> 1^1^1 = 1.
		// Next P-positions: (0, 1, 1) -> rem 2. (1, 0, 1) -> rem 2. (1, 1, 0) -> rem 2.
		// rem = 1 + min(2, 2, 2) = 3
		assertEquals(3, game.remoteness(Arrays.asList(1L, 1L, 1L)));
		// N-position: (1, 2, 7) -> 1^2^7 = 4.
		// Next P-positions: (1, 2, 3) (since 7^4 = 3 < 7). rem = 1 + rem(1, 2, 3) = 1 + 6 = 7.
		assertEquals(7, game.remoteness(Arrays.asList(1L, 2L, 7L)));
	}

	@Test
	public void testNimSumRemotenessPath() {
		ImpartialGames.NimSum game = new ImpartialGames.NimSum(3);
		List<Long> state = Arrays.asList(1L, 2L, 7L);
		List<List<Long>> path = game.getRemotenessPath(state);

		// From 1, 2, 7, the remoteness is 7. So path length should be 8.
		assertEquals(8, path.size());
		// Each transition should decrease remoteness by 1
		for (int i = 0; i < path.size(); i++) {
			assertEquals(7 - i, game.remoteness(path.get(i)));
		}
		// Path starts with the initial state
		assertEquals(state, path.get(0));
		// Path ends with the terminal state (0, 0, 0)
		assertEquals(Arrays.asList(0L, 0L, 0L), path.get(path.size() - 1));
	}

	@Test
	public void testRemotenessesGraph() {
		// 0 -> 1 -> 2
		List<int[]> edges = Arrays.asList(new int[]{0, 1}, new int[]{1, 2});
		int[] rem = ImpartialGame.remotenesses(3, edges);
		assertArrayEquals(new int[]{2, 1, 0}, rem);

		// 0 -> 1, 1 -> 0 (Draw)
		edges = Arrays.asList(new int[]{0, 1}, new int[]{1, 0});
		rem = ImpartialGame.remotenesses(2, edges);
		assertArrayEquals(new int[]{-1, -1}, rem);

		// 0 -> 1, 0 -> 2, 1 -> 3, 2 -> 3 (3 is terminal)
		// 3: L(0), 1: W(1), 2: W(1), 0: L(2)
		edges = Arrays.asList(new int[]{0, 1}, new int[]{0, 2}, new int[]{1, 3}, new int[]{2, 3});
		rem = ImpartialGame.remotenesses(4, edges);
		assertArrayEquals(new int[]{2, 1, 1, 0}, rem);

		// Multiple paths with different lengths
		// 0 -> 1, 0 -> 2, 1 -> 2 (2 is terminal)
		// 2: L(0), 1: W(1), 0: W(1) (shortest path to loss is 0->2, rem=1)
		edges = Arrays.asList(new int[]{0, 1}, new int[]{0, 2}, new int[]{1, 2});
		rem = ImpartialGame.remotenesses(3, edges);
		assertArrayEquals(new int[]{1, 1, 0}, rem);
	}
}
