package com.binaris.oneslib.test.ability;

import com.binaris.oneslib.api.AbilityContext;
import com.binaris.oneslib.api.OneShotAnimation;
import com.binaris.oneslib.api.impl.LifetimeAbility;

import org.lwjgl.glfw.GLFW;

/**
 * The declarative one-shot timeline: 20 ticks of wind-up, the hit at tick 25, 15 ticks of
 * recovery. The duration comes from the spec and the damage is attached to the hit tick, so
 * there is no tick literal in the ability.
 */
public final class ImpSlamAttack extends LifetimeAbility {

    public ImpSlamAttack() {
        this("imp_packet_attack");
    }

    public ImpSlamAttack(String id) {
        super(id, OneShotAnimation.of("attack", 20, 25, 15), settings -> settings
                .name("Imp Slam")
                .cooldown(30)
                .keybind(GLFW.GLFW_KEY_N));
    }

    @Override
    public void onStart(AbilityContext context) {
        context.playOneShot();
        context.onOneShotHit(hit -> hit.area(3.0D).damage(2.0F).hit());
    }
}
