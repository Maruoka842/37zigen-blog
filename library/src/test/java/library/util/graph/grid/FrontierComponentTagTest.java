package library.util.graph.grid;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

public class FrontierComponentTagTest {

    @Test
    public void testFrontier4TagInitializationAndMerge() {
        // Frontier4 with 4 elements
        Frontier4 state = Frontier4.getInitialState(4);

        // At c=0, start a vertex with tag = 1 (binary: 0001)
        Frontier4.Builder builder0 = state.startVertex(1);
        Frontier4 state1 = builder0.build();

        // The newly created component should be active on the frontier with tag = 1
        assertEquals(1, state1.parent[0]);
        assertEquals(1, state1.tags[1]); // ID 1 has tag 1

        // At c=1, start another vertex with tag = 2 (binary: 0010) and connect it LEFT
        Frontier4.Builder builder1 = state1.startVertex(2);
        boolean connected = builder1.connect(Frontier4.Direction.LEFT);
        assertTrue(connected);

        // Building this should merge the components, OR-ing their tags (1 | 2 = 3)
        Frontier4 state2 = builder1.build();

        // Check that components are merged and tags are merged
        assertEquals(state2.parent[0], state2.parent[1]);
        int mergedId = state2.parent[0];
        assertTrue(mergedId > 0);
        assertEquals(3, state2.tags[mergedId]);
    }

    @Test
    public void testFrontier4TagRetirement() {
        // Frontier4 of size 3
        Frontier4 state = Frontier4.getInitialState(3);

        // Place tag 4 at c=0
        Frontier4 state1 = state.startVertex(4).build();

        // Skip c=1 (without vertex). Since c=0 has the tag, it is not dying yet
        Frontier4 state2 = state1.nextWithoutVertex();

        // Skip c=2 (without vertex)
        Frontier4 state3 = state2.nextWithoutVertex();

        // At this point c wraps back to 0. Since c=0 has the tag and we call nextWithoutVertex()
        // at c=0, that component is about to die (retire) as it has no other active cells.
        Frontier4.Builder builderAt0 = state3.startVertex(0);
        builderAt0.parent[0] = 0; // Simulate skip without vertex manually / using constructor

        // Let's use the standard Builder with nextWithoutVertex to test getRetiringTag
        Frontier4.Builder skipBuilder = new Frontier4.Builder(state3);
        // Note: nextWithoutVertex does used=false and parent[c]=0
        // We can manually set that on skipBuilder
        skipBuilder.connect(Frontier4.Direction.UP); // Try up to connect, but not connecting retired component

        // Actually, let's test retiring tag by using nextWithoutVertex() or building directly
        // If we don't connect to UP, it should retire and we should get tag 4
        Frontier4.Builder builderWithNoConnect = new Frontier4.Builder(state3);
        assertEquals(4, builderWithNoConnect.getRetiringTag());
    }

    @Test
    public void testFrontier8TagShiftRetirement() {
        // Frontier8 of size 2
        Frontier8 state = Frontier8.getInitialState(2); // parent.length = 3

        // At c=0, place tag 8
        Frontier8 state1 = state.startVertex(8).build(); // state1.c = 1, parent=[1, 0, 0], tags[1]=8

        // Shift should move everything right
        // Before shifting:
        // We can check retiring tag if any. Since we shift right, the rightmost element (at index 2) retiring?
        // In parent=[1, 0, 0], elements are active.
        // Let's advance c to W (2) so that index 2 has something, then shift.
        // Let's build up to c=2
        Frontier8.Builder b1 = state1.startVertex(0);
        b1.connect(Frontier8.Direction.LEFT); // connects c=1 to c=0 (ID 1)
        Frontier8 state2 = b1.build(); // state2.c = 2, parent=[1, 1, 0]

        Frontier8.Builder b2 = state2.startVertex(16);
        b2.connect(Frontier8.Direction.LEFT); // connects c=2 to c=1 (ID 1), tags[1] = 8 | 16 = 24
        Frontier8 state3 = b2.build(); // state3.c = 3, but Frontier8 handles c wrap or shift.

        // Let's perform shift on state3
        // Shift shifts everything to the right, index 2 will be shifted out?
        // Let's check state3.parent is length 3. Shift copy [0, 1] to [1, 2] and set index 0 to 0.
        // So element at index 2 (which is parent[2]) retires if there are no other occurrences.
        // Let's check if we can get retiring tag from state3 before shift
        int retiring = state3.getShiftRetiringTag();
        // Since state3 has parent=[1, 1, 1] (or similar) of same ID, there are other occurrences. So it is not retiring yet.
        // Let's construct a state where only index 2 has a component.
        byte[] p = new byte[] {0, 0, 1};
        int[] t = new int[6];
        t[1] = 64;
        Frontier8 singleState = new Frontier8(p, t, 0, false, 0, 0);
        assertEquals(64, singleState.getShiftRetiringTag());

        Frontier8 shifted = singleState.shift();
        assertTrue(shifted.hasDead);
        assertEquals(1, shifted.deadEnds);
    }
}
