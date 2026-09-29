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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/**
 * Client HUD that renders the current One's keybinds. Only abilities whose
 * {@link KeybindUi} is {@link KeybindUi#BOXES} are shown: each keybind is drawn
 * inside a square with the ability name below it, up to {@code columns} per row (never more
 * columns than the One has abilities), in the corner of the screen selected in the config.
 * Each cell reserves enough horizontal space for the longest of its box or scaled name, so
 * long names never overlap or leave the screen.
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
            if (ability.settings().keybindUi() == KeybindUi.BOXES
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
        int cellWidth = 0;
        for (int i = 0; i < abilities.size(); i++) {
            int nameWidth = font.width(abilities.get(i).settings().name());
            float scale = Math.min(nameScale, (float) maxNameWidth / Math.max(1, nameWidth));
            int renderedWidth = (int) Math.ceil(nameWidth * scale);
            scales[i] = scale;
            cellWidth = Math.max(cellWidth, Math.max(box, renderedWidth) + cellPadding * 2);
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
            drawBox(gui, font, abilities.get(i), x, y, cellWidth, box, nameGap, scales[i]);
            x += cellWidth + gap;
        }
    }

    private static void drawBox(GuiGraphics gui, Font font, OneAbility ability, int x, int y, int cellWidth,
                                int box, int nameGap, float scale) {
        String key = "?";
        KeyMapping mapping = OnesKeybinds.mappingFor(ability.id());
        if (mapping != null && mapping.getKey() != InputConstants.UNKNOWN) {
            key = mapping.getKey().getDisplayName().getString();
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
}