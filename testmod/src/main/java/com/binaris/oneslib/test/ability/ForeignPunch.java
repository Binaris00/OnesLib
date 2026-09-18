package com.binaris.oneslib.test.ability;

import com.binaris.oneslib.api.AbilityContext;
import com.binaris.oneslib.api.impl.InstantAbility;

import net.minecraft.sounds.SoundEvents;

public final class ForeignPunch extends InstantAbility {

    public ForeignPunch() {
        super("foreign_punch", settings -> settings.name("Punch").cooldown(20));
    }

    @Override
    public void onStart(AbilityContext context) {
        context.sound(SoundEvents.PLAYER_ATTACK_STRONG);
    }
}
