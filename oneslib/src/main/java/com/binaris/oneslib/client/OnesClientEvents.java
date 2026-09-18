package com.binaris.oneslib.client;

import com.binaris.oneslib.OnesLib;
import com.binaris.oneslib.client.keybind.OnesKeybinds;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
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
}
