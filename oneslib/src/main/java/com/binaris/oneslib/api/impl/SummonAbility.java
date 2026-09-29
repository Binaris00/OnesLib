package com.binaris.oneslib.api.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.binaris.oneslib.api.AbilityContext;
import com.binaris.oneslib.api.AbilitySettings;
import com.binaris.oneslib.api.EndReason;
import com.binaris.oneslib.api.OneAbility;

import net.minecraft.world.entity.LivingEntity;

public abstract class SummonAbility extends OneAbility {

    private static final String SUMMONED = SummonAbility.class.getName() + ".summoned";

    protected SummonAbility(String id, int durationTicks, Consumer<AbilitySettings.Builder> consumer) {
        super(id, builder -> {
            builder.duration(durationTicks);
            consumer.accept(builder);
        });
    }

    protected abstract List<LivingEntity> createSummons(AbilityContext context);

    protected void onSummonTick(AbilityContext context, LivingEntity summon) {
    }

    @Override
    public void onStart(AbilityContext context) {
        List<LivingEntity> created = this.createSummons(context);
        if (created == null) {
            context.end();
            return;
        }
        List<LivingEntity> summoned = this.summoned(context);
        for (LivingEntity summon : created) {
            if (summon == null) {
                continue;
            }
            context.level().addFreshEntity(summon);
            summoned.add(summon);
        }
    }

    @Override
    public void onTick(AbilityContext context) {
        List<LivingEntity> summoned = this.summoned(context);
        summoned.removeIf(summon -> !summon.isAlive());
        for (LivingEntity summon : summoned) {
            this.onSummonTick(context, summon);
        }
    }

    @Override
    public void onEnd(AbilityContext context, EndReason reason) {
        List<LivingEntity> summoned = this.summoned(context);
        for (LivingEntity summon : summoned) {
            summon.discard();
        }
        summoned.clear();
    }

    private List<LivingEntity> summoned(AbilityContext context) {
        return context.state(SUMMONED, ArrayList::new);
    }
}
