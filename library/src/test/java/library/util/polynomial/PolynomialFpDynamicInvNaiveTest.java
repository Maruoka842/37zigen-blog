package library.util.polynomial;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.Test;

import library.util.polynomial.PolynomialFpDynamic3D;
import library.util.polynomial.PolynomialFpDynamic4D;

public class PolynomialFpDynamicInvNaiveTest {

    @Test
    public void testInvNaive3D() {
        long mod = 998244353L;
        PolynomialFpDynamic3D poly3d = new PolynomialFpDynamic3D(mod);
        Random rand = new Random(42);

        for (int trial = 0; trial < 10; trial++) {
            // Generate random dimensions (small for naive multiplication)
            int maxX = rand.nextInt(4) + 1;
            int maxY = rand.nextInt(4) + 1;
            int maxZ = rand.nextInt(4) + 1;

            // Create random polynomial F with non-zero constant term
            long[][][] F = new long[maxX][maxY][maxZ];
            for (int i = 0; i < maxX; i++) {
                for (int j = 0; j < maxY; j++) {
                    for (int k = 0; k < maxZ; k++) {
                        F[i][j][k] = rand.nextInt((int) mod);
                    }
                }
            }
            // Ensure constant term is non-zero
            if (F[0][0][0] == 0) {
                F[0][0][0] = rand.nextInt((int) mod - 1) + 1;
            }

            // Compute G = 1/F using invNaive
            long[][][] G = poly3d.invNaive(F);

            // Compute G * F
            long[][][] product = poly3d.mulNaive(G, F);

            boolean isOne = true;
            for (int i = 0; i < maxX && isOne; i++) {
                for (int j = 0; j < maxY && isOne; j++) {
                    for (int k = 0; k < maxZ && isOne; k++) {
                        long expected = (i == 0 && j == 0 && k == 0) ? 1 : 0;
                        if (product[i][j][k] != expected) {
                            isOne = false;
                        }
                    }
                }
            }

            assertTrue(isOne,
                "G * F should equal 1. Trial: " + trial +
                ", F dimensions: [" + maxX + "][" + maxY + "][" + maxZ + "]");
        }
    }

    @Test
    public void testInvNaive3DSpecific() {
        long mod = 998244353L;
        PolynomialFpDynamic3D poly3d = new PolynomialFpDynamic3D(mod);

        // Test case: F = 1 + x + y + z
        long[][][] F = {
            {{1, 1}, {1, 0}},
            {{1, 0}, {0, 0}}
        };

        long[][][] G = poly3d.invNaive(F);
        long[][][] product = poly3d.mulNaive(G, F);

        boolean isOne = true;
        for (int i = 0; i < F.length && isOne; i++) {
            for (int j = 0; j < F[i].length && isOne; j++) {
                for (int k = 0; k < F[i][j].length && isOne; k++) {
                    long expected = (i == 0 && j == 0 && k == 0) ? 1 : 0;
                    if (product[i][j][k] != expected) {
                        isOne = false;
                    }
                }
            }
        }

        assertTrue(isOne,
            "G * F should equal 1 for F = 1 + x + y + z");
    }

    @Test
    public void testInvNaive4D() {
        long mod = 998244353L;
        PolynomialFpDynamic4D poly4d = new PolynomialFpDynamic4D(mod);
        Random rand = new Random(42);

        for (int trial = 0; trial < 10; trial++) {
            // Generate random dimensions (small for naive multiplication)
            int maxX = rand.nextInt(4) + 1;
            int maxY = rand.nextInt(4) + 1;
            int maxZ = rand.nextInt(4) + 1;
            int maxW = rand.nextInt(4) + 1;

            // Create random polynomial F with non-zero constant term
            long[][][][] F = new long[maxX][maxY][maxZ][maxW];
            for (int i = 0; i < maxX; i++) {
                for (int j = 0; j < maxY; j++) {
                    for (int k = 0; k < maxZ; k++) {
                        for (int l = 0; l < maxW; l++) {
                            F[i][j][k][l] = rand.nextInt((int) mod);
                        }
                    }
                }
            }
            // Ensure constant term is non-zero
            if (F[0][0][0][0] == 0) {
                F[0][0][0][0] = rand.nextInt((int) mod - 1) + 1;
            }

            // Compute G = 1/F using invNaive
            long[][][][] G = poly4d.invNaive(F);

            // Compute G * F
            long[][][][] product = poly4d.mulNaive(G, F);

            // Check that the product equals 1 modulo the input dimensions.
            boolean isOne = true;
            for (int i = 0; i < maxX && isOne; i++) {
                for (int j = 0; j < maxY && isOne; j++) {
                    for (int k = 0; k < maxZ && isOne; k++) {
                        for (int l = 0; l < maxW && isOne; l++) {
                            long expected = (i == 0 && j == 0 && k == 0 && l == 0) ? 1 : 0;
                            if (product[i][j][k][l] != expected) {
                                isOne = false;
                            }
                        }
                    }
                }
            }

            assertTrue(isOne,
                "G * F should equal 1. Trial: " + trial +
                ", F dimensions: [" + maxX + "][" + maxY + "][" + maxZ + "][" + maxW + "]");
        }
    }

    @Test
    public void testInvNaive4DSpecific() {
        long mod = 998244353L;
        PolynomialFpDynamic4D poly4d = new PolynomialFpDynamic4D(mod);

        // Test case: F = 1 + x + y + z + w
        long[][][][] F = {{{{1, 1}, {1, 0}}, {{1, 0}, {0, 0}}}, {{{0, 0}, {0, 0}}, {{0, 0}, {0, 0}}}};

        long[][][][] G = poly4d.invNaive(F);
        long[][][][] product = poly4d.mulNaive(G, F);

        // Check that the product equals 1 modulo the input dimensions.
        boolean isOne = true;
        for (int i = 0; i < F.length && isOne; i++) {
            for (int j = 0; j < F[i].length && isOne; j++) {
                for (int k = 0; k < F[i][j].length && isOne; k++) {
                    for (int l = 0; l < F[i][j][k].length && isOne; l++) {
                        long expected = (i == 0 && j == 0 && k == 0 && l == 0) ? 1 : 0;
                        if (product[i][j][k][l] != expected) {
                            isOne = false;
                        }
                    }
                }
            }
        }

        assertTrue(isOne, "G * F should equal 1 for F = 1 + x + y + z + w");
    }
}
