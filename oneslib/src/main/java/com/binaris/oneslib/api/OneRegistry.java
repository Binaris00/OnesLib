package com.binaris.oneslib.api;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.world.entity.EntityType;
import org.lwjgl.glfw.GLFW;

public final class OneRegistry {

    private final Map<String, One> byId = new LinkedHashMap<>();
    private final Map<String, One> abilityOwners = new LinkedHashMap<>();

    public synchronized void register(One one) {
        if (this.byId.containsKey(one.id())) {
            throw new IllegalStateException("Duplicate One id '" + one.id() + "'");
        }

        Set<Integer> keybinds = new HashSet<>();
        for (OneAbility ability : one.abilities()) {
            One owner = this.abilityOwners.putIfAbsent(ability.id(), one);
            if (owner != null) {
                throw new IllegalStateException("Ability '" + ability.id() + "' is already registered by One '"
                        + owner.id() + "'");
            }
            int keybind = ability.settings().keybind();
            if (keybind != GLFW.GLFW_KEY_UNKNOWN && !keybinds.add(keybind)) {
                throw new IllegalStateException("One '" + one.id()
                        + "' has two abilities bound to the same keybind");
            }
        }

        EntityType<?> type = one.entityType();
        if (type != null) {
            for (One registered : this.byId.values()) {
                if (registered.entityType() == type) {
                    throw new IllegalStateException("Entity type " + type
                            + " is already registered as One '" + registered.id() + "'");
                }
            }
        }

        this.byId.put(one.id(), one);
    }

    public synchronized Optional<One> byId(String id) {
        return Optional.ofNullable(this.byId.get(id));
    }

    public synchronized Optional<One> byType(@Nullable EntityType<?> type) {
        if (type == null) {
            return Optional.empty();
        }
        for (One one : this.byId.values()) {
            if (one.entityType() == type) {
                return Optional.of(one);
            }
        }
        return Optional.empty();
    }

    public synchronized Collection<One> all() {
        return List.copyOf(this.byId.values());
    }
}
