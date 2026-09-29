package com.binaris.oneslib.server.progress;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.binaris.oneslib.Ones;
import com.binaris.oneslib.api.One;
import com.binaris.oneslib.api.OneAbility;
import com.binaris.oneslib.server.morph.ServerOneManager;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.BossEvent;

/**
 * Owns the registered evolutions, the per-player progress and the boss bars.
 *
 * <p>Bars are tracked per player UUID and re-created on login, so a reconnect restores the bar
 * and a disconnect removes only that player's bar. Handing the bar lifecycle to the library is
 * what removes the duplicated bars and the bars that survive a disconnect.
 */
public final class EvolutionManager {

    public static final EvolutionManager INSTANCE = new EvolutionManager();

    private final Map<String, Evolution> evolutions = new LinkedHashMap<>();
    private final Map<UUID, ServerBossEvent> bars = new HashMap<>();

    private EvolutionManager() {
    }

    public void register(Evolution evolution) {
        this.evolutions.put(evolution.id(), evolution);
    }

    public List<Evolution> evolutions() {
        return List.copyOf(this.evolutions.values());
    }

    public java.util.Optional<Evolution> byId(String id) {
        return java.util.Optional.ofNullable(this.evolutions.get(id));
    }

    /** Puts the player on the first stage of {@code evolution} if they are not on it already. */
    public void join(ServerPlayer player, Evolution evolution) {
        UUID id = player.getUUID();
        EvolutionData data = EvolutionData.get(player);
        if (data.get(id) == null) {
            data.set(id, new EvolutionData.PlayerProgress(evolution.id(), 0, Set.of()));
            ServerOneManager.INSTANCE.morph(player, evolution.firstStage());
        }
        this.syncBar(player, evolution);
    }

    /** The evolution the player is currently progressing through, if any. */
    public java.util.Optional<Evolution> evolutionOf(ServerPlayer player) {
        EvolutionData.PlayerProgress progress = EvolutionData.get(player).get(player.getUUID());
        return progress == null ? java.util.Optional.empty() : this.byId(progress.evolutionId());
    }

    public int stageIndex(ServerPlayer player) {
        EvolutionData.PlayerProgress progress = EvolutionData.get(player).get(player.getUUID());
        return progress == null ? 0 : progress.stageIndex();
    }

    public String stageOneId(ServerPlayer player) {
        return this.evolutionOf(player)
                .map(evolution -> {
                    int index = this.stageIndex(player);
                    return index < evolution.stages().size() ? evolution.stages().get(index) : evolution.lastStage();
                })
                .orElse(null);
    }

    /**
     * Records an ability use. When usage tracking is on and every ability of the current stage's
     * One has been used, the bar fills; call {@link #advance} to move on.
     */
    public void onAbilityUsed(ServerPlayer player, String abilityId) {
        java.util.Optional<Evolution> maybe = this.evolutionOf(player);
        if (maybe.isEmpty()) {
            return;
        }
        Evolution evolution = maybe.get();
        if (!evolution.trackUsage()) {
            return;
        }
        EvolutionData.PlayerProgress progress = EvolutionData.get(player).get(player.getUUID());
        if (progress == null || !progress.usedAbilities().contains(abilityId)) {
            EvolutionData.get(player).markAbilityUsed(player.getUUID(), abilityId);
        }
        this.syncBar(player, evolution);
    }

    /** Fills one more segment, without requiring the player to have used every ability. */
    public void fillSegment(ServerPlayer player) {
        java.util.Optional<Evolution> maybe = this.evolutionOf(player);
        if (maybe.isEmpty()) {
            return;
        }
        Evolution evolution = maybe.get();
        EvolutionData.PlayerProgress progress = EvolutionData.get(player).get(player.getUUID());
        if (progress == null) {
            return;
        }
        int used = EvolutionData.get(player).usedCount(player.getUUID());
        if (used >= evolution.segments()) {
            return;
        }
        EvolutionData.get(player).markAbilityUsed(player.getUUID(), "segment:" + used);
        this.syncBar(player, evolution);
    }

