package com.binaris.oneslib.client;

import com.binaris.oneslib.Ones;
import com.binaris.oneslib.OnesLib;
import com.binaris.oneslib.api.One;
import com.binaris.oneslib.client.keybind.OnesKeybinds;
import com.binaris.oneslib.client.render.OneRenderer;
import com.binaris.oneslib.common.registry.OnesEntities;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = OnesLib.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class OnesClient {

    private OnesClient() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(OnesClient::registerRenderers);
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        OnesKeybinds.register(event);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void registerRenderers() {
        EntityRenderers.register(OnesEntities.PROJECTILE.get(), ThrownItemRenderer::new);

        for (One one : Ones.registry().all()) {
            if (!one.createdEntityType()) {
                continue;
            }
            EntityRenderers.register((EntityType) one.entityType(),
                    (EntityRendererProvider) context -> new OneRenderer(context, one));
        }
    }
}
