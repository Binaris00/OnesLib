package com.binaris.oneslib.common.network;

import java.util.UUID;
import java.util.function.Supplier;

import com.binaris.oneslib.client.ClientOneSkinManager;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

/**
 * Server to client transport for skin-change Ones. Tells every client which player is currently
 * skinned as a skin One (and which One), so the player's render uses the One's skin instead of
 * their own. An empty {@code oneId} clears the skin.
 */
public final class SyncSkinOnePacket {

    private final UUID owner;
    private final String oneId;

    public SyncSkinOnePacket(UUID owner, String oneId) {
        this.owner = owner;
        this.oneId = oneId == null ? "" : oneId;
    }

    public SyncSkinOnePacket(FriendlyByteBuf buf) {
        this.owner = buf.readUUID();
        this.oneId = buf.readUtf();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(this.owner);
        buf.writeUtf(this.oneId);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientOneSkinManager.update(this.owner, this.oneId));
        ctx.get().setPacketHandled(true);
    }
}