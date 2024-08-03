package ru.komiss77.modules.player.profile;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.WeatherType;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Cfg;
import ru.komiss77.Ostrov;
import ru.komiss77.Timer;
import ru.komiss77.commands.PvpCmd;
import ru.komiss77.enums.StatFlag;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;


public class LocalSettings implements InventoryProvider {

    private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Section.ВОЗМОЖНОСТИ.glassMat).name("§8.").build());
    private final ClickableItem c = ClickableItem.empty(new ItemStack(Material.GLOW_LICHEN));


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
        content.fillRect(0, 35, c);
        //выставить иконки внизу
        for (Section section : Section.values()) {
            content.set(section.slot, Section.getMenuItem(section, op));
        }


        if (PvpCmd.getFlag(PvpCmd.PvpFlag.allow_pvp_command)) {

            content.set(1, 1, ClickableItem.of(new ItemBuilder(op.pvp_allow ? Material.DIAMOND_SWORD : Material.SHIELD)
                    .name("§7Разрешение ПВП")
                    .lore("")
                    .lore("§7Сейчас:")
                    .lore(op.pvp_allow ? "§6Боец" : "§bПацифист")
                    .lore(op.pvp_allow ? "§7Вы можете нападать" : "§7Вы не можете нападать,")
                    .lore(op.pvp_allow ? "§7и получать ответку." : "§7но и на вас не нападут.")
                    .lore("")
                    .lore("§7ЛКМ - менять")
                    .lore("")
                    .build(), e -> {
                p.performCommand("pvp " + (op.pvp_allow ? "off" : "on"));
                reopen(p, content);
            }));

        } else {

            content.set(1, 1, ClickableItem.empty(new ItemBuilder(Material.GRAY_DYE)
                    .name("§7Разрешение ПВП")
                    .lore("§7Выключено")
                    .lore("§7на этом сервере")
                    .build()
            ));

        }


        final boolean canFly = ApiOstrov.isLocalBuilder(p) || (Cfg.fly_command && p.hasPermission("ostrov.fly"));
        final boolean canSpeed = ApiOstrov.isLocalBuilder(p) || (Cfg.speed_command && p.hasPermission("ostrov.speed"));
