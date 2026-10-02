package com.binaris.oneslib.mixin;

import com.binaris.oneslib.api.One;
import com.binaris.oneslib.api.OnesApi;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Applies {@code visual.thirdPersonDistance} when the camera entity is morphed as a One.
 * Vanilla's third-person distance is 4.0, which clips inside the bounding box of tall Ones.
 */
@Mixin(Camera.class)
public class CameraMixin {

    private static final double VANILLA_THIRD_PERSON_DISTANCE = 4.0D;

    @ModifyConstant(method = "setup", constant = @Constant(doubleValue = VANILLA_THIRD_PERSON_DISTANCE))
    private double oneslib$thirdPersonDistance(double distance, BlockGetter level, Entity cameraEntity,
                                              boolean detached, boolean mirrored, float partialTick) {
        if (!(cameraEntity instanceof Player player)) {
            return VANILLA_THIRD_PERSON_DISTANCE;
        }
        One one = OnesApi.oneFor(player).orElse(null);
        if (one == null || !one.visual().hasThirdPersonDistance()) {
            return VANILLA_THIRD_PERSON_DISTANCE;
        }
        return one.visual().thirdPersonDistance();
    }
}
