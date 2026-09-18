package com.binaris.oneslib.api.item;

import com.binaris.oneslib.api.OnesApi;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class AbilityItem extends Item {

    private final String abilityId;

    public AbilityItem(String abilityId, Properties properties) {
        super(properties);
        this.abilityId = abilityId;
    }

    public String abilityId() {
        return this.abilityId;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer) {
            if (OnesApi.activate(serverPlayer, this.abilityId, true)) {
                return InteractionResultHolder.success(stack);
            }
            return InteractionResultHolder.fail(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, true);
    }
}
