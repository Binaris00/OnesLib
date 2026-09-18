package com.binaris.oneslib.test.gametest;

import com.binaris.oneslib.api.OnesApi;
import com.binaris.oneslib.test.TestMod;
import com.binaris.oneslib.test.util.TestPlayers;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TestMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class FlightGameTest {

    private FlightGameTest() {
    }

    @GameTest(template = "empty")
    public static void flight_revokedAfterDemorph(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.spawn(helper);
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        player.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0F, 0.0F);

        OnesApi.morph(player, "dragon");
        helper.assertTrue(player.getAbilities().mayfly, "flying One should grant flight");

        OnesApi.demorph(player);
        helper.assertTrue(!player.getAbilities().mayfly, "flight should be revoked after demorph");
        helper.assertTrue(!player.getAbilities().flying, "flying flag should be cleared after demorph");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void flight_revokedOnNonFlyingMorph(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.spawn(helper);
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        player.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0F, 0.0F);

        OnesApi.morph(player, "dragon");
        helper.assertTrue(player.getAbilities().mayfly, "flying One should grant flight");

        OnesApi.morph(player, "siren_head");
        helper.assertTrue(!player.getAbilities().mayfly, "non-flying One should revoke flight");
        helper.assertTrue(!player.getAbilities().flying, "flying flag should be cleared");

        OnesApi.demorph(player);
        helper.assertTrue(!player.getAbilities().mayfly, "flight should stay revoked after demorph");
        helper.succeed();
    }
}
