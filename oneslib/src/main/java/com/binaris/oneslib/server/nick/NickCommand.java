package com.binaris.oneslib.server.nick;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public final class NickCommand {

    private static final int MAX_VISIBLE_LENGTH = 32;

    private NickCommand() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("nick")
                .then(Commands.literal("set")
                        .executes(ctx -> set(ctx, ctx.getSource().getPlayerOrException(), ""))
                        .then(Commands.argument("target", EntityArgument.players())
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.argument("name", StringArgumentType.greedyString())
                                        .executes(ctx -> set(ctx,
                                                EntityArgument.getPlayers(ctx, "target"),
                                                StringArgumentType.getString(ctx, "name")))))
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                .executes(ctx -> set(ctx,
                                        ctx.getSource().getPlayerOrException(),
                                        StringArgumentType.getString(ctx, "name")))))
                .then(Commands.literal("clear")
                        .executes(ctx -> clear(ctx, ctx.getSource().getPlayerOrException()))
                        .then(Commands.argument("target", EntityArgument.players())
                                .requires(source -> source.hasPermission(2))
                                .executes(ctx -> clear(ctx, EntityArgument.getPlayers(ctx, "target")))))
                .then(Commands.literal("status")
                        .executes(ctx -> status(ctx, ctx.getSource().getPlayerOrException()))
                        .then(Commands.argument("target", EntityArgument.players())
                                .requires(source -> source.hasPermission(2))
                                .executes(ctx -> status(ctx, EntityArgument.getPlayers(ctx, "target")))))
                .then(Commands.literal("list")
                        .requires(source -> source.hasPermission(2))
                        .executes(NickCommand::list));
    }

    private static int set(CommandContext<CommandSourceStack> ctx, ServerPlayer target, String rawName) {
        if (rawName.isEmpty()) {
            clear(ctx, target);
            return 1;
        }
        String message = validate(rawName);
        if (message != null) {
            ctx.getSource().sendFailure(Component.literal(message));
            return 0;
        }
        NickNameSavedData.get(ctx.getSource().getServer()).setNick(target.getUUID(), rawName);
        apply(target);
        ctx.getSource().sendSuccess(() -> Component.literal("Nickname set for "
                        + target.getGameProfile().getName() + ": ").withStyle(ChatFormatting.GREEN)
                .append(format(rawName)), true);
        return 1;
    }

    private static int set(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> targets, String rawName) {
        if (rawName.isEmpty()) {
            return clear(ctx, targets);
        }
        String message = validate(rawName);
        if (message != null) {
            ctx.getSource().sendFailure(Component.literal(message));
            return 0;
        }
        for (ServerPlayer target : targets) {
            NickNameSavedData.get(ctx.getSource().getServer()).setNick(target.getUUID(), rawName);
            apply(target);
        }
        ctx.getSource().sendSuccess(() -> Component.literal("Nickname set for "
                        + targets.size() + " player(s): ").withStyle(ChatFormatting.GREEN)
                .append(format(rawName)), true);
        return targets.size();
    }

    private static int clear(CommandContext<CommandSourceStack> ctx, ServerPlayer target) {
        NickNameSavedData.get(ctx.getSource().getServer()).clearNick(target.getUUID());
        apply(target);
        ctx.getSource().sendSuccess(() -> Component.literal("Nickname cleared for "
                + target.getGameProfile().getName() + ".").withStyle(ChatFormatting.GREEN), true);
        return 1;
    }

    private static int clear(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> targets) {
        for (ServerPlayer target : targets) {
            NickNameSavedData.get(ctx.getSource().getServer()).clearNick(target.getUUID());
            apply(target);
        }
        ctx.getSource().sendSuccess(() -> Component.literal("Nickname cleared for "
                + targets.size() + " player(s).").withStyle(ChatFormatting.GREEN), true);
        return targets.size();
    }

    private static int status(CommandContext<CommandSourceStack> ctx, ServerPlayer target) {
        String nick = NickNameSavedData.get(ctx.getSource().getServer()).getNick(target.getUUID());
        if (nick == null) {
            ctx.getSource().sendSuccess(() -> Component.literal(target.getGameProfile().getName()
                    + " has no nickname.").withStyle(ChatFormatting.YELLOW), false);
            return 0;
        }
        ctx.getSource().sendSuccess(() -> Component.literal(target.getGameProfile().getName() + ": ")
                .append(format(nick)), false);
        return 1;
    }

    private static int status(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> targets) {
        int active = 0;
        for (ServerPlayer target : targets) {
            if (NickNameSavedData.get(ctx.getSource().getServer()).getNick(target.getUUID()) != null) {
                active++;
            }
            status(ctx, target);
        }
        return active;
    }

    private static int list(CommandContext<CommandSourceStack> ctx) {
        Map<UUID, String> all = NickNameSavedData.get(ctx.getSource().getServer()).all();
        if (all.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.literal("No nicknames are active.")
                    .withStyle(ChatFormatting.YELLOW), false);
            return 0;
        }
        ctx.getSource().sendSuccess(() -> Component.literal("Active nicknames: " + all.size())
                .withStyle(ChatFormatting.YELLOW), false);
        all.forEach((uuid, rawName) -> ctx.getSource().sendSuccess(() -> Component.literal(uuid + ": ")
                .append(format(rawName)), false));
        return all.size();
    }

    public static Component formattedName(ServerPlayer player) {
        String raw = NickNameSavedData.get(player.getServer()).getNick(player.getUUID());
        return raw == null ? null : format(raw);
    }

    private static void apply(ServerPlayer player) {
        player.refreshDisplayName();
        player.refreshTabListName();
    }

    private static String validate(String rawName) {
        String visible = rawName.replaceAll("&.", "").trim();
        if (visible.isEmpty()) {
            return "Nickname cannot be blank.";
        }
        if (visible.length() > MAX_VISIBLE_LENGTH) {
            return "Nickname cannot be longer than " + MAX_VISIBLE_LENGTH + " visible characters.";
        }
        return null;
    }

    private static Component format(String rawName) {
        return Component.literal(rawName.replace("&", "\u00a7"));
    }
}