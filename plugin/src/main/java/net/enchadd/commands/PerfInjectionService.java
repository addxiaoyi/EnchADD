package net.enchadd.commands;

import net.enchadd.enchants.EnchADDEnchant;
import net.enchadd.utils.EnchantStats;
import net.enchadd.utils.ParticleQueue;
import net.kyori.adventure.key.Key;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

final class PerfInjectionService {

    private PerfInjectionService() {
    }

    static boolean handle(JavaPlugin plugin,
                          CommandSender sender,
                          Map<Key, EnchADDEnchant> enchants,
                          int triggerCount,
                          int particleCount) {
        if (enchants.isEmpty()) {
            sender.sendMessage(PerfInjectionMessageComposer.noEnchants());
            return true;
        }

        Key probeKey = enchants.containsKey(Key.key("enchadd:airbag"))
                ? Key.key("enchadd:airbag")
                : enchants.keySet().iterator().next();

        if (particleCount > 0 && plugin.getServer().getWorlds().isEmpty()) {
            sender.sendMessage(PerfInjectionMessageComposer.noWorlds());
            particleCount = 0;
        }

        final int totalTriggers = triggerCount;
        final int totalParticles = particleCount;
        final World world = totalParticles > 0 ? plugin.getServer().getWorlds().get(0) : null;
        final var location = world != null ? world.getSpawnLocation().add(0, 1, 0) : null;
        final int triggerBatchSize = Math.min(4_000, totalTriggers);
        final int particleBatchSize = Math.min(2_000, Math.max(totalParticles, 1));
        final int[] remainingTriggers = {totalTriggers};
        final int[] remainingParticles = {totalParticles};
        final int[] submittedParticles = {0};
        final org.bukkit.scheduler.BukkitTask[] taskRef = {null};

        taskRef[0] = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            int injectTriggers = Math.min(triggerBatchSize, remainingTriggers[0]);
            EnchantStats.recordBatch(probeKey, injectTriggers);
            remainingTriggers[0] -= injectTriggers;

            if (world != null && location != null && remainingParticles[0] > 0) {
                int injectParticles = Math.min(particleBatchSize, remainingParticles[0]);
                ParticleQueue.submit(world, location, Particle.END_ROD, injectParticles, 0.2, 0.2, 0.2, 0.0);
                submittedParticles[0] += injectParticles;
                remainingParticles[0] -= injectParticles;
            }

            if (remainingTriggers[0] > 0 || remainingParticles[0] > 0) {
                return;
            }

            org.bukkit.scheduler.BukkitTask task = taskRef[0];
            if (task != null) {
                task.cancel();
            }
            String done = PerfInjectionMessageComposer.doneStatus(totalTriggers, submittedParticles[0], probeKey);
            plugin.getLogger().info(done);
            sender.sendMessage(PerfInjectionMessageComposer.done(done));
        }, 1L, 1L);

        sender.sendMessage(PerfInjectionMessageComposer.queued(
                totalTriggers,
                totalParticles,
                probeKey,
                triggerBatchSize,
                particleBatchSize
        ));
        return true;
    }
}
