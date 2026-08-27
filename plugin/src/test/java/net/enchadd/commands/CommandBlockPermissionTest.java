package net.enchadd.commands;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Command Block Permission Test
 *
 * Verifies that command blocks are handled correctly with respect to permissions.
 * In Minecraft, command blocks by default bypass certain permission checks unless
 * explicitly configured. This test ensures the plugin correctly handles command
 * block senders.
 */
@DisplayName("Command Block Permission Tests")
class CommandBlockPermissionTest {

    @Nested
    @DisplayName("EnchantListPermissionSupport evaluate() Static Method Tests")
    class PermissionSupportEvaluateTests {

        @Test
        @DisplayName("evaluate() should allow non-player sender regardless of permission status")
        void evaluate_shouldAllowNonPlayerRegardlessOfPermission() {
            // When playerSender is false (console/command block), always return true
            assertTrue(EnchantListPermissionSupport.evaluate(false, false),
                    "Non-player sender should always be allowed even without permission");
            assertTrue(EnchantListPermissionSupport.evaluate(false, true),
                    "Non-player sender should be allowed with permission");
        }

        @Test
        @DisplayName("evaluate() should respect player permissions")
        void evaluate_shouldRespectPlayerPermissions() {
            // When playerSender is true, respect the permission
            assertFalse(EnchantListPermissionSupport.evaluate(true, false),
                    "Player without permission should be denied");
            assertTrue(EnchantListPermissionSupport.evaluate(true, true),
                    "Player with permission should be allowed");
        }
    }

    @Nested
    @DisplayName("Permission Node Constants Tests")
    class PermissionConstantsTests {

        @Test
        @DisplayName("Permission constants should start with 'enchadd.'")
        void permissionConstants_shouldStartWithEnchadd() {
            assertTrue(EnchantListCommand.PERM_LIST.contains("enchadd."),
                    "List permission should start with 'enchadd.'");
            assertTrue(EnchantListCommand.PERM_INFO.contains("enchadd."),
                    "Info permission should start with 'enchadd.'");
            assertTrue(EnchantListCommand.PERM_EXPORT.contains("enchadd."),
                    "Export permission should start with 'enchadd.'");
            assertTrue(EnchantListCommand.PERM_ADMIN.contains("enchadd."),
                    "Admin permission should start with 'enchadd.'");
        }

        @Test
        @DisplayName("Permission constants should have correct suffixes")
        void permissionConstants_shouldHaveCorrectSuffixes() {
            assertTrue(EnchantListCommand.PERM_LIST.endsWith("list"),
                    "List permission should end with 'list'");
            assertTrue(EnchantListCommand.PERM_INFO.endsWith("info"),
                    "Info permission should end with 'info'");
            assertTrue(EnchantListCommand.PERM_EXPORT.endsWith("export"),
                    "Export permission should end with 'export'");
            assertTrue(EnchantListCommand.PERM_ADMIN.endsWith("admin"),
                    "Admin permission should end with 'admin'");
        }

        @Test
        @DisplayName("All permission constants should be unique")
        void permissionConstants_shouldBeUnique() {
            assertFalse(EnchantListCommand.PERM_LIST.equals(EnchantListCommand.PERM_INFO),
                    "List and info permissions should be different");
            assertFalse(EnchantListCommand.PERM_LIST.equals(EnchantListCommand.PERM_EXPORT),
                    "List and export permissions should be different");
            assertFalse(EnchantListCommand.PERM_LIST.equals(EnchantListCommand.PERM_ADMIN),
                    "List and admin permissions should be different");
            assertFalse(EnchantListCommand.PERM_INFO.equals(EnchantListCommand.PERM_EXPORT),
                    "Info and export permissions should be different");
            assertFalse(EnchantListCommand.PERM_INFO.equals(EnchantListCommand.PERM_ADMIN),
                    "Info and admin permissions should be different");
            assertFalse(EnchantListCommand.PERM_EXPORT.equals(EnchantListCommand.PERM_ADMIN),
                    "Export and admin permissions should be different");
        }

        @Test
        @DisplayName("Permission constants should not be empty")
        void permissionConstants_shouldNotBeEmpty() {
            assertFalse(EnchantListCommand.PERM_LIST.isEmpty(),
                    "List permission should not be empty");
            assertFalse(EnchantListCommand.PERM_INFO.isEmpty(),
                    "Info permission should not be empty");
            assertFalse(EnchantListCommand.PERM_EXPORT.isEmpty(),
                    "Export permission should not be empty");
            assertFalse(EnchantListCommand.PERM_ADMIN.isEmpty(),
                    "Admin permission should not be empty");
        }

