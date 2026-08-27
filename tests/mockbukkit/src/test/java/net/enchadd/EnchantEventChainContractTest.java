package net.enchadd;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EnchantEventChainContractTest {

    private static final Path MAIN_JAVA = TestProjectPaths.PLUGIN_MAIN_JAVA;

    @Test
    void criticalEnchantListenersAreRegisteredInLifecycleService() throws IOException {
        String lifecycle = read("net", "enchadd", "EnchADDLifecycleService.java");
        String registrar = read("net", "enchadd", "listeners", "EnchantListenerRegistrar.java");
        String assembler = read("net", "enchadd", "listeners", "ListenerRegistrationAssembler.java");
        String plan = read("net", "enchadd", "listeners", "ListenerRegistrationPlan.java");
        assertTrue(lifecycle.contains("EnchantListenerRegistrar.registerAll(plugin);"));
        assertTrue(registrar.contains("ListenerRegistrationAssembler.registerAll(plugin);"));
        assertTrue(assembler.contains("ListenerRegistry.registerIfEnabled(entry.key(), () -> entry.factory().create(plugin));"));
        assertTrue(plan.contains("entry(\"purify\", PurifyListener::new)"));
        assertTrue(plan.contains("entry(\"dispel\", DispelListener::new)"));
        assertTrue(plan.contains("entry(\"quell\", QuellListener::new)"));
        assertTrue(plan.contains("entry(\"volley\", VolleyListener::new)"));
    }

    @Test
    void criticalListenersDeclareEventEntryPoints() throws IOException {
        assertTrue(read("net", "enchadd", "listeners", "PurifyListener.java").contains("@EventHandler"));
        assertTrue(read("net", "enchadd", "listeners", "DispelListener.java").contains("@EventHandler"));
        assertTrue(read("net", "enchadd", "listeners", "QuellListener.java").contains("@EventHandler"));
        assertTrue(read("net", "enchadd", "listeners", "VolleyListener.java").contains("@EventHandler"));
    }

    @Test
    void allEnchantListenersEitherUseEventsOrManagedTask() throws IOException {
        Path listenersDir = MAIN_JAVA.resolve(Path.of("net", "enchadd", "listeners"));
        List<String> listenerFiles = Files.list(listenersDir)
            .filter(path -> path.getFileName().toString().endsWith("Listener.java"))
            .map(path -> path.getFileName().toString())
            .filter(name -> !name.equals("EnchantListenerRegistrar.java"))
            .filter(name -> !name.equals("LifecycleListener.java"))
            .toList();

        for (String fileName : listenerFiles) {
            String source = Files.readString(listenersDir.resolve(fileName), StandardCharsets.UTF_8);
            boolean hasEventHandler = source.contains("@EventHandler");
            boolean hasManagedTask = source.contains("ScheduledTask") && source.contains("cleanup()");
            assertTrue(hasEventHandler || hasManagedTask, fileName + " must declare event entry or managed task cleanup");
        }
    }

    private static String read(String... segments) throws IOException {
        return Files.readString(MAIN_JAVA.resolve(Path.of("", segments)), StandardCharsets.UTF_8);
    }
}
