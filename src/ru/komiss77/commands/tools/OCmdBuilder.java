package ru.komiss77.commands.tools;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import ru.komiss77.Ostrov;
import ru.komiss77.commands.OCommand;
import ru.komiss77.utils.TCUtils;


public class OCmdBuilder {

    private final LiteralArgumentBuilder<CommandSourceStack> origin;
    private final String synthax;


    private ArgumentBuilder<CommandSourceStack, ?> command;
    private List<String> aliases;
    private String desc;

    private @Nullable Command<CommandSourceStack> run;

    public OCmdBuilder(final String cmd) {
        origin = Commands.literal(cmd);
        command = origin;
        synthax = "/" + cmd;
        desc = "Комманда";
        aliases = List.of();
        run = null;
    }

    public OCmdBuilder(final String cmd, final String stx) {
        origin = Commands.literal(cmd);
        command = origin;
        synthax = stx;
        desc = "Комманда";
        aliases = List.of();
        run = null;
    }

    public OCmdBuilder suggest(final Suggestor suggests) {
        if (command instanceof final RequiredArgumentBuilder<?, ?> rarg)
            rarg.suggests((cntx, sb)-> {
                suggests.get(cntx).forEach(sb::suggest);
                return sb.buildFuture();
            });
        return this;
    }

    public OCmdBuilder run(final Command<CommandSourceStack> cmd) {
        run = cmd;
        return this;
    }

    public OCmdBuilder then(final RequiredArgumentBuilder<CommandSourceStack, ?> arg) {
        if (run == null) command.executes(cntx->{
            final CommandSender cs = cntx.getSource().getSender();
            cs.sendMessage(TCUtils.form("§cВведено неправ. кол-во аргументов!\n§cСинтакс комманды:\n§e" + synthax));
            return 0;
        });
        else command.executes(run);
        command.then(arg);
        command = arg;
        run = null;
        return this;
    }

    public OCmdBuilder description(final String desc) {
        this.desc = desc;
        return this;
    }

    public OCmdBuilder aliases(final String... aliases) {
        this.aliases = Arrays.asList(aliases);
        return this;
    }

    public void register() {
        if (run == null) command.executes(cntx->{
            final CommandSender cs = cntx.getSource().getSender();
            cs.sendMessage(TCUtils.form("§cВведено неправ. кол-во аргументов!\n§cСинтакс комманды:\n§e" + synthax));
            return 0;
        });
        else command.executes(run);
        Ostrov.regCommand(new OCommand() {
            @Override
            public LiteralCommandNode<CommandSourceStack> command() {
                return origin.build();
            }

            @Override
            public List<String> aliases() {
                return aliases;
            }

            @Override
            public String description() {
                return desc;
            }
        });
    }
}