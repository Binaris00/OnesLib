package com.binaris.oneslib.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AbilitySettingsTest {

    @Test
    void defaults() {
        AbilitySettings settings = AbilitySettings.builder("golpe").build();

        assertEquals("golpe", settings.id());
        assertEquals("golpe", settings.name());
        assertEquals(0, settings.cooldownTicks());
        assertEquals(0, settings.durationTicks());
        assertTrue(settings.cooldownMessage());
        assertFalse(settings.keybindOnly());
        assertEquals(KeybindUi.NONE, settings.keybindUi());
        assertEquals("ability.oneslib.golpe", settings.translationKey());
        assertTrue(settings.isAllowed(null));
    }

    @Test
    void builderOverrides() {
        AbilitySettings settings = AbilitySettings.builder("golpe")
                .name("Golpe Brutal")
                .cooldown(80)
                .duration(31)
                .keybind(71)
                .keybindOnly()
                .cooldownMessage(false)
                .keybindUi(KeybindUi.BOXES)
                .activation(player -> false)
                .build();

        assertEquals("Golpe Brutal", settings.name());
        assertEquals(80, settings.cooldownTicks());
        assertEquals(31, settings.durationTicks());
        assertEquals(71, settings.keybind());
        assertTrue(settings.keybindOnly());
        assertFalse(settings.cooldownMessage());
        assertEquals(KeybindUi.BOXES, settings.keybindUi());
        assertFalse(settings.isAllowed(null));
    }
}
