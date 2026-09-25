package com.binaris.oneslib.api;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.binaris.oneslib.common.state.StateChannel;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

public final class One {

    private final String id;
    private final Supplier<EntityType<? extends LivingEntity>> entityType;
    private final boolean createdEntityType;
    private final OneData.Attributes attributes;
    private final OneData.Visual visual;
    private final OneData.Animations animations;
    private final List<OneData.Part> parts;
    @Nullable
    private final OneData.Npc npc;
    private final StateChannel stateChannel;
    private final EquipmentSlot stateSlot;
    private final ResourceLocation asset;
    private final List<OneAbility> abilities;

    One(
            String id,
            Supplier<EntityType<? extends LivingEntity>> entityType,
            boolean createdEntityType,
            OneData.Attributes attributes,
            OneData.Visual visual,
            OneData.Animations animations,
            List<OneData.Part> parts,
            @Nullable OneData.Npc npc,
            StateChannel stateChannel,
            EquipmentSlot stateSlot,
            ResourceLocation asset,
            List<OneAbility> abilities
    ) {
        this.id = id;
        this.entityType = entityType;
        this.createdEntityType = createdEntityType;
        this.attributes = attributes;
        this.visual = visual;
        this.animations = animations;
        this.parts = parts;
        this.npc = npc;
        this.stateChannel = stateChannel;
        this.stateSlot = stateSlot;
        this.asset = asset;
        this.abilities = abilities;
    }

    public String id() {
        return this.id;
    }

    @SuppressWarnings("unchecked")
    public EntityType<? extends LivingEntity> entityType() {
        return (EntityType<? extends LivingEntity>) this.entityType.get();
    }

    public boolean createdEntityType() {
        return this.createdEntityType;
    }

    public OneData.Attributes attributes() {
        return this.attributes;
    }

    public OneData.Visual visual() {
        return this.visual;
    }

    public OneData.Animations animations() {
        return this.animations;
    }

    public List<OneData.Part> parts() {
        return this.parts;
    }

    public @Nullable OneData.Npc npc() {
        return this.npc;
    }

    public StateChannel stateChannel() {
        return this.stateChannel;
    }

    public EquipmentSlot stateSlot() {
        return this.stateSlot;
    }

    public ResourceLocation asset() {
        return this.asset;
    }

    public String assetId() {
        return this.asset.getPath();
    }

    public List<OneAbility> abilities() {
        return this.abilities;
    }

    public Optional<OneAbility> ability(String abilityId) {
        for (OneAbility ability : this.abilities) {
            if (ability.id().equals(abilityId)) {
                return Optional.of(ability);
            }
        }
        return Optional.empty();
    }
}
