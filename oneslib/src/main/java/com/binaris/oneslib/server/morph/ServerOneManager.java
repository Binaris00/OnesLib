package com.binaris.oneslib.server.morph;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import com.binaris.oneslib.Ones;
import com.binaris.oneslib.api.EndReason;
import com.binaris.oneslib.api.One;
import com.binaris.oneslib.api.OneData;
import com.binaris.oneslib.api.OneMorph;
import com.binaris.oneslib.api.event.OneDemorphEvent;
import com.binaris.oneslib.api.event.OneMorphEvent;
import com.binaris.oneslib.common.network.OnesNetwork;
import com.binaris.oneslib.common.network.SyncSkinOnePacket;
import com.binaris.oneslib.common.state.OneState;
import com.binaris.oneslib.server.ability.AbilityEngine;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.MinecraftForge;
import tocraft.walkers.api.PlayerShape;
import tocraft.walkers.impl.DimensionsRefresher;
import tocraft.walkers.impl.PlayerDataProvider;

public final class ServerOneManager {

    public static final ServerOneManager INSTANCE = new ServerOneManager();

    private static final UUID HEALTH_ID = UUID.fromString("6f5a2c1e-0001-4b7a-9c2d-7e8f9a0b1c2d");
    private static final UUID SPEED_ID = UUID.fromString("6f5a2c1e-0002-4b7a-9c2d-7e8f9a0b1c2d");
    private static final UUID DAMAGE_ID = UUID.fromString("6f5a2c1e-0003-4b7a-9c2d-7e8f9a0b1c2d");
    private static final UUID KNOCKBACK_ID = UUID.fromString("6f5a2c1e-0004-4b7a-9c2d-7e8f9a0b1c2d");
    private static final UUID BLOCK_REACH_ID = UUID.fromString("6f5a2c1e-0005-4b7a-9c2d-7e8f9a0b1c2d");
    private static final UUID ENTITY_REACH_ID = UUID.fromString("6f5a2c1e-0006-4b7a-9c2d-7e8f9a0b1c2d");

    private static final double BASE_HEALTH = 20.0D;
    private static final double BASE_SPEED = 0.1D;
    private static final double BASE_DAMAGE = 1.0D;
    private static final double BASE_BLOCK_REACH = 4.5D;
    private static final double BASE_ENTITY_REACH = 3.0D;

    private final Map<UUID, ActiveOne> active = new ConcurrentHashMap<>();

    private ServerOneManager() {
    }

    public record ActiveOne(One one, LivingEntity shape, boolean previousMayfly, boolean previousFlying) {
    }

    public Optional<ActiveOne> active(ServerPlayer player) {
        return Optional.ofNullable(this.active.get(player.getUUID()));
    }

    public void morph(ServerPlayer player, String oneId) {
        One one = Ones.registry().byId(oneId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown One '" + oneId + "'"));
        this.morph(player, one);
    }

    public void morph(ServerPlayer player, One one) {
        ActiveOne previous = this.active.remove(player.getUUID());

        boolean previousMayfly;
        boolean previousFlying;
        if (previous != null) {
            this.clear(player, previous, EndReason.REPLACED, true);
            previousMayfly = previous.previousMayfly();
            previousFlying = previous.previousFlying();
        } else {
            previousMayfly = player.getAbilities().mayfly;
            previousFlying = player.getAbilities().flying;
        }

        ServerLevel level = player.serverLevel();
        EntityType<? extends LivingEntity> type = one.entityType();
        LivingEntity shape = type.create(level);
        if (shape == null) {
            return;
        }

        if (shape instanceof OneMorph morph) {
            morph.oneOwner(player.getUUID());
        }
        shape.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());

        if (one.visual().isSkinMorph()) {
            // Skin-change One: the player keeps the vanilla model and no Walkers shape is assigned.
            // Only the client-side skin texture changes, synced via SyncSkinOnePacket.
            this.syncSkin(player, one.id());
        } else {
            this.applyShape(player, shape);
        }

        this.active.put(player.getUUID(), new ActiveOne(one, shape, previousMayfly, previousFlying));

        this.applyAttributes(player, one);
        this.applyEffects(player, one);
        this.applyFlight(player, one);
        AbilityEngine.INSTANCE.activatePassives(player, one);

        MinecraftForge.EVENT_BUS.post(new OneMorphEvent(player, one));
    }

    public void demorph(ServerPlayer player) {
        this.clear(player, EndReason.CANCELLED, true);
    }

    private void clear(ServerPlayer player, EndReason reason, boolean postEvent) {
        ActiveOne previous = this.active.remove(player.getUUID());
        if (previous != null && previous.one().visual().isSkinMorph()) {
            this.syncSkin(player, "");
        } else {
            this.applyShape(player, null);
        }
        if (previous != null) {
            this.clear(player, previous, reason, postEvent);
        }
    }

