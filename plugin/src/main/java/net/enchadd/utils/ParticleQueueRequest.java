package net.enchadd.utils;

import org.bukkit.Particle;
import org.bukkit.World;

record ParticleQueueRequest(
        World world,
        double x,
        double y,
        double z,
        Particle particle,
        int count,
        double offsetX,
        double offsetY,
        double offsetZ,
        double speed
) {
    void spawn() {
        world.spawnParticle(particle, x, y, z, count, offsetX, offsetY, offsetZ, speed);
    }
}
