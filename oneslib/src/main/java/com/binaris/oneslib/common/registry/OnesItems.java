package com.binaris.oneslib.common.registry;

import com.binaris.oneslib.OnesLib;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class OnesItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, OnesLib.MOD_ID);

    public static final RegistryObject<Item> STATE_TOKEN =
            ITEMS.register("state_token", () -> new Item(new Item.Properties().stacksTo(1)));

    private OnesItems() {
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
