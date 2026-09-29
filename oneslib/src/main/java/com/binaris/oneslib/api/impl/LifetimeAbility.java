package com.binaris.oneslib.api.impl;

import java.util.function.Consumer;

import com.binaris.oneslib.api.AbilitySettings;
import com.binaris.oneslib.api.OneAbility;
import com.binaris.oneslib.api.OneShotAnimation;

public abstract class LifetimeAbility extends OneAbility {

    protected LifetimeAbility(String id, int durationTicks, Consumer<AbilitySettings.Builder> consumer) {
        super(id, builder -> {
            builder.duration(durationTicks);
            consumer.accept(builder);
        });
    }

    protected LifetimeAbility(String id, int durationTicks) {
        this(id, durationTicks, builder -> {
        });
    }

    /** Takes the ability duration from the one-shot timeline, so the phases cannot drift apart. */
    protected LifetimeAbility(String id, OneShotAnimation oneShot, Consumer<AbilitySettings.Builder> consumer) {
        super(id, builder -> {
            builder.oneShot(oneShot);
            builder.duration(oneShot.totalTicks());
            consumer.accept(builder);
        });
    }

    protected LifetimeAbility(String id, OneShotAnimation oneShot) {
        this(id, oneShot, builder -> {
        });
    }
}
