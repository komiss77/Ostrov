
package ru.komiss77.modules.kits;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.commands.OCommand;
import ru.komiss77.commands.args.Resolver;

import java.util.List;
import java.util.concurrent.CompletableFuture;


public class KitCmd implements OCommand {

  @Override
  public LiteralCommandNode<CommandSourceStack> command() {
    final String act = "action", kit = "kit";
    return Commands.literal("kit").executes(cntx-> {
        final CommandSender cs = cntx.getSource().getExecutor();
        if (!(cs instanceof final Player pl)) {
          cs.sendMessage("§eНе консольная команда!");
          return 0;
        }
        KitManager.openGuiMain(pl);
        return Command.SINGLE_SUCCESS;
      })
      .then(Resolver.string(act).suggests((cntx, sb) -> {
          final CommandSender cs = cntx.getSource().getExecutor();
          if (!(cs instanceof final Player pl)) {
            return CompletableFuture.completedFuture(sb.build());
          }
          if (ApiOstrov.isStaff(pl)) {
            sb.suggest("give");
            sb.suggest("admin");
          }
          sb.suggest("buyaccess");
          sb.suggest("sellaccess");
          sb.suggest("gui");
          return CompletableFuture.completedFuture(sb.build());
        })
        .executes(cntx-> {
          final CommandSender cs = cntx.getSource().getExecutor();
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
        .then(Resolver.string(kit).suggests((cntx, sb) -> {
          return switch (Resolver.string(cntx, act)) {
            case "buyaccess", "sellaccess", "give" -> {
              KitManager.getKitsNames().forEach(s -> sb.suggest(s));
              yield CompletableFuture.completedFuture(sb.build());
            }
            default -> CompletableFuture.completedFuture(sb.build());
          };
        }).executes(cntx-> {
          final CommandSender cs = cntx.getSource().getExecutor();
          if (!(cs instanceof final Player pl)) {
            cs.sendMessage("§eНе консольная команда!");
            return 0;
          }

          final String k = Resolver.string(cntx, kit);

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
  public List<String> aliases() {
    return List.of("кит");
  }

  @Override
  public String description() {
    return "Кит комманда";
  }
}
   
    
    
    
    
    
    
    
    
