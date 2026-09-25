package com.binaris.oneslib.api;

import java.util.function.IntSupplier;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.binaris.oneslib.OnesLib;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import org.lwjgl.glfw.GLFW;

public final class AbilitySettings {

    private final String id;
    private final String name;
    private final IntSupplier cooldownTicks;
    private final IntSupplier durationTicks;
    private final int keybind;
    @Nullable
    private final SoundEvent sound;
    private final boolean keybindOnly;
    private final boolean cooldownMessage;
    private final KeybindUi keybindUi;
    @Nullable
    private final Predicate<ServerPlayer> activation;

    AbilitySettings(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.cooldownTicks = builder.cooldownTicks;
        this.durationTicks = builder.durationTicks;
        this.keybind = builder.keybind;
        this.sound = builder.sound;
        this.keybindOnly = builder.keybindOnly;
        this.cooldownMessage = builder.cooldownMessage;
        this.keybindUi = builder.keybindUi;
        this.activation = builder.activation;
    }

    public static Builder builder(String id) {
        return new Builder(id);
    }

    public String id() {
        return this.id;
    }

    public String name() {
        return this.name;
    }

    public String translationKey() {
        return "ability." + OnesLib.MOD_ID + "." + this.id;
    }

    public int cooldownTicks() {
        return this.cooldownTicks.getAsInt();
    }

    public int durationTicks() {
        return this.durationTicks.getAsInt();
    }

    public int keybind() {
        return this.keybind;
    }

    public @Nullable SoundEvent sound() {
        return this.sound;
    }

    public boolean keybindOnly() {
        return this.keybindOnly;
    }

    public boolean cooldownMessage() {
        return this.cooldownMessage;
    }

    public KeybindUi keybindUi() {
        return this.keybindUi;
    }

    public boolean isAllowed(ServerPlayer player) {
        return this.activation == null || this.activation.test(player);
    }

    public static final class Builder {

        private final String id;
        private String name;
        private IntSupplier cooldownTicks = () -> 0;
        private IntSupplier durationTicks = () -> 0;
        private int keybind = GLFW.GLFW_KEY_UNKNOWN;
        @Nullable
        private SoundEvent sound;
        private boolean keybindOnly;
        private boolean cooldownMessage = true;
        private KeybindUi keybindUi = KeybindUi.NONE;
        @Nullable
        private Predicate<ServerPlayer> activation;

        Builder(String id) {
            this.id = id;
            this.name = id;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder cooldown(int ticks) {
            return this.cooldown(() -> ticks);
        }

        public Builder cooldown(IntSupplier ticks) {
            this.cooldownTicks = ticks;
            return this;
        }

        public Builder duration(int ticks) {
            return this.duration(() -> ticks);
        }

        public Builder duration(IntSupplier ticks) {
            this.durationTicks = ticks;
            return this;
        }

        public Builder keybind(int glfwKey) {
            this.keybind = glfwKey;
            return this;
        }

        public Builder sound(SoundEvent sound) {
            this.sound = sound;
            return this;
        }

        public Builder keybindOnly() {
            return this.keybindOnly(true);
        }

        public Builder keybindOnly(boolean value) {
            this.keybindOnly = value;
            return this;
        }

        public Builder cooldownMessage(boolean value) {
            this.cooldownMessage = value;
            return this;
        }

        public Builder keybindUi(KeybindUi keybindUi) {
            this.keybindUi = keybindUi;
            return this;
        }

        public Builder activation(Predicate<ServerPlayer> predicate) {
            this.activation = predicate;
            return this;
        }

        public AbilitySettings build() {
            return new AbilitySettings(this);
        }
    }
}
