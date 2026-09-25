package com.binaris.oneslib.api;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.binaris.oneslib.Ones;
import com.binaris.oneslib.common.state.StateChannel;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.phys.Vec3;

public final class OneBuilder {

    private final String id;
    @Nullable
    private EntityType.EntityFactory<? extends LivingEntity> factory;
    @Nullable
    private Supplier<EntityType<? extends LivingEntity>> existingType;
    private MobCategory category = MobCategory.CREATURE;
    private float width = 0.6F;
    private float height = 1.8F;
    private OneData.Attributes attributes = OneData.Attributes.DEFAULT;
    private OneData.Visual visual = OneData.Visual.DEFAULT;
    private OneData.Animations animations = OneData.Animations.DEFAULT;
    private final List<OneData.Part> parts = new ArrayList<>();
    @Nullable
    private OneData.Npc npc;
    private StateChannel stateChannel = StateChannel.ITEM_SLOT;
    private EquipmentSlot stateSlot = EquipmentSlot.FEET;
    private ResourceLocation asset;
    private final List<OneAbility> abilities = new ArrayList<>();
    @Nullable
    private One built;

    public OneBuilder(String id) {
        this.id = id;
        this.asset = Ones.id(id);
    }

    public <T extends LivingEntity> OneBuilder entity(EntityType.EntityFactory<T> factory) {
        this.factory = factory;
        return this;
    }

    public OneBuilder entityType(Supplier<EntityType<? extends LivingEntity>> type) {
        this.existingType = type;
        return this;
    }

