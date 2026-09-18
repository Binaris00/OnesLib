package com.binaris.oneslib.test.entity;

import com.binaris.oneslib.common.entity.OneEntity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;

public class ImpOne extends OneEntity {

    public ImpOne(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }
}
