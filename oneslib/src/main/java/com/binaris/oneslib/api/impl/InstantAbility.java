package com.binaris.oneslib.api.impl;

import java.util.function.Consumer;

import com.binaris.oneslib.api.AbilitySettings;
import com.binaris.oneslib.api.OneAbility;

public abstract class InstantAbility extends OneAbility {

    protected InstantAbility(String id, Consumer<AbilitySettings.Builder> consumer) {
        super(id, builder -> {
            builder.duration(0);
            consumer.accept(builder);
        });
    }

    protected InstantAbility(String id) {
        this(id, builder -> {
        });
    }
}
