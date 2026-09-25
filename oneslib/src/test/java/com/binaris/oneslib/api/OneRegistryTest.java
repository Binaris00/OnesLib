package com.binaris.oneslib.api;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.binaris.oneslib.common.state.StateChannel;

import net.minecraft.world.entity.EquipmentSlot;

class OneRegistryTest {

    @Test
    void duplicateOneIdFails() {
        OneRegistry registry = new OneRegistry();
        registry.register(one("tails", new TestAbility("tails_fly")));

        assertThrows(IllegalStateException.class,
                () -> registry.register(one("tails", new TestAbility("other_ability"))));
    }

    @Test
    void duplicateAbilityIdFails() {
        OneRegistry registry = new OneRegistry();
        registry.register(one("tails", new TestAbility("shared")));

        assertThrows(IllegalStateException.class,
                () -> registry.register(one("eddy", new TestAbility("shared"))));
    }

    @Test
    void duplicateKeybindInSameOneFails() {
        OneRegistry registry = new OneRegistry();

        assertThrows(IllegalStateException.class, () -> registry.register(new One("tails", () -> null, false,
                OneData.Attributes.DEFAULT, OneData.Visual.DEFAULT, OneData.Animations.DEFAULT, List.of(), null,
                StateChannel.ITEM_SLOT,
                EquipmentSlot.FEET, null,
                List.of(new TestAbility("first", 71), new TestAbility("second", 71)))));
    }

    @Test
    void sameKeybindAcrossDifferentOnesIsAllowed() {
        OneRegistry registry = new OneRegistry();
        registry.register(new One("tails", () -> null, false, OneData.Attributes.DEFAULT, OneData.Visual.DEFAULT,
                OneData.Animations.DEFAULT, List.of(), null, StateChannel.ITEM_SLOT, EquipmentSlot.FEET, null,
                List.of(new TestAbility("tails_fly", 71))));
        registry.register(new One("eddy", () -> null, false, OneData.Attributes.DEFAULT, OneData.Visual.DEFAULT,
                OneData.Animations.DEFAULT, List.of(), null, StateChannel.ITEM_SLOT, EquipmentSlot.FEET, null,
                List.of(new TestAbility("eddy_heal", 71))));

        assertTrue(registry.byId("eddy").isPresent());
    }

    @Test
    void lookupByIdAndAbility() {
        OneRegistry registry = new OneRegistry();
        registry.register(one("tails", new TestAbility("tails_fly")));

        assertTrue(registry.byId("tails").isPresent());
        assertTrue(registry.byId("tails").orElseThrow().ability("tails_fly").isPresent());
        assertTrue(registry.byId("tails").orElseThrow().ability("missing").isEmpty());
        assertTrue(registry.byType(null).isEmpty());
        assertTrue(registry.byId("missing").isEmpty());
    }

    private static One one(String id, OneAbility ability) {
        return new One(id, () -> null, false, OneData.Attributes.DEFAULT, OneData.Visual.DEFAULT,
                OneData.Animations.DEFAULT, List.of(), null, StateChannel.ITEM_SLOT, EquipmentSlot.FEET, null,
                List.of(ability));
    }

    private static final class TestAbility extends OneAbility {

        TestAbility(String id) {
            super(id, settings -> {
            });
        }

        TestAbility(String id, int keybind) {
            super(id, settings -> settings.keybind(keybind));
        }
    }
}
