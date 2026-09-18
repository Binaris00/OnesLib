package com.binaris.oneslib.test.ability;

import com.binaris.oneslib.api.AbilityContext;
import com.binaris.oneslib.api.EndReason;
import com.binaris.oneslib.api.impl.LifetimeAbility;

import org.lwjgl.glfw.GLFW;

public final class GiantOgreGrab extends LifetimeAbility {

    public GiantOgreGrab() {
        super("giant_ogre_grab", 40, settings -> settings
                .name("Grab and Bite")
                .cooldown(100)
                .keybind(GLFW.GLFW_KEY_N));
    }

    @Override
    public void onStart(AbilityContext context) {
        context.animate("grab and bite");
    }

    @Override
    public void onTick(AbilityContext context) {
        if (context.tick() == 20) {
            context.area(3.0D).damage(12.0F).knockback(1.0D).hit();
        }
    }

    @Override
    public void onEnd(AbilityContext context, EndReason reason) {
        context.stopAnimation();
    }
}
