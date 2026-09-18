package com.binaris.oneslib.common.network;

import java.util.function.Supplier;

import com.binaris.oneslib.server.ability.AbilityEngine;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class ActivateAbilityPacket {

    private final String abilityId;
    private final boolean fromItem;

    public ActivateAbilityPacket(String abilityId, boolean fromItem) {
        this.abilityId = abilityId;
        this.fromItem = fromItem;
    }

    public ActivateAbilityPacket(FriendlyByteBuf buf) {
        this.abilityId = buf.readUtf();
        this.fromItem = buf.readBoolean();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(this.abilityId);
        buf.writeBoolean(this.fromItem);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                AbilityEngine.INSTANCE.activate(player, this.abilityId, this.fromItem);
            }
        });
        context.setPacketHandled(true);
    }
}
