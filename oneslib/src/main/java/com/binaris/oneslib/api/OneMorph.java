package com.binaris.oneslib.api;

import java.util.UUID;

import javax.annotation.Nullable;

import com.binaris.oneslib.common.state.OneAnimation;

public interface OneMorph {

    @Nullable
    UUID oneOwner();

    void oneOwner(@Nullable UUID owner);

    default @Nullable OneAnimation oneAnimation() {
        return null;
    }

    default void oneAnimation(@Nullable OneAnimation animation) {
    }
}
