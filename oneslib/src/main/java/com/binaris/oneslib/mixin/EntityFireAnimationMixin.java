package com.binaris.oneslib.mixin;

import com.binaris.oneslib.OnesLib;
import com.binaris.oneslib.api.One;
import com.binaris.oneslib.api.OnesApi;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityFireAnimationMixin {

    static {
        OnesLib.LOGGER.warn("[FIRE-DIAG] EntityFireAnimationMixin class loaded");
    }

    public static int calls = 0;
    public static int players = 0;
    public static int morphed = 0;
    public static int hidden = 0;

    @Inject(method = "displayFireAnimation", at = @At("HEAD"), cancellable = true)
    private void oneslib$hideFireAnimation(CallbackInfoReturnable<Boolean> cir) {
        calls++;
        Entity self = (Entity) (Object) this;
        if (!(self instanceof Player player)) {
            return;
        }
        players++;
        One one = OnesApi.oneFor(player).orElse(null);
        if (one == null) {
            return;
        }
        morphed++;
        if (one.visual().hideFireOverlay()) {
            hidden++;
            cir.setReturnValue(false);
        }
    }
}
