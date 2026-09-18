package com.binaris.oneslib.test.gametest;

import com.binaris.oneslib.api.OnesApi;
import com.binaris.oneslib.common.registry.OnesItems;
import com.binaris.oneslib.test.TestMod;
import com.binaris.oneslib.test.util.TestPlayers;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TestMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class AbilityGameTest {

    private AbilityGameTest() {
    }

    @GameTest(template = "empty")
    public static void golpe_damagesAtTick10(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.spawn(helper);
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        player.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0F, 0.0F);

        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(3, 2, 3));

        OnesApi.morph(player, "evil_hulk");
        helper.assertTrue(OnesApi.activate(player, "evil_hulk_golpe"), "ability should activate");

        for (int i = 0; i < 10; i++) {
            OnesApi.tick(player);
        }

        helper.assertTrue(zombie.getHealth() < zombie.getMaxHealth(),
                "zombie should take damage but has " + zombie.getHealth());
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void lifetime_startsCooldownAndBlocks(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.spawn(helper);
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        player.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0F, 0.0F);

        OnesApi.morph(player, "evil_hulk");
        helper.assertTrue(OnesApi.activate(player, "evil_hulk_grito"), "ability should activate");

        for (int i = 0; i < 60; i++) {
            OnesApi.tick(player);
        }

        helper.assertTrue(!OnesApi.isActive(player, "evil_hulk_grito"), "ability should finish");
        helper.assertTrue(OnesApi.isOnCooldown(player, "evil_hulk_grito"), "ability should be on cooldown");
        helper.assertTrue(!OnesApi.activate(player, "evil_hulk_grito"), "activation should be blocked");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void toggle_grantsAndRemovesFlight(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.spawn(helper);
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        player.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0F, 0.0F);

        OnesApi.morph(player, "tails");
        helper.assertTrue(OnesApi.activate(player, "tails_fly"), "toggle should activate");
        helper.assertTrue(player.getAbilities().mayfly, "flight should be granted");

        OnesApi.activate(player, "tails_fly");

        helper.assertTrue(!player.getAbilities().mayfly, "flight should be removed");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void demorph_cancelsAbilities(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.spawn(helper);
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        player.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0F, 0.0F);

        OnesApi.morph(player, "evil_hulk");
        helper.assertTrue(OnesApi.activate(player, "evil_hulk_grito"), "ability should activate");
        helper.assertTrue(OnesApi.isActive(player, "evil_hulk_grito"), "ability should be active");

        OnesApi.demorph(player);

        helper.assertTrue(!OnesApi.isActive(player, "evil_hulk_grito"), "ability should be cancelled");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void stateChannel_tokenWrittenAndCleared(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.spawn(helper);
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        player.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0F, 0.0F);

        OnesApi.morph(player, "evil_hulk");
        helper.assertTrue(OnesApi.activate(player, "evil_hulk_grito"), "ability should activate");

        helper.assertTrue(player.getItemBySlot(EquipmentSlot.FEET).is(OnesItems.STATE_TOKEN.get()),
                "state token should be in the player feet slot");

        for (int i = 0; i < 60; i++) {
            OnesApi.tick(player);
        }

        helper.assertTrue(player.getItemBySlot(EquipmentSlot.FEET).isEmpty(), "state token should be cleared");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void stateChannel_preservesAndRestoresItem(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.spawn(helper);
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        player.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0F, 0.0F);
        player.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.DIAMOND_BOOTS));

        OnesApi.morph(player, "evil_hulk");
        helper.assertTrue(OnesApi.activate(player, "evil_hulk_grito"), "ability should activate");
        helper.assertTrue(player.getItemBySlot(EquipmentSlot.FEET).is(OnesItems.STATE_TOKEN.get()),
                "state token should occupy the feet slot");

        OnesApi.demorph(player);
        helper.assertTrue(player.getItemBySlot(EquipmentSlot.FEET).is(Items.DIAMOND_BOOTS),
                "previous item should be restored on demorph");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void throwAbility_fliesAndEnds(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.spawn(helper);
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        player.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0F, -60.0F);

        OnesApi.morph(player, "dragon");
        helper.assertTrue(OnesApi.activate(player, "dragon_fireball"), "throw ability should activate");

        for (int i = 0; i < 5; i++) {
            OnesApi.tick(player);
        }
        helper.assertTrue(OnesApi.isActive(player, "dragon_fireball"), "projectile should still be flying");

        for (int i = 0; i < 120; i++) {
            OnesApi.tick(player);
        }
        helper.assertTrue(!OnesApi.isActive(player, "dragon_fireball"), "throw ability should end");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void foreignOne_morphsAndActivates(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.spawn(helper);
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        player.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0F, 0.0F);

        OnesApi.morph(player, "foreign");

        helper.assertTrue(OnesApi.isMorphedAs(player, "foreign"), "foreign One should morph");
        helper.assertTrue(OnesApi.activate(player, "foreign_punch"), "foreign ability should activate");
        helper.succeed();
    }
}
