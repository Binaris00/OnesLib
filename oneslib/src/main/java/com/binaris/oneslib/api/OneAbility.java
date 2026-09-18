package com.binaris.oneslib.api;

import java.util.function.Consumer;

public abstract class OneAbility {

    private final String id;
    private final AbilitySettings settings;

    protected OneAbility(String id, Consumer<AbilitySettings.Builder> consumer) {
        AbilitySettings.Builder builder = AbilitySettings.builder(id);
        consumer.accept(builder);
        this.settings = builder.build();
        this.id = id;
    }

    public final String id() {
        return this.id;
    }

    public final AbilitySettings settings() {
        return this.settings;
    }

    public boolean isPassive() {
        return false;
    }

    public boolean isToggle() {
        return false;
    }

    public boolean canActivate(AbilityContext context) {
        return true;
    }

    public void onStart(AbilityContext context) {
    }

    public void onTick(AbilityContext context) {
    }

    public void onEnd(AbilityContext context, EndReason reason) {
    }
}
