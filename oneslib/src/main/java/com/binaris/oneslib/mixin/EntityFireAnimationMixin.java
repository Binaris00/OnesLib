package com.binaris.oneslib.mixin;

import com.binaris.oneslib.api.One;
import com.binaris.oneslib.api.OnesApi;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Hides the entity fire animation on the player while morphed as a One whose
 * {@code visual.hideFireOverlay} is enabled.
 */
@Mixin(Entity.class)
public class EntityFireAnimationMixin {

    @Inject(method = "displayFireAnimation", at = @At("HEAD"), cancellable = true)
    private void oneslib$hideFireAnimation(CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity) (Object) this;
        if (!(self instanceof Player player)) {
            return;
        }
        One one = OnesApi.oneFor(player).orElse(null);
        if (one != null && one.visual().hideFireOverlay()) {
            cir.setReturnValue(false);
        }
    }
}