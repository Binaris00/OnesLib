package com.binaris.oneslib.api;

import java.util.Optional;

import com.binaris.oneslib.Ones;
import com.binaris.oneslib.server.ability.AbilityEngine;
import com.binaris.oneslib.server.morph.ServerOneManager;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class OnesApi {

    private OnesApi() {
    }

    public static void morph(ServerPlayer player, String oneId) {
        ServerOneManager.INSTANCE.morph(player, oneId);
    }

    public static void demorph(ServerPlayer player) {
        ServerOneManager.INSTANCE.demorph(player);
    }

    public static Optional<One> currentOne(Player player) {
        return Optional.ofNullable(Ones.currentOne(player));
    }

    public static boolean isMorphedAs(Player player, String oneId) {
        One one = Ones.currentOne(player);
        return one != null && one.id().equals(oneId);
    }

    public static boolean activate(ServerPlayer player, String abilityId) {
        return AbilityEngine.INSTANCE.activate(player, abilityId, false);
    }

    public static boolean activate(ServerPlayer player, String abilityId, boolean fromItem) {
        return AbilityEngine.INSTANCE.activate(player, abilityId, fromItem);
    }

    public static void deactivate(ServerPlayer player, String abilityId) {
        AbilityEngine.INSTANCE.end(player, abilityId, EndReason.CANCELLED);
    }

    public static void deactivateAll(ServerPlayer player) {
        AbilityEngine.INSTANCE.deactivateAll(player, EndReason.CANCELLED);
    }

    public static boolean isActive(ServerPlayer player, String abilityId) {
        return AbilityEngine.INSTANCE.isActive(player, abilityId);
    }

    public static boolean isOnCooldown(ServerPlayer player, String abilityId) {
        return AbilityEngine.INSTANCE.isOnCooldown(player, abilityId);
    }

    public static int cooldownLeft(ServerPlayer player, String abilityId) {
        return AbilityEngine.INSTANCE.cooldownLeft(player, abilityId);
    }

    public static void cooldown(ServerPlayer player, String abilityId, int ticks) {
        AbilityEngine.INSTANCE.cooldown(player, abilityId, ticks);
    }

    public static void clearCooldown(ServerPlayer player, String abilityId) {
        AbilityEngine.INSTANCE.clearCooldown(player, abilityId);
    }

    public static void tick(ServerPlayer player) {
        ServerOneManager.INSTANCE.tick(player);
        AbilityEngine.INSTANCE.tick(player);
    }
}
