package net.enchadd.listeners.support;

import org.bukkit.util.Vector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CombatTriggerSupportTest {
    @Test
    void backstabRewardsRearHitsButNotFrontOrSideHits() {
        Vector facing = new Vector(0, 0, 1);
        assertEquals(12.0, ShadowstrikeHitSupport.damage(10, 2, 1, facing, new Vector(0, 0, -2)));
        assertEquals(10.0, ShadowstrikeHitSupport.damage(10, 2, 1, facing, new Vector(0, 0, 2)));
        assertEquals(10.0, ShadowstrikeHitSupport.damage(10, 2, 1, facing, new Vector(2, 0, 0)));
    }

    @Test
    void overlappingOrInvalidPositionsCannotTriggerBackstab() {
        Vector facing = new Vector(0, 0, 1);
        assertEquals(10.0, ShadowstrikeHitSupport.damage(10, 2, 1, facing, new Vector()));
        assertEquals(10.0, ShadowstrikeHitSupport.damage(10, 2, 1, facing, new Vector(Double.NaN, 0, -1)));
        assertEquals(10.0, ShadowstrikeHitSupport.damage(10, 2, 1, new Vector(), new Vector(0, 0, -1)));
    }

    @Test
    void backstabPreservesZeroDamageAndRejectsInvalidBonus() {
        Vector facing = new Vector(0, 0, 1);
        Vector behind = new Vector(0, 0, -1);
        assertEquals(0.0, ShadowstrikeHitSupport.damage(0, 2, 1, facing, behind));
        assertEquals(10.0, ShadowstrikeHitSupport.damage(10, 2, Double.NaN, facing, behind));
        assertEquals(14.0, ShadowstrikeHitSupport.damage(10, Integer.MAX_VALUE, 10, facing, behind));
    }

    @Test
    void backstabCalculationDoesNotMutateDirections() {
        Vector facing = new Vector(0, 0, 2);
        Vector behind = new Vector(0, 0, -3);
        ShadowstrikeHitSupport.damage(10, 2, 1, facing, behind);
        assertEquals(new Vector(0, 0, 2), facing);
        assertEquals(new Vector(0, 0, -3), behind);
    }

    @Test
    void normalComboProgressionPreservesConfiguredMinimum() {
        assertEquals(5, CombatTriggerSupport.requiredHits(5, 1, 2, 1));
        assertEquals(4, CombatTriggerSupport.requiredHits(5, 1, 2, 2));
        assertEquals(3, CombatTriggerSupport.requiredHits(5, 1, 2, 3));
        assertEquals(2, CombatTriggerSupport.requiredHits(5, 1, 2, 5));
        assertEquals(1, CombatTriggerSupport.nextCombo(0));
        assertEquals(5, CombatTriggerSupport.nextCombo(4));
    }

    @Test
    void extremeComboConfigurationCannotWrapAround() {
        assertEquals(2, CombatTriggerSupport.requiredHits(5, Integer.MAX_VALUE, 2, Integer.MAX_VALUE));
        assertEquals(5, CombatTriggerSupport.requiredHits(5, -1, 2, 3));
        assertEquals(1, CombatTriggerSupport.requiredHits(-1, 0, -1, 1));
        assertEquals(Integer.MAX_VALUE, CombatTriggerSupport.nextCombo(Integer.MAX_VALUE));
        assertEquals(1, CombatTriggerSupport.nextCombo(Integer.MIN_VALUE));
    }
}
