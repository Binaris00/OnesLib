package com.binaris.oneslib.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class AbilityContextTest {

    @Test
    void scheduleRunsOnce() {
        AbilityContext context = new AbilityContext(null, null, new TestAbility("test", 10), null);
        List<Integer> hits = new ArrayList<>();

        context.schedule(3, c -> hits.add(c.tick()));
        for (int i = 0; i < 10; i++) {
            context.advance();
        }

        assertEquals(List.of(3), hits);
    }

    @Test
    void scheduleEveryRepeats() {
        AbilityContext context = new AbilityContext(null, null, new TestAbility("test", 10), null);
        List<Integer> hits = new ArrayList<>();

        context.scheduleEvery(2, c -> hits.add(c.tick()));
        for (int i = 0; i < 7; i++) {
            context.advance();
        }

        assertEquals(List.of(2, 4, 6), hits);
    }

    @Test
    void progressUsesDuration() {
        AbilityContext context = new AbilityContext(null, null, new TestAbility("test", 10), null);
        for (int i = 0; i < 5; i++) {
            context.advance();
        }

        assertEquals(0.5F, context.progress(), 0.001F);
    }

    @Test
    void endIsRequested() {
        AbilityContext context = new AbilityContext(null, null, new TestAbility("test", 10), null);

        context.end();

        assertEquals(true, context.isEndRequested());
        assertEquals(false, context.isActive());
    }

    private static final class TestAbility extends OneAbility {

        TestAbility(String id, int duration) {
            super(id, settings -> settings.duration(duration));
        }
    }
}
