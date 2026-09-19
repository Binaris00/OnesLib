package com.binaris.oneslib.server.nick;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class NickNameSavedData extends SavedData {

    private static final String DATA_NAME = "oneslib_nicknames";
    private static final String TAG_NICKS = "nicks";

    private final Map<UUID, String> nicknames = new HashMap<>();

    private NickNameSavedData() {
    }

    public static NickNameSavedData get(MinecraftServer server) {
        DimensionDataStorage storage = server.overworld().getDataStorage();
        return storage.computeIfAbsent(NickNameSavedData::load, NickNameSavedData::new, DATA_NAME);
    }

    private static NickNameSavedData load(CompoundTag tag) {
        NickNameSavedData data = new NickNameSavedData();
        ListTag list = tag.getList(TAG_NICKS, Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) {
            String[] parts = list.getString(i).split(":", 2);
            if (parts.length == 2) {
                try {
                    data.nicknames.put(UUID.fromString(parts[0]), parts[1]);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        return data;
    }

    public String getNick(UUID uuid) {
        return nicknames.get(uuid);
    }

    public void setNick(UUID uuid, String rawName) {
        nicknames.put(uuid, rawName);
        setDirty();
    }

    public void clearNick(UUID uuid) {
        nicknames.remove(uuid);
        setDirty();
    }

    public Map<UUID, String> all() {
        return nicknames;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        nicknames.forEach((uuid, rawName) -> list.add(StringTag.valueOf(uuid + ":" + rawName)));
        tag.put(TAG_NICKS, list);
        return tag;
    }
}