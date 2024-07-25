package ru.komiss77.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent.Builder;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.BookMeta;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.commands.args.Resolver;
import ru.komiss77.enums.Stat;
import ru.komiss77.listener.InteractLst;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.player.profile.E_Pass;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.StackBuilder;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


public class PassportCmd implements OCommand {

  @Override
  public LiteralCommandNode<CommandSourceStack> command() {
    final String act = "action";
    return Commands.literal("passport").executes(cntx-> {
        final CommandSender cs = cntx.getSource().getExecutor();
        if (!(cs instanceof final Player pl)) {
          cs.sendMessage("§eНе консольная команда!");
          return 0;
        }

        final Oplayer op = PM.getOplayer(pl);
        if (op.isGuest) {
          cs.sendMessage(Ostrov.PREFIX+"§cГостям паспорт не выдавался! Зарегайтесь!");
          return 0;
        }
        op.menu.openPassport(op.getPlayer());
        return Command.SINGLE_SUCCESS;
      })
      .then(Resolver.string(act).suggests((cntx, sb) -> {
          sb.suggest("give");
          sb.suggest("edit");
          Bukkit.getOnlinePlayers().forEach(p -> sb.suggest(p.getName()));
          return CompletableFuture.completedFuture(sb.build());
        })
        .executes(cntx-> {
          final CommandSender cs = cntx.getSource().getExecutor();
          if (!(cs instanceof final Player pl)) {
            cs.sendMessage("§eНе консольная команда!");
            return 0;
          }

          final Oplayer op = PM.getOplayer(pl);
          final String arg = Resolver.string(cntx, act);
          switch (arg) {
            case "give":
              pl.closeInventory();
              final int slot = ItemUtils.findItem(pl, InteractLst.passport);
              if (slot>=0) {
                //if (p.getInventory().getItemInMainHand().getType()!=Material.AIR) {
                cs.sendMessage(Ostrov.PREFIX+"§cУ вас уже есть копия паспотра, слот "+slot+"!");
                return 0;
              }
              if (ItemUtils.giveItemTo(pl, InteractLst.passport.clone(), 4, false)) {
                pl.sendMessage("§7Вот твой паспорт!");
              }
              return Command.SINGLE_SUCCESS;
            case "edit":
              if (op.isGuest) {
                cs.sendMessage(Ostrov.PREFIX+"§cГостям паспорт не выдавался! Зарегайтесь!");
                return 0;
              }
              op.menu.openPassport(op.getPlayer());
              return Command.SINGLE_SUCCESS;
            default:
              final Player tgt = Bukkit.getPlayerExact(arg);
              if (tgt == null) {
                pl.sendMessage(Ostrov.PREFIX+"§cТакой игрок не онлайн");
                return 0;
              }

              if (ApiOstrov.isStaff(pl) || op.getStat(Stat.PLAY_TIME)>18000) {
                //PassportHandler.showPasport(p,arg[1]);
                //ApiOstrov.sendMessage(p, Action.SHOW_PASSPORT, 0, 0, arg[1], "");
                pl.sendMessage("не готово");
                return Command.SINGLE_SUCCESS;
              }
              cs.sendMessage(Ostrov.PREFIX+"§cПросматривать чужой паспорт может персонал или люди наигравшие боьлее 5 часов!");
              return 0;
          }
        }))
      .build();
  }

  @Override
  public List<String> aliases() {
    return List.of("пасспорт");
  }

  @Override
  public String description() {
    return "Присмотр паспорта";
  }

  /*private void help(final Player p) {
    p.sendMessage(Component.text("§3/passport see <ник> - §7посмотреть паспорт игрока §8<<клик")
      .hoverEvent(HoverEvent.showText(Component.text("§aКлик - набрать")))
      .clickEvent(ClickEvent.suggestCommand("/passport see ")));

    p.sendMessage(Component.text("§3/passport get - §7получить копию паспорта §8<<клик")
      .hoverEvent(HoverEvent.showText(Component.text("§aКлик - набрать")))
      .clickEvent(ClickEvent.suggestCommand("/passport get")));
  }*/
    
    public static void showLocal(final Player owner, final Player target) {
        createBook(owner, PM.getPassportData(PM.getOplayer(target), false));
    }

    
    public static void showGlobal(final Player player, final String bungee_raw_data) {
        Map<E_Pass,String>pass_data=new HashMap<>();
        createBook(player, pass_data);
        player.playSound(player.getEyeLocation(), Sound.BLOCK_SNOW_STEP, 0.5F, 2F);
    }
    
    
    
    
    
