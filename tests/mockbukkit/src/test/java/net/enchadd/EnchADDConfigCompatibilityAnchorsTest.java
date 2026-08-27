package net.enchadd;

import org.bukkit.configuration.ConfigurationSection;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class EnchADDConfigCompatibilityAnchorsTest {

    @Test
    void keepsRuntimeConfigReflectionFieldAnchor() throws Exception {
        Field field = EnchADDConfig.class.getDeclaredField("runtimeConfig");
        assertNotNull(field);
    }

    @Test
    void keepsConflictLoaderCompatibilityMethodSignature() throws Exception {
        Method method = EnchADDConfig.class.getDeclaredMethod(
                "loadConflictsFromConfig",
                ConfigurationSection.class
        );
        assertNotNull(method);
    }
}
