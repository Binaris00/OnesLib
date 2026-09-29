package com.binaris.oneslib.server;

import java.util.Optional;
import java.util.stream.Collectors;

import com.binaris.oneslib.Ones;
import com.binaris.oneslib.api.EndReason;
import com.binaris.oneslib.api.One;
import com.binaris.oneslib.common.OnesVerification;
import com.binaris.oneslib.server.ability.AbilityEngine;
import com.binaris.oneslib.server.morph.ServerOneManager;
import com.binaris.oneslib.server.progress.Evolution;
import com.binaris.oneslib.server.progress.EvolutionData;
import com.binaris.oneslib.server.progress.EvolutionManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class OnesCommand {

    private static final SuggestionProvider<CommandSourceStack> ONE_IDS = (context, builder) ->
            SharedSuggestionProvider.suggest(Ones.registry().all().stream().map(One::id).toList(), builder);

    private static final SuggestionProvider<CommandSourceStack> EVOLUTION_IDS = (context, builder) ->
            SharedSuggestionProvider.suggest(
                    EvolutionManager.INSTANCE.evolutions().stream().map(Evolution::id).toList(), builder);

    private OnesCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("ones")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("morph")
                        .then(Commands.argument("one", StringArgumentType.word())
                                .suggests(ONE_IDS)
                                .executes(OnesCommand::morph)))
                .then(Commands.literal("demorph")
                        .executes(OnesCommand::demorph))
                .then(Commands.literal("list")
                        .executes(OnesCommand::list))
                .then(Commands.literal("verify")
                        .executes(OnesCommand::verify))
                .then(Commands.literal("give")
                        .then(Commands.argument("one", StringArgumentType.word())
                                .suggests(ONE_IDS)
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(OnesCommand::give))))
                .then(Commands.literal("remove")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(OnesCommand::remove)))
                .then(Commands.literal("reset")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(OnesCommand::reset)))
                .then(Commands.literal("progress")
                        .then(Commands.literal("join")
                                .then(Commands.argument("evolution", StringArgumentType.word())
                                        .suggests(EVOLUTION_IDS)
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(OnesCommand::progressJoin))))
                        .then(Commands.literal("advance")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(OnesCommand::progressAdvance)))
                        .then(Commands.literal("show")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(OnesCommand::progressShow)))));
    }

    private static int morph(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String id = StringArgumentType.getString(context, "one");
        if (Ones.registry().byId(id).isEmpty()) {
            context.getSource().sendFailure(Component.literal("Unknown One '" + id + "'"));
            return 0;
        }
        ServerOneManager.INSTANCE.morph(context.getSource().getPlayerOrException(), id);
        context.getSource().sendSuccess(() -> Component.literal("Morphed into '" + id + "'"), false);
        return 1;
    }

    private static int give(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String id = StringArgumentType.getString(context, "one");
        if (Ones.registry().byId(id).isEmpty()) {
            context.getSource().sendFailure(Component.literal("Unknown One '" + id + "'"));
            return 0;
        }
        ServerPlayer target = EntityArgument.getPlayer(context, "player");
        ServerOneManager.INSTANCE.morph(target, id);
        context.getSource().sendSuccess(
                () -> Component.literal("Morphed " + target.getName().getString() + " into '" + id + "'"), false);
        return 1;
    }

    private static int remove(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "player");
        if (ServerOneManager.INSTANCE.active(target).isEmpty()) {
            context.getSource().sendFailure(
                    Component.literal(target.getName().getString() + " is not morphed"));
            return 0;
        }
        ServerOneManager.INSTANCE.demorph(target);
        context.getSource().sendSuccess(
                () -> Component.literal("Demorphed " + target.getName().getString()), false);
        return 1;
    }

    /**
     * The "restore everything to a safe default state" operation QA keeps asking for: drops the
     * morph, clears every ability, its cooldowns and its effects. Consumers used to implement
     * this per project.
     */
    private static int reset(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "player");
        ServerOneManager.INSTANCE.demorph(target);
        AbilityEngine.INSTANCE.deactivateAll(target, EndReason.CANCELLED);
        AbilityEngine.INSTANCE.clearAll(target);
        context.getSource().sendSuccess(
                () -> Component.literal("Reset " + target.getName().getString() + " to a clean state"), false);
        return 1;
    }

    private static int demorph(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerOneManager.INSTANCE.demorph(context.getSource().getPlayerOrException());
        context.getSource().sendSuccess(() -> Component.literal("Morph cleared"), false);
        return 1;
    }

    private static int list(CommandContext<CommandSourceStack> context) {
        String ids = Ones.registry().all().stream().map(One::id).collect(Collectors.joining(", "));
        context.getSource().sendSuccess(
                () -> Component.literal(ids.isEmpty() ? "No Ones registered" : "Ones: " + ids), false);
        return 1;
    }

    private static int progressJoin(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String id = StringArgumentType.getString(context, "evolution");
        Optional<Evolution> evolution = EvolutionManager.INSTANCE.byId(id);
        if (evolution.isEmpty()) {
            context.getSource().sendFailure(Component.literal("Unknown evolution '" + id + "'"));
            return 0;
        }
        ServerPlayer target = EntityArgument.getPlayer(context, "player");
        EvolutionManager.INSTANCE.join(target, evolution.get());
        context.getSource().sendSuccess(() -> Component.literal(
                target.getName().getString() + " joined evolution '" + id + "' at stage 0"), false);
        return 1;
    }

    private static int progressAdvance(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "player");
        if (EvolutionManager.INSTANCE.advance(target)) {
            context.getSource().sendSuccess(() -> Component.literal(
                    target.getName().getString() + " advanced to stage "
                            + EvolutionManager.INSTANCE.stageIndex(target)), false);
            return 1;
        }
        context.getSource().sendFailure(Component.literal(
                target.getName().getString() + " is not in an evolution or is already at the last stage"));
        return 0;
    }

    private static int progressShow(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "player");
        Optional<Evolution> evolution = EvolutionManager.INSTANCE.evolutionOf(target);
        if (evolution.isEmpty()) {
            context.getSource().sendFailure(
                    Component.literal(target.getName().getString() + " is not in an evolution"));
            return 0;
        }
        Evolution current = evolution.get();
        context.getSource().sendSuccess(() -> Component.literal(
                current.id() + " stage " + EvolutionManager.INSTANCE.stageIndex(target) + "/"
                        + (current.stages().size() - 1) + " ('"
                        + EvolutionManager.INSTANCE.stageOneId(target) + "') used "
                        + EvolutionData.get(target).usedCount(target.getUUID())), false);
        return 1;
    }

    private static int verify(CommandContext<CommandSourceStack> context) {
        OnesVerification.Report report = OnesVerification.verify();
        if (report.clean()) {
            context.getSource().sendSuccess(() -> Component.literal(
                    "Ones Lib OK: " + report.ones() + " Ones, " + report.abilities() + " abilities"), false);
            return 1;
        }
        for (String problem : report.problems()) {
            context.getSource().sendFailure(Component.literal(problem));
        }
        return 0;
    }
}
