package ru.komiss77.commands.tools;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
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


public class OCmdBuilder {

    private final LiteralArgumentBuilder<CommandSourceStack> origin;
    private final String synthax;

    private ArgumentBuilder<CommandSourceStack, ?> curr;
    private ArgumentBuilder<CommandSourceStack, ?> last;
    private List<String> aliases;
    private boolean delimit;
    private String desc;

    private @Nullable Command<CommandSourceStack> cmd;
    private @Nullable Suggestor suggests;

    public OCmdBuilder(final String cmd) {
        origin = Commands.literal(cmd);
        curr = origin;
        last = null;
        synthax = "/" + cmd;
        desc = "Комманда";
        aliases = List.of();
        suggests = null;
        this.cmd = null;
    }

    public OCmdBuilder(final String cmd, final String stx) {
        origin = Commands.literal(cmd);
        curr = origin;
        last = null;
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
        construct();
        last = curr;
//        this.current.then(arg);
        curr = arg;
        return this;
    }

    private static final Set<StringArgumentType> STRING = Set.of(StringArgumentType.string(),
        StringArgumentType.greedyString(), StringArgumentType.word());

    private void construct() {
        if (suggests != null && curr instanceof
            final RequiredArgumentBuilder<?, ?> rarg) {
            final Suggestor finSugg = suggests;
            rarg.suggests((cntx, sb) -> {
                @SuppressWarnings("unchecked")
                final CommandContext<CommandSourceStack> ccs = (CommandContext<CommandSourceStack>) cntx;
                finSugg.get(ccs).forEach(sb::suggest);
                return sb.buildFuture();
            });
        }

        if (cmd == null) curr.executes(cntx->{
            final CommandSender cs = cntx.getSource().getSender();
            cs.sendMessage(TCUtil.form("§cВведено неправ. кол-во аргументов!\n§cСинтакс комманды:\n§e" + synthax));
            return 0;
        });
        else {
//            Ostrov.log("del-" + delimit + ", sugg-" + suggests + ", arg-" + curr.getClass());
            final Command<CommandSourceStack> finCMD = cmd;
            if (delimit && suggests != null && curr instanceof
                final RequiredArgumentBuilder<?, ?> rarg) {
                final Suggestor finSugg = suggests;
                curr.executes(cntx -> {
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
                curr.executes(finCMD);
            }
        }

        if (last != null) last.then(curr);
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
        construct();
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