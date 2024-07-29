package ru.komiss77.commands;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;

import java.util.List;

public interface OCommand {
    LiteralCommandNode<CommandSourceStack> command();

    List<String> aliases();

    String description();
}
