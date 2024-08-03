package ru.komiss77.commands.tools;

import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;

import java.util.Set;

public interface Suggestor {
    Set<String> get(final CommandContext<CommandSourceStack> cntx);
}