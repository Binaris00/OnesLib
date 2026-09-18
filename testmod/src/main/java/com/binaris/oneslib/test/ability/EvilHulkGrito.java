package com.binaris.oneslib.test.ability;

import com.binaris.oneslib.api.AbilityContext;
import com.binaris.oneslib.api.EndReason;
import com.binaris.oneslib.api.impl.LifetimeAbility;

import org.lwjgl.glfw.GLFW;
import software.bernie.geckolib.core.animation.Animation;

public final class EvilHulkGrito extends LifetimeAbility {

    public EvilHulkGrito() {
        super("evil_hulk_grito", 60, settings -> settings
                .name("Grito")
                .cooldown(100)
                .keybind(GLFW.GLFW_KEY_H));
    }

    @Override
    public void onStart(AbilityContext context) {
        context.animate("attack2", Animation.LoopType.LOOP);
    }

    @Override
    public void onEnd(AbilityContext context, EndReason reason) {
        context.stopAnimation();
    }
}