    public void respawn(ServerPlayer player) {
        ActiveOne activeOne = this.active.get(player.getUUID());
        if (activeOne == null) {
            return;
        }
        One one = activeOne.one();
        AbilityEngine.INSTANCE.deactivateAll(player, EndReason.PLAYER_DIED);
        this.applyAttributes(player, one);
        this.applyEffects(player, one);
        this.applyFlight(player, one);
        AbilityEngine.INSTANCE.activatePassives(player, one);
    }

    /**
     * Death policy. By default the One is dropped on death: every active ability ends with
     * {@link EndReason#PLAYER_DIED} and the morph is cleared. When the One declares
     * {@code persistOnDeath} the morph survives and {@link #respawn(ServerPlayer)} puts
     * everything back; {@code persistEffectsOnDeath} additionally keeps the One's effects
     * across the death, which vanilla would otherwise strip.
     */
    public void onDeath(ServerPlayer player) {
        ActiveOne activeOne = this.active.get(player.getUUID());
        if (activeOne == null) {
            return;
        }
        if (activeOne.one().attributes().persistOnDeath()) {
            return;
        }
        this.clear(player, EndReason.PLAYER_DIED, true);
    }

    public void forget(ServerPlayer player) {
        ActiveOne previous = this.active.remove(player.getUUID());
        AbilityEngine.INSTANCE.deactivateAll(player, EndReason.MORPH_LOST);
        if (previous != null) {
            if (previous.one().visual().isSkinMorph()) {
                this.syncSkin(player, "");
            }
            OneState.clear(previous.shape());
        }
    }

