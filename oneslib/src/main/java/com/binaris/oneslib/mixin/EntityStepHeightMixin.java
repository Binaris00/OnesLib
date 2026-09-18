package com.binaris.oneslib.mixin;

import com.binaris.oneslib.Ones;
import com.binaris.oneslib.api.One;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityStepHeightMixin {

    @Inject(method = "maxUpStep()F", at = @At("HEAD"), cancellable = true)
    private void oneslib$applyStepHeight(CallbackInfoReturnable<Float> cir) {
        if (!((Object) this instanceof Player player)) {
            return;
        }
        One one = Ones.currentOne(player);
        if (one != null && one.attributes().stepHeight() > 0.0F) {
            cir.setReturnValue(one.attributes().stepHeight());
        }
    }
}
