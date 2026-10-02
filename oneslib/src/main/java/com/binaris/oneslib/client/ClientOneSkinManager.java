package com.binaris.oneslib.client;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import com.binaris.oneslib.Ones;
import com.binaris.oneslib.api.One;
import com.binaris.oneslib.api.OneData;

import net.minecraft.resources.ResourceLocation;

/**
 * Client-side view of skin-change Ones. Keyed by the owner player UUID; stores the One and the
 * resolved skin texture, so both the render (skin swap) and {@code Ones.currentOne} (ability
 * resolution) work without a Walkers shape.
 */
public final class ClientOneSkinManager {

    public record Skin(One one, ResourceLocation texture, boolean usePlayerSkin) {
    }

    private static final Map<UUID, Skin> SKINS = new ConcurrentHashMap<>();

    private ClientOneSkinManager() {
    }

    public static void update(UUID owner, String oneId) {
        if (oneId.isEmpty()) {
            SKINS.remove(owner);
            return;
        }
        One one = Ones.registry().byId(oneId).orElse(null);
        if (one == null || !one.visual().isSkinMorph()) {
            SKINS.remove(owner);
            return;
        }
        OneData.Visual visual = one.visual();
        SKINS.put(owner, new Skin(one, visual.resolvedSkinTexture(one.asset()), visual.usePlayerSkin()));
    }

    @Nullable
    public static Skin get(UUID owner) {
        return SKINS.get(owner);
    }

    /** The skin One a player is currently skinned as, or {@code null}. */
    @Nullable
    public static One one(UUID owner) {
        Skin skin = SKINS.get(owner);
        return skin == null ? null : skin.one();
    }

    public static void clear() {
        SKINS.clear();
    }
}