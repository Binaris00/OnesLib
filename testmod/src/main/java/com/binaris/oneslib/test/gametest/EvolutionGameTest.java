package com.binaris.oneslib.test.gametest;

import com.binaris.oneslib.api.OnesApi;
import com.binaris.oneslib.server.progress.Evolution;
import com.binaris.oneslib.server.progress.EvolutionBuilder;
import com.binaris.oneslib.server.progress.EvolutionData;
import com.binaris.oneslib.server.progress.EvolutionManager;
import com.binaris.oneslib.server.progress.RespawnPolicy;
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
public final class EvolutionGameTest {

    private EvolutionGameTest() {
    }

    @GameTest(template = "empty")
    public static void evolution_stagesAdvanceInOrder(GameTestHelper helper) {
        Evolution evolution = EvolutionBuilder.create("test_line")
                .stage("imp")
                .stage("evil_hulk")
                .stage("dragon")
                .segments(2)
                .trackUsage()
                .respawnPolicy(RespawnPolicy.KEEP_STAGE)
                .build();

        ServerPlayer player = TestPlayers.spawn(helper);
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        player.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0F, 0.0F);

        EvolutionManager.INSTANCE.register(evolution);
        EvolutionManager.INSTANCE.join(player, evolution);

        helper.assertTrue(OnesApi.isMorphedAs(player, "imp"), "joining should morph into the first stage");
        helper.assertTrue(EvolutionManager.INSTANCE.stageIndex(player) == 0, "the first stage is index 0");

        helper.assertTrue(EvolutionManager.INSTANCE.advance(player), "the first advance should work");
        helper.assertTrue(OnesApi.isMorphedAs(player, "evil_hulk"),
                "advancing should morph into the next stage");
        helper.assertTrue(EvolutionManager.INSTANCE.stageIndex(player) == 1, "the stage index should be 1");

        helper.assertTrue(EvolutionManager.INSTANCE.advance(player), "the second advance should work");
        helper.assertTrue(OnesApi.isMorphedAs(player, "dragon"), "the third stage should be dragon");

        helper.assertTrue(!EvolutionManager.INSTANCE.advance(player),
                "advancing past the last stage should fail");
        helper.succeed();
    }

    /**
     * The ability list is derived from One.abilities(), never hand-kept, so a new ability on a
     * stage cannot silently break the bar.
     */
    @GameTest(template = "empty")
    public static void evolution_tracksAbilityUsage(GameTestHelper helper) {
        Evolution evolution = EvolutionBuilder.create("usage_line")
                .stage("imp")
                .stage("evil_hulk")
                .segments(2)
                .trackUsage()
                .build();

        ServerPlayer player = TestPlayers.spawn(helper);
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        player.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0F, 0.0F);

        EvolutionManager.INSTANCE.register(evolution);
        EvolutionManager.INSTANCE.join(player, evolution);

        int before = EvolutionData.get(player).usedCount(player.getUUID());
        helper.assertTrue(before == 0, "a fresh stage should have no recorded usage");

        OnesApi.activate(player, "imp_attack");
        int after = EvolutionData.get(player).usedCount(player.getUUID());
        helper.assertTrue(after == 1, "using an ability should be recorded but the count was " + after);

        // Passive abilities are auto-activated by the morph and must not count.
        OnesApi.morph(player, "tails");
        helper.assertTrue(EvolutionData.get(player).usedCount(player.getUUID()) == 1,
                "passive abilities should not count toward progress");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void evolution_respawnPolicyIsHonoured(GameTestHelper helper) {
        Evolution keep = EvolutionBuilder.create("keep_line")
                .stage("immortal")
                .stage("dragon")
                .segments(1)
                .respawnPolicy(RespawnPolicy.KEEP_STAGE)
                .build();
        Evolution reset = EvolutionBuilder.create("reset_line")
                .stage("immortal")
                .stage("dragon")
                .segments(1)
                .respawnPolicy(RespawnPolicy.RESET)
                .build();

        ServerPlayer player = TestPlayers.spawn(helper);
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        player.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0F, 0.0F);

        EvolutionManager.INSTANCE.register(keep);
        EvolutionManager.INSTANCE.join(player, keep);
        EvolutionManager.INSTANCE.onRespawn(player);
        helper.assertTrue(OnesApi.isMorphedAs(player, "immortal"),
                "KEEP_STAGE should keep the stage across a respawn");

        EvolutionManager.INSTANCE.register(reset);
        EvolutionManager.INSTANCE.join(player, reset);
        EvolutionManager.INSTANCE.onRespawn(player);
        helper.assertTrue(!OnesApi.isMorphedAs(player, "immortal"),
                "RESET should drop the morph on respawn");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void evolution_progressIsClamped(GameTestHelper helper) {
        Evolution evolution = EvolutionBuilder.create("clamp_line")
                .stage("imp")
                .stage("dragon")
                .segments(3)
                .build();

        helper.assertTrue(evolution.progress(0) == 0.0F, "no progress should be 0");
        helper.assertTrue(Math.abs(evolution.progress(2) - 2.0F / 3.0F) < 0.0001F, "progress should be a ratio");
        helper.assertTrue(evolution.progress(99) == 1.0F, "progress must never exceed 1");
        helper.assertTrue(evolution.nextStage("dragon") == null, "the last stage has no next stage");
        helper.assertTrue(evolution.isLast("dragon"), "dragon should be the last stage");
        helper.succeed();
    }
}
