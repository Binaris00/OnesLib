package com.binaris.oneslib.common.network;

import com.binaris.oneslib.Ones;

import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class OnesNetwork {

    private static final String PROTOCOL_VERSION = "2";

    private static SimpleChannel channel;
    private static int packetId;

    private OnesNetwork() {
    }

    public static void register() {
        SimpleChannel simpleChannel = NetworkRegistry.ChannelBuilder
                .named(Ones.id("main"))
                .networkProtocolVersion(() -> PROTOCOL_VERSION)
                .clientAcceptedVersions(PROTOCOL_VERSION::equals)
                .serverAcceptedVersions(PROTOCOL_VERSION::equals)
                .simpleChannel();

        simpleChannel.messageBuilder(ActivateAbilityPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ActivateAbilityPacket::encode)
                .decoder(ActivateAbilityPacket::new)
                .consumerMainThread(ActivateAbilityPacket::handle)
                .add();

        simpleChannel.messageBuilder(SyncAnimationPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncAnimationPacket::encode)
                .decoder(SyncAnimationPacket::new)
                .consumerMainThread(SyncAnimationPacket::handle)
                .add();

        simpleChannel.messageBuilder(SyncSkinOnePacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncSkinOnePacket::encode)
                .decoder(SyncSkinOnePacket::new)
                .consumerMainThread(SyncSkinOnePacket::handle)
                .add();

        channel = simpleChannel;
    }

    public static void sendToServer(Object message) {
        channel.sendToServer(message);
    }

    public static void sendTo(Object message, PacketDistributor.PacketTarget target) {
        channel.send(target, message);
    }
}
