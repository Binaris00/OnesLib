package com.binaris.oneslib.test.ability;

import com.binaris.oneslib.api.AbilityContext;
import com.binaris.oneslib.api.EndReason;
import com.binaris.oneslib.api.impl.ToggleAbility;

import org.lwjgl.glfw.GLFW;
import software.bernie.geckolib.core.animation.Animation;

public final class TailsFly extends ToggleAbility {

    public TailsFly() {
        super("tails_fly", settings -> settings.name("Tails Fly").keybind(GLFW.GLFW_KEY_R));
    }

    @Override
    public void onStart(AbilityContext context) {
        context.fly(true);
        context.animate("fly", Animation.LoopType.LOOP);
    }

    @Override
    public void onEnd(AbilityContext context, EndReason reason) {
        context.fly(false);
        context.stopAnimation();
    }
}
