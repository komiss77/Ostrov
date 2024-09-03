package ru.komiss77.commands;

import com.mojang.brigadier.suggestion.SuggestionsBuilder;
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

    public static String arg(SuggestionsBuilder sb, int position) {
        int idx = sb.getInput().indexOf(" ");
        if (idx < 0) {
            return "";
        } else {
            final String[] arg = sb.getInput().substring(idx + 1).split(" ");
            if (position < arg.length) {
                return arg[position];
            } else {
                return "";
            }
        }
    }

}
