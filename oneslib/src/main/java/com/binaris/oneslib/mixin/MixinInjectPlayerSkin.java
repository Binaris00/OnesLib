package com.binaris.oneslib.mixin;

import com.binaris.oneslib.client.ClientOneSkinManager;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Swaps the rendered skin of a player who is morphed as a skin-change One (replicating how
 * StreaventFramework does it). The player keeps the vanilla player model; only the texture
 * changes. {@code usePlayerSkin} keeps the player's own skin.
 */
@Mixin(AbstractClientPlayer.class)
public abstract class MixinInjectPlayerSkin {

    @Inject(method = "getSkinTextureLocation", at = @At("HEAD"), cancellable = true)
    private void oneslib$skin(CallbackInfoReturnable<ResourceLocation> cir) {
        AbstractClientPlayer player = (AbstractClientPlayer) (Object) this;
        ClientOneSkinManager.Skin skin = ClientOneSkinManager.get(player.getUUID());
        if (skin == null || skin.usePlayerSkin()) {
            return;
        }
        cir.setReturnValue(skin.texture());
    }
}