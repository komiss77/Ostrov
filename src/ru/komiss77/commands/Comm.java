package ru.komiss77.commands;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;

import java.util.List;

public class Comm implements OCommand {
  @Override
  public LiteralCommandNode<CommandSourceStack> command() {
    return null;
  }

  @Override
  public List<String> aliases() {
    return List.of();
  }

  @Override
  public String description() {
    return "";
  }
}
