package com.binaris.oneslib.server.ability;

public final class CooldownData {

    private int remainingTicks;

    public CooldownData(int remainingTicks) {
        this.remainingTicks = remainingTicks;
    }

    public int remainingTicks() {
        return this.remainingTicks;
    }

    public void add(int ticks) {
        this.remainingTicks += ticks;
    }

    public boolean tick() {
        this.remainingTicks--;
        return this.remainingTicks <= 0;
    }
}
