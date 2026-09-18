package com.binaris.oneslib.common.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class Targeting {

    private Targeting() {
    }

    public static Optional<LivingEntity> raycast(LivingEntity source, double distance) {
        return raycast(source, distance, entity -> true);
    }

    public static Optional<LivingEntity> raycast(LivingEntity source, double distance,
                                                 Predicate<LivingEntity> filter) {
        List<LivingEntity> hits = raycastAll(source, distance, filter, 1);
        return hits.isEmpty() ? Optional.empty() : Optional.of(hits.get(0));
    }

    public static List<LivingEntity> raycastAll(LivingEntity source, double distance) {
        return raycastAll(source, distance, entity -> true, 0);
    }

    public static List<LivingEntity> raycastAll(LivingEntity source, double distance, int limit) {
        return raycastAll(source, distance, entity -> true, limit);
    }

    public static List<LivingEntity> raycastAll(LivingEntity source, double distance,
                                                Predicate<LivingEntity> filter, int limit) {
        Vec3 start = source.getEyePosition();
        Vec3 end = raycastEnd(source, distance);
        AABB box = new AABB(start, end).inflate(1.0D);

        List<Map.Entry<LivingEntity, Double>> hits = new ArrayList<>();
        for (LivingEntity entity : source.level().getEntitiesOfClass(LivingEntity.class, box,
                entity -> entity != source && !entity.isSpectator() && entity.isPickable() && filter.test(entity))) {
            Optional<Vec3> clip = entity.getBoundingBox().inflate(entity.getPickRadius()).clip(start, end);
            clip.ifPresent(vec -> hits.add(Map.entry(entity, start.distanceToSqr(vec))));
        }

        hits.sort(Comparator.comparingDouble(Map.Entry::getValue));

        List<LivingEntity> result = new ArrayList<>();
        for (Map.Entry<LivingEntity, Double> hit : hits) {
            result.add(hit.getKey());
            if (limit > 0 && result.size() >= limit) {
                break;
            }
        }
        return result;
    }

    public static Vec3 raycastEnd(LivingEntity source, double distance) {
        Vec3 start = source.getEyePosition();
        Vec3 end = start.add(source.getLookAngle().scale(distance));
        BlockHitResult blockHit = source.level().clip(
                new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, source));
        return blockHit.getType() == HitResult.Type.MISS ? end : blockHit.getLocation();
    }
}
