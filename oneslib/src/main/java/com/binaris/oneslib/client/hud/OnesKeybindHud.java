package com.binaris.oneslib.client.hud;

import java.util.ArrayList;
import java.util.List;

import com.binaris.oneslib.Ones;
import com.binaris.oneslib.OnesLib;
import com.binaris.oneslib.api.KeybindUi;
import com.binaris.oneslib.api.One;
import com.binaris.oneslib.api.OneAbility;
import com.binaris.oneslib.client.config.OnesConfig;
import com.binaris.oneslib.client.config.OnesConfig.HudPosition;
import com.binaris.oneslib.client.keybind.OnesKeybinds;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/**
 * Client HUD that renders the current One's keybinds. Two modes, per ability:
 * {@link KeybindUi#BOXES} draws the key inside a square with the ability name below it, and
 * {@link KeybindUi#ICONS} draws a black square with the ability's item texture inside and the
 * keybind beside it. Both share the same config (position, boxSize, gap, columns, colors).
 */
@Mod.EventBusSubscriber(modid = OnesLib.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class OnesKeybindHud {

    private OnesKeybindHud() {
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.options.hideGui) {
            return;
        }
        One current = Ones.currentOne(player);
        if (current == null) {
            return;
        }

        List<OneAbility> abilities = new ArrayList<>();
        for (OneAbility ability : current.abilities()) {
            KeybindUi mode = ability.settings().keybindUi();
            if ((mode == KeybindUi.BOXES || mode == KeybindUi.ICONS)
                    && ability.settings().keybind() != GLFW.GLFW_KEY_UNKNOWN) {
                abilities.add(ability);
            }
        }
        if (abilities.isEmpty()) {
            return;
        }

        Font font = minecraft.font;
        GuiGraphics gui = event.getGuiGraphics();

        int box = OnesConfig.boxSize();
        int gap = OnesConfig.gap();
        int columns = Math.min(OnesConfig.columns(), abilities.size());
        int nameGap = OnesConfig.nameGap();
        int margin = OnesConfig.margin();
        int cellPadding = OnesConfig.cellPadding();
        float nameScale = OnesConfig.nameScale();

        int nameHeight = Math.max(7, (int) Math.ceil(font.lineHeight * nameScale));
        int cellHeight = box + nameGap + nameHeight;
        int rows = (abilities.size() + columns - 1) / columns;
        int gridHeight = rows * cellHeight + (rows - 1) * gap;

        int maxNameWidth = (gui.guiWidth() - margin * 2 - gap * (columns - 1)) / columns;

        float[] scales = new float[abilities.size()];
        int[] cellWidths = new int[abilities.size()];
        int cellWidth = 0;
        for (int i = 0; i < abilities.size(); i++) {
            OneAbility ability = abilities.get(i);
            if (ability.settings().keybindUi() == KeybindUi.ICONS) {
                int keyWidth = font.width(keyOf(ability)) + 2;
                cellWidths[i] = box + keyWidth + cellPadding * 2;
            } else {
                int nameWidth = font.width(ability.settings().name());
                float scale = Math.min(nameScale, (float) maxNameWidth / Math.max(1, nameWidth));
                int renderedWidth = (int) Math.ceil(nameWidth * scale);
                scales[i] = scale;
                cellWidths[i] = Math.max(box, renderedWidth) + cellPadding * 2;
            }
            cellWidth = Math.max(cellWidth, cellWidths[i]);
        }

        int startX = margin;
        int startY = margin;
        HudPosition position = OnesConfig.position();
        int gridWidth = cellWidth * columns + gap * (columns - 1);
        if (position == HudPosition.TOP_RIGHT || position == HudPosition.BOTTOM_RIGHT) {
            startX = gui.guiWidth() - gridWidth - margin;
        }
        if (position == HudPosition.BOTTOM_LEFT || position == HudPosition.BOTTOM_RIGHT) {
            startY = gui.guiHeight() - gridHeight - margin;
        }

        int x = startX;
        int y = startY;
        for (int i = 0; i < abilities.size(); i++) {
            if (i > 0 && i % columns == 0) {
                x = startX;
                y += cellHeight + gap;
            }
            drawCell(gui, font, abilities.get(i), x, y, cellWidth, box, nameGap, scales[i]);
            x += cellWidth + gap;
        }
    }

    private static String keyOf(OneAbility ability) {
        KeyMapping mapping = OnesKeybinds.mappingFor(ability.id());
        if (mapping != null && mapping.getKey() != InputConstants.UNKNOWN) {
            return mapping.getKey().getDisplayName().getString();
        }
        return "?";
    }

    private static void drawCell(GuiGraphics gui, Font font, OneAbility ability, int x, int y, int cellWidth,
                                 int box, int nameGap, float scale) {
        String key = keyOf(ability);

        if (ability.settings().keybindUi() == KeybindUi.ICONS) {
            drawIcon(gui, font, ability, key, x, y, cellWidth, box);
            return;
        }

        String name = ability.settings().name();
        int boxX = x + (cellWidth - box) / 2;
        gui.fill(boxX - 1, y - 1, boxX + box + 1, y + box + 1, OnesConfig.borderColor());
        gui.fill(boxX, y, boxX + box, y + box, OnesConfig.backgroundColor());
        gui.drawCenteredString(font, key, boxX + box / 2, y + box / 2 - font.lineHeight / 2, OnesConfig.keyColor());

        int centerX = x + cellWidth / 2;
        PoseStack pose = gui.pose();
        pose.pushPose();
        try {
            pose.translate(centerX, y + box + nameGap, 0.0F);
            pose.scale(scale, scale, 1.0F);
            gui.drawCenteredString(font, name, 0, 0, OnesConfig.nameColor());
        } finally {
            pose.popPose();
        }
    }

    private static void drawIcon(GuiGraphics gui, Font font, OneAbility ability, String key,
                                 int x, int y, int cellWidth, int box) {
        int boxX = x + (cellWidth - box) / 2;

        gui.fill(boxX - 1, y - 1, boxX + box + 1, y + box + 1, OnesConfig.borderColor());
        gui.fill(boxX, y, boxX + box, y + box, 0xFF000000);

        if (ability.settings().iconItem() != null) {
            float scale = box / 16.0F;
            PoseStack pose = gui.pose();
            pose.pushPose();
            try {
                pose.translate(boxX, y, 0.0F);
                pose.scale(scale, scale, 1.0F);
                gui.renderItem(new ItemStack(ability.settings().iconItem()), 0, 0);
            } finally {
                pose.popPose();
            }
        } else {
            gui.drawCenteredString(font, key, boxX + box / 2, y + box / 2 - font.lineHeight / 2, OnesConfig.keyColor());
        }

        gui.drawString(font, key, boxX + box + 2, y + box / 2 - font.lineHeight / 2, OnesConfig.keyColor());
    }
}