    private static void createBook (Player player, Map<E_Pass, String> pass_data) {
        
        final Builder page1 = Component.text().content("  §4§lПаспорт Островитянина\n");
        final Builder page2 = Component.text();
        final Builder page3 = Component.text();
        final Builder page4 = Component.text();
        
        String value;
        int int_value;
        
        for (final E_Pass pass : pass_data.keySet()) {
            value = pass_data.get(pass);//pass.default_value;
            int_value=ApiOstrov.getInteger(value, 0);
                
                switch (pass) {
                        
                    case SIENCE -> value = ApiOstrov.dateFromStamp(int_value);
                        
                    case PLAY_TIME -> value = ApiOstrov.secondToTime(int_value);// + "\n §3("+ApiOstrov.secondToTime(op.);
                        
                    case REPUTATION -> //int_value = int_value + (pass_data.containsKey(Data.РЕПУТАЦИЯ_БАЗА) ? Integer.valueOf(pass_data.get(Data.РЕПУТАЦИЯ_БАЗА)): 0);
                        value = (int_value<0?"§4":(int_value>0?"§2":"§1"))+int_value;
                        
                    case KARMA -> value = (int_value<0?"§4":(int_value>0?"§2":"§1"))+int_value;
                        
                    case BIRTH -> {
                        if (value.length()==10 && Ostrov.isInteger(value.substring(6, 10))) {
                            value = value+" ("+(Calendar.getInstance().get(Calendar.YEAR) - Integer.parseInt(value.substring(6, 10)))+")";
                        }
                    }
                        
                    case IPPROTECT -> value = int_value==0 ? "§5Нет" : "§bДа";
                    
                    default -> {}
					
                }
            
                
                if (pass.slot<=8) {
                    
                    page1.append(Component.text("§6"+pass.item_name+"\n §1"+value+"\n"));
                    //text= new TextComponent("§6"+pass.item_name+"\n §1"+value+"\n");
                    //page1.addExtra(text);
                    
                } else if (pass.slot>=9 && pass.slot<=17) {
                    
                    page2.append(Component.text("§6"+pass.item_name+"\n §1"+value+"\n"));
                    //text= new TextComponent("§6"+pass.item_name+"\n §1"+value+"\n");
                    //page2.addExtra(text);
                    
                } else if (pass.slot>=18 && pass.slot<=26) {
                    
                    page3.append(Component.text("§6"+pass.item_name+"\n §1"+value+"\n"));
                    //text= new TextComponent("§6"+pass.item_name+"\n §1"+value+"\n");
                    //page3.addExtra(text);
                    
                } else if (pass.slot>=27 && pass.slot<=35) {
                    
                    switch (pass) {
                        
                        case DISCORD, PHONE -> page4.append(Component.text("§6"+pass.item_name+": §1"+value.replaceAll(" ", " §1")+"\n"));
                    //text= new TextComponent("§6"+pass.item_name+": §1"+value.replaceAll(" ", " §1")+"\n");
                        case EMAIL -> page4.append(Component.text("§6"+pass.item_name+"\n §1"+value.replaceAll(" ", " §1")+"\n"));
                    //text= new TextComponent("§6"+pass.item_name+"\n §1"+value.replaceAll(" ", " §1")+"\n");
                            
                        case ABOUT ->  page4.append(Component.text("§6"+pass.item_name+"\n §1"+value.replaceAll(" ", " §1")));
                    //text= new TextComponent("§6"+pass.item_name+"\n §1"+value.replaceAll(" ", " §1"));
                            
                        default -> {
                            if (value.equals("не указано")) {
                                //text= new TextComponent("§6"+pass.item_name+": §1"+value+"\n");
                            } else {
                                page4.append(Component.text("§6"+pass.item_name+": §1§nссылка (клик)\n")
                                        .hoverEvent(HoverEvent.showText(Component.text("Клик - открыть")))
                                        .clickEvent(ClickEvent.openUrl(value)));
                            }
                        }
                    }
                }
            
        }
        
         
        final ItemStack book = StackBuilder.of(ItemType.WRITTEN_BOOK)
                .name("Паспорт Островитянина")
                .build();
        
        final BookMeta bookMeta = (BookMeta) book.getItemMeta();
        
        bookMeta.addPages(page1.build(), page2.build(), page3.build(), page4.build());

        bookMeta.setTitle("Паспорт");
        bookMeta.setAuthor("Остров77");

        book.setItemMeta(bookMeta);  
        
        player.openBook(book);
        //open(player,book );
    }
    
    
    
    
    
  
    
    
    
    
    
    
    


}
    
    
 
