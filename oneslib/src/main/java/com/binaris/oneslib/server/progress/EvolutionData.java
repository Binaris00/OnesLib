package com.binaris.oneslib.server.progress;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Per-player evolution state, persisted in the world save under the library's own key.
 *
 * <p>Using one {@link SavedData} instead of {@code player.getPersistentData()} is what keeps two
 * character mods from colliding on an NBT root, and it makes the whole state inspectable.
 */
public final class EvolutionData extends SavedData {

    public record PlayerProgress(String evolutionId, int stageIndex, Set<String> usedAbilities) {

        public PlayerProgress {
            usedAbilities = Set.copyOf(usedAbilities);
        }
    }

    private final Map<UUID, PlayerProgress> progress = new HashMap<>();

    public static EvolutionData get(ServerPlayer player) {
        return get(player.server);
    }

    public static EvolutionData get(net.minecraft.server.MinecraftServer server) {
        return server.overworld().getDataStorage()
                .computeIfAbsent(EvolutionData::load, EvolutionData::new, "oneslib_evolution");
    }

    public static EvolutionData load(CompoundTag tag) {
        EvolutionData data = new EvolutionData();
        data.read(tag);
        return data;
    }

    public PlayerProgress get(UUID player) {
        return this.progress.get(player);
    }

    public void set(UUID player, PlayerProgress value) {
        if (value == null) {
            this.progress.remove(player);
        } else {
            this.progress.put(player, value);
        }
        this.setDirty();
    }

    public void remove(UUID player) {
        this.progress.remove(player);
        this.setDirty();
    }

    public void markAbilityUsed(UUID player, String abilityId) {
        PlayerProgress current = this.progress.get(player);
        if (current == null) {
            return;
        }
        Set<String> used = new HashSet<>(current.usedAbilities());
        if (!used.add(abilityId)) {
            return;
        }
        this.progress.put(player, new PlayerProgress(current.evolutionId(), current.stageIndex(), used));
        this.setDirty();
    }

    public int usedCount(UUID player) {
        PlayerProgress current = this.progress.get(player);
        return current == null ? 0 : current.usedAbilities().size();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag players = new ListTag();
        for (Map.Entry<UUID, PlayerProgress> entry : this.progress.entrySet()) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID("Player", entry.getKey());
            playerTag.putString("Evolution", entry.getValue().evolutionId());
            playerTag.putInt("Stage", entry.getValue().stageIndex());
            ListTag used = new ListTag();
            for (String abilityId : entry.getValue().usedAbilities()) {
                used.add(StringTag.valueOf(abilityId));
            }
            playerTag.put("Used", used);
            players.add(playerTag);
        }
        tag.put("Players", players);
        return tag;
    }

    public void read(CompoundTag tag) {
        this.progress.clear();
        ListTag players = tag.getList("Players", Tag.TAG_COMPOUND);
        for (int i = 0; i < players.size(); i++) {
            CompoundTag playerTag = players.getCompound(i);
            Set<String> used = new HashSet<>();
            ListTag usedTag = playerTag.getList("Used", Tag.TAG_STRING);
            for (int j = 0; j < usedTag.size(); j++) {
                used.add(usedTag.getString(j));
            }
            this.progress.put(playerTag.getUUID("Player"),
                    new PlayerProgress(playerTag.getString("Evolution"), playerTag.getInt("Stage"), used));
        }
    }
}
