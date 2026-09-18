package com.binaris.oneslib;

import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.binaris.oneslib.api.One;
import com.binaris.oneslib.api.OneBuilder;
import com.binaris.oneslib.api.OneRegistry;
import com.binaris.oneslib.common.network.OnesNetwork;
import com.binaris.oneslib.common.registry.OnesEntities;
import com.binaris.oneslib.common.registry.OnesItems;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import tocraft.walkers.api.PlayerShape;

public final class Ones {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, OnesLib.MOD_ID);

    private static final OneRegistry REGISTRY = new OneRegistry();

    private Ones() {
    }

    public static void bootstrap(IEventBus modBus) {
        ENTITY_TYPES.register(modBus);
        OnesEntities.register(modBus);
        OnesItems.register(modBus);
        modBus.addListener(Ones::onEntityAttributes);
        modBus.addListener(Ones::onCommonSetup);
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(OnesNetwork::register);
    }

    public static void register(String id, Consumer<OneBuilder> consumer) {
        OneBuilder builder = new OneBuilder(id);
        consumer.accept(builder);
        REGISTRY.register(builder.build());
    }

    public static OneRegistry registry() {
        return REGISTRY;
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(OnesLib.MOD_ID, path);
    }

    @Nullable
    public static One currentOne(Player player) {
        LivingEntity shape = PlayerShape.getCurrentShape(player);
        if (shape == null) {
            return null;
        }
        return REGISTRY.byType(shape.getType()).orElse(null);
    }

    @SuppressWarnings("unchecked")
    public static Supplier<EntityType<? extends LivingEntity>> registerEntityType(
            String id,
            EntityType.EntityFactory<? extends LivingEntity> factory,
            MobCategory category,
            float width,
            float height
    ) {
        RegistryObject<EntityType<?>> object = ENTITY_TYPES.register(id, () -> {
            EntityType.Builder<LivingEntity> builder =
                    (EntityType.Builder<LivingEntity>) EntityType.Builder.of(factory, category);
            return builder.sized(width, height).clientTrackingRange(10).build(id);
        });
        return () -> object.isPresent() ? (EntityType<? extends LivingEntity>) object.get() : null;
    }

    private static void onEntityAttributes(EntityAttributeCreationEvent event) {
        for (One one : REGISTRY.all()) {
            if (!one.createdEntityType()) {
                continue;
            }
            event.put(one.entityType(), Mob.createMobAttributes()
                    .add(Attributes.MAX_HEALTH, one.attributes().health())
                    .add(Attributes.MOVEMENT_SPEED, one.attributes().speed())
                    .add(Attributes.ATTACK_DAMAGE, one.attributes().damage())
                    .add(Attributes.FOLLOW_RANGE, 32.0D)
                    .build());
        }
    }
}