    /**
     * Sends the current skin-change state of a player to every connected client. Also used to push
     * all active skin morphs to a player who just joined.
     */
    public void syncSkin(String oneId, ServerPlayer owner) {
        MinecraftServer server = owner.getServer();
        if (server == null) {
            return;
        }
        SyncSkinOnePacket packet = new SyncSkinOnePacket(owner.getUUID(), oneId);
        for (ServerPlayer target : server.getPlayerList().getPlayers()) {
            OnesNetwork.sendTo(packet, net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> target));
        }
    }

    private void syncSkin(ServerPlayer owner, String oneId) {
        this.syncSkin(oneId, owner);
    }

    /** Pushes every currently-active skin morph to a freshly joined player. */
    public void syncAllSkinsTo(ServerPlayer target) {
        for (Map.Entry<UUID, ActiveOne> entry : this.active.entrySet()) {
            ActiveOne activeOne = entry.getValue();
            if (activeOne.one().visual().isSkinMorph()) {
                OnesNetwork.sendTo(
                        new SyncSkinOnePacket(entry.getKey(), activeOne.one().id()),
                        net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> target));
            }
        }
    }

    public void tick(ServerPlayer player) {
        ActiveOne activeOne = this.active.get(player.getUUID());

        if (activeOne != null && activeOne.one().visual().isSkinMorph()) {
            // Skin-change One: no Walkers shape exists, so the shape-sync/re-apply logic does
            // not apply. State is already synced on morph/demorph.
            return;
        }

        LivingEntity current = PlayerShape.getCurrentShape(player);

        if (activeOne == null) {
            if (current != null) {
                Ones.registry().byType(current.getType()).ifPresent(one -> this.bind(player, one, current));
            }
            return;
        }

        if (current == activeOne.shape()) {
            return;
        }

        if (current == null) {
            // Walkers lost the shape. This manager is the authority, so the presentation is
            // re-applied instead of demorphing: a desynced shape must not silently drop the
            // player's attributes, effects, flight and fall-damage immunity.
            this.applyShape(player, activeOne.shape());
            return;
        }

        this.active.remove(player.getUUID(), activeOne);
        this.clear(player, activeOne, EndReason.REPLACED, true);

        if (current != null) {
            Ones.registry().byType(current.getType()).ifPresent(one -> this.bind(player, one, current));
        }
    }

    private void applyShape(ServerPlayer player, @Nullable LivingEntity shape) {
        ((PlayerDataProvider) player).walkers$setCurrentShape(shape);
        ((DimensionsRefresher) player).shape_refreshDimensions();
        MinecraftServer server = player.getServer();
        if (server != null) {
            // Broadcast to every connected player so observers see morph/evolution
            // changes in real time (PlayerShape.sync(player) only targets self).
            for (ServerPlayer target : server.getPlayerList().getPlayers()) {
                PlayerShape.sync(player, target);
            }
        } else {
            PlayerShape.sync(player);
        }
    }

    private void bind(ServerPlayer player, One one, LivingEntity shape) {
        if (shape instanceof OneMorph morph) {
            morph.oneOwner(player.getUUID());
        }
        boolean previousMayfly = player.getAbilities().mayfly;
        boolean previousFlying = player.getAbilities().flying;
        if (!one.attributes().canFly() && !player.isCreative() && !player.isSpectator()) {
            previousMayfly = false;
            previousFlying = false;
        }
        this.active.put(player.getUUID(), new ActiveOne(one, shape, previousMayfly, previousFlying));
        this.applyAttributes(player, one);
        this.applyEffects(player, one);
        this.applyFlight(player, one);
        AbilityEngine.INSTANCE.activatePassives(player, one);
        MinecraftForge.EVENT_BUS.post(new OneMorphEvent(player, one));
    }

    private void clear(ServerPlayer player, ActiveOne previous, EndReason reason, boolean postEvent) {
        AbilityEngine.INSTANCE.deactivateAll(player, reason);
        OneState.clear(previous.shape());
        this.clearAttributes(player, previous.one().attributes());
        if (!previous.one().attributes().persistEffectsOnDeath() || reason != EndReason.PLAYER_DIED) {
            this.clearEffects(player, previous.one());
        }
        this.restoreFlight(player, previous);
        if (postEvent) {
            MinecraftForge.EVENT_BUS.post(new OneDemorphEvent(player, previous.one()));
        }
    }

    private void applyAttributes(ServerPlayer player, One one) {
        OneData.Attributes attributes = one.attributes();
        AttributeInstance health = player.getAttribute(Attributes.MAX_HEALTH);
        double baseHealth = health == null ? BASE_HEALTH : health.getBaseValue();
        setModifier(player, Attributes.MAX_HEALTH, HEALTH_ID, "oneslib:health", attributes.health() - baseHealth);
        setModifier(player, Attributes.MOVEMENT_SPEED, SPEED_ID, "oneslib:speed", attributes.speed() - BASE_SPEED);
        setModifier(player, Attributes.ATTACK_DAMAGE, DAMAGE_ID, "oneslib:damage", attributes.damage() - BASE_DAMAGE);
        setModifier(player, Attributes.ATTACK_KNOCKBACK, KNOCKBACK_ID, "oneslib:knockback", attributes.knockback());
        setModifier(player, ForgeMod.BLOCK_REACH.get(), BLOCK_REACH_ID, "oneslib:block_reach",
                (attributes.reach() - 1.0D) * BASE_BLOCK_REACH);
        setModifier(player, ForgeMod.ENTITY_REACH.get(), ENTITY_REACH_ID, "oneslib:entity_reach",
                (attributes.reach() - 1.0D) * BASE_ENTITY_REACH);
        if (attributes.resetHealthOnMorph()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private void clearAttributes(ServerPlayer player, OneData.Attributes attributes) {
        removeModifier(player, Attributes.MAX_HEALTH, HEALTH_ID);
        removeModifier(player, Attributes.MOVEMENT_SPEED, SPEED_ID);
        removeModifier(player, Attributes.ATTACK_DAMAGE, DAMAGE_ID);
        removeModifier(player, Attributes.ATTACK_KNOCKBACK, KNOCKBACK_ID);
        removeModifier(player, ForgeMod.BLOCK_REACH.get(), BLOCK_REACH_ID);
        removeModifier(player, ForgeMod.ENTITY_REACH.get(), ENTITY_REACH_ID);
        if (attributes.resetHealthOnMorph()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private static void setModifier(ServerPlayer player, Attribute attribute, UUID id, String name, double amount) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        instance.removeModifier(id);
        if (amount != 0.0D) {
            instance.addTransientModifier(
                    new AttributeModifier(id, name, amount, AttributeModifier.Operation.ADDITION));
        }
    }

    private static void removeModifier(ServerPlayer player, Attribute attribute, UUID id) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance != null) {
            instance.removeModifier(id);
        }
    }

    private void applyEffects(ServerPlayer player, One one) {
        for (OneData.EffectSpec spec : one.attributes().effects()) {
            int duration = spec.durationTicks() < 0 ? -1 : spec.durationTicks();
            player.addEffect(new MobEffectInstance(spec.effect(), duration, spec.amplifier(), false, false));
        }
    }

    private void clearEffects(ServerPlayer player, One one) {
        for (OneData.EffectSpec spec : one.attributes().effects()) {
            player.removeEffect(spec.effect());
        }
    }

    private void applyFlight(ServerPlayer player, One one) {
        if (player.isCreative() || player.isSpectator()) {
            return;
        }
        if (one.attributes().canFly()) {
            player.getAbilities().mayfly = true;
        } else {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
        }
        player.onUpdateAbilities();
    }

    private void restoreFlight(ServerPlayer player, ActiveOne activeOne) {
        if (player.isCreative() || player.isSpectator()) {
            return;
        }
        player.getAbilities().mayfly = activeOne.previousMayfly();
        player.getAbilities().flying = activeOne.previousFlying() && activeOne.previousMayfly();
        player.onUpdateAbilities();
    }
}
