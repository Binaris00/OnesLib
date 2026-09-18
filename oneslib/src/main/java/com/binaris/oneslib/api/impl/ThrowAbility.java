package com.binaris.oneslib.api.impl;

import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.binaris.oneslib.api.AbilityContext;
import com.binaris.oneslib.api.AbilitySettings;
import com.binaris.oneslib.api.EndReason;
import com.binaris.oneslib.api.OneAbility;
import com.binaris.oneslib.common.entity.OneProjectile;
import com.binaris.oneslib.common.registry.OnesEntities;
import com.binaris.oneslib.common.util.Particles;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public abstract class ThrowAbility extends OneAbility {

    private final int lifetimeTicks;
    @Nullable
    private final ItemStack visual;
    @Nullable
    private Entity projectile;

    protected ThrowAbility(String id, int lifetimeTicks, @Nullable ItemStack visual,
                           Consumer<AbilitySettings.Builder> consumer) {
        super(id, builder -> {
            builder.duration(lifetimeTicks);
            consumer.accept(builder);
        });
        this.lifetimeTicks = lifetimeTicks;
        this.visual = visual;
    }

    protected ThrowAbility(String id, int lifetimeTicks, @Nullable ItemStack visual) {
        this(id, lifetimeTicks, visual, builder -> {
        });
    }

    public int lifetimeTicks() {
        return this.lifetimeTicks;
    }

    protected Entity createProjectile(AbilityContext context) {
        return new OneProjectile(OnesEntities.PROJECTILE.get(), context.level(), this.visual);
    }

    protected Vec3 initialVelocity(AbilityContext context) {
        return context.player().getLookAngle().scale(2.0D);
    }

    protected double gravity() {
        return 0.03D;
    }

    @Nullable
    protected ParticleOptions trailParticle() {
        return null;
    }

    protected boolean canHit(AbilityContext context, Entity target) {
        return !target.isSpectator() && target.isPickable()
                && target != context.player() && target != context.shape();
    }

    protected void onProjectileTick(AbilityContext context, Entity projectile) {
    }

    protected void onHitEntity(AbilityContext context, Entity projectile, LivingEntity target) {
    }

    protected void onHitBlock(AbilityContext context, Entity projectile, BlockHitResult hit) {
    }

    @Override
    public void onStart(AbilityContext context) {
        Entity created = this.createProjectile(context);
        if (created == null) {
            context.end();
            return;
        }
        Vec3 start = context.player().getEyePosition().add(context.player().getLookAngle().scale(0.4D));
        created.moveTo(start.x, start.y, start.z, context.player().getYRot(), context.player().getXRot());
        created.setDeltaMovement(this.initialVelocity(context));
        context.level().addFreshEntity(created);
        this.projectile = created;
    }

    @Override
    public void onTick(AbilityContext context) {
        Entity entity = this.projectile;
        if (entity == null || !entity.isAlive()) {
            context.end();
            return;
        }

        Vec3 velocity = entity.getDeltaMovement();
        Vec3 start = entity.position();
        Vec3 end = start.add(velocity);

        BlockHitResult blockHit = context.level().clip(
                new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));
        Vec3 endPos = blockHit.getType() == HitResult.Type.MISS ? end : blockHit.getLocation();

        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(entity, start, endPos,
                entity.getBoundingBox().expandTowards(velocity).inflate(1.0D),
                target -> this.canHit(context, target), start.distanceToSqr(endPos));

        if (entityHit != null && entityHit.getEntity() instanceof LivingEntity target) {
            this.onHitEntity(context, entity, target);
            entity.discard();
            context.end();
            return;
        }

        if (blockHit.getType() != HitResult.Type.MISS) {
            this.onHitBlock(context, entity, blockHit);
            entity.discard();
            context.end();
            return;
        }

        entity.move(MoverType.SELF, velocity);
        entity.setDeltaMovement(velocity.add(0.0D, -this.gravity(), 0.0D));
        entity.hasImpulse = true;

        ParticleOptions particle = this.trailParticle();
        if (particle != null) {
            Particles.spawn(context.level(), particle, entity.position(), 3,
                    new Vec3(0.05D, 0.05D, 0.05D), 0.0D);
        }
        this.onProjectileTick(context, entity);
    }

    @Override
    public void onEnd(AbilityContext context, EndReason reason) {
        if (this.projectile != null) {
            this.projectile.discard();
            this.projectile = null;
        }
    }
}
