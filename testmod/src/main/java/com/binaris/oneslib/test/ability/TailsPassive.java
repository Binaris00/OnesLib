package com.binaris.oneslib.test.ability;

import com.binaris.oneslib.api.AbilityContext;
import com.binaris.oneslib.api.EndReason;
import com.binaris.oneslib.api.impl.PassiveAbility;

import net.minecraft.world.effect.MobEffects;

public final class TailsPassive extends PassiveAbility {

    public TailsPassive() {
        super("tails_passive", settings -> settings.name("Tails Passive"));
    }

    @Override
    public void onStart(AbilityContext context) {
        context.effect(MobEffects.DAMAGE_BOOST, -1, 1);
        context.effect(MobEffects.MOVEMENT_SPEED, -1, 1);
    }

    @Override
    public void onEnd(AbilityContext context, EndReason reason) {
        context.removeEffect(MobEffects.DAMAGE_BOOST);
        context.removeEffect(MobEffects.MOVEMENT_SPEED);
    }
}