        @Test
        @DisplayName("Permission constants should follow standard format")
        void permissionConstants_shouldFollowStandardFormat() {
            // All permissions should follow the pattern: enchadd.<action>
            assertTrue(EnchantListCommand.PERM_LIST.matches("enchadd\\..+"),
                    "List permission should match pattern 'enchadd.<action>'");
            assertTrue(EnchantListCommand.PERM_INFO.matches("enchadd\\..+"),
                    "Info permission should match pattern 'enchadd.<action>'");
            assertTrue(EnchantListCommand.PERM_EXPORT.matches("enchadd\\..+"),
                    "Export permission should match pattern 'enchadd.<action>'");
            assertTrue(EnchantListCommand.PERM_ADMIN.matches("enchadd\\..+"),
                    "Admin permission should match pattern 'enchadd.<action>'");
        }
    }

    @Nested
    @DisplayName("Permission Behavior Tests")
    class PermissionBehaviorTests {

        @Test
        @DisplayName("Non-player sender (command block) logic should bypass permission checks")
        void nonPlayerSender_shouldBypassPermissionChecks() {
            // The key behavior: non-player senders (like command blocks and console)
            // should always return true from evaluate(), bypassing permission checks
            boolean isPlayer = false;
            boolean hasPermission = false;

            boolean result = EnchantListPermissionSupport.evaluate(isPlayer, hasPermission);

            assertTrue(result,
                    "Non-player sender (command block/console) should bypass permission checks");
        }

        @Test
        @DisplayName("Player sender without permission should be denied")
        void playerSenderWithoutPermission_shouldBeDenied() {
            boolean isPlayer = true;
            boolean hasPermission = false;

            boolean result = EnchantListPermissionSupport.evaluate(isPlayer, hasPermission);

            assertFalse(result,
                    "Player without permission should be denied");
        }

        @Test
        @DisplayName("Player sender with permission should be allowed")
        void playerSenderWithPermission_shouldBeAllowed() {
            boolean isPlayer = true;
            boolean hasPermission = true;

            boolean result = EnchantListPermissionSupport.evaluate(isPlayer, hasPermission);

            assertTrue(result,
                    "Player with permission should be allowed");
        }

        @Test
        @DisplayName("Non-player sender with permission should also be allowed")
        void nonPlayerSenderWithPermission_shouldBeAllowed() {
            boolean isPlayer = false;
            boolean hasPermission = true;

            boolean result = EnchantListPermissionSupport.evaluate(isPlayer, hasPermission);

            assertTrue(result,
                    "Non-player sender should be allowed (consistent behavior)");
        }
    }

    @Nested
    @DisplayName("Subcommand Permission Requirements Tests")
    class SubcommandPermissionTests {

        @Test
        @DisplayName("Export subcommands should have separate permission")
        void exportSubcommands_shouldHaveSeparatePermission() {
            // Export operations should have their own permission
            assertFalse(EnchantListCommand.PERM_EXPORT.isEmpty(),
                    "Export permission should be defined");
            assertFalse(EnchantListCommand.PERM_EXPORT.equals(EnchantListCommand.PERM_LIST),
                    "Export permission should be separate from list permission");
        }

        @Test
        @DisplayName("Admin subcommands should have separate permission")
        void adminSubcommands_shouldHaveSeparatePermission() {
            // Admin operations should have their own permission
            assertFalse(EnchantListCommand.PERM_ADMIN.isEmpty(),
                    "Admin permission should be defined");
            assertFalse(EnchantListCommand.PERM_ADMIN.equals(EnchantListCommand.PERM_LIST),
                    "Admin permission should be separate from list permission");
        }

        @Test
        @DisplayName("Info subcommand should have separate permission")
        void infoSubcommand_shouldHaveSeparatePermission() {
            // Info operations should have their own permission
            assertFalse(EnchantListCommand.PERM_INFO.isEmpty(),
                    "Info permission should be defined");
            assertFalse(EnchantListCommand.PERM_INFO.equals(EnchantListCommand.PERM_LIST),
                    "Info permission should be separate from list permission");
        }
    }
}
