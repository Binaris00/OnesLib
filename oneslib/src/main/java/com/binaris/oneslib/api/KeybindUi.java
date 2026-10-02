package com.binaris.oneslib.api;

/**
 * Controls how an ability's keybind is shown on the client HUD while the player is morphed.
 */
public enum KeybindUi {
    /** No HUD element is rendered for this ability. */
    NONE,
    /** The keybind is rendered inside a square with the ability name below it. */
    BOXES,
    /** A black square with the ability's item texture inside and the keybind drawn beside it. */
    ICONS
}