package com.binaris.oneslib.test.ability;

import com.binaris.oneslib.api.AbilityContext;
import com.binaris.oneslib.api.EndReason;
import com.binaris.oneslib.api.impl.LifetimeAbility;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import org.lwjgl.glfw.GLFW;

public final class EvilHulkGolpe extends LifetimeAbility {

    public EvilHulkGolpe() {
        super("evil_hulk_golpe", 31, settings -> settings
                .name("Golpe Brutal")
                .cooldown(80)
                .keybind(GLFW.GLFW_KEY_G));
    }

    @Override
    public void onStart(AbilityContext context) {
        context.animate("attack3");
    }

    @Override
    public void onTick(AbilityContext context) {
        if (context.tick() % 2 == 0) {
            context.trail(ParticleTypes.CRIT, 0.3D);
        }
        if (context.tick() == 10) {
            context.area(5.0D).damage(10.0F).knockback(1.5D).hit();
            context.particles(ParticleTypes.EXPLOSION, 12);
            context.ring(ParticleTypes.CRIT, 5.0D);
            context.sound(SoundEvents.GENERIC_EXPLODE);
        }
    }

    @Override
    public void onEnd(AbilityContext context, EndReason reason) {
        context.stopAnimation();
    }
}
