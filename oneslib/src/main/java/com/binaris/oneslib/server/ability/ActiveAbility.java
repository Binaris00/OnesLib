package com.binaris.oneslib.server.ability;

import com.binaris.oneslib.api.AbilityContext;
import com.binaris.oneslib.api.One;
import com.binaris.oneslib.api.OneAbility;

public final class ActiveAbility {

    private final One one;
    private final OneAbility ability;
    private final AbilityContext context;

    public ActiveAbility(One one, OneAbility ability, AbilityContext context) {
        this.one = one;
        this.ability = ability;
        this.context = context;
    }

    public One one() {
        return this.one;
    }

    public OneAbility ability() {
        return this.ability;
    }

    public AbilityContext context() {
        return this.context;
    }
}
