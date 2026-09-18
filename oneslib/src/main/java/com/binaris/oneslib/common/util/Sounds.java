package com.binaris.oneslib.common.util;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;

public final class Sounds {

    private Sounds() {
    }

    public static void play(LivingEntity source, SoundEvent sound, float volume, float pitch) {
        source.level().playSound(null, source.getX(), source.getY(), source.getZ(), sound, SoundSource.PLAYERS,
                volume, pitch);
    }
}
