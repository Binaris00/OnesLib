package com.binaris.oneslib.server.progress;

import java.util.ArrayList;
import java.util.List;

public final class EvolutionBuilder {

    private final String id;
    private final List<String> stages = new ArrayList<>();
    private int segments = 5;
    private boolean trackUsage;
    private RespawnPolicy respawnPolicy = RespawnPolicy.RESET;
    private int color = 0xFF8000FF;

    private EvolutionBuilder(String id) {
        this.id = id;
    }

    public static EvolutionBuilder create(String id) {
        return new EvolutionBuilder(id);
    }

    /** Adds a stage. The order they are declared in is the evolution order. */
    public EvolutionBuilder stage(String oneId) {
        this.stages.add(oneId);
        return this;
    }

    /** Number of filled segments needed to advance one stage. */
    public EvolutionBuilder segments(int segments) {
        this.segments = segments;
        return this;
    }

    /**
     * Counts a stage as complete once the player has used every ability of that stage's One.
     * The ability list is read from {@code One.abilities()}, never hand-maintained.
     */
    public EvolutionBuilder trackUsage() {
        this.trackUsage = true;
        return this;
    }

    public EvolutionBuilder respawnPolicy(RespawnPolicy respawnPolicy) {
        this.respawnPolicy = respawnPolicy;
        return this;
    }

    /** ARGB color of the boss bar. */
    public EvolutionBuilder barColor(int color) {
        this.color = color;
        return this;
    }

    public Evolution build() {
        return new Evolution(this.id, this.stages, this.segments, this.trackUsage, this.respawnPolicy,
                this.color);
    }
}
