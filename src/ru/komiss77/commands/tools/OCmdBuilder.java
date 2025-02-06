package ru.komiss77.commands.tools;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import ru.komiss77.Ostrov;
import ru.komiss77.commands.OCommand;
import ru.komiss77.modules.translate.TransLiter;
import ru.komiss77.utils.TCUtil;


public class OCmdBuilder {

    private final String name;
    private final String synthax;
    private CommandNode<CommandSourceStack> last;
    private ArgumentBuilder<CommandSourceStack, ?> arg;
    private LiteralCommandNode<CommandSourceStack> origin;
    private List<String> aliases;
    private boolean delimit;
    private String desc;

    private @Nullable Command<CommandSourceStack> cmd;
    private @Nullable Suggestor suggests;

    public OCmdBuilder(final String cmd) {
        name = cmd;
        arg = Commands.literal(cmd);
        synthax = "/" + cmd;
        desc = "Комманда";
        aliases = List.of();
        suggests = null;
        this.cmd = null;
    }

    public OCmdBuilder(final String cmd, final String stx) {
        name = cmd;
        arg = Commands.literal(cmd);
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

    public OCmdBuilder then(final RequiredArgumentBuilder<CommandSourceStack, ?> rarg) {
        final CommandNode<CommandSourceStack> node = construct();
        if (last == null) origin = (LiteralCommandNode<CommandSourceStack>) node;
        else last.addChild(node);
        last = node; arg = rarg;
        delimit = false;
        suggests = null;
        cmd = null;
        return this;
    }

    private CommandNode<CommandSourceStack> construct() {
        if (suggests != null && arg instanceof
            final RequiredArgumentBuilder<?, ?> rarg) {
            final Suggestor finSugg = suggests;
            rarg.suggests((cntx, sb) -> {
                @SuppressWarnings("unchecked")
                final CommandContext<CommandSourceStack> ccs = (CommandContext<CommandSourceStack>) cntx;
                Resolver.matching(cntx, finSugg.get(ccs).stream()).forEach(sb::suggest);
                return sb.buildFuture();
            });
        }

        if (cmd == null) {
            arg.executes(cntx -> {
                final CommandSender cs = cntx.getSource().getSender();
                cs.sendMessage(TCUtil.form("§cВведено неправ. кол-во аргументов!\n§cСинтакс комманды:\n§e" + synthax));
                return 0;
            });
            return arg.build();
        }

        final Command<CommandSourceStack> finCMD = cmd;
        if (delimit && suggests != null && arg instanceof
            final RequiredArgumentBuilder<?, ?> rarg) {
            final Suggestor finSugg = suggests;
            arg.executes(cntx -> {
                if (!(rarg.getType() instanceof StringArgumentType)) return finCMD.run(cntx);
                final Set<String> suggs = finSugg.get(cntx);
                if (!suggs.contains(Resolver.string(cntx, rarg.getName()))) {
                    final CommandSender cs = cntx.getSource().getSender();
                    cs.sendMessage(TCUtil.form("§cВибери одну опцию из списка:\n§6"
                        + String.join(", ", suggs) + "\n§cСинтакс комманды:\n§e" + synthax));
                    return 0;
                }

                return finCMD.run(cntx);
            });
        } else arg.executes(finCMD);
        return arg.build();
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
        final CommandNode<CommandSourceStack> node = construct();
        if (last == null) origin = (LiteralCommandNode<CommandSourceStack>) node;
        else last.addChild(node);

        if (origin == null) {
            Ostrov.log_warn("Could not build cmd " + synthax);
            return;
        }

        final Set<String> convs = new HashSet<>(aliases);
        for (final String als : aliases)
            convs.add(TransLiter.reLayOut(als));
        convs.add(TransLiter.reLayOut(name));
        Ostrov.regCommand(new OCommand() {
            public LiteralCommandNode<CommandSourceStack> command() {
                return origin;
            }
            public Set<String> aliases() {
                return convs;
            }
            public String description() {
                return desc;
            }
        });
    }

    public interface Suggestor {
        Set<String> get(final CommandContext<CommandSourceStack> dts);
    }
}