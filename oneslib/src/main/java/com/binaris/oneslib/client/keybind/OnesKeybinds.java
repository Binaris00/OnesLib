package com.binaris.oneslib.client.keybind;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.binaris.oneslib.Ones;
import com.binaris.oneslib.api.One;
import com.binaris.oneslib.api.OneAbility;
import com.binaris.oneslib.common.network.ActivateAbilityPacket;
import com.binaris.oneslib.common.network.OnesNetwork;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

public final class OnesKeybinds {

    private static final Map<KeyMapping, String> ABILITY_KEYS = new LinkedHashMap<>();

    private OnesKeybinds() {
    }

    public static void register(RegisterKeyMappingsEvent event) {
        ABILITY_KEYS.clear();
        for (One one : Ones.registry().all()) {
            for (OneAbility ability : one.abilities()) {
                if (ability.settings().keybind() == GLFW.GLFW_KEY_UNKNOWN) {
                    continue;
                }
                KeyMapping mapping = new KeyMapping(ability.settings().translationKey(),
                        ability.settings().keybind(), "key.categories.oneslib.abilities");
                ABILITY_KEYS.put(mapping, ability.id());
                event.register(mapping);
            }
        }
    }

    public static void handleInput() {
        Player player = Minecraft.getInstance().player;
        One current = player == null ? null : Ones.currentOne(player);

        for (Map.Entry<KeyMapping, String> entry : ABILITY_KEYS.entrySet()) {
            while (entry.getKey().consumeClick()) {
                if (current == null) {
                    continue;
                }
                OneAbility ability = current.ability(entry.getValue()).orElse(null);
                if (ability != null) {
                    OnesNetwork.sendToServer(new ActivateAbilityPacket(ability.id(), false));
                }
            }
        }
    }

    @Nullable
    public static KeyMapping mappingFor(String abilityId) {
        for (Map.Entry<KeyMapping, String> entry : ABILITY_KEYS.entrySet()) {
            if (entry.getValue().equals(abilityId)) {
                return entry.getKey();
            }
        }
        return null;
    }
}
