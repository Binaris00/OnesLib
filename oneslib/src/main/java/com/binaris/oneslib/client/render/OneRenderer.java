package com.binaris.oneslib.client.render;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.binaris.oneslib.OnesLib;
import com.binaris.oneslib.api.One;
import com.binaris.oneslib.common.entity.OneEntity;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.GeckoLibException;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class OneRenderer extends GeoEntityRenderer<OneEntity> {

    private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();

    public OneRenderer(EntityRendererProvider.Context context, One one) {
        super(context, new DefaultedEntityGeoModel<OneEntity>(one.asset()));
    }

    @Override
    public void render(OneEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        try {
            super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        } catch (GeckoLibException exception) {
            String key = entity.getType() + "|" + exception.getMessage();
            if (WARNED.add(key)) {
                OnesLib.LOGGER.error("Skipping render for {}: {}", entity.getType(), exception.getMessage());
            }
        }
    }
}
