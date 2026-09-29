package com.binaris.oneslib.server.ability;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.binaris.oneslib.Ones;
import com.binaris.oneslib.api.AbilityContext;
import com.binaris.oneslib.api.EndReason;
import com.binaris.oneslib.api.One;
import com.binaris.oneslib.api.OneAbility;
import com.binaris.oneslib.common.util.Sounds;
import com.binaris.oneslib.server.morph.ServerOneManager;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public final class AbilityEngine {

    public static final AbilityEngine INSTANCE = new AbilityEngine();

    private final Map<UUID, Map<String, ActiveAbility>> active = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, CooldownData>> cooldowns = new ConcurrentHashMap<>();

    private AbilityEngine() {
    }

    public boolean activate(ServerPlayer player, String abilityId, boolean fromItem) {
        One one = Ones.currentOne(player);
        if (one == null) {
            return false;
        }
        OneAbility ability = one.ability(abilityId).orElse(null);
        if (ability == null) {
            return false;
        }

        Map<String, ActiveAbility> playerActive = this.active.get(player.getUUID());
        if (playerActive != null && playerActive.containsKey(abilityId)) {
            if (ability.isToggle()) {
                this.end(player, abilityId, EndReason.TOGGLED_OFF);
                return true;
            }
            return false;
        }

        if (fromItem && ability.settings().keybindOnly()) {
            return false;
        }
        if (!ability.settings().isAllowed(player)) {
            return false;
        }

        if (this.isOnCooldown(player, abilityId)) {
            if (ability.settings().cooldownMessage()) {
                int seconds = (this.cooldownLeft(player, abilityId) + 19) / 20;
                player.displayClientMessage(Component.translatable("message.oneslib.cooldown",
                        ability.settings().name(), seconds), true);
            }
            return false;
        }

        LivingEntity shape = ServerOneManager.INSTANCE.active(player)
                .map(ServerOneManager.ActiveOne::shape)
                .orElse(null);
        if (shape == null) {
            return false;
        }

        AbilityContext context = new AbilityContext(player, one, ability, shape);
        if (!ability.canActivate(context)) {
            return false;
        }

        playerActive = this.active.computeIfAbsent(player.getUUID(), key -> new ConcurrentHashMap<>());
        playerActive.put(abilityId, new ActiveAbility(one, ability, context));

        if (ability.settings().sound() != null) {
            Sounds.play(player, ability.settings().sound(), 1.0F, 1.0F);
        }
        ability.onStart(context);

        if (ability.settings().durationTicks() == 0 && !ability.isToggle() && !ability.isPassive()) {
            this.end(player, abilityId, EndReason.FINISHED);
        }
        return true;
    }

    public void end(ServerPlayer player, String abilityId, EndReason reason) {
        Map<String, ActiveAbility> playerActive = this.active.get(player.getUUID());
        if (playerActive == null) {
            return;
        }
        ActiveAbility activeAbility = playerActive.remove(abilityId);
        if (activeAbility == null) {
            return;
        }

        activeAbility.ability().onEnd(activeAbility.context(), reason);

        if (reason != EndReason.MORPH_LOST) {
            int cooldownTicks = activeAbility.ability().settings().cooldownTicks();
            if (cooldownTicks > 0) {
                this.cooldown(player, abilityId, cooldownTicks);
            }
        }
    }

    public void deactivateAll(ServerPlayer player, EndReason reason) {
        Map<String, ActiveAbility> playerActive = this.active.get(player.getUUID());
        if (playerActive == null) {
            return;
        }
        for (String abilityId : List.copyOf(playerActive.keySet())) {
            this.end(player, abilityId, reason);
        }
    }

    public void forget(ServerPlayer player) {
        this.deactivateAll(player, EndReason.MORPH_LOST);
        this.active.remove(player.getUUID());
        this.cooldowns.remove(player.getUUID());
    }

    public void activatePassives(ServerPlayer player, One one) {
        for (OneAbility ability : one.abilities()) {
            if (ability.isPassive()) {
                this.activate(player, ability.id(), false);
            }
        }
    }

    public boolean isActive(ServerPlayer player, String abilityId) {
        Map<String, ActiveAbility> playerActive = this.active.get(player.getUUID());
        return playerActive != null && playerActive.containsKey(abilityId);
    }

    public boolean isOnCooldown(ServerPlayer player, String abilityId) {
        Map<String, CooldownData> playerCooldowns = this.cooldowns.get(player.getUUID());
        return playerCooldowns != null && playerCooldowns.containsKey(abilityId);
    }

    public int cooldownLeft(ServerPlayer player, String abilityId) {
        Map<String, CooldownData> playerCooldowns = this.cooldowns.get(player.getUUID());
        CooldownData data = playerCooldowns == null ? null : playerCooldowns.get(abilityId);
        return data == null ? 0 : data.remainingTicks();
    }

    public void cooldown(ServerPlayer player, String abilityId, int ticks) {
        this.cooldowns.computeIfAbsent(player.getUUID(), key -> new ConcurrentHashMap<>())
                .put(abilityId, new CooldownData(ticks));
    }

    public void clearCooldown(ServerPlayer player, String abilityId) {
        Map<String, CooldownData> playerCooldowns = this.cooldowns.get(player.getUUID());
        if (playerCooldowns != null) {
            playerCooldowns.remove(abilityId);
        }
    }

    public void tick(ServerPlayer player) {
        this.tickCooldowns(player);

        Map<String, ActiveAbility> playerActive = this.active.get(player.getUUID());
        if (playerActive == null || playerActive.isEmpty()) {
            return;
        }

        One current = Ones.currentOne(player);
        for (ActiveAbility activeAbility : List.copyOf(playerActive.values())) {
            if (current == null || !current.id().equals(activeAbility.one().id())) {
                this.end(player, activeAbility.ability().id(), EndReason.MORPH_LOST);
                continue;
            }

            activeAbility.context().advance();
            activeAbility.ability().onTick(activeAbility.context());

            if (activeAbility.context().isEndRequested()) {
                this.end(player, activeAbility.ability().id(), EndReason.FINISHED);
                continue;
            }

            int duration = activeAbility.ability().settings().durationTicks();
            if (duration > 0 && activeAbility.context().tick() >= duration) {
                this.end(player, activeAbility.ability().id(), EndReason.FINISHED);
            }
        }
    }

    private void tickCooldowns(ServerPlayer player) {
        Map<String, CooldownData> playerCooldowns = this.cooldowns.get(player.getUUID());
        if (playerCooldowns == null) {
            return;
        }
        for (Map.Entry<String, CooldownData> entry : List.copyOf(playerCooldowns.entrySet())) {
            if (entry.getValue().tick()) {
                playerCooldowns.remove(entry.getKey());
            }
        }
    }
}
