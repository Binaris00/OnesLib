package com.binaris.oneslib.common.network;

import java.util.UUID;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.binaris.oneslib.common.state.OneAnimation;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

/**
 * Server to client transport for a morphed player's animation state, used by
 * {@code StateChannel.PACKET}. The owner is the morphed player, not the shape: the shape is
 * never added to the world, so entity data of the shape is never broadcast.
 */
public class SyncAnimationPacket {

    private final UUID owner;
    private final String name;
    private final String loopId;
    private final int durationTicks;

    public SyncAnimationPacket(UUID owner, @Nullable OneAnimation animation) {
        this.owner = owner;
        this.name = animation == null ? "" : animation.name();
        this.loopId = animation == null ? "" : animation.loopId();
        this.durationTicks = animation == null ? 0 : animation.durationTicks();
    }

    public SyncAnimationPacket(FriendlyByteBuf buf) {
        this.owner = buf.readUUID();
        this.name = buf.readUtf();
        this.loopId = buf.readUtf();
        this.durationTicks = buf.readVarInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(this.owner);
        buf.writeUtf(this.name);
        buf.writeUtf(this.loopId);
        buf.writeVarInt(this.durationTicks);
    }

    @Nullable
    public OneAnimation animation() {
        if (this.name.isEmpty()) {
            return null;
        }
        return new OneAnimation(this.name, OneAnimation.loopTypeFromId(this.loopId), this.durationTicks);
    }

    public UUID owner() {
        return this.owner;
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> ClientAnimationStore.accept(this.owner, this.animation()));
        context.setPacketHandled(true);
    }
}
