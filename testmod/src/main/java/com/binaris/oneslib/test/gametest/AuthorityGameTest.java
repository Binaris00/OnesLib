package com.binaris.oneslib.test.gametest;

import com.binaris.oneslib.api.OnesApi;
import com.binaris.oneslib.common.registry.OnesItems;
import com.binaris.oneslib.common.state.OneAnimation;
import com.binaris.oneslib.common.state.OneState;
import com.binaris.oneslib.server.morph.ServerOneManager;
import com.binaris.oneslib.test.TestMod;
import com.binaris.oneslib.test.util.TestPlayers;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import software.bernie.geckolib.core.animation.Animation;

import tocraft.walkers.api.PlayerShape;
import tocraft.walkers.impl.PlayerDataProvider;

@GameTestHolder(TestMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class AuthorityGameTest {

    private AuthorityGameTest() {
    }

    /**
     * The library is the authority: losing the walkers shape must not demorph the player, it
     * must re-apply the shape it already owns. This is what used to force consumers to keep a
     * parallel UUID set just so fall-damage immunity survived a desync.
     */
    @GameTest(template = "empty")
    public static void desyncedShape_doesNotDemorph(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.spawn(helper);
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        player.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0F, 0.0F);

        OnesApi.morph(player, "siren_head");
        helper.assertTrue(OnesApi.isMorphedAs(player, "siren_head"), "player should start morphed");

        // Simulate another mod dropping the shape: the library must keep its own answer.
        ((PlayerDataProvider) player).walkers$setCurrentShape(null);
        ServerOneManager.INSTANCE.tick(player);

        helper.assertTrue(OnesApi.isMorphedAs(player, "siren_head"),
                "a desynced shape must not demorph the player");
        helper.assertTrue(PlayerShape.getCurrentShape(player) != null,
                "the library should re-apply its own shape");
        helper.assertTrue(player.getMaxHealth() == 100.0F,
                "attributes should survive the desync but max health was " + player.getMaxHealth());

        OnesApi.demorph(player);
        helper.assertTrue(!OnesApi.isMorphedAs(player, "siren_head"), "player should be demorphed");
        helper.succeed();
    }

    /** An explicit demorph still clears the library's state, shape loss notwithstanding. */
    @GameTest(template = "empty")
    public static void demorph_clearsStateEvenWithLostShape(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.spawn(helper);
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        player.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0F, 0.0F);

        OnesApi.morph(player, "imp");
        ((PlayerDataProvider) player).walkers$setCurrentShape(null);

        OnesApi.demorph(player);

        helper.assertTrue(!OnesApi.isMorphedAs(player, "imp"), "player should be demorphed");
        helper.assertTrue(PlayerShape.getCurrentShape(player) == null, "the shape should be cleared");

        ServerOneManager.INSTANCE.tick(player);

        helper.assertTrue(!OnesApi.isMorphedAs(player, "imp"),
                "ticking after a demorph must not resurrect the morph");
        helper.succeed();
    }

    /**
     * The PACKET channel must not touch the player's equipment. The item hack is the default
     * channel precisely because it is visible and lossy; PACKET exists to avoid it.
     */
    @GameTest(template = "empty")
    public static void packetChannel_doesNotUseTheStateToken(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.spawn(helper);
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        player.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0F, 0.0F);

        OnesApi.morph(player, "imp_packet");
        helper.assertTrue(OnesApi.isMorphedAs(player, "imp_packet"), "player should be morphed as imp_packet");

        LivingEntity shape = PlayerShape.getCurrentShape(player);
        helper.assertTrue(shape != null, "the shape should exist");
        OneState.write(shape, new OneAnimation("attack", Animation.LoopType.PLAY_ONCE, 20));

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            helper.assertTrue(!player.getItemBySlot(slot).is(OnesItems.STATE_TOKEN.get()),
                    "the PACKET channel must not put a state token in " + slot);
        }

        OnesApi.demorph(player);
        helper.succeed();
    }

    /** The item hack, by contrast, does put the token on the player and restores the old item. */
    @GameTest(template = "empty")
    public static void itemSlotChannel_doesUseTheStateToken(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.spawn(helper);
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        player.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0F, 0.0F);

        OnesApi.morph(player, "imp");

        LivingEntity shape = PlayerShape.getCurrentShape(player);
        helper.assertTrue(shape != null, "the shape should exist");
        OneState.write(shape, new OneAnimation("attack", Animation.LoopType.PLAY_ONCE, 20));

        helper.assertTrue(player.getItemBySlot(EquipmentSlot.FEET).is(OnesItems.STATE_TOKEN.get()),
                "the ITEM_SLOT channel should put a state token on the player");

        OnesApi.demorph(player);
        helper.assertTrue(!player.getItemBySlot(EquipmentSlot.FEET).is(OnesItems.STATE_TOKEN.get()),
                "the previous item should be restored on demorph");
        helper.succeed();
    }

    /** resetHealthOnMorph defaults to true, so morphing refills to the new maximum. */
    @GameTest(template = "empty")
    public static void morph_refillsHealthByDefault(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.spawn(helper);
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        player.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0F, 0.0F);

        player.setHealth(4.0F);
        OnesApi.morph(player, "evil_hulk");

        helper.assertTrue(player.getHealth() == 120.0F,
                "morph should refill health to max but was " + player.getHealth());
        helper.succeed();
    }

    /** A finite durationTicks must not become infinite. */
    @GameTest(template = "empty")
    public static void morph_honorsFiniteEffectDuration(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.spawn(helper);
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        player.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0F, 0.0F);

        player.removeEffect(MobEffects.MOVEMENT_SPEED);
        OnesApi.morph(player, "timed_effect");

        MobEffectInstance effect = player.getEffect(MobEffects.MOVEMENT_SPEED);
        helper.assertTrue(effect != null, "the timed effect should be applied");
        helper.assertTrue(effect.getDuration() > 0 && effect.getDuration() <= 100,
                "the effect should expire but its duration was " + effect.getDuration());
        helper.succeed();
    }
}
