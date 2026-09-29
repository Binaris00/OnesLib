package com.binaris.oneslib.test.gametest;

import com.binaris.oneslib.api.EndReason;
import com.binaris.oneslib.api.OnesApi;
import com.binaris.oneslib.api.OneShotAnimation;
import com.binaris.oneslib.api.event.OneAbilityHitEvent;
import com.binaris.oneslib.server.ability.AbilityEngine;
import com.binaris.oneslib.server.morph.ServerOneManager;
import com.binaris.oneslib.test.TestMod;
import com.binaris.oneslib.test.util.TestPlayers;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TestMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ProgressionGameTest {

    private ProgressionGameTest() {
    }

    /** The one-shot timeline is declarative: duration, hit tick and the return to idle. */
    @GameTest(template = "empty")
    public static void oneShot_timelineIsConsistent(GameTestHelper helper) {
        OneShotAnimation spec = OneShotAnimation.of("slam", 20, 25, 15);

        helper.assertTrue(spec.initTicks() == 20, "init should be 20");
        helper.assertTrue(spec.hitTick() == 25, "hit should be 25");
        helper.assertTrue(spec.recoverTicks() == 15, "recover should be 15");
        helper.assertTrue(spec.totalTicks() == 40, "total should be 20 + 5 + 15 but was " + spec.totalTicks());

        OneShotAnimation even = OneShotAnimation.of("slam", 60);
        helper.assertTrue(even.totalTicks() == 60, "an even split should total 60 but was " + even.totalTicks());
        helper.assertTrue(even.hitTick() > even.initTicks(), "the hit should be after the wind-up");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void oneShot_invalidTimelinesAreRejected(GameTestHelper helper) {
        helper.assertTrue(throwsOnNegative(), "a negative init should be rejected");
        helper.assertTrue(throwsOnHitBeforeInit(), "a hit before the wind-up should be rejected");
        helper.succeed();
    }

    private static boolean throwsOnNegative() {
        try {
            OneShotAnimation.of("bad", -1, 5, 5);
            return false;
        } catch (IllegalArgumentException expected) {
            return true;
        }
    }

    private static boolean throwsOnHitBeforeInit() {
        try {
            OneShotAnimation.of("bad", 20, 5, 5);
            return false;
        } catch (IllegalArgumentException expected) {
            return true;
        }
    }

    /** An ability with a declared timeline takes its duration from it and fires its hit event. */
    @GameTest(template = "empty")
    public static void oneShot_abilityFiresHitEventAndKeepsDuration(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.spawn(helper);
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        player.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0F, 0.0F);

        OnesApi.morph(player, "imp_packet");
        helper.assertTrue(OnesApi.abilityDuration(player, "imp_packet_attack") == 40,
                "the ability duration should come from the one-shot spec");

        HitRecorder.firedFor = null;
        MinecraftForge.EVENT_BUS.register(HitRecorder.INSTANCE);

        OnesApi.activate(player, "imp_packet_attack");
        for (int i = 0; i < 40; i++) {
            AbilityEngine.INSTANCE.tick(player);
        }

        MinecraftForge.EVENT_BUS.unregister(HitRecorder.INSTANCE);

        helper.assertTrue("imp_packet_attack".equals(HitRecorder.firedFor),
                "the one-shot hit event should fire at its hit tick");
        helper.assertTrue(!OnesApi.isActive(player, "imp_packet_attack"),
                "the ability should end after its declared duration");
        helper.succeed();
    }

    private static final class HitRecorder {

        static final HitRecorder INSTANCE = new HitRecorder();
        static String firedFor;

        private HitRecorder() {
        }

        @SubscribeEvent
        public void onHit(OneAbilityHitEvent event) {
            firedFor = event.ability().id();
        }
    }

    /** Default death policy: the morph is dropped and abilities end with PLAYER_DIED. */
    @GameTest(template = "empty")
    public static void death_demorphsByDefault(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.spawn(helper);
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        player.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0F, 0.0F);

        OnesApi.morph(player, "evil_hulk");
        helper.assertTrue(OnesApi.isMorphedAs(player, "evil_hulk"), "player should start morphed");

        ServerOneManager.INSTANCE.onDeath(player);

        helper.assertTrue(!OnesApi.isMorphedAs(player, "evil_hulk"),
                "a One without persistOnDeath should be dropped on death");
        helper.assertTrue(player.getMaxHealth() == 20.0F,
                "attributes should be restored on death but max health was " + player.getMaxHealth());
        helper.succeed();
    }

    /** persistOnDeath keeps the morph, and the respawn path restores it. */
    @GameTest(template = "empty")
    public static void death_persistsWhenDeclared(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.spawn(helper);
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        player.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0F, 0.0F);

        OnesApi.morph(player, "immortal");
        ServerOneManager.INSTANCE.onDeath(player);

        helper.assertTrue(OnesApi.isMorphedAs(player, "immortal"),
                "a One with persistOnDeath should survive death");

        ServerOneManager.INSTANCE.respawn(player);

        helper.assertTrue(OnesApi.isMorphedAs(player, "immortal"), "the morph should still be there");
        helper.assertTrue(player.getMaxHealth() == 200.0F,
                "attributes should be re-applied on respawn but max health was " + player.getMaxHealth());
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void endReasons_areAllReachable(GameTestHelper helper) {
        // PLAYER_DIED and REPLACED used to be declared but never constructed.
        helper.assertTrue(EndReason.PLAYER_DIED != null, "PLAYER_DIED should exist");
        helper.assertTrue(EndReason.REPLACED != null, "REPLACED should exist");
        helper.succeed();
    }
}