    public boolean isComplete(ServerPlayer player) {
        return this.evolutionOf(player)
                .map(evolution -> EvolutionData.get(player).usedCount(player.getUUID()) >= evolution.segments())
                .orElse(false);
    }

    /** Moves the player to the next stage and morphs them into it. Returns false at the last one. */
    public boolean advance(ServerPlayer player) {
        java.util.Optional<Evolution> maybe = this.evolutionOf(player);
        if (maybe.isEmpty()) {
            return false;
        }
        Evolution evolution = maybe.get();
        int index = this.stageIndex(player);
        if (index + 1 >= evolution.stages().size()) {
            return false;
        }
        EvolutionData.get(player).set(player.getUUID(),
                new EvolutionData.PlayerProgress(evolution.id(), index + 1, Set.of()));
        ServerOneManager.INSTANCE.morph(player, evolution.stages().get(index + 1));
        this.syncBar(player, evolution);
        return true;
    }

    /** Jumps to a stage by One id, used by admin commands and by skip-stage debugging. */
    public boolean setStage(ServerPlayer player, String oneId) {
        java.util.Optional<Evolution> maybe = this.evolutionOf(player);
        if (maybe.isEmpty()) {
            return false;
        }
        Evolution evolution = maybe.get();
        int index = evolution.stageIndex(oneId);
        if (index < 0) {
            return false;
        }
        EvolutionData.get(player).set(player.getUUID(),
                new EvolutionData.PlayerProgress(evolution.id(), index, Set.of()));
        ServerOneManager.INSTANCE.morph(player, oneId);
        this.syncBar(player, evolution);
        return true;
    }

    public void clear(ServerPlayer player) {
        EvolutionData.get(player).remove(player.getUUID());
        ServerBossEvent bar = this.bars.remove(player.getUUID());
        if (bar != null) {
            bar.removeAllPlayers();
        }
    }

    public void onLogin(ServerPlayer player) {
        this.evolutionOf(player).ifPresent(evolution -> this.syncBar(player, evolution));
    }

    public void onLogout(ServerPlayer player) {
        ServerBossEvent bar = this.bars.remove(player.getUUID());
        if (bar != null) {
            bar.removeAllPlayers();
        }
    }

    public void onRespawn(ServerPlayer player) {
        this.evolutionOf(player).ifPresent(evolution -> {
            if (evolution.respawnPolicy() == RespawnPolicy.RESET) {
                ServerOneManager.INSTANCE.demorph(player);
                this.clear(player);
                return;
            }
            String stage = this.stageOneId(player);
            if (stage != null) {
                ServerOneManager.INSTANCE.morph(player, stage);
            }
            this.syncBar(player, evolution);
        });
    }

    /**
     * Recomputes the bar from the registered One, never from a hand-kept ability list: the
     * abilities of the current stage's One are the source of truth.
     */
    private void syncBar(ServerPlayer player, Evolution evolution) {
        int used = EvolutionData.get(player).usedCount(player.getUUID());
        int required = evolution.trackUsage() ? this.requiredAbilities(evolution, player) : evolution.segments();
        int shown = Math.min(used, required);

        ServerBossEvent bar = this.bars.computeIfAbsent(player.getUUID(), id -> {
            ServerBossEvent created = new ServerBossEvent(Component.literal(evolution.id()),
                    BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);
            created.addPlayer(player);
            return created;
        });
        bar.setName(Component.literal(evolution.id() + " — " + this.stageOneId(player)));
        bar.setProgress(evolution.progress(shown));
    }

    private int requiredAbilities(Evolution evolution, ServerPlayer player) {
        String stage = this.stageOneId(player);
        One one = stage == null ? null : Ones.registry().byId(stage).orElse(null);
        if (one == null) {
            return evolution.segments();
        }
        Set<String> ids = new HashSet<>();
        for (OneAbility ability : one.abilities()) {
            if (!ability.isPassive()) {
                ids.add(ability.id());
            }
        }
        return Math.max(1, ids.size());
    }
}
