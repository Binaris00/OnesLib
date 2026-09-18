package com.binaris.oneslib.test.gametest;

import com.binaris.oneslib.api.OnesApi;
import com.binaris.oneslib.common.state.OneAnimation;
import com.binaris.oneslib.common.state.OneState;
import com.binaris.oneslib.test.TestMod;
import com.binaris.oneslib.test.util.TestPlayers;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TestMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class TargetingGameTest {

    private TargetingGameTest() {
    }

    @GameTest(template = "empty")
    public static void area_followsPlayer(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.spawn(helper);
        BlockPos morphPos = helper.absolutePos(new BlockPos(0, 2, 0));
        player.moveTo(morphPos.getX() + 0.5D, morphPos.getY(), morphPos.getZ() + 0.5D, 0.0F, 0.0F);

        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(4, 2, 4));

        OnesApi.morph(player, "evil_hulk");

        BlockPos movedPos = helper.absolutePos(new BlockPos(3, 2, 3));
        player.moveTo(movedPos.getX() + 0.5D, movedPos.getY(), movedPos.getZ() + 0.5D, 0.0F, 0.0F);

        helper.assertTrue(OnesApi.activate(player, "evil_hulk_golpe"), "ability should activate");
        for (int i = 0; i < 10; i++) {
            OnesApi.tick(player);
        }

        helper.assertTrue(zombie.getHealth() < zombie.getMaxHealth(),
                "area damage should follow the player, zombie has " + zombie.getHealth());
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void animation_token_replaysAfterEnd(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.spawn(helper);
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 2));
        player.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0F, 0.0F);

        OnesApi.morph(player, "siren_head");
        helper.assertTrue(OnesApi.activate(player, "siren_head_laser"), "first activation");
        for (int i = 0; i < 40; i++) {
            OnesApi.tick(player);
        }
        OneAnimation first = OneState.read(player);
        helper.assertTrue(first != null && "beam".equals(first.name()), "first beam animation");

        for (int i = 0; i < 50; i++) {
            OnesApi.tick(player);
        }
        helper.assertTrue(!OnesApi.isActive(player, "siren_head_laser"), "ability should finish");

        OnesApi.clearCooldown(player, "siren_head_laser");
        helper.assertTrue(OnesApi.activate(player, "siren_head_laser"), "second activation");
        for (int i = 0; i < 40; i++) {
            OnesApi.tick(player);
        }
        OneAnimation second = OneState.read(player);
        helper.assertTrue(second != null && "beam".equals(second.name()), "second beam animation");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void raycast_followsPlayerRotation(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.spawn(helper);
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 2));
        player.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0F, 0.0F);

        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(4, 2, 2));

        OnesApi.morph(player, "siren_head");

        player.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, -90.0F, 15.0F);

        helper.assertTrue(OnesApi.activate(player, "siren_head_laser"), "ability should activate");
        for (int i = 0; i < 40; i++) {
            OnesApi.tick(player);
        }

        OneAnimation animation = OneState.read(player);
        helper.assertTrue(animation != null && "beam".equals(animation.name()),
                "beam animation should be active at tick 40");
        helper.assertTrue(zombie.getHealth() < zombie.getMaxHealth(),
                "raycast should use the player rotation, zombie has " + zombie.getHealth());
        helper.succeed();
    }
}
