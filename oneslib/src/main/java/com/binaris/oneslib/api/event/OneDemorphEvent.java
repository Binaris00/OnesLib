package com.binaris.oneslib.api.event;

import com.binaris.oneslib.api.One;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.Event;

public class OneDemorphEvent extends Event {

    private final ServerPlayer player;
    private final One previousOne;

    public OneDemorphEvent(ServerPlayer player, One previousOne) {
        this.player = player;
        this.previousOne = previousOne;
    }

    public ServerPlayer player() {
        return this.player;
    }

    public One previousOne() {
        return this.previousOne;
    }
}
