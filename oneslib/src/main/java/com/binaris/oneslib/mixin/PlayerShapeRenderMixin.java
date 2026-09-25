package com.binaris.oneslib.mixin;

import com.binaris.oneslib.common.entity.OneEntity;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.LivingEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tocraft.walkers.api.PlayerShape;

/**
 * Copies the owner player's motion state onto their shape before rendering.
 *
 * <p>walkers' own player-renderer mixin only syncs {@code walkAnimation} and the swing
 * fields, leaving the shape's {@code deltaMovement}/{@code tickCount} stale on remote
 * clients. GeckoLib derives {@code state.isMoving()} from the shape's velocity and uses
 * {@code tickCount} as the animation clock, so this keeps the shape's motion data in sync
 * with the player for every viewer (local and remote).
 */
@Mixin(PlayerRenderer.class)
public abstract class PlayerShapeRenderMixin {

    @Inject(method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"))
    private void oneslib$syncShapeMotion(AbstractClientPlayer player, float entityYaw, float partialTick,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        LivingEntity shape = PlayerShape.getCurrentShape(player);
        if (shape instanceof OneEntity) {
            shape.setDeltaMovement(player.getDeltaMovement());
            shape.tickCount = player.tickCount;
        }
    }
}