package com.binaris.oneslib.mixin;

import com.binaris.oneslib.api.One;
import com.binaris.oneslib.api.OnesApi;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Suppresses the fire animation drawn on a One's shape when {@code visual.hideFireOverlay}
 * is set. Only entities whose type belongs to a registered One are considered, so mobs,
 * items and vanilla players keep their fire animation.
 */
@Mixin(Entity.class)
public class EntityFireAnimationMixin {

    @Inject(method = "displayFireAnimation", at = @At("HEAD"), cancellable = true)
    private void oneslib$hideFireAnimation(CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity) (Object) this;
        if (!(self instanceof LivingEntity living)) {
            return;
        }
        One one = OnesApi.oneForEntity(living).orElse(null);
        if (one != null && one.visual().hideFireOverlay()) {
            cir.setReturnValue(false);
        }
    }
}
