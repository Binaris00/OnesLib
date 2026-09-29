package com.binaris.oneslib.server.progress;

import java.util.List;

/**
 * An ordered line of One stages with a progress bar, owned by the library instead of by every
 * consumer.
 *
 * <p>It exists because the alternative is what every project ends up writing: a manager class
 * with its own NBT root, a hand-kept list of ability ids to decide when a segment is complete
 * (which silently breaks the moment a stage gains a new ability), and a boss bar whose lifecycle
 * leaks across logins. Deriving the ability list from {@code One.abilities()} is the point of
 * this class: the source of truth is the registered One, so adding an ability updates the bar
 * with no second edit.
 *
 * <pre>{@code
 * Evolution evolution = EvolutionBuilder.create("godzilla_line")
 *         .stage("egg")
 *         .stage("baby")
 *         .stage("teen")
 *         .stage("adult")
 *         .trackUsage()
 *         .segments(5)
 *         .barColor(BarColor.PURPLE)
 *         .respawnPolicy(RespawnPolicy.KEEP_STAGE)
 *         .build();
 *
 * EvolutionManager.INSTANCE.register(evolution);
 * }</pre>
 */
public record Evolution(String id, List<String> stages, int segments, boolean trackUsage,
                        RespawnPolicy respawnPolicy, int color) {

    public Evolution {
        stages = List.copyOf(stages);
        if (stages.size() < 2) {
            throw new IllegalArgumentException("An evolution needs at least two stages");
        }
        if (segments < 1) {
            throw new IllegalArgumentException("segments must be >= 1");
        }
    }

    public int stageIndex(String oneId) {
        return this.stages.indexOf(oneId);
    }

    public boolean contains(String oneId) {
        return this.stages.contains(oneId);
    }

    public String firstStage() {
        return this.stages.get(0);
    }

    public String lastStage() {
        return this.stages.get(this.stages.size() - 1);
    }

    public String nextStage(String oneId) {
        int index = this.stageIndex(oneId);
        if (index < 0 || index + 1 >= this.stages.size()) {
            return null;
        }
        return this.stages.get(index + 1);
    }

    public boolean isLast(String oneId) {
        return oneId != null && oneId.equals(this.lastStage());
    }

    public int indexOfSegment(int completed) {
        return Math.max(0, Math.min(this.segments - 1, completed));
    }

    public float progress(int completed) {
        return this.segments <= 0 ? 1.0F : Math.min(1.0F, (float) completed / (float) this.segments);
    }
}
