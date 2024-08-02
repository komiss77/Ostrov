package ru.komiss77.commands.tools;

import com.mojang.brigadier.context.CommandContext;

import java.util.List;

public interface Suggestor {
    List<String> get(final CommandContext<?> cntx);
}
