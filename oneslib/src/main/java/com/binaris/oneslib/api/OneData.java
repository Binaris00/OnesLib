package com.binaris.oneslib.api;

import java.util.List;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.phys.Vec3;

public final class OneData {

    private OneData() {
    }

    public record EffectSpec(MobEffect effect, int durationTicks, int amplifier) {
    }

    public record Attributes(
            float health,
            float speed,
            float damage,
            float knockback,
            float reach,
            float stepHeight,
            boolean ignoreFallDamage,
            int flySeconds,
            boolean resetHealthOnMorph,
            boolean persistOnDeath,
            boolean persistEffectsOnDeath,
            List<EffectSpec> effects
    ) {

        public static final Attributes DEFAULT =
                new Attributes(20.0F, 0.1F, 2.0F, 0.0F, 1.0F, 0.0F, false, 0, true, false, false, List.of());

        public boolean canFly() {
            return this.flySeconds != 0;
        }

        public boolean hasInfiniteFlight() {
            return this.flySeconds < 0;
        }
     }

    public record Visual(float modelScale, float guiScale, boolean firstPersonHand, boolean showNameTag,
                         boolean hideSelfNameTag, boolean hideFireOverlay, boolean hideScreenFireOverlay,
                         float thirdPersonDistance, float fov, Vec3 cameraOffset) {

        public static final Visual DEFAULT = new Visual(1.0F, 1.0F, true, true, false, false, false,
                0.0F, 0.0F, Vec3.ZERO);

        /** {@code true} when the One overrides the vanilla third-person camera distance. */
        public boolean hasThirdPersonDistance() {
            return this.thirdPersonDistance > 0.0F;
        }

        /** {@code true} when the One overrides the client's configured field of view. */
        public boolean hasFov() {
            return this.fov > 0.0F;
        }
    }

    public record Animations(String crouchAnimation, String attackAnimation, int attackDurationTicks,
                             double animationSpeed) {

        public static final Animations DEFAULT = new Animations("", "", 0, 1.0D);

        public boolean hasCrouch() {
            return !this.crouchAnimation.isBlank();
        }

        public boolean hasAttack() {
            return !this.attackAnimation.isBlank() && this.attackDurationTicks > 0;
        }
    }

    public record Part(String id, float width, float height, Vec3 offset, boolean damageParent) {
    }

    public record Npc(
            boolean attacksPlayers,
            boolean movesAround,
            boolean looksAtPlayer,
            float speed,
            boolean gravity,
            boolean invulnerable
    ) {

        public static final Npc DEFAULT = new Npc(false, false, false, 0.25F, true, false);
    }
}
