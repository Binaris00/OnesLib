package com.binaris.oneslib.common.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import software.bernie.geckolib.core.animation.RawAnimation;

class OneAnimationTest {

    @Test
    void playOnceKeepsValues() {
        OneAnimation animation = OneAnimation.playOnce("attack", 20);

        assertEquals("attack", animation.name());
        assertEquals("play_once", animation.loopId());
        assertEquals(20, animation.durationTicks());
        assertFalse(animation.loops());
    }

    @Test
    void loopHasNoDuration() {
        OneAnimation animation = OneAnimation.loop("fly");

        assertEquals(-1, animation.durationTicks());
        assertEquals("loop", animation.loopId());
    }

    @Test
    void loopIdRoundTrips() {
        for (String id : new String[]{"play_once", "loop", "hold_on_last_frame"}) {
            OneAnimation animation = new OneAnimation("a", OneAnimation.loopTypeFromId(id), 0);
            assertEquals(id, animation.loopId());
        }
    }

    @Test
    void rawAnimationIsCreated() {
        RawAnimation raw = OneAnimation.loop("idle").toRawAnimation();
        assertNotNull(raw);
    }
}
