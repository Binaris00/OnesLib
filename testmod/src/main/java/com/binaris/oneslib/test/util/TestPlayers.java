package com.binaris.oneslib.test.util;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import io.netty.channel.embedded.EmbeddedChannel;

public final class TestPlayers {

    private TestPlayers() {
    }

    public static ServerPlayer spawn(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        MinecraftServer server = level.getServer();

        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        EmbeddedChannel channel = new EmbeddedChannel();
        ObfuscationReflectionHelper.setPrivateValue(Connection.class, connection, channel, "channel");

        GameProfile profile = new GameProfile(UUID.randomUUID(), "oneslib_test_player");
        ServerPlayer player = new ServerPlayer(server, level, profile);
        server.getPlayerList().placeNewPlayer(connection, player);
        return player;
    }
}
