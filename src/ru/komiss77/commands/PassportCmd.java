package ru.komiss77.commands;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent.Builder;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.enums.Stat;
import ru.komiss77.listener.InteractLst;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.player.profile.E_Pass;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtils;




public class PassportCmd implements CommandExecutor {
    
    

   // public PassportCmd() {
 //       init();
//    }
//    
    private void help(final Player p) {
        p.sendMessage(Component.text("§3/passport see <ник> - §7посмотреть паспорт игрока §8<<клик")
            	.hoverEvent(HoverEvent.showText(Component.text("§aКлик - набрать")))
            	.clickEvent(ClickEvent.suggestCommand("/passport see ")));
        
        p.sendMessage(Component.text("§3/passport get - §7получить копию паспорта §8<<клик")
            	.hoverEvent(HoverEvent.showText(Component.text("§aКлик - набрать")))
            	.clickEvent(ClickEvent.suggestCommand("/passport get")));
    }

    
    
    
    
    
    
    @Override
    public boolean onCommand(CommandSender cs, Command cmd, String string, String[] arg) {
        
        if ( ! (cs instanceof Player) ) {
            if (arg.length==1 && arg[0].equals("reload")) {
        //        reload();
            } else {
                cs.sendMessage("§e/"+this.getClass().getSimpleName()+" reload §7- перезагрузить настройки команды");
            }
            return true;
        }

        final Player p=(Player) cs;
        final Oplayer op = PM.getOplayer(p);
        //if (!allow_command) {
        //    p.sendMessage( "");
        //    return true;
        //}
            
            switch (arg.length) { 
                
                case 0 -> {
                }
                    
                case 1 -> {
                    
                    switch (arg[0]) {
                        
                        case "get" -> {
                            p.closeInventory();
                            final int slot = ItemUtils.findItem(p, InteractLst.passport);
                            if (slot>0) {
                                //if (p.getInventory().getItemInMainHand().getType()!=Material.AIR) {
                                cs.sendMessage(Ostrov.PREFIX+"§cУ вас уже есть копия паспотра, слот "+slot+"!");
                                return true;
                            }
                            if (ItemUtils.giveItemTo(p, InteractLst.passport.clone(), 4, false)) {
                                p.sendMessage("§7Вот Ваш паспорт!");
                            }
                        }
                        
                        case "see" -> {
                            p.closeInventory();
                            cs.sendMessage(Ostrov.PREFIX+"§cУкажите ник!");
                            return true;
                        }
                        
                        case "edit" -> {
                            if (op.isGuest) {
                                cs.sendMessage(Ostrov.PREFIX+"§cГостям паспорт не выдавался! Зарегайтесь!");
                            } else {
                                op.menu.openPassport(op.getPlayer());
                            }
                            return true;
                        }

                    }
                }
                    
                case 2 -> {
                    if (arg[0].equals("see")) {
                        if (arg[0].equals(cs.getName()) || PM.getOplayer(cs.getName()).hasGroup("moder") || p.hasPermission("pasport.see")|| op.getStat(Stat.PLAY_TIME)>18000) { 
                            //PassportHandler.showPasport(p,arg[1]);
                            //ApiOstrov.sendMessage(p, Action.SHOW_PASSPORT, 0, 0, arg[1], "");
                            p.sendMessage("не готово");
                        } else {
                            cs.sendMessage(Ostrov.PREFIX+"§cПросматривать чужой паспорт могут модераторы,вип,премиум или наигравшие боьльше 300 часов!");
                        }
                        return true;
                    }
                }
                    
                    
            }

        help(p);
        return true;
    }
    



    
    
    
    

 //   public void init() {
  //      try {
            //allow_command = Conf.GetCongig().getBoolean("modules.command.pvp.use");
     
            //if (!allow_command) {
             //   Ostrov.log_ok ("§e"+this.getClass().getSimpleName()+" выключен.");
            //    return;
            //}
            
            //Bukkit.getPluginManager().registerEvents(this, Ostrov.getInstance());
            
    //        Ostrov.log_ok ("§2"+this.getClass().getSimpleName()+" активен!");
            
    //    } catch (Exception ex) { 
    //        Ostrov.log_err("§4Не удалось загрузить настройки "+this.getClass().getSimpleName()+" : "+ex.getMessage());
    //    }
  //  }

  //  public void reload () {
        //HandlerList.unregisterAll(this);
    //    Config.loadConfigs();
     //   init();
 //   }
    
    
    
    
    
    
    
    
    @SuppressWarnings("deprecation")
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
            int_value=ApiOstrov.getInteger(value);
                
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
        
         
        final ItemStack book = new ItemBuilder(Material.WRITTEN_BOOK)
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
    
    
 
