package com.binaris.oneslib.mixin;

import com.binaris.oneslib.api.One;
import com.binaris.oneslib.api.OnesApi;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.ForgeHooksClient;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import tocraft.walkers.api.PlayerShape;

/**
 * Suppresses the full-screen fire overlay of the local player when
 * {@code visual.hideScreenFireOverlay} is set on their One. Redirecting the forge hook to
 * {@code true} means "already handled", so vanilla's overlay is skipped without touching
 * anything outside the local player's own One.
 */
@Mixin(targets = "net.minecraft.client.renderer.ScreenEffectRenderer")
public class ScreenEffectFireOverlayMixin {

    @Redirect(
            method = "renderScreenEffect",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/client/ForgeHooksClient;renderFireOverlay"
                            + "(Lnet/minecraft/world/entity/player/Player;Lcom/mojang/blaze3d/vertex/PoseStack;)Z"
            )
    )
    private boolean oneslib$hideScreenFireOverlay(Player player, PoseStack poseStack) {
        LivingEntity shape = PlayerShape.getCurrentShape(player);
        One one = shape == null ? null : OnesApi.oneForEntity(shape).orElse(null);
        if (one != null && one.visual().hideScreenFireOverlay()) {
            return true;
        }
        return ForgeHooksClient.renderFireOverlay(player, poseStack);
    }
}
