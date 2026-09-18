package com.binaris.oneslib.common.anim;

import com.binaris.oneslib.common.state.OneAnimation;
import com.binaris.oneslib.common.state.OneState;

import net.minecraft.world.entity.LivingEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;

public final class OneAnimationController<T extends LivingEntity & GeoAnimatable> extends AnimationController<T> {

    public OneAnimationController(T entity) {
        super(entity, "ones_action", 0, state -> handle(state));
    }

    private static <T extends LivingEntity & GeoAnimatable> PlayState handle(AnimationState<T> state) {
        OneAnimation animation = OneState.read(state.getAnimatable());
        if (animation == null) {
            state.getController().forceAnimationReset();
            return PlayState.STOP;
        }
        return state.setAndContinue(animation.toRawAnimation());
    }
}
