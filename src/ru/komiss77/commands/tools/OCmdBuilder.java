package ru.komiss77.commands.tools;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import ru.komiss77.Ostrov;
import ru.komiss77.commands.OCommand;
import ru.komiss77.utils.TCUtil;


public class OCmdBuilder {

    private final String synthax;
    private final LinkedList<ArgumentBuilder<CommandSourceStack, ?>> args;
    private List<String> aliases;
    private boolean delimit;
    private String desc;

    private @Nullable Command<CommandSourceStack> cmd;
    private @Nullable Suggestor suggests;

    public OCmdBuilder(final String cmd) {
        args = new LinkedList<>();
        args.add(Commands.literal(cmd));
        synthax = "/" + cmd;
        desc = "Комманда";
        aliases = List.of();
        suggests = null;
        this.cmd = null;
    }

    public OCmdBuilder(final String cmd, final String stx) {
        args = new LinkedList<>();
        args.add(Commands.literal(cmd));
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

    private static final Set<StringArgumentType> STRING = Set.of(StringArgumentType.string(),
        StringArgumentType.greedyString(), StringArgumentType.word());

    public OCmdBuilder then(final RequiredArgumentBuilder<CommandSourceStack, ?> arg) {
        construct();
        args.add(arg);
        delimit = false;
        suggests = null;
        cmd = null;
        return this;
    }

    private void construct() {
        final ArgumentBuilder<CommandSourceStack, ?> last = args.getLast();
        if (suggests != null && last instanceof
            final RequiredArgumentBuilder<?, ?> rarg) {
            final Suggestor finSugg = suggests;
            rarg.suggests((cntx, sb) -> {
                @SuppressWarnings("unchecked")
                final CommandContext<CommandSourceStack> ccs = (CommandContext<CommandSourceStack>) cntx;
                finSugg.get(ccs).forEach(sb::suggest);
                return sb.buildFuture();
            });
        }

        if (cmd == null) last.executes(cntx -> {
            final CommandSender cs = cntx.getSource().getSender();
            cs.sendMessage(TCUtil.form("§cВведено неправ. кол-во аргументов!\n§cСинтакс комманды:\n§e" + synthax));
            return 0;
        });
        else {
//            Ostrov.log("del-" + delimit + ", sugg-" + suggests + ", arg-" + last.getClass());
            final Command<CommandSourceStack> finCMD = cmd;
            if (delimit && suggests != null && last instanceof
                final RequiredArgumentBuilder<?, ?> rarg) {
                final Suggestor finSugg = suggests;
                last.executes(cntx -> {
                    final Set<String> sgs = finSugg.get(cntx);
                    if (!STRING.contains(rarg.getType())) {
                        return finCMD.run(cntx);
                    }

                    if (!sgs.contains(Resolver.string(cntx, rarg.getName()))) {
                        final CommandSender cs = cntx.getSource().getSender();
                        cs.sendMessage(TCUtil.form("§cВибери одну опцию из списка:\n§e"
                            + String.join(", ", sgs) + "\n§cСинтакс комманды:\n§e" + synthax));
                        return 0;
                    }

                    return finCMD.run(cntx);
                });
            }
            else {
                last.executes(finCMD);
            }
        }
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
        construct();
        ArgumentBuilder<CommandSourceStack, ?> arg = args.pollLast();
        while (arg != null) {
            final ArgumentBuilder<CommandSourceStack, ?> prev = args.pollLast();
            if (prev == null) break;
            prev.then(arg);
            arg = prev;
        }

        if (arg == null) {
            Ostrov.log_warn("Could not build cmd " + synthax);
            return;
        }

        if (!(arg.build() instanceof final LiteralCommandNode<CommandSourceStack> origin)) {
            Ostrov.log_warn("Could not build cmd " + synthax);
            return;
        }

        Ostrov.regCommand(new OCommand() {
            @Override
            public LiteralCommandNode<CommandSourceStack> command() {
                return origin;
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