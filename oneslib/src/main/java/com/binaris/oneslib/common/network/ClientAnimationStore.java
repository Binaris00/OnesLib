package com.binaris.oneslib.common.network;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import com.binaris.oneslib.common.state.OneAnimation;

/**
 * Client-side cache of the animation state received through {@code StateChannel.PACKET},
 * keyed by the morphed player's UUID. The shape the client renders is a deserialized copy with
 * no server-side entity behind it, so the state has to be addressed by its owner.
 */
public final class ClientAnimationStore {

    private static final Map<UUID, OneAnimation> ANIMATIONS = new ConcurrentHashMap<>();

    private ClientAnimationStore() {
    }

    public static void accept(UUID owner, @Nullable OneAnimation animation) {
        if (animation == null) {
            ANIMATIONS.remove(owner);
        } else {
            ANIMATIONS.put(owner, animation);
        }
    }

    @Nullable
    public static OneAnimation get(UUID owner) {
        return ANIMATIONS.get(owner);
    }

    public static void clear() {
        ANIMATIONS.clear();
    }
}
