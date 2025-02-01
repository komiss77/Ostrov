package ru.komiss77.modules.kits;

import java.util.Set;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.commands.OCommand;
import ru.komiss77.commands.tools.Resolver;

public class KitCmd implements OCommand {

    @Override
    public LiteralCommandNode<CommandSourceStack> command() {
      final String act = "action", kitName = "kitName";
        return Commands.literal("kit").executes(cntx -> {
                final CommandSender cs = cntx.getSource().getSender();
                if (!(cs instanceof final Player pl)) {
                    cs.sendMessage("§eНе консольная команда!");
                    return 0;
                }
                KitManager.openGuiMain(pl);
                return Command.SINGLE_SUCCESS;
            })
            .then(Resolver.string(act).suggests((cntx, sb) -> {
                    final CommandSender cs = cntx.getSource().getSender();
                    if (!(cs instanceof final Player pl)) {
                        return sb.buildFuture();
                    }
                    if (ApiOstrov.isStaff(pl)) {
                        sb.suggest("give");
                        sb.suggest("admin");
                    }
                    sb.suggest("buyaccess");
                    sb.suggest("sellaccess");
                    sb.suggest("gui");
                    return sb.buildFuture();
                })
                .executes(cntx -> {
                    final CommandSender cs = cntx.getSource().getSender();
                    if (!(cs instanceof final Player pl)) {
                        cs.sendMessage("§eНе консольная команда!");
                        return 0;
                    }

                    return switch (Resolver.string(cntx, act)) {
                        case "buyaccess", "sellaccess", "give" -> {
                            pl.sendMessage(Ostrov.PREFIX + "§cУкажи название набора!");
                            yield 0;
                        }
                        case "gui" -> {
                            KitManager.openGuiMain(pl);
                            yield Command.SINGLE_SUCCESS;
                        }
                        case "admin" -> {
                            if (ApiOstrov.isLocalBuilder(pl, true)) {
                                KitManager.openKitEditMain(pl);
                                yield Command.SINGLE_SUCCESS;
                            }
                            yield 0;
                        }
                        default -> 0;
                    };
                })
                .then(Commands.argument(kitName, StringArgumentType.greedyString())//.then(Resolver.string(reason)
                    //не распознаёт названия в кирилице! Выдаются в ГУИ
                    .suggests((cntx, sb) -> {
                    return switch (Resolver.string(cntx, act)) {
                        case "buyaccess", "sellaccess", "give" -> {
                            KitManager.getKitsNames().forEach(s -> sb.suggest(s));
                            yield sb.buildFuture();
                        }
                        default -> sb.buildFuture();
                    };
                }).executes(cntx -> {
                    final CommandSender cs = cntx.getSource().getSender();
                    if (!(cs instanceof final Player pl)) {
                        cs.sendMessage("§eНе консольная команда!");
                        return 0;
                    }

                      final String k = Resolver.string(cntx, kitName);

                    return switch (Resolver.string(cntx, act)) {
                        case "buyaccess" -> {
                            KitManager.buyKitAcces(pl, k.toLowerCase());
                            yield Command.SINGLE_SUCCESS;
                        }
                        case "sellaccess" -> {
                            KitManager.trySellAcces(pl, k.toLowerCase());
                            yield Command.SINGLE_SUCCESS;
                        }
                        case "give" -> {
                            if (ApiOstrov.isLocalBuilder(pl, true)) {
                                KitManager.tryGiveKit(pl, k.toLowerCase());
                                yield Command.SINGLE_SUCCESS;
                            }
                            yield 0;
                        }
                        case "gui" -> {
                            KitManager.openGuiMain(pl);
                            yield Command.SINGLE_SUCCESS;
                        }
                        case "admin" -> {
                            if (ApiOstrov.isLocalBuilder(pl, true)) {
                                KitManager.openKitEditMain(pl);
                                yield Command.SINGLE_SUCCESS;
                            }
                            yield 0;
                        }
                        default -> 0;
                    };
                })))
            .build();
    }

    @Override
    public Set<String> aliases() {
      return Set.of("kits");
    }

    @Override
    public String description() {
      return "Наборы";
    }
}
   
    
    
    
    
    
    
    
    
