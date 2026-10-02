package com.binaris.oneslib.server;

import com.binaris.oneslib.Ones;
import com.binaris.oneslib.OnesLib;
import com.binaris.oneslib.api.One;
import com.binaris.oneslib.server.ability.AbilityEngine;
import com.binaris.oneslib.server.morph.ServerOneManager;
import com.binaris.oneslib.server.nick.NickCommand;
import com.binaris.oneslib.server.progress.EvolutionManager;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = OnesLib.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class OnesServer {

    private OnesServer() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (event.player instanceof ServerPlayer player) {
            ServerOneManager.INSTANCE.tick(player);
            AbilityEngine.INSTANCE.tick(player);
        }
    }

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        One one = Ones.currentOne(player);
        if (one != null && one.attributes().ignoreFallDamage()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ServerOneManager.INSTANCE.onDeath(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            EvolutionManager.INSTANCE.onRespawn(player);
            ServerOneManager.INSTANCE.respawn(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            EvolutionManager.INSTANCE.onLogin(player);
            ServerOneManager.INSTANCE.syncAllSkinsTo(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            EvolutionManager.INSTANCE.onLogout(player);
            ServerOneManager.INSTANCE.forget(player);
            AbilityEngine.INSTANCE.forget(player);
        }
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        OnesCommand.register(event.getDispatcher());
        event.getDispatcher().register(NickCommand.register());
    }
}
