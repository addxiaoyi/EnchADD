package com.enadd.core.budget;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EntityBudgetControllerTest {

    @Test
    public void testAllocateWithinBudget() {
        EntityBudgetController controller = EntityBudgetController.getInstance();
        boolean result = controller.allocate(EntityBudgetController.BudgetCategory.PARTICLE, 10);
        assertTrue(result);
        controller.free(EntityBudgetController.BudgetCategory.PARTICLE, 10);
    }

    @Test
    public void testCanAllocate() {
        EntityBudgetController controller = EntityBudgetController.getInstance();
        assertTrue(controller.canAllocate(EntityBudgetController.BudgetCategory.PROJECTILE, 5));
    }

    @Test
    public void testGetUsagePercent() {
        EntityBudgetController controller = EntityBudgetController.getInstance();
        float percent = controller.getUsagePercent(EntityBudgetController.BudgetCategory.SUMMONED);
        assertTrue(percent >= 0);
    }

    @Test
    public void testGetStatus() {
        EntityBudgetController controller = EntityBudgetController.getInstance();
        EntityBudgetController.BudgetStatus status = controller.getStatus();
        assertNotNull(status);
    }

    @Test
    public void testResetAll() {
        EntityBudgetController controller = EntityBudgetController.getInstance();
        controller.allocate(EntityBudgetController.BudgetCategory.AREA_EFFECT, 50);
        controller.resetAll();
        assertEquals(0, controller.getUsage(EntityBudgetController.BudgetCategory.AREA_EFFECT));
    }
}
