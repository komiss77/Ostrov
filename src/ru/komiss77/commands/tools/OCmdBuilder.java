package ru.komiss77.commands.tools;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import ru.komiss77.Ostrov;
import ru.komiss77.commands.OCommand;
import ru.komiss77.utils.TCUtil;


public class OCmdBuilder {

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
        arg = Commands.literal(cmd);
        synthax = "/" + cmd;
        desc = "Комманда";
        aliases = List.of();
        suggests = null;
        this.cmd = null;
    }

    public OCmdBuilder(final String cmd, final String stx) {
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
                final CommandContext<CommandSourceStack> ccs =
                    (CommandContext<CommandSourceStack>) cntx;
                final Map<String, ParsedArgument<CommandSourceStack, ?>> args = new HashMap<>();
                final CommandContext<CommandSourceStack> cx = new CommandContext<>(ccs.getSource(), cntx.getInput(),
                    args, ccs.getCommand(), ccs.getRootNode(), ccs.getNodes(), ccs.getRange(),
                    ccs.getChild(), ccs.getRedirectModifier(), ccs.isForked());
                final Details dts = new Details(ccs);
                dts.matching(finSugg.get(dts).stream()).forEach(sb::suggest);
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
                if (!(rarg.getType() instanceof StringArgumentType)) {
                    return finCMD.run(cntx);
                }
                final Details dts = new Details(cntx);
                final Set<String> sgs = dts.matching(finSugg.get(dts).stream());
                if (!sgs.contains(Resolver.string(cntx, rarg.getName()))) {
                    final CommandSender cs = cntx.getSource().getSender();
                    cs.sendMessage(TCUtil.form("§cВибери одну опцию из списка:\n§6"
                        + String.join(", ", sgs) + "\n§cСинтакс комманды:\n§e" + synthax));
                    return 0;
                }

                return finCMD.run(cntx);
            });
        } else arg.executes(finCMD);
        return arg.build();
    }

//    private void <S> stack() {}

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

    public static class Details extends CommandContext<CommandSourceStack> {
        public Details(final CommandContext<CommandSourceStack> cntx) {
            super(cntx.getSource(), cntx.getInput(), new HashMap<>(), cntx.getCommand(), cntx.getRootNode(),
                cntx.getNodes(), cntx.getRange(), cntx.getChild(), cntx.getRedirectModifier(), cntx.isForked());
        }

        public String arg(final int last) {
            final String in = getInput();
            final String[] args = in.split(" ");
            final int sub;
            if (in.charAt(in.length() - 1) == ' ') {
                if (last == 0) return "";
                sub = 0;
            } else sub = 1;
            final int ix = args.length - sub - last;
            return ix < 1 ? "" : args[ix];
        }

        protected Set<String> matching(final Stream<String> sgs) {
            final String arg = arg(0);
            return sgs.filter(s -> s.contains(arg))
                .collect(Collectors.toSet());
        }
    }

    public interface Suggestor {
        Set<String> get(final Details dts);
    }
}