package com.binaris.oneslib.common.state;

import java.util.UUID;

import javax.annotation.Nullable;

import com.binaris.oneslib.Ones;
import com.binaris.oneslib.api.One;
import com.binaris.oneslib.api.OneMorph;
import com.binaris.oneslib.common.registry.OnesItems;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.core.animation.Animation;

public final class OneState {

    private static final String TAG_ANIMATION = "Animation";
    private static final String TAG_LOOP = "Loop";
    private static final String TAG_DURATION = "Duration";
    private static final String TAG_PREVIOUS = "Previous";

    private OneState() {
    }

    public static void write(LivingEntity entity, @Nullable OneAnimation animation) {
        if (entity instanceof OneMorph morph) {
            morph.oneAnimation(animation);
        }

        One one = Ones.registry().byType(entity.getType()).orElse(null);
        StateChannel channel = one == null ? StateChannel.ITEM_SLOT : one.stateChannel();
        if (channel == StateChannel.SYNCED_DATA) {
            return;
        }

        EquipmentSlot slot = one == null ? EquipmentSlot.FEET : one.stateSlot();
        LivingEntity carrier = resolveOwner(entity);
        writeToken(carrier, slot, animation);

        if (carrier != entity && entity.getItemBySlot(slot).is(OnesItems.STATE_TOKEN.get())) {
            entity.setItemSlot(slot, ItemStack.EMPTY);
        }
    }

    @Nullable
    public static OneAnimation read(LivingEntity entity) {
        if (entity instanceof OneMorph morph) {
            OneAnimation animation = morph.oneAnimation();
            if (animation != null) {
                return animation;
            }
        }

        One one = Ones.registry().byType(entity.getType()).orElse(null);
        EquipmentSlot slot = one == null ? EquipmentSlot.FEET : one.stateSlot();

        LivingEntity owner = resolveOwner(entity);
        if (owner != entity) {
            return readToken(owner.getItemBySlot(slot));
        }
        return readToken(entity.getItemBySlot(slot));
    }

    public static void clear(LivingEntity entity) {
        write(entity, null);
    }

    private static LivingEntity resolveOwner(LivingEntity entity) {
        if (entity instanceof Player) {
            return entity;
        }
        if (entity instanceof OneMorph morph) {
            UUID owner = morph.oneOwner();
            if (owner != null) {
                Player player = entity.level().getPlayerByUUID(owner);
                if (player != null) {
                    return player;
                }
            }
        }
        return entity;
    }

    private static void writeToken(LivingEntity carrier, EquipmentSlot slot, @Nullable OneAnimation animation) {
        ItemStack current = carrier.getItemBySlot(slot);

        if (animation == null) {
            if (current.is(OnesItems.STATE_TOKEN.get())) {
                carrier.setItemSlot(slot, readPrevious(current));
            }
            return;
        }

        if (current.is(OnesItems.STATE_TOKEN.get())) {
            applyAnimation(current.getOrCreateTag(), animation);
            return;
        }

        ItemStack token = new ItemStack(OnesItems.STATE_TOKEN.get());
        CompoundTag tag = token.getOrCreateTag();
        applyAnimation(tag, animation);
        if (!current.isEmpty()) {
            tag.put(TAG_PREVIOUS, current.save(new CompoundTag()));
        }
        carrier.setItemSlot(slot, token);
    }

    private static void applyAnimation(CompoundTag tag, OneAnimation animation) {
        tag.putString(TAG_ANIMATION, animation.name());
        tag.putString(TAG_LOOP, animation.loopId());
        tag.putInt(TAG_DURATION, animation.durationTicks());
    }

    private static ItemStack readPrevious(ItemStack token) {
        CompoundTag tag = token.getTag();
        if (tag == null || !tag.contains(TAG_PREVIOUS)) {
            return ItemStack.EMPTY;
        }
        return ItemStack.of(tag.getCompound(TAG_PREVIOUS));
    }

    @Nullable
    private static OneAnimation readToken(ItemStack stack) {
        if (!stack.is(OnesItems.STATE_TOKEN.get()) || !stack.hasTag()) {
            return null;
        }
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return null;
        }
        String name = tag.getString(TAG_ANIMATION);
        if (name.isEmpty()) {
            return null;
        }
        Animation.LoopType loopType = OneAnimation.loopTypeFromId(tag.getString(TAG_LOOP));
        return new OneAnimation(name, loopType, tag.getInt(TAG_DURATION));
    }
}
