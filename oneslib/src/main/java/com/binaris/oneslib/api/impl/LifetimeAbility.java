package com.binaris.oneslib.api.impl;

import java.util.function.Consumer;

import com.binaris.oneslib.api.AbilitySettings;
import com.binaris.oneslib.api.OneAbility;

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
}
