package com.binaris.oneslib.test.gametest;

import com.binaris.oneslib.api.OnesApi;
import com.binaris.oneslib.test.TestMod;
import com.binaris.oneslib.test.util.TestPlayers;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TestMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class MorphGameTest {

    private MorphGameTest() {
    }

    @GameTest(template = "empty")
    public static void morph_appliesAttributesAndEffects(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.spawn(helper);
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        player.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0F, 0.0F);

        OnesApi.morph(player, "evil_hulk");

        helper.assertTrue(OnesApi.isMorphedAs(player, "evil_hulk"), "player should be morphed as evil_hulk");
        helper.assertTrue(player.getMaxHealth() == 120.0F, "max health should be 120 but was " + player.getMaxHealth());
        helper.assertTrue(Math.abs(player.getAttributeValue(Attributes.MOVEMENT_SPEED) - 0.24D) < 0.001D,
                "movement speed should be 0.24");

        OnesApi.demorph(player);

        helper.assertTrue(!OnesApi.isMorphedAs(player, "evil_hulk"), "player should be demorphed");
        helper.assertTrue(player.getMaxHealth() == 20.0F,
                "max health should be restored but was " + player.getMaxHealth());
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void morph_appliesPassive(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.spawn(helper);
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        player.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0F, 0.0F);

        OnesApi.morph(player, "tails");

        helper.assertTrue(player.hasEffect(MobEffects.DAMAGE_BOOST), "passive should apply damage boost");
        helper.assertTrue(OnesApi.isActive(player, "tails_passive"), "passive ability should be active");

        OnesApi.demorph(player);

        helper.assertTrue(!player.hasEffect(MobEffects.DAMAGE_BOOST), "passive effects should be removed");
        helper.succeed();
    }
}
