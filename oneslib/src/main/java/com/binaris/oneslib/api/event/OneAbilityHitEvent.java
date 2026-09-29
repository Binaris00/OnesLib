package com.binaris.oneslib.api.event;

import com.binaris.oneslib.api.OneAbility;
import com.binaris.oneslib.api.OneShotAnimation;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.Event;

/**
 * Fired on the server at the hit tick of a {@link OneShotAnimation} timeline, so the damage of
 * an attack is attached to its declared hit phase instead of to a tick literal inside
 * {@code onTick}.
 */
public class OneAbilityHitEvent extends Event {

    private final ServerPlayer player;
    private final OneAbility ability;
    private final OneShotAnimation oneShot;

    public OneAbilityHitEvent(ServerPlayer player, OneAbility ability, OneShotAnimation oneShot) {
        this.player = player;
        this.ability = ability;
        this.oneShot = oneShot;
    }

    public ServerPlayer player() {
        return this.player;
    }

    public OneAbility ability() {
        return this.ability;
    }

    /** The timeline this hit belongs to, with its init / hit / recover boundaries. */
    public OneShotAnimation oneShot() {
        return this.oneShot;
    }
}
