package com.binaris.oneslib.api.event;

import com.binaris.oneslib.api.One;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.Event;

public class OneMorphEvent extends Event {

    private final ServerPlayer player;
    private final One one;

    public OneMorphEvent(ServerPlayer player, One one) {
        this.player = player;
        this.one = one;
    }

    public ServerPlayer player() {
        return this.player;
    }

    public One one() {
        return this.one;
    }
}
