package com.binaris.oneslib.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
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
    private int lingerTicks;
    private int repeatInterval;
    private int repeatTimes;
    private int pendingRepeats;
    private BiConsumer<Integer, Runnable> scheduler;

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

    /**
     * Keeps the area active for {@code ticks}, hitting once per tick. {@code 0} (the default)
     * is a single instant hit, which is what a burst like a slam wants. Note that a lingering
     * area applies its knockback every tick, so persistent zones usually want a much smaller
     * value than a one-shot one.
     */
    public Area linger(int ticks) {
        this.lingerTicks = Math.max(0, ticks);
        return this;
    }

    /**
     * Hits repeatedly over the next {@code ticks}, once every {@code interval} ticks. The first
     * hit is immediate, so {@code repeat(20, 3)} lands on the calling tick and then at +20 and
     * +40. Overrides {@link #linger(int)}.
     */
    public Area repeat(int interval, int times) {
        this.repeatInterval = Math.max(1, interval);
        this.repeatTimes = Math.max(0, times);
        return this;
    }

    /**
     * Supplies the delayed-tick scheduler used by {@link #linger(int)} and
     * {@link #repeat(int, int)}. It is injected by {@code AbilityContext.area} so the area is
     * driven by the ability's own clock: when the ability ends, the pending hits stop with it.
     * Without it, a configured linger or repeat degrades to a single hit.
     */
    public Area scheduler(BiConsumer<Integer, Runnable> scheduler) {
        this.scheduler = scheduler;
        return this;
    }

    public List<LivingEntity> entities() {
        AABB box = new AABB(this.center, this.center).inflate(this.radius);
        return this.level.getEntitiesOfClass(LivingEntity.class, box,
                entity -> (!this.excludeOwner || entity != this.owner) && this.filter.test(entity));
    }

    /**
     * Applies the configured hit. The return value is the number of targets affected by the
     * first pulse; the remaining pulses of a linger or repeat are scheduled and add to it.
     */
    public int hit() {
        int total = this.apply();
        if (this.scheduler == null) {
            return total;
        }
        if (this.repeatTimes > 0) {
            this.scheduleRepeats(this.repeatTimes - 1);
        } else if (this.lingerTicks > 0) {
            this.scheduleLinger(this.lingerTicks - 1);
        }
        return total;
    }

    private int apply() {
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

    private void scheduleRepeats(int remaining) {
        this.pendingRepeats = remaining;
        this.scheduler.accept(this.repeatInterval, () -> {
            this.apply();
            if (--this.pendingRepeats > 0) {
                this.scheduleRepeats(this.pendingRepeats);
            }
        });
    }

    private void scheduleLinger(int remaining) {
        this.scheduler.accept(1, () -> {
            this.apply();
            if (remaining > 0) {
                this.scheduleLinger(remaining - 1);
            }
        });
    }
}
