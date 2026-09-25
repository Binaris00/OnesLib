package com.binaris.oneslib.common.entity;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import com.binaris.oneslib.Ones;
import com.binaris.oneslib.api.One;
import com.binaris.oneslib.api.OneMorph;
import com.binaris.oneslib.common.anim.OneAnimationController;
import com.binaris.oneslib.common.state.OneAnimation;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.Animation;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class OneEntity extends PathfinderMob implements GeoEntity, OneMorph {

    private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER =
            SynchedEntityData.defineId(OneEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<String> DATA_ANIMATION =
            SynchedEntityData.defineId(OneEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> DATA_ANIMATION_LOOP =
            SynchedEntityData.defineId(OneEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> DATA_ANIMATION_DURATION =
            SynchedEntityData.defineId(OneEntity.class, EntityDataSerializers.INT);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private long attackEndTime;
    private boolean wasSwinging;

    public OneEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_OWNER, Optional.empty());
        this.entityData.define(DATA_ANIMATION, "");
        this.entityData.define(DATA_ANIMATION_LOOP, "");
        this.entityData.define(DATA_ANIMATION_DURATION, 0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        One one = Ones.registry().byType(this.getType()).orElse(null);
        double speed = one != null ? one.animations().animationSpeed() : 1.0D;
        controllers.add(new AnimationController<>(this, "ones_base", 5, this::baseAnimation)
                .setAnimationSpeed(speed));
        controllers.add(new OneAnimationController<>(this));
        this.registerCustomControllers(controllers);
    }

    protected void registerCustomControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    private PlayState baseAnimation(AnimationState<OneEntity> state) {
        One one = Ones.registry().byType(this.getType()).orElse(null);
        Player owner = this.oneOwner() != null ? this.level().getPlayerByUUID(this.oneOwner()) : null;
        if (one != null) {
            if (one.animations().hasAttack()) {
                boolean swinging = (owner != null && owner.swinging) || this.swinging;
                if (swinging && !this.wasSwinging) {
                    this.attackEndTime = this.level().getGameTime() + one.animations().attackDurationTicks();
                }
                this.wasSwinging = swinging;
            }
            if (one.animations().hasCrouch() && owner != null && owner.isShiftKeyDown()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop(one.animations().crouchAnimation()));
            }
            if (one.animations().hasAttack() && this.level().getGameTime() < this.attackEndTime) {
                return state.setAndContinue(RawAnimation.begin().thenLoop(one.animations().attackAnimation()));
            }
        }
        boolean moving = owner != null ? ownerIsMoving(owner) : state.isMoving();
        if (moving) {
            return state.setAndContinue(RawAnimation.begin().thenLoop("walk"));
        }
        return state.setAndContinue(RawAnimation.begin().thenLoop("idle"));
    }

    private static boolean ownerIsMoving(Player owner) {
        double avg = (Math.abs(owner.getX() - owner.xOld) + Math.abs(owner.getZ() - owner.zOld)) / 2.0D;
        return avg >= 0.015D;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public @Nullable UUID oneOwner() {
        return this.entityData.get(DATA_OWNER).orElse(null);
    }

    @Override
    public void oneOwner(@Nullable UUID owner) {
        this.entityData.set(DATA_OWNER, Optional.ofNullable(owner));
    }

    @Override
    public @Nullable OneAnimation oneAnimation() {
        String name = this.entityData.get(DATA_ANIMATION);
        if (name.isEmpty()) {
            return null;
        }
        Animation.LoopType loopType = OneAnimation.loopTypeFromId(this.entityData.get(DATA_ANIMATION_LOOP));
        return new OneAnimation(name, loopType, this.entityData.get(DATA_ANIMATION_DURATION));
    }

    @Override
    public void oneAnimation(@Nullable OneAnimation animation) {
        if (animation == null) {
            this.entityData.set(DATA_ANIMATION, "");
            this.entityData.set(DATA_ANIMATION_LOOP, "");
            this.entityData.set(DATA_ANIMATION_DURATION, 0);
            return;
        }
        this.entityData.set(DATA_ANIMATION, animation.name());
        this.entityData.set(DATA_ANIMATION_LOOP, animation.loopId());
        this.entityData.set(DATA_ANIMATION_DURATION, animation.durationTicks());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        UUID owner = this.oneOwner();
        if (owner != null) {
            tag.putUUID("OneOwner", owner);
        }
        OneAnimation animation = this.oneAnimation();
        tag.putString("OneAnimation", animation == null ? "" : animation.name());
        tag.putString("OneAnimationLoop", animation == null ? "" : animation.loopId());
        tag.putInt("OneAnimationDuration", animation == null ? 0 : animation.durationTicks());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("OneOwner")) {
            this.oneOwner(tag.getUUID("OneOwner"));
        }
        if (tag.contains("OneAnimation")) {
            Animation.LoopType loopType = OneAnimation.loopTypeFromId(tag.getString("OneAnimationLoop"));
            this.oneAnimation(new OneAnimation(tag.getString("OneAnimation"), loopType,
                    tag.getInt("OneAnimationDuration")));
        }
    }
}
