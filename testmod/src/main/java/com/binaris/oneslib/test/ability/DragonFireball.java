package com.binaris.oneslib.test.ability;

import com.binaris.oneslib.api.AbilityContext;
import com.binaris.oneslib.api.KeybindUi;
import com.binaris.oneslib.api.impl.ThrowAbility;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.lwjgl.glfw.GLFW;

public final class DragonFireball extends ThrowAbility {

    public DragonFireball() {
        super("dragon_fireball", 100, new ItemStack(Items.FIRE_CHARGE),
                settings -> settings.name("Dragon Fireball").cooldown(40).keybind(GLFW.GLFW_KEY_F)
                        .keybindUi(KeybindUi.BOXES));
    }

    @Override
    protected ParticleOptions trailParticle() {
        return ParticleTypes.FLAME;
    }

    @Override
    protected void onHitEntity(AbilityContext context, Entity projectile, LivingEntity target) {
        target.hurt(context.player().damageSources().playerAttack(context.player()), 8.0F);
        context.level().explode(context.player(), target.getX(), target.getY(), target.getZ(), 1.0F, false,
                Level.ExplosionInteraction.NONE);
    }

    @Override
    protected void onHitBlock(AbilityContext context, Entity projectile, BlockHitResult hit) {
        context.particles(ParticleTypes.EXPLOSION, 8);
    }
}
