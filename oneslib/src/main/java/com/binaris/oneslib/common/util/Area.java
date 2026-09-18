package com.binaris.oneslib.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.binaris.oneslib.api.OneData;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class Area {

    private final ServerLevel level;
    private final LivingEntity owner;
    private final Vec3 center;
    private final double radius;
    private final List<OneData.EffectSpec> effects = new ArrayList<>();
    private boolean excludeOwner = true;
    private Predicate<LivingEntity> filter = entity -> true;
    private float damage;
    private double knockback;

    public Area(ServerLevel level, LivingEntity owner, Vec3 center, double radius) {
        this.level = level;
        this.owner = owner;
        this.center = center;
        this.radius = radius;
    }

    public Area excludeOwner(boolean value) {
        this.excludeOwner = value;
        return this;
    }

    public Area filter(Predicate<LivingEntity> filter) {
        this.filter = filter;
        return this;
    }

    public Area damage(float amount) {
        this.damage = amount;
        return this;
    }

    public Area knockback(double strength) {
        this.knockback = strength;
        return this;
    }

    public Area effect(MobEffect effect, int durationTicks, int amplifier) {
        this.effects.add(new OneData.EffectSpec(effect, durationTicks, amplifier));
        return this;
    }

    public List<LivingEntity> entities() {
        AABB box = new AABB(this.center, this.center).inflate(this.radius);
        return this.level.getEntitiesOfClass(LivingEntity.class, box,
                entity -> (!this.excludeOwner || entity != this.owner) && this.filter.test(entity));
    }

    public int hit() {
        List<LivingEntity> targets = this.entities();
        for (LivingEntity target : targets) {
            if (this.damage > 0.0F) {
                DamageSource source = this.owner instanceof Player player
                        ? this.owner.damageSources().playerAttack(player)
                        : this.owner.damageSources().mobAttack(this.owner);
                target.hurt(source, this.damage);
            }
            if (this.knockback > 0.0D) {
                Vec3 direction = target.position().subtract(this.center);
                if (direction.lengthSqr() < 1.0E-4D) {
                    direction = new Vec3(0.0D, 1.0D, 0.0D);
                }
                Vec3 push = direction.normalize().scale(this.knockback);
                target.push(push.x, 0.4D, push.z);
                target.hurtMarked = true;
            }
            for (OneData.EffectSpec spec : this.effects) {
                target.addEffect(new MobEffectInstance(spec.effect(), spec.durationTicks(), spec.amplifier(),
                        false, false));
            }
        }
        return targets.size();
    }
}
