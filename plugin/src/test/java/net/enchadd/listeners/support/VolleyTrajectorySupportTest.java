package net.enchadd.listeners.support;

import java.util.Random;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VolleyTrajectorySupportTest {
    @Test
    void spreadPreservesSpeedForWeakAndFullDrawShots() {
        Random random = new Random(42);
        for (double speed : new double[]{0.01, 0.5, 3.0}) {
            Vector shot = new Vector(0, 0, speed);
            for (int i = 0; i < 100; i++) {
                Vector spread = VolleyTrajectorySupport.spread(shot, 1.5, random);
                assertEquals(speed, spread.length(), 1.0e-12);
            }
            assertEquals(new Vector(0, 0, speed), shot);
        }
    }

    @Test
    void disabledAndInvalidSpreadLeaveDirectionUnchanged() {
        Vector shot = new Vector(1, 2, 3);
        for (double spread : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY}) {
            Vector adjusted = VolleyTrajectorySupport.spread(shot, spread, new Random(42));
            assertEquals(shot, adjusted);
            assertNotSame(shot, adjusted);
        }
    }

    @Test
    void excessiveSpreadUsesConfiguredSafetyCeiling() {
        Vector shot = new Vector(0, 0, 3);
        assertEquals(VolleyTrajectorySupport.spread(shot, 1.5, new Random(42)),
                VolleyTrajectorySupport.spread(shot, 100, new Random(42)));
    }

    @Test
    void cancellingOffsetFallsBackToOriginalShot() {
        Random random = new Random() {
            @Override
            public double nextDouble() {
                return 0.0;
            }
        };
        Vector shot = new Vector(0.25, 0.25, 0.25);
        assertEquals(shot, VolleyTrajectorySupport.spread(shot, 0.5, random));
    }

    @Test
    void invalidVelocityCannotGenerateAdditionalMotion() {
        for (Vector shot : new Vector[]{new Vector(), new Vector(Double.NaN, 0, 0),
                new Vector(Double.POSITIVE_INFINITY, 0, 0), new Vector(Double.MAX_VALUE, 0, 0)}) {
            assertFalse(VolleyTrajectorySupport.hasVelocity(shot));
            assertEquals(new Vector(), VolleyTrajectorySupport.spread(shot, 0.5, new Random(42)));
        }
    }

    @Test
    void arrowCountRespectsLevelAndSpawnCapsWithoutOverflow() {
        assertEquals(6, VolleyTrajectorySupport.extraArrows(3, 3, 2, 32));
        assertEquals(6, VolleyTrajectorySupport.extraArrows(Integer.MAX_VALUE, 3, 2, 32));
        assertEquals(32, VolleyTrajectorySupport.extraArrows(Integer.MAX_VALUE,
                Integer.MAX_VALUE, Integer.MAX_VALUE, 32));
    }

    @Test
    void nonPositiveCountInputsDisableAdditionalArrows() {
        assertEquals(0, VolleyTrajectorySupport.extraArrows(-1, 3, -1, 32));
        assertEquals(0, VolleyTrajectorySupport.extraArrows(3, 0, 1, 32));
        assertEquals(0, VolleyTrajectorySupport.extraArrows(3, 3, 0, 32));
        assertEquals(0, VolleyTrajectorySupport.extraArrows(3, 3, 1, -1));
    }
}
