package com.binaris.oneslib.test.ability;

import java.util.List;

import com.binaris.oneslib.api.AbilityContext;
import com.binaris.oneslib.api.EndReason;
import com.binaris.oneslib.api.impl.LifetimeAbility;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import org.lwjgl.glfw.GLFW;

public final class SirenHeadLaser extends LifetimeAbility {

    public SirenHeadLaser() {
        super("siren_head_laser", 80, settings -> settings
                .name("Laser")
                .cooldown(100)
                .keybind(GLFW.GLFW_KEY_V));
    }

    @Override
    public void onStart(AbilityContext context) {
        context.sound(SoundEvents.WARDEN_SONIC_CHARGE);
    }

    @Override
    public void onTick(AbilityContext context) {
        if (context.tick() < 40) {
            context.trail(ParticleTypes.ELECTRIC_SPARK, 0.2D);
            return;
        }

        if (context.tick() == 40) {
            context.animate("beam");
            context.sound(SoundEvents.WARDEN_SONIC_BOOM);
            context.ring(ParticleTypes.SONIC_BOOM, 1.5D);
        }

        context.beam(ParticleTypes.SONIC_BOOM, 64.0D);

        List<LivingEntity> targets = context.raycastAll(64.0D, 4);
        for (LivingEntity target : targets) {
            target.hurt(context.player().damageSources().playerAttack(context.player()), 8.0F);
        }
    }

    @Override
    public void onEnd(AbilityContext context, EndReason reason) {
        context.stopAnimation();
    }
}
