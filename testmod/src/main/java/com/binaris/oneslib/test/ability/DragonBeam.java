package com.binaris.oneslib.test.ability;

import java.util.List;

import com.binaris.oneslib.api.AbilityContext;
import com.binaris.oneslib.api.EndReason;
import com.binaris.oneslib.api.KeybindUi;
import com.binaris.oneslib.api.impl.LifetimeAbility;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import org.lwjgl.glfw.GLFW;

public final class DragonBeam extends LifetimeAbility {

    public DragonBeam() {
        super("dragon_beam", 100, settings -> settings
                .name("Dragon Beam")
                .cooldown(200)
                .keybind(GLFW.GLFW_KEY_B)
                .keybindUi(KeybindUi.BOXES));
    }

    @Override
    public void onStart(AbilityContext context) {
        context.animate("attack");
        context.sound(SoundEvents.ENDER_DRAGON_GROWL);
    }

    @Override
    public void onTick(AbilityContext context) {
        context.trail(ParticleTypes.DRAGON_BREATH, 0.4D);

        if (context.tick() < 40) {
            return;
        }

        context.beam(ParticleTypes.DRAGON_BREATH, 32.0D);
        context.beam(ParticleTypes.FLAME, 32.0D);

        if (context.tick() == 40) {
            context.animate("fire_breathe");
            context.ring(ParticleTypes.END_ROD, 2.5D);
            context.sound(SoundEvents.ENDER_DRAGON_SHOOT);

            List<LivingEntity> targets = context.raycastAll(32.0D, 8);
            for (LivingEntity target : targets) {
                target.hurt(context.player().damageSources().playerAttack(context.player()), 6.0F);
            }
        }
    }

    @Override
    public void onEnd(AbilityContext context, EndReason reason) {
        context.stopAnimation();
    }
}
