package com.binaris.oneslib.api;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.binaris.oneslib.common.state.OneAnimation;
import com.binaris.oneslib.common.state.OneState;
import com.binaris.oneslib.common.util.Area;
import com.binaris.oneslib.common.util.Particles;
import com.binaris.oneslib.common.util.Sounds;
import com.binaris.oneslib.common.util.Targeting;
import com.binaris.oneslib.common.util.Titles;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animation.Animation;

public final class AbilityContext {

    private final ServerPlayer player;
    private final One one;
    private final OneAbility ability;
    private final LivingEntity shape;
    private final List<ScheduledAction> schedules = new ArrayList<>();
    private int ticks;
    private boolean endRequested;

    public AbilityContext(ServerPlayer player, One one, OneAbility ability, LivingEntity shape) {
        this.player = player;
        this.one = one;
        this.ability = ability;
        this.shape = shape;
    }

    public ServerPlayer player() {
        return this.player;
    }

    public ServerLevel level() {
        return this.player.serverLevel();
    }

    public One one() {
        return this.one;
    }

    public OneAbility ability() {
        return this.ability;
    }

    public LivingEntity shape() {
        return this.shape;
    }

    public int tick() {
        return this.ticks;
    }

    public float progress() {
        int duration = this.ability.settings().durationTicks();
        if (duration <= 0) {
            return 1.0F;
        }
        return Math.min(1.0F, (float) this.ticks / (float) duration);
    }

    public boolean isActive() {
        return !this.endRequested;
    }

    public void animate(String name) {
        this.animate(name, Animation.LoopType.PLAY_ONCE, 20);
    }

    public void animate(String name, Animation.LoopType loopType) {
        this.animate(name, loopType, loopType == Animation.LoopType.LOOP ? -1 : 20);
    }

    public void animate(String name, Animation.LoopType loopType, int durationTicks) {
        OneAnimation animation = new OneAnimation(name, loopType, durationTicks);
        OneState.write(this.shape, animation);

        if (loopType != Animation.LoopType.LOOP && durationTicks > 0) {
            this.schedule(durationTicks, context -> {
                if (animation.equals(OneState.read(this.shape))) {
                    OneState.clear(this.shape);
                }
            });
        }
    }

    public void stopAnimation() {
        OneState.clear(this.shape);
    }

    public void particles(ParticleOptions particle, int count) {
        this.particles(particle, count, 0.3D, 0.02D);
    }

    public void particles(ParticleOptions particle, int count, double spread, double speed) {
        Vec3 center = this.player.position().add(0.0D, this.player.getBbHeight() * 0.5D, 0.0D);
        Particles.spawn(this.level(), particle, center, count, new Vec3(spread, spread, spread), speed);
    }

    public void trail(ParticleOptions particle, double spread) {
        Particles.trail(this.level(), particle, this.player, spread);
    }

    public void sound(SoundEvent sound) {
        this.sound(sound, 1.0F, 1.0F);
    }

    public void sound(SoundEvent sound, float volume, float pitch) {
        Sounds.play(this.player, sound, volume, pitch);
    }

    public void title(Component title, @Nullable Component subtitle) {
        Titles.send(this.player, title, subtitle, 10, 40, 10);
    }

    public void actionBar(Component message) {
        this.player.displayClientMessage(message, true);
    }

    public void effect(MobEffect effect, int durationTicks, int amplifier) {
        this.player.addEffect(new MobEffectInstance(effect, durationTicks, amplifier, false, false));
    }

    public void removeEffect(MobEffect effect) {
        this.player.removeEffect(effect);
    }

    public Area area(double radius) {
        return new Area(this.level(), this.player, this.player.position(), radius);
    }

    public Optional<LivingEntity> raycast(double distance) {
        return Targeting.raycast(this.player, distance);
    }

    public Optional<LivingEntity> raycast(double distance, Predicate<LivingEntity> filter) {
        return Targeting.raycast(this.player, distance, filter);
    }

    public List<LivingEntity> raycastAll(double distance) {
        return Targeting.raycastAll(this.player, distance);
    }

    public List<LivingEntity> raycastAll(double distance, int limit) {
        return Targeting.raycastAll(this.player, distance, limit);
    }

    public Vec3 raycastEnd(double distance) {
        return Targeting.raycastEnd(this.player, distance);
    }

    public void beam(ParticleOptions particle, double distance) {
        Vec3 start = this.player.getEyePosition();
        Vec3 end = Targeting.raycastEnd(this.player, distance);
        Particles.line(this.level(), particle, start, end, 0.5D);
    }

    public void ring(ParticleOptions particle, double radius) {
        Vec3 center = this.player.position().add(0.0D, 0.1D, 0.0D);
        Particles.ring(this.level(), particle, center, radius, 24);
    }

    public void particlesAt(ParticleOptions particle, Vec3 pos, int count, double spread, double speed) {
        Particles.spawn(this.level(), particle, pos, count, new Vec3(spread, spread, spread), speed);
    }

    public void fly(boolean value) {
        this.player.getAbilities().mayfly = value;
        if (!value) {
            this.player.getAbilities().flying = false;
        }
        this.player.onUpdateAbilities();
    }

    public void schedule(int delayTicks, Consumer<AbilityContext> action) {
        this.schedules.add(new ScheduledAction(this.ticks + delayTicks, -1, action));
    }

    public void scheduleEvery(int interval, Consumer<AbilityContext> action) {
        this.schedules.add(new ScheduledAction(this.ticks + interval, interval, action));
    }

    public void end() {
        this.endRequested = true;
    }

    public boolean isEndRequested() {
        return this.endRequested;
    }

    public void advance() {
        this.ticks++;
        Iterator<ScheduledAction> iterator = this.schedules.iterator();
        while (iterator.hasNext()) {
            ScheduledAction scheduled = iterator.next();
            if (this.ticks < scheduled.nextTick()) {
                continue;
            }
            scheduled.action().accept(this);
            if (scheduled.interval() > 0) {
                scheduled.advance();
            } else {
                iterator.remove();
            }
        }
    }

    private static final class ScheduledAction {

        private int nextTick;
        private final int interval;
        private final Consumer<AbilityContext> action;

        ScheduledAction(int nextTick, int interval, Consumer<AbilityContext> action) {
            this.nextTick = nextTick;
            this.interval = interval;
            this.action = action;
        }

        int nextTick() {
            return this.nextTick;
        }

        int interval() {
            return this.interval;
        }

        Consumer<AbilityContext> action() {
            return this.action;
        }

        void advance() {
            this.nextTick += this.interval;
        }
    }
}
