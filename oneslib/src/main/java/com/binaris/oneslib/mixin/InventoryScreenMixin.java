package com.binaris.oneslib.mixin;

import com.binaris.oneslib.Ones;
import com.binaris.oneslib.OnesLib;
import com.binaris.oneslib.api.One;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.LivingEntity;

import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Applies {@code visual.guiScale} when a One is rendered through
 * {@link InventoryScreen#renderEntityInInventory}, the shared entry point used by
 * every morph-selection GUI (sf MorphWidget, remorphed EntityWidget, walkers VariantMenu).
 * Values below 1.0 shrink the preview (useful for huge Ones like Godzilla adult).
 */
@Mixin(InventoryScreen.class)
public class InventoryScreenMixin {

    @Inject(
            method = "renderEntityInInventory",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPoseMatrix(Lorg/joml/Matrix4f;)V",
                    shift = At.Shift.AFTER
            )
    )
    private static void oneslib$applyGuiScale(GuiGraphics guiGraphics, int x, int y, int scale,
            Quaternionf baseRotation, Quaternionf entityRotation, LivingEntity entity, CallbackInfo ci) {
        OnesLib.LOGGER.info("[guiScale] entity={} type={} baseScale={}",
                entity.getClass().getName(), entity.getType(), scale);
        One one = Ones.registry().byType(entity.getType()).orElse(null);
        if (one == null) {
            OnesLib.LOGGER.info("[guiScale] byType empty for {}", entity.getType());
            return;
        }
        float guiScale = one.visual().guiScale();
        OnesLib.LOGGER.info("[guiScale] one={} guiScale={}", one.id(), guiScale);
        if (guiScale == 1.0F || guiScale <= 0.0F) {
            return;
        }
        guiGraphics.pose().scale(guiScale, guiScale, guiScale);
    }
}
