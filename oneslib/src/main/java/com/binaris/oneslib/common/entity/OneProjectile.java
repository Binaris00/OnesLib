package com.binaris.oneslib.common.entity;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class OneProjectile extends Projectile implements ItemSupplier {

    private ItemStack visual = new ItemStack(Items.FIRE_CHARGE);

    public OneProjectile(EntityType<? extends OneProjectile> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public OneProjectile(EntityType<? extends OneProjectile> type, Level level, @Nullable ItemStack visual) {
        this(type, level);
        if (visual != null && !visual.isEmpty()) {
            this.visual = visual;
        }
    }

    public ItemStack visual() {
        return this.visual;
    }

    @Override
    public ItemStack getItem() {
        return this.visual;
    }

    @Override
    public void tick() {
        this.baseTick();
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("Visual")) {
            this.visual = ItemStack.of(tag.getCompound("Visual"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (!this.visual.isEmpty()) {
            tag.put("Visual", this.visual.save(new CompoundTag()));
        }
    }

    public void dropVisual() {
        if (!this.level().isClientSide && !this.visual.isEmpty()) {
            ItemEntity drop = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), this.visual.copy());
            drop.setPickUpDelay(40);
            this.level().addFreshEntity(drop);
        }
    }
}
