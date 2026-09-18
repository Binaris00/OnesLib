package com.binaris.oneslib.api.impl;

import java.util.function.Consumer;

import com.binaris.oneslib.api.AbilitySettings;
import com.binaris.oneslib.api.OneAbility;

public abstract class PassiveAbility extends OneAbility {

    protected PassiveAbility(String id, Consumer<AbilitySettings.Builder> consumer) {
        super(id, consumer);
    }

    @Override
    public final boolean isPassive() {
        return true;
    }
}
