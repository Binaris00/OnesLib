package com.binaris.oneslib.server.progress;

/** What happens to a player's evolution when they die. */
public enum RespawnPolicy {
    /** The morph is dropped, as with any other One that does not declare {@code persistOnDeath}. */
    RESET,
    /** The stage is kept and the morph is re-applied on respawn. */
    KEEP_STAGE,
    /** The stage and the One's effects are both kept. */
    KEEP_STAGE_AND_BUFFS
}
