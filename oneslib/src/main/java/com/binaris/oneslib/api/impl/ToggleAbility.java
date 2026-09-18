package com.binaris.oneslib.api.impl;

import java.util.function.Consumer;

import com.binaris.oneslib.api.AbilitySettings;
import com.binaris.oneslib.api.OneAbility;

public abstract class ToggleAbility extends OneAbility {

    protected ToggleAbility(String id, Consumer<AbilitySettings.Builder> consumer) {
        super(id, builder -> {
            builder.duration(-1);
            consumer.accept(builder);
        });
    }

    protected ToggleAbility(String id) {
        this(id, builder -> {
        });
    }

    @Override
    public final boolean isToggle() {
        return true;
    }
}
