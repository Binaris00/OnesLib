package com.binaris.oneslib.test.entity;

import java.util.UUID;

import javax.annotation.Nullable;

import com.binaris.oneslib.api.OneMorph;
import com.binaris.oneslib.common.anim.OneAnimationController;
import com.binaris.oneslib.common.state.OneAnimation;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class ForeignOne extends PathfinderMob implements GeoEntity, OneMorph {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    @Nullable
    private UUID owner;
    @Nullable
    private OneAnimation animation;

    public ForeignOne(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new OneAnimationController<>(this));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public @Nullable UUID oneOwner() {
        return this.owner;
    }

    @Override
    public void oneOwner(@Nullable UUID owner) {
        this.owner = owner;
    }

    @Override
    public @Nullable OneAnimation oneAnimation() {
        return this.animation;
    }

    @Override
    public void oneAnimation(@Nullable OneAnimation animation) {
        this.animation = animation;
    }
}
