package com.binaris.oneslib.common.util;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class Particles {

    private Particles() {
    }

    public static void spawn(ServerLevel level, ParticleOptions particle, Vec3 pos, int count, Vec3 spread,
                             double speed) {
        level.sendParticles(particle, pos.x, pos.y, pos.z, count, spread.x, spread.y, spread.z, speed);
    }

    public static void trail(ServerLevel level, ParticleOptions particle, LivingEntity entity, double spread) {
        Vec3 pos = entity.position().add(0.0D, entity.getBbHeight() * 0.5D, 0.0D);
        spawn(level, particle, pos, 3, new Vec3(spread, spread * 0.5D, spread), 0.0D);
    }

    public static void line(ServerLevel level, ParticleOptions particle, Vec3 start, Vec3 end, double spacing) {
        Vec3 delta = end.subtract(start);
        double length = delta.length();
        if (length <= 0.0D || spacing <= 0.0D) {
            return;
        }
        Vec3 step = delta.scale(spacing / length);
        int points = (int) Math.ceil(length / spacing);
        for (int i = 0; i <= points; i++) {
            Vec3 pos = start.add(step.scale(i));
            level.sendParticles(particle, pos.x, pos.y, pos.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    public static void ring(ServerLevel level, ParticleOptions particle, Vec3 center, double radius, int points) {
        for (int i = 0; i < points; i++) {
            double angle = (Math.PI * 2.0D * i) / points;
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;
            level.sendParticles(particle, x, center.y, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }
}
