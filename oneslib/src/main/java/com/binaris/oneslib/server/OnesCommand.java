package com.binaris.oneslib.server;

import java.util.stream.Collectors;

import com.binaris.oneslib.Ones;
import com.binaris.oneslib.api.One;
import com.binaris.oneslib.common.OnesVerification;
import com.binaris.oneslib.server.morph.ServerOneManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

public final class OnesCommand {

    private static final SuggestionProvider<CommandSourceStack> ONE_IDS = (context, builder) ->
            SharedSuggestionProvider.suggest(Ones.registry().all().stream().map(One::id).toList(), builder);

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
                        .executes(OnesCommand::verify)));
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
