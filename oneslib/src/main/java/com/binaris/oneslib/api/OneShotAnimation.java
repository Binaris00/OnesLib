package com.binaris.oneslib.api;

import java.util.function.Consumer;

/**
 * Declarative timeline for a one-shot attack animation.
 *
 * <p>The convention this encodes is: a one-shot animation returns to the One's idle state when
 * it finishes, and a 60 tick animation is 20 ticks of wind-up, the hit itself, and the rest as
 * recovery. Expressing that as three numbers keeps "where do I apply the damage" a declaration
 * instead of a magic tick literal, and it makes the library decide when the animation is cleared
 * so the return to idle is not cut short.
 *
 * <p>Usage from an ability:
 * <pre>{@code
 * public final class SlamAbility extends InstantAbility {
 *     public SlamAbility() {
 *         super("slam", 60, OneShotAnimation.of("slam", 20, 25, 15),
 *                 settings -> settings.cooldown(80));
 *     }
 *
 *     @Override
 *     public void onStart(AbilityContext context) {
 *         context.playOneShot();                       // animates and returns to idle
 *         context.schedule(context.oneShot().hitTick(), c ->
 *                 c.area(5.0D).damage(10.0F).knockback(1.5D).hit());
 *     }
 * }
 * }</pre>
 */
public final class OneShotAnimation {

    private final String name;
    private final int initTicks;
    private final int hitTick;
    private final int recoverTicks;

    private OneShotAnimation(String name, int initTicks, int hitTick, int recoverTicks) {
        if (initTicks < 0 || recoverTicks < 0) {
            throw new IllegalArgumentException("initTicks and recoverTicks must be >= 0");
        }
        if (hitTick < initTicks) {
            throw new IllegalArgumentException("hitTick must not be before initTicks");
        }
        this.name = name;
        this.initTicks = initTicks;
        this.hitTick = hitTick;
        this.recoverTicks = recoverTicks;
    }

    public static OneShotAnimation of(String name, int initTicks, int hitTick, int recoverTicks) {
        return new OneShotAnimation(name, initTicks, hitTick, recoverTicks);
    }

    /** Wind-up, hit and recovery in three equal parts of a {@code totalTicks} animation. */
    public static OneShotAnimation of(String name, int totalTicks) {
        int third = Math.max(1, totalTicks / 3);
        return new OneShotAnimation(name, third, 2 * third, Math.max(0, totalTicks - 2 * third));
    }

    public String name() {
        return this.name;
    }

    /** Ticks before the hit lands, during which the One is committed but cannot be interrupted. */
    public int initTicks() {
        return this.initTicks;
    }

    /** Tick, relative to the start, where the hit lands. */
    public int hitTick() {
        return this.hitTick;
    }

    /** Ticks after the hit during which the One is visibly recovering. */
    public int recoverTicks() {
        return this.recoverTicks;
    }

    public int totalTicks() {
        return this.initTicks + this.recoverTicks + (this.hitTick - this.initTicks);
    }

    public int durationTicks() {
        return this.totalTicks();
    }

    @Override
    public String toString() {
        return "OneShotAnimation[" + this.name + " init=" + this.initTicks + " hit=" + this.hitTick
                + " recover=" + this.recoverTicks + "]";
    }
}
