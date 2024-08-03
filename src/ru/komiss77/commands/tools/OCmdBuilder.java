package ru.komiss77.commands.tools;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import ru.komiss77.Ostrov;
import ru.komiss77.commands.OCommand;
import ru.komiss77.utils.TCUtil;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Set;


public class OCmdBuilder {

    private final LiteralArgumentBuilder<CommandSourceStack> origin;
    private final String synthax;


    private ArgumentBuilder<CommandSourceStack, ?> arg;
    private List<String> aliases;
    private boolean delimit;
    private String desc;

    private @Nullable Command<CommandSourceStack> cmd;
    private @Nullable Suggestor suggests;

    public OCmdBuilder(final String cmd) {
        origin = Commands.literal(cmd);
        arg = origin;
        synthax = "/" + cmd;
        desc = "Комманда";
        aliases = List.of();
        suggests = null;
        this.cmd = null;
    }

    public OCmdBuilder(final String cmd, final String stx) {
        origin = Commands.literal(cmd);
        arg = origin;
        synthax = stx;
        desc = "Комманда";
        aliases = List.of();
        suggests = null;
        this.cmd = null;
    }

    public OCmdBuilder suggest(final Suggestor sugg, final boolean force) {
        suggests = sugg;
        delimit = force;
        return this;
    }

    public OCmdBuilder run(final Command<CommandSourceStack> cmd) {
        this.cmd = cmd;
        return this;
    }

    public OCmdBuilder then(final RequiredArgumentBuilder<CommandSourceStack, ?> arg) {
        execute();
        this.arg.then(arg);
        this.arg = arg;
        return this;
    }

    private void execute() {
        if (suggests != null && arg instanceof
            final RequiredArgumentBuilder<?, ?> rarg)
            rarg.suggests((cntx, sb)-> {
                @SuppressWarnings("unchecked")
                final CommandContext<CommandSourceStack> ccs = (CommandContext<CommandSourceStack>) cntx;
                suggests.get(ccs).forEach(sb::suggest);
                return sb.buildFuture();
            });

        if (cmd == null) arg.executes(cntx->{
            final CommandSender cs = cntx.getSource().getSender();
            cs.sendMessage(TCUtil.form("§cВведено неправ. кол-во аргументов!\n§cСинтакс комманды:\n§e" + synthax));
            return 0;
        });
        else {
            if (delimit && suggests != null && arg instanceof
                final RequiredArgumentBuilder<?, ?> rarg) {
                final Suggestor sugg = suggests;
                arg.executes(cntx -> {
                    final Set<String> sgs = sugg.get(cntx);
                    if (!rarg.getType().equals(StringArgumentType.string())) {
                        return cmd.run(cntx);
                    }

                    if (!sgs.contains(Resolver.string(cntx, rarg.getName()))) {
                        final CommandSender cs = cntx.getSource().getSender();
                        cs.sendMessage(TCUtil.form("§cВибери одну опцию из списка:\n§e"
                            + String.join(", ", sgs) + "\n§cСинтакс комманды:\n§e" + synthax));
                        return 0;
                    }

                    return cmd.run(cntx);
                });
            }
            else arg.executes(cmd);
        }

        delimit = false;
        suggests = null;
        cmd = null;
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
        execute();
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