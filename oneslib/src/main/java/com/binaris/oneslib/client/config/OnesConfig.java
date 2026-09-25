package com.binaris.oneslib.client.config;

import com.binaris.oneslib.OnesLib;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

/**
 * Client-side configuration for the OnesLib HUD. All values can be edited in the
 * {@code oneslib-client.toml} file inside the config directory.
 */
@Mod.EventBusSubscriber(modid = OnesLib.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class OnesConfig {

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.IntValue BOX_SIZE;
    private static final ForgeConfigSpec.IntValue GAP;
    private static final ForgeConfigSpec.IntValue COLUMNS;
    private static final ForgeConfigSpec.IntValue NAME_GAP;
    private static final ForgeConfigSpec.IntValue MARGIN;
    private static final ForgeConfigSpec.IntValue CELL_PADDING;
    private static final ForgeConfigSpec.DoubleValue NAME_SCALE;
    private static final ForgeConfigSpec.ConfigValue<Integer> BORDER_COLOR;
    private static final ForgeConfigSpec.ConfigValue<Integer> BACKGROUND_COLOR;
    private static final ForgeConfigSpec.ConfigValue<Integer> KEY_COLOR;
    private static final ForgeConfigSpec.ConfigValue<Integer> NAME_COLOR;
    private static final ForgeConfigSpec.EnumValue<HudPosition> POSITION;

    public static final ForgeConfigSpec SPEC;

    static {
        BUILDER.comment("HUD that renders the current One's keybinds on the screen.",
                        "Color values are in ARGB hexadecimal format, e.g. 0xFFFFFFFF is opaque white.")
                .push("hud");

        POSITION = BUILDER.comment("Corner of the screen where the HUD is drawn.",
                                  "Allowed values: TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT.")
                .defineEnum("position", HudPosition.BOTTOM_RIGHT);
        BOX_SIZE = BUILDER.comment("Size of the square each keybind is drawn inside.")
                .defineInRange("boxSize", 10, 1, 256);
        GAP = BUILDER.comment("Spacing between cells.")
                .defineInRange("gap", 0, 0, 64);
        COLUMNS = BUILDER.comment("Number of cells per row.")
                .defineInRange("columns", 4, 1, 16);
        NAME_GAP = BUILDER.comment("Vertical gap between the keybind box and its name.")
                .defineInRange("nameGap", 2, 0, 64);
        MARGIN = BUILDER.comment("Distance between the HUD and the screen edges.")
                .defineInRange("margin", 2, 0, 256);
        CELL_PADDING = BUILDER.comment("Padding between a cell's content and its borders.")
                .defineInRange("cellPadding", 2, 0, 64);
        NAME_SCALE = BUILDER.comment("Scale of the ability name text.")
                .defineInRange("nameScale", 0.4D, 0.1D, 2.0D);
        BORDER_COLOR = BUILDER.comment("ARGB color of the box border.")
                .define("borderColor", 0xFF808080);
        BACKGROUND_COLOR = BUILDER.comment("ARGB color of the box background.")
                .define("backgroundColor", 0x99000000);
        KEY_COLOR = BUILDER.comment("ARGB color of the key name text.")
                .define("keyColor", 0xFFFFFFFF);
        NAME_COLOR = BUILDER.comment("ARGB color of the ability name text.")
                .define("nameColor", 0xFFFFFFFF);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    private OnesConfig() {
    }

    public static HudPosition position() {
        return POSITION.get();
    }

    public static int boxSize() {
        return BOX_SIZE.get();
    }

    public static int gap() {
        return GAP.get();
    }

    public static int columns() {
        return COLUMNS.get();
    }

    public static int nameGap() {
        return NAME_GAP.get();
    }

    public static int margin() {
        return MARGIN.get();
    }

    public static int cellPadding() {
        return CELL_PADDING.get();
    }

    public static float nameScale() {
        return NAME_SCALE.get().floatValue();
    }

    public static int borderColor() {
        return BORDER_COLOR.get();
    }

    public static int backgroundColor() {
        return BACKGROUND_COLOR.get();
    }

    public static int keyColor() {
        return KEY_COLOR.get();
    }

    public static int nameColor() {
        return NAME_COLOR.get();
    }

    /** Corner of the screen where the HUD is rendered. */
    public enum HudPosition {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT
    }

    @SubscribeEvent
    public static void onLoad(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == SPEC) {
            OnesLib.LOGGER.debug("OnesLib client config loaded.");
        }
    }

    @SubscribeEvent
    public static void onReload(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == SPEC) {
            OnesLib.LOGGER.debug("OnesLib client config reloaded.");
        }
    }
}