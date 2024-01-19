package ru.komiss77.modules.player.profile;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.WeatherType;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityRegainHealthEvent;

import ru.komiss77.ApiOstrov;
import ru.komiss77.Config;
import ru.komiss77.Ostrov;
import ru.komiss77.Timer;
import ru.komiss77.commands.PvpCmd;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;





public class LocalSettings implements InventoryProvider {
    
    
    
   private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Section.ВОЗМОЖНОСТИ.glassMat).name("§8.").build());
    

    
    public LocalSettings() {
    }
    
    @Override
    public void onClose(final Player p, final InventoryContent content) {
        PM.getOplayer(p).menu.current = null;
    }
    
    
    
    @Override
    public void init(final Player p, final InventoryContent content) {
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
        //content.fillRect(0,0,  5,8, ClickableItem.empty(fill));
        
        final Oplayer op = PM.getOplayer(p);
        //final ProfileManager pm = op.menu;

        //линия - разделитель
        content.fillRow(4, fill);
        
        //выставить иконки внизу
        for (Section section:Section.values()) {
            content.set(section.slot, Section.getMenuItem(section, op));
        }
        
        
        
        
        
        if (ApiOstrov.isLocalBuilder(p) || PvpCmd.getFlag(PvpCmd.PvpFlag.allow_pvp_command)) {
            
            content.set(1,1, ClickableItem.of(new ItemBuilder(op.pvp_allow ? Material.DIAMOND_SWORD : Material.SHIELD)
                .name("§7Разрешение ПВП")
                .addLore("")
                .addLore("§7Сейчас:")
                .addLore(op.pvp_allow ? "§6Боец" : "§bПацифист")
                .addLore(op.pvp_allow ? "§7Вы можете нападать": "§7Вы не можете нападать,")
                .addLore(op.pvp_allow ? "§7и получать ответку.": "§7но и на вас не нападут.")
                .addLore("")
                .addLore( "§7ЛКМ - менять" )
                .addLore("")
                .build(), e-> {
                    op.pvp_allow = !op.pvp_allow;
                    reopen(p, content);
                }));

        } else {
            
            content.set(1,1, ClickableItem.empty(new ItemBuilder(Material.WOODEN_SWORD)
                .name("§7Разрешение ПВП")
                .addLore( "§7Выключено" )
                .addLore("§7на этом сервере")
                .build()
            ));
            
        }

        
        
        
        
        final boolean canFly = ApiOstrov.isLocalBuilder(p) || (Config.fly_command && p.hasPermission("ostrov.fly"));
        final boolean canSpeed = ApiOstrov.isLocalBuilder(p) || (Config.speed_command && p.hasPermission("ostrov.speed")) ;
//Bukkit.broadcastMessage("fly_command?"+Config.fly_command+" canFly?"+canFly);
//Bukkit.broadcastMessage("speed_command?"+Config.speed_command+" canSpeed?"+canSpeed);
            int ammount = (int) ( p.getAllowFlight() ? p.getFlySpeed()*10f : p.getWalkSpeed()*10f );
            ammount++;
            
            content.set(1,2, ClickableItem.of(new ItemBuilder(p.getAllowFlight() ? Material.FEATHER : Material.IRON_BOOTS)
                .name(p.getAllowFlight() ? "§6Крылья" : "§bНоги")
                .setAmount( ammount)
                .addLore("")
                .addLore(canFly ? "§7ЛКМ - менять режим" : (Config.fly_command ?  "§7нет права §costrov.fly" : "§8Недоступно на этом сервере"))
                .addLore(canSpeed ? "§7ПКМ - менять скорость" :  (Config.speed_command ? "§7нет права §costrov.speed" : "§8Недоступно на этом сервере") )
                .build(), e-> {
                    if (e.isLeftClick() && canFly) {
                        if (p.getAllowFlight()) {
                            p.setFlying(false); p.setAllowFlight(false); 
                        } else {
                            p.setAllowFlight(true); p.setFlying(true); 
                        }
                        reopen(p, content);
                        return;
                        
                    } else if (e.isRightClick() && canSpeed) {
                        float curr;
                        if (p.getAllowFlight()) {
                            curr = p.getFlySpeed();
                            curr += 0.1f;
                            if (curr>1) curr = 0;
                            p.setFlySpeed(curr);
                        } else { 
                            curr = p.getWalkSpeed();
                            curr += 0.1f;
                            if (curr>1) curr = 0;
                            p.setWalkSpeed(curr);
                        }
                        reopen(p, content);
                        return;
                    }
                    PM.soundDeny(p);
                    //p.performCommand("spawn");
                }));

        
        
        
              
        
        
        
        

        
        
        
        
        if (ApiOstrov.isLocalBuilder(p) || (Config.ptime_command && p.hasPermission("ostrov.ptime"))) {
            
            content.set(1,3, ClickableItem.of(new ItemBuilder(Material.CLOCK)
                .name("§7Личное время")
                .setAmount(p.isPlayerTimeRelative() && p.getPlayerTimeOffset()>1000 ? (int)p.getPlayerTimeOffset()/1000 : 1)
                .addLore("")
                .addLore("§7Сейчас:")
                .addLore(p.isPlayerTimeRelative() ? "§eМеняется" : "§bЗаморожено")
                .addLore("")
                .addLore( p.isPlayerTimeRelative() ? "§7ЛКМ - заморозить" : "§7ЛКМ - меняться" )
                .addLore( "§7ПКМ - изменить время" )
                .addLore( "§7Шифт+ПКМ - сброс" )
                .addLore("")
                .build(), e-> {
                    switch (e.getClick()) {
                        case LEFT:
                            p.setPlayerTime(p.getPlayerTime(), !p.isPlayerTimeRelative());
                            break;
                        case RIGHT:
                            long time = p.getPlayerTime();
                            time +=2000;
                            if (time>24000) time = 0;
                            p.setPlayerTime(time, p.isPlayerTimeRelative());
                            break;
                        case SHIFT_RIGHT:
                            p.resetPlayerTime();
                            break;
						default:
							break;
                    }
                    reopen(p, content);
                }));

        } else {
            
            content.set(1,3, ClickableItem.empty(new ItemBuilder(Material.CLOCK)
                .name("§7Личное время")
                .addLore( Config.ptime_command ?  "§7нет права §costrov.ptime"  : "§8Недоступно на этом сервере")
                //.addLore("§7на этом сервере")
                .build()
            ));
            
        }

        
        
        
        
        
        if (ApiOstrov.isLocalBuilder(p) || (Config.pweather_command && p.hasPermission("ostrov.pweather"))) {
            
            content.set(1,4, ClickableItem.of(new ItemBuilder(p.getPlayerWeather()==null ?  Material.NAUTILUS_SHELL  : p.getPlayerWeather() == WeatherType.CLEAR ? Material.SUNFLOWER : Material.WATER_BUCKET)
                .name("§7Личная погода")
                .addLore("")
                .addLore( "§7ЛКМ - менять" )
                .addLore( "§7ПКМ - сброс на серверное" )
                .addLore("")
                .build(), e-> {
                    switch (e.getClick()) {
                        case LEFT:
                            if (p.getPlayerWeather()==null || p.getPlayerWeather()==WeatherType.CLEAR) { 
                                p.setPlayerWeather(WeatherType.DOWNFALL);
                            } else {
                                p.setPlayerWeather(WeatherType.CLEAR);
                            }
                            break;
                        case SHIFT_RIGHT:
                            p.resetPlayerWeather();
                            break;
						default:
							break;
                    }
                    reopen(p, content);
                }));

        } else {
            
            content.set(1, 4, ClickableItem.empty(new ItemBuilder(Material.SUNFLOWER)
                .name("§7Личная погода")
                .addLore( Config.pweather_command ? "§7нет права §costrov.pweather" : "§8Недоступно на этом сервере")
                .addLore("§7на этом сервере")
                .build()
            ));
            
        }
        
        
        
        
        
        
        
        
        if (ApiOstrov.isLocalBuilder(p) || (Config.heal_command && p.hasPermission("ostrov.heal"))) {
            
            if (op.pvp_time>0) {
                content.set(1, 5, ClickableItem.empty(new ItemBuilder(Material.APPLE)
                    .name("§7Исцеление")
                    .addLore("")
                    .addLore("§eРежим битвы!")
                    .addLore( "§6Будет доступно через "+op.pvp_time )
                    .addLore("")
                    .build()
                ));
            } else {
                content.set(1,5, ClickableItem.of(new ItemBuilder(Material.GOLDEN_APPLE)
                    .name("§7Исцеление")
                    .addLore("")
                    .addLore( "§7ЛКМ - восстановить здоровье" )
                    .addLore( "§7и снять порчу." )
                    .addLore("")
                    .build(), e-> {
                        if (p.getHealth() == 0) return;
                        final double amount =p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() - p.getHealth();
                        final EntityRegainHealthEvent erhe = new EntityRegainHealthEvent(p, amount, EntityRegainHealthEvent.RegainReason.CUSTOM);
                        Ostrov.getInstance().getServer().getPluginManager().callEvent(erhe);
                        double newAmount = p.getHealth() + erhe.getAmount();
                        if (newAmount > p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()) newAmount = p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                        p.setHealth(newAmount);
                        p.setFoodLevel(20);
                        p.setFireTicks(0);
                        p.getActivePotionEffects().stream().forEach((effect) -> {
                            p.removePotionEffect(effect.getType());
                        });
                        reopen(p, content);
                    }));
            }

        } else {
            
            content.set(1, 5, ClickableItem.empty(new ItemBuilder(Material.APPLE)
                .name("§7Исцеление")
                .addLore( Config.heal_command ? "§7нет права §costrov.heal" : "§8Недоступно на этом сервере")
                .build()
            ));
            
        }
        
        
        // if ( ItemUtils.Need_repair(p) ) 
        if (ApiOstrov.isLocalBuilder(p) || (Config.repair_command && p.hasPermission("ostrov.repair"))) {
            
            if (Timer.has(p, "repair")) {
                content.set(1, 6, ClickableItem.empty(new ItemBuilder(Material.DAMAGED_ANVIL)
                    .name("§7Кузня")
                    .addLore("")
                    .addLore("")
                    .addLore( "§6Будет доступно через: "+Timer.getLeft(p, "repair") )
                    .addLore("")
                    .build()
                ));
            } else if (op.pvp_time>0) {
                content.set(1, 6, ClickableItem.empty(new ItemBuilder(Material.DAMAGED_ANVIL)
                    .name("§7Кузня")
                    .addLore("")
                    .addLore("")
                    .addLore("§eРежим битвы!")
                    .addLore( "§6Будет доступно через "+op.pvp_time )
                    .addLore("")
                    .build()
                ));
            } else{
                content.set(1,6, ClickableItem.of(new ItemBuilder(Material.ANVIL)
                    .name("§7Кузня")
                    .addLore("")
                    .addLore( "§7ЛКМ - починка всего" )
                    .addLore( "§7в инвентаре" )
                    .addLore("")
                    .build(), e-> {
                        Timer.add(p, "repair", 60);
                        p.sendMessage( "§aОтремонтировано предметов: "+ItemUtils.repairAll(p) );
                        //p.sendMessage( "§aОтремонтировано: "+ItemUtils.Repair_all(p).toString().replaceAll("\\[|\\]", "") );
                        reopen(p, content);
                    }
                ));
            }

        } else {
            
            content.set(1, 6, ClickableItem.empty(new ItemBuilder(Material.DAMAGED_ANVIL)
                .name("§7Кузня")
                .addLore( Config.repair_command ?  "§7нет права §costrov.repair" : "§8Недоступно на этом сервере")
                .build()
            ));
            
        }
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
      //  if (Ostrov.deluxeChat) {
            final boolean local = op.isLocalChat();//= op.hasFlag(StatFlag.LocalChat); //Ostrov.deluxechatPlugin.isLocal(p.getUniqueId().toString());
            content.set(1,7, ClickableItem.of(new ItemBuilder( local ? Material.SCUTE : Material.GUNPOWDER)
                .name("§7Режим чата")
                .addLore(local ? "§7Сейчас: §bлокальный" : "§7Сейчас: §eглобальный")
                .addLore( local ? "§7ЛКМ - сделать глобальным" : "§7ЛКМ - сделать локальным" )
                .addLore( "§7" )
                .addLore("В режиме &bглобальный")
                .addLore("вы получаете сообщения со всех серверов,")
                .addLore("и на всех серверах видят ваши сообщения.")
                .addLore("В режиме &bлокальный")
                .addLore("вы получаете сообщения только")
                .addLore("от игроков с этого сервера,")
                .addLore("Ваши сообщения так же будут")
                .addLore("видны только на этом сервере.")
                .addLore( "§7" )
                .addLore("§eКомандой /msg ник сообщение")
                .addLore("§eможно начать личный диалог.")
                .build(), e-> {
                    op.setLocalChat(!local);
                    //if (local) {
                        //DchatHook.setGlobal(p);
                     //   op.setFlag(StatFlag.LocalChat, false);
                   // } else {
                        //DchatHook.setLocal(p);
                    //    op.setFlag(StatFlag.LocalChat, true);
                   // }
                    reopen(p, content);
                }
            ));
      /*  } else {
            content.set(1, 7, ClickableItem.empty(new ItemBuilder(Material.GUNPOWDER)
                .name("§7Режим чата")
                .addLore( "§7Сейчас: §bлокальный" )
                .addLore("")
                .addLore( "§cИзменение невозможно" )
                .addLore("")
                .build()
            ));
        }*/
               
        
        

        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
  
              
            
        
        





        

        /*
        
        content.set( 5, 8, ClickableItem.of( new ItemBuilder(Material.OAK_DOOR).name("Закрыть").build(), e -> 
        {
            p.closeInventory();
        }
        ));
        
*/

        

    }


    
    
    
    
    
    
    
    
    
}
