package net.enchadd.commands;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnchantListPermissionSupportTest {

    @Test
    void permissionResultAllowsConsoleAndBlocksDeniedPlayers() {
        assertTrue(EnchantListPermissionSupport.evaluate(false, false));
        assertTrue(EnchantListPermissionSupport.evaluate(true, true));
        assertFalse(EnchantListPermissionSupport.evaluate(true, false));
    }
}
