package com.binaris.oneslib.client.render;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.binaris.oneslib.OnesLib;
import com.binaris.oneslib.api.One;
import com.binaris.oneslib.common.entity.OneEntity;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.GeckoLibException;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class OneRenderer extends GeoEntityRenderer<OneEntity> {

    private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();

    private final ResourceLocation modelLocation;
    private final ResourceLocation textureLocation;
    private final ResourceLocation animationLocation;

    public OneRenderer(EntityRendererProvider.Context context, One one) {
        super(context, new DefaultedEntityGeoModel<OneEntity>(one.asset()));
        String namespace = one.asset().getNamespace();
        String path = one.asset().getPath();
        this.modelLocation = ResourceLocation.fromNamespaceAndPath(namespace, "geo/entity/" + path + ".geo.json");
        this.textureLocation = ResourceLocation.fromNamespaceAndPath(namespace, "textures/entity/" + path + ".png");
        this.animationLocation = ResourceLocation.fromNamespaceAndPath(namespace, "animations/entity/" + path + ".animation.json");
    }

    @Override
    public void render(OneEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        if (!hasAssets()) {
            String key = entity.getType().getDescriptionId();
            if (WARNED.add(key)) {
                OnesLib.LOGGER.warn("Skipping render for {}: missing assets ({}, {}, {})",
                        entity.getType().getDescriptionId(), this.modelLocation, this.textureLocation,
                        this.animationLocation);
            }
            return;
        }
        try {
            super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        } catch (GeckoLibException exception) {
            String key = entity.getType() + "|" + exception.getMessage();
            if (WARNED.add(key)) {
                OnesLib.LOGGER.error("Skipping render for {}: {}", entity.getType(), exception.getMessage());
            }
        }
    }

    private boolean hasAssets() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.getResourceManager().getResource(this.modelLocation).isPresent()
                && minecraft.getResourceManager().getResource(this.textureLocation).isPresent()
                && minecraft.getResourceManager().getResource(this.animationLocation).isPresent();
    }
}