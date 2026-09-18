package com.binaris.oneslib.common.network;

import com.binaris.oneslib.Ones;

import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class OnesNetwork {

    private static final String PROTOCOL_VERSION = "1";

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

        channel = simpleChannel;
    }

    public static void sendToServer(Object message) {
        channel.sendToServer(message);
    }
}
