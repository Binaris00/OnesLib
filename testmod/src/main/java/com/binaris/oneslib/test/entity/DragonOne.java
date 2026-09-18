package com.binaris.oneslib.test.entity;

import com.binaris.oneslib.common.entity.OneEntity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;

public class DragonOne extends OneEntity {

    public DragonOne(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }
}
