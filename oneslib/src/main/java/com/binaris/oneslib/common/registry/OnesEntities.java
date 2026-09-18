package com.binaris.oneslib.common.registry;

import com.binaris.oneslib.OnesLib;
import com.binaris.oneslib.common.entity.OneProjectile;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class OnesEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, OnesLib.MOD_ID);

    public static final RegistryObject<EntityType<OneProjectile>> PROJECTILE =
            ENTITY_TYPES.register("projectile", () -> EntityType.Builder
                    .<OneProjectile>of(OneProjectile::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(6)
                    .build("projectile"));

    private OnesEntities() {
    }

    public static void register(IEventBus modBus) {
        ENTITY_TYPES.register(modBus);
    }
}
