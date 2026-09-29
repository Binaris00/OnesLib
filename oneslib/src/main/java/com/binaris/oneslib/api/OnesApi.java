package com.binaris.oneslib.api;

import java.util.Optional;

import com.binaris.oneslib.Ones;
import com.binaris.oneslib.server.ability.AbilityEngine;
import com.binaris.oneslib.server.morph.ServerOneManager;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
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

    /**
     * Resolves the One an entity belongs to by its type. Use this in render callbacks that
     * receive the shape rather than the player, such as the shared
     * {@code InventoryScreen.renderEntityInInventory} entry point.
     */
    public static Optional<One> oneForEntity(LivingEntity entity) {
        return Ones.registry().byType(entity.getType());
    }

    public static Optional<One> oneFor(Player player) {
        return currentOne(player);
    }

    public static boolean isMorphed(Player player) {
        return Ones.currentOne(player) != null;
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

    /**
     * Ticks an ability of the player's current One lasts. With a {@link OneShotAnimation} this
     * is the whole init + hit + recover timeline, so an ability does not have to repeat its own
     * duration in a magic number.
     */
    public static int abilityDuration(Player player, String abilityId) {
        return currentOne(player)
                .flatMap(one -> one.ability(abilityId))
                .map(ability -> ability.settings().durationTicks())
                .orElse(0);
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
