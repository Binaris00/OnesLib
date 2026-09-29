package com.binaris.oneslib.test.ability;

import com.binaris.oneslib.api.AbilityContext;
import com.binaris.oneslib.api.impl.InstantAbility;

import org.lwjgl.glfw.GLFW;

public final class ImpAttack extends InstantAbility {

    public ImpAttack() {
        this("imp_attack");
    }

    public ImpAttack(String id) {
        super(id, settings -> settings
                .name("Imp Attack")
                .cooldown(30)
                .keybind(GLFW.GLFW_KEY_M));
    }

    @Override
    public void onStart(AbilityContext context) {
        context.animate("attack");
    }
}
