package ru.komiss77.modules.player.profile;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.WeatherType;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.potion.PotionEffectTypeCategory;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Cfg;
import ru.komiss77.Ostrov;
import ru.komiss77.enums.StatFlag;
import ru.komiss77.modules.entities.PvPManager;
import ru.komiss77.modules.games.GM;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.EntityUtil;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;


public class LocalSettings implements InventoryProvider {

    private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Section.ВОЗМОЖНОСТИ.glassMat).name("§8.").build());
    private static final ItemStack blank = new ItemBuilder(ItemType.GRAY_STAINED_GLASS_PANE).name("<black>.").build();

    @Override
    public void onClose(final Player p, final InventoryContent content) {
        PM.getOplayer(p).menu.current = null;
    }

    private static final int ABIL_CD = 100;
    @Override
    public void init(final Player p, final InventoryContent content) {
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
        //content.fillRect(0,0,  5,8, ClickableItem.empty(fill));

        final Oplayer op = PM.getOplayer(p);
        //final ProfileManager pm = op.menu;

        //линия - разделитель
        content.fillRow(0, ClickableItem.empty(blank));
        content.fillRow(1, fill);
        //выставить иконки внизу
        for (Section section : Section.values()) {
            content.set(2, section.column, Section.getMenuItem(section, op));
        }

        if (PvPManager.getFlag(PvPManager.PvpFlag.allow_pvp_command) && !PvPManager.isForced(p, op, false)) {
            content.set(0, 0, ClickableItem.of(new ItemBuilder(op.pvp_allow ? ItemType.DIAMOND_SWORD : ItemType.SHIELD)
                .name("§7Настройка ПВП")
                .lore("")
                .lore("§7Сейчас:")
                .lore(op.pvp_allow ? "§6Боец" : "§aПацифист")
                .lore(op.pvp_allow ? "§7Ты можете нападать на других" : "§7Ты не можете нападать,")
                .lore(op.pvp_allow ? "§7и получать от них урон" : "§7но и на тебя не нападут")
                .lore("§7ЛКМ - менять")
                .build(), e -> {
                final ItemStack it = e.getCurrentItem();
                if (it == null || p.hasCooldown(it)) return;
                p.setCooldown(it, ABIL_CD);
                p.performCommand("pvp " + (op.pvp_allow ? "off" : "on"));
                reopen(p, content);
            }));
        } else {
            content.set(0, 0, ClickableItem.empty(new ItemBuilder(blank)
                .name("§7Настройка ПВП")
                .lore("§8Недоступно")
                .build()
            ));
        }

        if (op.isLocalChat()) {
            content.set(0, 1, ClickableItem.of(new ItemBuilder(ItemType.CALIBRATED_SCULK_SENSOR)
                .name("<gold>Чат: <dark_aqua>Все-Серверный")
                .lore("§7Клик - сделать <aqua>локальным")
                .lore("")
                .lore("<yellow>При все-серверном чате:")
                .lore("<mithril>Видно сообщения: игроков со всех режимов,")
                .lore("<mithril>Написанные сообщения: видны всем игрокам")
                .lore("")
                .lore("<mithril>Для личного сообщения:")
                .lore("§e/msg ник сообщение")
                .build(), e -> {
                final ItemStack it = e.getCurrentItem();
                if (it == null || p.hasCooldown(it)) return;
                p.setCooldown(it, ABIL_CD);
                op.setLocalChat(true);
                reopen(p, content);
            }));
        } else {
            content.set(0, 1, ClickableItem.of(new ItemBuilder(ItemType.SCULK_SENSOR)
                .name("<gold>Чат: <aqua>Локальный")
                .lore("§7Клик - сделать <dark_aqua>все-серверным")
                .lore("")
                .lore("<yellow>При локальном чате:")
                .lore("<mithril>Видно сообщения: игроков")
                .lore("<mithril>режима " + GM.GAME.displayName + "<gray>,")
                .lore("<mithril>Написанные сообщения: видны")
                .lore("<mithril>только игрокам на " + GM.GAME.displayName)
                .lore("")
                .lore("<mithril>Для личного сообщения:")
                .lore("§e/msg ник сообщение")
                .build(), e -> {
                final ItemStack it = e.getCurrentItem();
                if (it == null || p.hasCooldown(it)) return;
                p.setCooldown(it, ABIL_CD);
                op.setLocalChat(false);
                reopen(p, content);
            }));
        }


        if (ApiOstrov.isLocalBuilder(p) || (Cfg.ptime_command && p.hasPermission("ostrov.ptime"))) {
            content.set(0, 2, ClickableItem.of(new ItemBuilder(ItemType.CLOCK)
                .name("<olive>Личное Время")
                .amount(p.isPlayerTimeRelative() && p.getPlayerTimeOffset() > 1000 ? (int) p.getPlayerTimeOffset() / 1000 : 1)
                .lore("§7Сейчас: " + (p.isPlayerTimeRelative() ? "§eЦиклично" : "§bЗаморожено"))
                .lore("")
                .lore("§7ЛКМ - изменить время")
                .lore(p.isPlayerTimeRelative() ? "§7ПКМ - заморозить" : "§7ПКМ - зациклить")
                .lore("<mithril>Шифт+Клик - сброс")
                .build(), e -> {
                switch (e.getClick()) {
                    case LEFT:
                        final long time = p.getPlayerTimeOffset() + 1000;
                        p.setPlayerTime(time > 24000 ? 0 : time, p.isPlayerTimeRelative());
                        break;
                    case RIGHT:
                        p.setPlayerTime(p.getPlayerTimeOffset(), !p.isPlayerTimeRelative());
                        break;
                    case SHIFT_LEFT, SHIFT_RIGHT:
                        p.resetPlayerTime();
                        break;
                    default:
                        break;
                }
                reopen(p, content);
            }));
        } else {
            content.set(0, 2, ClickableItem.empty(new ItemBuilder(blank)
                .name("§7Личное Время")
                .lore("§8Недоступно")
                .build()
            ));
        }


        if (ApiOstrov.isLocalBuilder(p) || (Cfg.pweather_command && p.hasPermission("ostrov.pweather"))) {
            final WeatherType wth = p.getPlayerWeather();
            content.set(0, 3, ClickableItem.of(new ItemBuilder(wth == null ? ItemType.NAUTILUS_SHELL : wth == WeatherType.CLEAR ? ItemType.SUNFLOWER : ItemType.WATER_BUCKET)
                .name("<indigo>Личная Погода")
                .lore("§7Сейчас: " + (wth == null ? "<dark_gray>Не Изменена" : wth == WeatherType.CLEAR ? "<yellow>Ясно" : "<blue>Пасмурно"))
                .lore("")
                .lore("§7ЛКМ - поменять")
                .lore("§7ПКМ - сброс")
                .build(), e -> {
                final ItemStack it = e.getCurrentItem();
                if (it == null || p.hasCooldown(it)) return;
                p.setCooldown(it, ABIL_CD);
                switch (e.getClick()) {
                    case LEFT, SHIFT_LEFT:
                        p.setPlayerWeather(p.getPlayerWeather() == null
                            || p.getPlayerWeather() == WeatherType.CLEAR
                            ? WeatherType.DOWNFALL : WeatherType.CLEAR);
                        break;
                    case RIGHT, SHIFT_RIGHT:
                        p.resetPlayerWeather();
                        break;
                    default:
                        break;
                }
                reopen(p, content);
            }));
        } else {
            content.set(0, 3, ClickableItem.empty(new ItemBuilder(blank)
                .name("§7Личная Погода")
                .lore("§8Недоступно")
                .build()
            ));
        }


        if (ApiOstrov.isLocalBuilder(p) || (Cfg.heal_command && p.hasPermission("ostrov.heal"))) {
            if (op.pvp_time > 0) {
                content.set(0, 4, ClickableItem.empty(new ItemBuilder(ItemType.APPLE)
                    .name("<cardinal>Исцеление")
                    .lore("")
                    .lore("<red>У тебя режим боя!")
                    .lore("<gold>Доступно через " + op.pvp_time + " сек")
                    .build()
                ));
            } else {
                content.set(0, 4, ClickableItem.of(new ItemBuilder(ItemType.GOLDEN_APPLE)
                    .name("<cardinal>Исцеление")
                    .lore("")
                    .lore("§7ЛКМ - восстановить здоровье")
                    .lore("§7и снять вредные эффекты")
                    .build(), e -> {
                    final ItemStack it = e.getCurrentItem();
                    if (it == null || p.hasCooldown(it)) return;
                    p.setCooldown(it, ABIL_CD);
                    if (p.getHealth() == 0) return;
                    final double max = p.getAttribute(Attribute.MAX_HEALTH).getValue();
                    final double amount = max - p.getHealth();
                    final EntityRegainHealthEvent erhe = new EntityRegainHealthEvent(p,
                        amount, EntityRegainHealthEvent.RegainReason.CUSTOM);
                    Bukkit.getPluginManager().callEvent(erhe);
                    p.setHealth(Math.min(p.getHealth() + erhe.getAmount(), max));
                    p.setFoodLevel(20);
                    p.setSaturation(20f);
                    p.setFireTicks(0);
                    p.setFreezeTicks(0);
                    p.getActivePotionEffects().stream().forEach(ef -> {
                        if (ef.getType().getCategory() == PotionEffectTypeCategory.HARMFUL)
                            p.removePotionEffect(ef.getType());
                    });
                    EntityUtil.effect(p, Sound.BLOCK_BREWING_STAND_BREW,
                        0.8f, Particle.HAPPY_VILLAGER);
                    p.closeInventory();
                }));
            }
        } else {
            content.set(0, 4, ClickableItem.empty(new ItemBuilder(blank)
                .name("§7Исцеление")
                .lore("§8Недоступно")
                .build()
            ));
        }

        if (ApiOstrov.isLocalBuilder(p) || (Cfg.repair_command && p.hasPermission("ostrov.repair"))) {
            if (op.pvp_time > 0) {
                content.set(0, 4, ClickableItem.empty(new ItemBuilder(ItemType.DAMAGED_ANVIL)
                    .name("<stale>Кузня")
                    .lore("")
                    .lore("<red>У тебя режим боя!")
                    .lore("<gold>Доступно через " + op.pvp_time + " сек")
                    .build()
                ));
            } else {
                content.set(0, 5, ClickableItem.of(new ItemBuilder(ItemType.ANVIL)
                    .name("<stale>Кузня")
                    .lore("")
                    .lore("§7ЛКМ - починка предметов")
                    .lore("§7в твоем инвентаре")
                    .build(), e -> {
                    final ItemStack it = e.getCurrentItem();
                    if (it == null || p.hasCooldown(it)) return;
                    p.setCooldown(it, ABIL_CD);
                    p.sendMessage(Ostrov.PREFIX + "§bИсправлено предметов: " + ItemUtil.repairAll(p));
                    EntityUtil.effect(p, Sound.BLOCK_SMITHING_TABLE_USE,
                        0.8f, Particle.ENCHANT);
                    p.closeInventory();
                }));
            }
        } else {
            content.set(0, 5, ClickableItem.empty(new ItemBuilder(ItemType.GRAY_STAINED_GLASS_PANE)
                .name("<stale>Кузня")
                .lore("§8Недоступно")
                .build()
            ));
        }


        //        final boolean canSpeed = ApiOstrov.isLocalBuilder(p) || (Cfg.speed_command && p.hasPermission("ostrov.speed")); // не имеет пользы

        if (ApiOstrov.isLocalBuilder(p) || (Cfg.fly_command && p.hasPermission("ostrov.fly"))) {
            if (p.isFlying() || p.getAllowFlight()) {
                int ammount = (int) (p.getFlySpeed() * 10f) + 1;
                content.set(0, 6, ClickableItem.of(new ItemBuilder(ItemType.FEATHER).name("<sky>Скорость Полета")
                    .amount(ammount).lore("").lore("§7ЛКМ: +1 скорость").lore("§7ПКМ: -1 скорость")
//                .lore(canFly ? "§7ПКМ - менять режим" : (Cfg.fly_command ? "§7Нет права на полет" : "§8Недоступно на этом сервере"))
                    .build(), e -> {
                    if (e.isLeftClick()) {
                        if (p.getAllowFlight()) {
                            final float curr = p.getFlySpeed() + 0.1f;
                            p.setFlySpeed(curr > 1f ? 0f : curr);
                        }/* else {
                            curr = p.getWalkSpeed();
                            curr += 0.1f;
                            if (curr > 1) curr = 0;
                            p.setWalkSpeed(curr);
                        }*/
                        reopen(p, content);
                        return;
                    } else if (e.isRightClick()) {
                        if (p.getAllowFlight()) {
                            final float curr = p.getFlySpeed() - 0.1f;
                            p.setFlySpeed(curr < 0f ? 1f : curr);
                        }/* else {
                            curr = p.getWalkSpeed();
                            curr += 0.1f;
                            if (curr > 1) curr = 0;
                            p.setWalkSpeed(curr);
                        }*/
                        reopen(p, content);
                        return;
                    }
                    PM.soundDeny(p);
                }));
            } else {
                content.set(0, 1, ClickableItem.of(new ItemBuilder(ItemType.ELYTRA)
                    .name("<sky>Полет").lore("").lore("§7Клик - включить").build(), e -> {
                    p.setAllowFlight(true);
                    p.setFlying(true);
                }));
            }
        } else {
            content.set(0, 1, ClickableItem.empty(new ItemBuilder(blank)
                .name("§7Полет")
                .lore("§8Недоступно")
                .build()
            ));
        }


        final boolean inf = op.hasFlag(StatFlag.InformatorOff);
        content.set(0, 7, ClickableItem.of(new ItemBuilder(inf ? ItemType.BUCKET : ItemType.PUFFERFISH_BUCKET)
            .name("<apple>Авто-Информатор").lore("").lore("§7Клик - " + (inf ? "§aВключить" : "§cВыключить")).build(), e -> {
            final ItemStack it = e.getCurrentItem();
            if (it == null || p.hasCooldown(it)) return;
            p.setCooldown(it, ABIL_CD);
            op.setFlag(StatFlag.InformatorOff, !inf);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.5f, 1);
            reopen(p, content);
        }));

               
        
        

        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
  
              
            
        
        





        

        /*
        
        content.set( 5, 8, ClickableItem.of( new ItemBuilder(ItemType.OAK_DOOR).name("Закрыть").build(), e -> 
        {
            p.closeInventory();
        }
        ));
        
*/


    }


}