    public OneBuilder hitbox(float width, float height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public OneBuilder category(MobCategory category) {
        this.category = category;
        return this;
    }

    public OneBuilder attributes(Consumer<AttributesBuilder> consumer) {
        AttributesBuilder builder = new AttributesBuilder(this.attributes);
        consumer.accept(builder);
        this.attributes = builder.build();
        return this;
    }

    public OneBuilder visual(Consumer<VisualBuilder> consumer) {
        VisualBuilder builder = new VisualBuilder(this.visual);
        consumer.accept(builder);
        this.visual = builder.build();
        return this;
    }

    public OneBuilder animations(Consumer<AnimationsBuilder> consumer) {
        AnimationsBuilder builder = new AnimationsBuilder(this.animations);
        consumer.accept(builder);
        this.animations = builder.build();
        return this;
    }

    public OneBuilder parts(Consumer<PartsBuilder> consumer) {
        PartsBuilder builder = new PartsBuilder(this.parts);
        consumer.accept(builder);
        return this;
    }

    public OneBuilder npc(Consumer<NpcBuilder> consumer) {
        NpcBuilder builder = new NpcBuilder(this.npc == null ? OneData.Npc.DEFAULT : this.npc);
        consumer.accept(builder);
        this.npc = builder.build();
        return this;
    }

    public OneBuilder stateChannel(StateChannel channel) {
        this.stateChannel = channel;
        return this;
    }

    public OneBuilder stateSlot(EquipmentSlot slot) {
        this.stateSlot = slot;
        return this;
    }

    public OneBuilder assets(String assetId) {
        this.asset = Ones.id(assetId);
        return this;
    }

    public OneBuilder assets(ResourceLocation asset) {
        this.asset = asset;
        return this;
    }

    public OneBuilder ability(OneAbility ability) {
        this.abilities.add(ability);
        return this;
    }

    public One build() {
        if (this.built != null) {
            return this.built;
        }
        if (this.factory == null && this.existingType == null) {
            throw new IllegalStateException("One '" + this.id + "' needs .entity(...) or .entityType(...)");
        }

        Supplier<EntityType<? extends LivingEntity>> type;
        boolean created = false;
        if (this.factory != null) {
            type = Ones.registerEntityType(this.id, this.factory, this.category, this.width, this.height);
            created = true;
        } else {
            type = this.existingType;
        }

        this.built = new One(this.id, type, created, this.attributes, this.visual, this.animations,
                List.copyOf(this.parts),
                this.npc, this.stateChannel, this.stateSlot, this.asset, List.copyOf(this.abilities));
        return this.built;
    }

    public static final class AttributesBuilder {

        private float health;
        private float speed;
        private float damage;
        private float knockback;
        private float reach;
        private float stepHeight;
        private boolean ignoreFallDamage;
        private int flySeconds;
        private final List<OneData.EffectSpec> effects = new ArrayList<>();

        AttributesBuilder(OneData.Attributes base) {
            this.health = base.health();
            this.speed = base.speed();
            this.damage = base.damage();
            this.knockback = base.knockback();
            this.reach = base.reach();
            this.stepHeight = base.stepHeight();
            this.ignoreFallDamage = base.ignoreFallDamage();
            this.flySeconds = base.flySeconds();
            this.effects.addAll(base.effects());
        }

        public AttributesBuilder health(float value) {
            this.health = value;
            return this;
        }

        public AttributesBuilder speed(float value) {
            this.speed = value;
            return this;
        }

        public AttributesBuilder damage(float value) {
            this.damage = value;
            return this;
        }

        public AttributesBuilder knockback(float value) {
            this.knockback = value;
            return this;
        }

        public AttributesBuilder reach(float multiplier) {
            this.reach = multiplier;
            return this;
        }

        public AttributesBuilder stepHeight(float value) {
            this.stepHeight = value;
            return this;
        }

        public AttributesBuilder ignoreFallDamage(boolean value) {
            this.ignoreFallDamage = value;
            return this;
        }

        public AttributesBuilder flySeconds(int seconds) {
            this.flySeconds = seconds;
            return this;
        }

        public AttributesBuilder effect(MobEffect effect, int durationTicks, int amplifier) {
            this.effects.add(new OneData.EffectSpec(effect, durationTicks, amplifier));
            return this;
        }

        public AttributesBuilder effect(MobEffect effect) {
            return this.effect(effect, -1, 0);
        }

        OneData.Attributes build() {
            return new OneData.Attributes(this.health, this.speed, this.damage, this.knockback, this.reach,
                    this.stepHeight, this.ignoreFallDamage, this.flySeconds, List.copyOf(this.effects));
        }
    }

    public static final class VisualBuilder {

        private float modelScale;
        private float guiScale;
        private boolean firstPersonHand;
        private boolean showNameTag;
        private Vec3 cameraOffset;

        VisualBuilder(OneData.Visual base) {
            this.modelScale = base.modelScale();
            this.guiScale = base.guiScale();
            this.firstPersonHand = base.firstPersonHand();
            this.showNameTag = base.showNameTag();
            this.cameraOffset = base.cameraOffset();
        }

        public VisualBuilder modelScale(float value) {
            this.modelScale = value;
            return this;
        }

        public VisualBuilder guiScale(float value) {
            this.guiScale = value;
            return this;
        }

        public VisualBuilder firstPersonHand(boolean value) {
            this.firstPersonHand = value;
            return this;
        }

        public VisualBuilder showNameTag(boolean value) {
            this.showNameTag = value;
            return this;
        }

        public VisualBuilder cameraOffset(double x, double y, double z) {
            this.cameraOffset = new Vec3(x, y, z);
            return this;
        }

        OneData.Visual build() {
            return new OneData.Visual(this.modelScale, this.guiScale, this.firstPersonHand, this.showNameTag,
                    this.cameraOffset);
        }
    }

    public static final class AnimationsBuilder {

        private String crouchAnimation;
        private String attackAnimation;
        private int attackDurationTicks;
        private double animationSpeed;

        AnimationsBuilder(OneData.Animations base) {
            this.crouchAnimation = base.crouchAnimation();
            this.attackAnimation = base.attackAnimation();
            this.attackDurationTicks = base.attackDurationTicks();
            this.animationSpeed = base.animationSpeed();
        }

        public AnimationsBuilder crouch(String name) {
            this.crouchAnimation = name;
            return this;
        }

        public AnimationsBuilder attack(String name, int durationTicks) {
            this.attackAnimation = name;
            this.attackDurationTicks = durationTicks;
            return this;
        }

        public AnimationsBuilder attackDuration(int ticks) {
            this.attackDurationTicks = ticks;
            return this;
        }

        public AnimationsBuilder animationSpeed(double speed) {
            this.animationSpeed = speed;
            return this;
        }

        OneData.Animations build() {
            return new OneData.Animations(this.crouchAnimation, this.attackAnimation, this.attackDurationTicks,
                    this.animationSpeed);
        }
    }

    public static final class PartsBuilder {

        private final List<OneData.Part> parts;

        PartsBuilder(List<OneData.Part> parts) {
            this.parts = parts;
        }

        public PartsBuilder add(String id, float width, float height, Vec3 offset, boolean damageParent) {
            this.parts.add(new OneData.Part(id, width, height, offset, damageParent));
            return this;
        }

        public PartsBuilder add(String id, float width, float height, float x, float y, float z, boolean damageParent) {
            return this.add(id, width, height, new Vec3(x, y, z), damageParent);
        }
    }

    public static final class NpcBuilder {

        private boolean attacksPlayers;
        private boolean movesAround;
        private boolean looksAtPlayer;
        private float speed;
        private boolean gravity;
        private boolean invulnerable;

        NpcBuilder(OneData.Npc base) {
            this.attacksPlayers = base.attacksPlayers();
            this.movesAround = base.movesAround();
            this.looksAtPlayer = base.looksAtPlayer();
            this.speed = base.speed();
            this.gravity = base.gravity();
            this.invulnerable = base.invulnerable();
        }

        public NpcBuilder attacksPlayers(boolean value) {
            this.attacksPlayers = value;
            return this;
        }

        public NpcBuilder movesAround(boolean value) {
            this.movesAround = value;
            return this;
        }

        public NpcBuilder looksAtPlayer(boolean value) {
            this.looksAtPlayer = value;
            return this;
        }

        public NpcBuilder speed(float value) {
            this.speed = value;
            return this;
        }

        public NpcBuilder gravity(boolean value) {
            this.gravity = value;
            return this;
        }

        public NpcBuilder invulnerable(boolean value) {
            this.invulnerable = value;
            return this;
        }

        OneData.Npc build() {
            return new OneData.Npc(this.attacksPlayers, this.movesAround, this.looksAtPlayer, this.speed,
                    this.gravity, this.invulnerable);
        }
    }
}