//Bukkit.broadcastMessage("fly_command?"+Config.fly_command+" canFly?"+canFly);
//Bukkit.broadcastMessage("speed_command?"+Config.speed_command+" canSpeed?"+canSpeed);
        int ammount = (int) (p.getAllowFlight() ? p.getFlySpeed() * 10f : p.getWalkSpeed() * 10f);
        ammount++;

        content.set(1, 3, ClickableItem.of(new ItemBuilder(p.getAllowFlight() ? Material.FEATHER : Material.IRON_BOOTS)
                .name(p.getAllowFlight() ? "§6Крылья" : "§bНоги")
                .amount(ammount)
                .lore("")
                .lore(canFly ? "§7ЛКМ - менять режим" : (Cfg.fly_command ? "§7нет права §costrov.fly" : "§8Недоступно на этом сервере"))
                .lore(canSpeed ? "§7ПКМ - менять скорость" : (Cfg.speed_command ? "§7нет права §costrov.speed" : "§8Недоступно на этом сервере"))
                .build(), e -> {
            if (e.isLeftClick() && canFly) {
                if (p.getAllowFlight()) {
                    p.setFlying(false);
                    p.setAllowFlight(false);
                } else {
                    p.setAllowFlight(true);
                    p.setFlying(true);
                }
                reopen(p, content);
                return;

            } else if (e.isRightClick() && canSpeed) {
                float curr;
                if (p.getAllowFlight()) {
                    curr = p.getFlySpeed();
                    curr += 0.1f;
                    if (curr > 1) curr = 0;
                    p.setFlySpeed(curr);
                } else {
                    curr = p.getWalkSpeed();
                    curr += 0.1f;
                    if (curr > 1) curr = 0;
                    p.setWalkSpeed(curr);
                }
                reopen(p, content);
                return;
            }
            PM.soundDeny(p);
            //p.performCommand("spawn");
        }));


        if (ApiOstrov.isLocalBuilder(p) || (Cfg.ptime_command && p.hasPermission("ostrov.ptime"))) {

            content.set(1, 5, ClickableItem.of(new ItemBuilder(Material.CLOCK)
                    .name("§7Личное время")
                    .amount(p.isPlayerTimeRelative() && p.getPlayerTimeOffset() > 1000 ? (int) p.getPlayerTimeOffset() / 1000 : 1)
                    .lore("")
                    .lore("§7Сейчас:")
                    .lore(p.isPlayerTimeRelative() ? "§eМеняется" : "§bЗаморожено")
                    .lore("")
                    .lore(p.isPlayerTimeRelative() ? "§7ЛКМ - заморозить" : "§7ЛКМ - меняться")
                    .lore("§7ПКМ - изменить время")
                    .lore("§7Шифт+ПКМ - сброс")
                    .lore("")
                    .build(), e -> {
                switch (e.getClick()) {
                    case LEFT:
                        p.setPlayerTime(p.getPlayerTime(), !p.isPlayerTimeRelative());
                        break;
                    case RIGHT:
                        long time = p.getPlayerTime();
                        time += 2000;
                        if (time > 24000) time = 0;
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

            content.set(1, 5, ClickableItem.empty(new ItemBuilder(Material.GRAY_DYE)
                    .name("§7Личное время")
                    .lore(Cfg.ptime_command ? "§7нет права §costrov.ptime" : "§8Недоступно на этом сервере")
                    //.addLore("§7на этом сервере")
                    .build()
            ));

        }


        if (ApiOstrov.isLocalBuilder(p) || (Cfg.pweather_command && p.hasPermission("ostrov.pweather"))) {

            content.set(1, 7, ClickableItem.of(new ItemBuilder(p.getPlayerWeather() == null ? Material.NAUTILUS_SHELL : p.getPlayerWeather() == WeatherType.CLEAR ? Material.SUNFLOWER : Material.WATER_BUCKET)
                    .name("§7Личная погода")
                    .lore("")
                    .lore("§7ЛКМ - менять")
                    .lore("§7ПКМ - сброс на серверное")
                    .lore("")
                    .build(), e -> {
                switch (e.getClick()) {
                    case LEFT:
                        if (p.getPlayerWeather() == null || p.getPlayerWeather() == WeatherType.CLEAR) {
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

            content.set(1, 7, ClickableItem.empty(new ItemBuilder(Material.GRAY_DYE)
                    .name("§7Личная погода")
                    .lore(Cfg.pweather_command ? "§7нет права §costrov.pweather" : "§8Недоступно на этом сервере")
                    .lore("§7на этом сервере")
                    .build()
            ));

        }


        if (ApiOstrov.isLocalBuilder(p) || (Cfg.heal_command && p.hasPermission("ostrov.heal"))) {

            if (op.pvp_time > 0) {
                content.set(2, 3, ClickableItem.empty(new ItemBuilder(Material.APPLE)
                        .name("§7Исцеление")
                        .lore("")
                        .lore("§eРежим битвы!")
                        .lore("§6Будет доступно через " + op.pvp_time)
                        .lore("")
                        .build()
                ));
            } else {
                content.set(2, 3, ClickableItem.of(new ItemBuilder(Material.GOLDEN_APPLE)
                        .name("§7Исцеление")
                        .lore("")
                        .lore("§7ЛКМ - восстановить здоровье")
                        .lore("§7и снять порчу.")
                        .lore("")
                        .build(), e -> {
                    if (p.getHealth() == 0) return;
                    final double amount = p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() - p.getHealth();
                    final EntityRegainHealthEvent erhe = new EntityRegainHealthEvent(p, amount, EntityRegainHealthEvent.RegainReason.CUSTOM);
                    Ostrov.getInstance().getServer().getPluginManager().callEvent(erhe);
                    double newAmount = p.getHealth() + erhe.getAmount();
                    if (newAmount > p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue())
                        newAmount = p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
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

            content.set(2, 3, ClickableItem.empty(new ItemBuilder(Material.GRAY_DYE)
                    .name("§7Исцеление")
                    .lore(Cfg.heal_command ? "§7нет права §costrov.heal" : "§8Недоступно на этом сервере")
                    .build()
            ));

        }


        // if ( ItemUtils.Need_repair(p) ) 
        if (ApiOstrov.isLocalBuilder(p) || (Cfg.repair_command && p.hasPermission("ostrov.repair"))) {

            if (Timer.has(p, "repair")) {
                content.set(2, 5, ClickableItem.empty(new ItemBuilder(Material.DAMAGED_ANVIL)
                        .name("§7Кузня")
                        .lore("")
                        .lore("")
                        .lore("§6Будет доступно через: " + Timer.getLeft(p, "repair"))
                        .lore("")
                        .build()
                ));
            } else if (op.pvp_time > 0) {
                content.set(2, 5, ClickableItem.empty(new ItemBuilder(Material.DAMAGED_ANVIL)
                        .name("§7Кузня")
                        .lore("")
                        .lore("")
                        .lore("§eРежим битвы!")
                        .lore("§6Будет доступно через " + op.pvp_time)
                        .lore("")
                        .build()
                ));
            } else {
                content.set(2, 5, ClickableItem.of(new ItemBuilder(Material.ANVIL)
                        .name("§7Кузня")
                        .lore("")
                        .lore("§7ЛКМ - починка всего")
                        .lore("§7в инвентаре")
                        .lore("")
                        .build(), e -> {
                            Timer.add(p, "repair", 60);
                            p.sendMessage("§aОтремонтировано предметов: " + ItemUtil.repairAll(p));
                            //p.sendMessage( "§aОтремонтировано: "+ItemUtils.Repair_all(p).toString().replaceAll("\\[|\\]", "") );
                            reopen(p, content);
                        }
                ));
            }

        } else {

            content.set(2, 5, ClickableItem.empty(new ItemBuilder(Material.GRAY_DYE)
                    .name("§7Кузня")
                    .lore(Cfg.repair_command ? "§7нет права §costrov.repair" : "§8Недоступно на этом сервере")
                    .build()
            ));

        }


        final boolean local = op.isLocalChat();//= op.hasFlag(StatFlag.LocalChat); //Ostrov.deluxechatPlugin.isLocal(p.getUniqueId().toString());
        content.set(3, 2, ClickableItem.of(new ItemBuilder(local ? Material.TURTLE_SCUTE : Material.GUNPOWDER)
                        .name("§7Режим чата")
                        .lore(local ? "§7Сейчас: §bлокальный" : "§7Сейчас: §eглобальный")
                        .lore(local ? "§7ЛКМ - сделать глобальным" : "§7ЛКМ - сделать локальным")
                        .lore("§7")
                        .lore("В режиме &bглобальный")
                        .lore("вы получаете сообщения со всех серверов,")
                        .lore("и на всех серверах видят ваши сообщения.")
                        .lore("В режиме &bлокальный")
                        .lore("вы получаете сообщения только")
                        .lore("от игроков с этого сервера,")
                        .lore("Ваши сообщения так же будут")
                        .lore("видны только на этом сервере.")
                        .lore("§7")
                        .lore("§eКомандой /msg ник сообщение")
                        .lore("§eможно начать личный диалог.")
                        .build(), e -> {
                    op.setLocalChat(!local);
                    reopen(p, content);
                }
        ));


        content.set(3, 6, ClickableItem.of(new ItemBuilder(op.hasFlag(StatFlag.InformatorOff) ? Material.BUCKET : Material.WATER_BUCKET)
                                .name("§7Сообщения автоинформатора")
                                .lore("")
                                .lore("§7сейчас: ")
                                .lore(op.hasFlag(StatFlag.InformatorOff) ? "§cвыключены" : "§aвключены")
                                .lore("")
                                .lore("§7ЛКМ - §eизменить")
                                .lore("")
                                .build()
                        , e -> {
                            op.setFlag(StatFlag.InformatorOff, !op.hasFlag(StatFlag.InformatorOff));
                            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.5f, 1);
                            reopen(p, content);
                        }
                )
        );

               
        
        

        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
  
              
            
        
        





        

        /*
        
        content.set( 5, 8, ClickableItem.of( new ItemBuilder(Material.OAK_DOOR).name("Закрыть").build(), e -> 
        {
            p.closeInventory();
        }
        ));
        
*/


    }


}
