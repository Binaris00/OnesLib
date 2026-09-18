package com.binaris.oneslib.common.state;

import software.bernie.geckolib.core.animation.Animation;
import software.bernie.geckolib.core.animation.RawAnimation;

public record OneAnimation(String name, Animation.LoopType loopType, int durationTicks) {

    public static OneAnimation playOnce(String name, int durationTicks) {
        return new OneAnimation(name, Animation.LoopType.PLAY_ONCE, durationTicks);
    }

    public static OneAnimation playAndHold(String name, int durationTicks) {
        return new OneAnimation(name, Animation.LoopType.HOLD_ON_LAST_FRAME, durationTicks);
    }

    public static OneAnimation loop(String name) {
        return new OneAnimation(name, Animation.LoopType.LOOP, -1);
    }

    public boolean loops() {
        return this.loopType == Animation.LoopType.LOOP;
    }

    public String loopId() {
        if (this.loopType == Animation.LoopType.LOOP) {
            return "loop";
        }
        if (this.loopType == Animation.LoopType.HOLD_ON_LAST_FRAME) {
            return "hold_on_last_frame";
        }
        return "play_once";
    }

    public static Animation.LoopType loopTypeFromId(String id) {
        return Animation.LoopType.fromString(id);
    }

    public RawAnimation toRawAnimation() {
        return RawAnimation.begin().then(this.name, this.loopType);
    }
}
