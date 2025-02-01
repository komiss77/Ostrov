package ru.komiss77.commands;

import java.util.Set;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;

public interface OCommand {
    LiteralCommandNode<CommandSourceStack> command();

    Set<String> aliases();

    String description();
}
