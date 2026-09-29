package com.binaris.oneslib.client;

import com.binaris.oneslib.OnesLib;
import com.binaris.oneslib.api.One;
import com.binaris.oneslib.api.OnesApi;
import com.binaris.oneslib.client.keybind.OnesKeybinds;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = OnesLib.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class OnesClientEvents {

    private OnesClientEvents() {
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        OnesKeybinds.handleInput();
    }

    /**
     * Applies {@code visual.fov} to the camera entity's One. Only the pass that starts from the
     * client's configured FOV is overridden, so the death zoom and the lava/water scaling that
     * {@code GameRenderer.getFov} applies afterwards keep working.
     */
    @SubscribeEvent
    public static void onComputeFov(ViewportEvent.ComputeFov event) {
        if (!event.usedConfiguredFov()) {
            return;
        }
        One one = currentOne(event.getCamera().getEntity());
        if (one != null && one.visual().hasFov()) {
            event.setFOV(one.visual().fov());
        }
    }

    /**
     * Honours {@code visual.showNameTag} and {@code visual.hideSelfNameTag}. This only has an
     * effect when something actually asks for the name tag to be drawn: walkers gates the
     * morphed player's name plate behind its own {@code showPlayerNametag} config, and other
     * mods may post their own ALLOW. See the conflict matrix in {@code AGENT.md}.
     */
    @SubscribeEvent
    public static void onRenderNameTag(RenderNameTagEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        One one = OnesApi.oneFor(player).orElse(null);
        if (one == null) {
            return;
        }
        if (!one.visual().showNameTag()) {
            event.setResult(Event.Result.DENY);
            return;
        }
        if (one.visual().hideSelfNameTag() && player == Minecraft.getInstance().player) {
            event.setResult(Event.Result.DENY);
        }
    }

    private static One currentOne(Entity cameraEntity) {
        return cameraEntity instanceof Player player ? OnesApi.oneFor(player).orElse(null) : null;
    }
}
