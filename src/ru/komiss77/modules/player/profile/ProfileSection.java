package ru.komiss77.modules.player.profile;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.commands.ReportCmd;
import ru.komiss77.enums.Data;
import ru.komiss77.enums.ServerType;
import ru.komiss77.enums.Stat;
import ru.komiss77.hook.SkinRestorerHook;
import ru.komiss77.modules.Pandora;
import ru.komiss77.modules.games.GM;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.player.mission.MissionManager;
import ru.komiss77.modules.player.mission.ProfileWithdrawMenu;
import ru.komiss77.modules.translate.Lang;
import ru.komiss77.utils.StringUtil;
import ru.komiss77.utils.TimeUtil;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.SmartInventory;


public class ProfileSection implements InventoryProvider {

    private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Section.ПРОФИЛЬ.glassMat).name("§8.").build());
    private static final ItemStack blank = new ItemBuilder(ItemType.GRAY_STAINED_GLASS_PANE).name("<black>.").build();


    @Override
    public void onClose(final Player p, final InventoryContent content) {
        PM.getOplayer(p).menu.current = null;
    }


    @Override
    public void init(final Player p, final InventoryContent content) {
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
        //content.fillRect(0,0,  5,8, ClickableItem.empty(fill));

        final Oplayer op = PM.getOplayer(p);
        final ProfileManager pm = op.menu;

        //линия - разделитель
        content.fillRow(0, ClickableItem.empty(blank));
        content.fillRow(1, fill);

        //выставить иконки внизу
        for (Section section : Section.values()) {
            content.set(section.slot, Section.getMenuItem(section, op));
        }

        /*final boolean justGame = op.hasSettings(Settings.JustGame);
        content.set(0, 4, ClickableItem.of(new ItemBuilder(op.eng ? ItemType.RIB_ARMOR_TRIM_SMITHING_TEMPLATE : ItemType.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE)
                    .name(op.eng ? "§7JustGame Menu Mode" : "§7Режим простого меню")
                    .lore("") //0
                    .lore(op.eng ? "§7Now: " + (justGame ? "§6ON" : "§aOFF") : "§7Сейчас: " + (justGame ? "§6Включен" : "§aВыключен")) //1
                    .lore(op.eng ? "§7Click - change" : "§7ЛКМ - изменить") //2
                    .lore("")
                    .lore("§7С режимом JustGame")
                    .lore("§7будут отключены квесты и")
                    .lore("§7будут сразы выдаваться все")
                    .lore("§7в лобби")
                    .lore("")
                    .flags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ITEM_SPECIFICS)
                    .build()
                , e -> {
                    op.setSettings(Settings.JustGame, !justGame);
                    reopen(p, content);
                }
            )
        );*/

        final int dailyLvl = op.getDailyStat(Stat.LEVEL);
        final int totalLvl = op.getStat(Stat.LEVEL);
        final int exp = op.getStat(Stat.EXP);
        content.set(1, 1, ClickableItem.empty(new ItemBuilder(ItemType.CLOCK)
            .name(op.eng ? "§eAccount Data" : "§eИгровые Данные")
            .lore("")
            .lore(Lang.t(p, Stat.LEVEL.desc) + totalLvl + "  " + StringUtil.getPercentBar(totalLvl * 25, exp, true))
            .lore((op.eng ? "§fIncreased today by: " : "§fПрирост за сегодня: ") + (dailyLvl > 0 ? "§e+" + dailyLvl : "§80"))
            .lore((op.eng ? "§fExp needed for next level: §a§l" : "§fОпыта до след. уровня: §a§l") + (totalLvl * 25 - exp + 1))
            .lore("")
            .lore(Lang.t(p, Stat.PLAY_TIME.desc) + TimeUtil.secondToTime(op.getStat(Stat.PLAY_TIME)))
            .lore(Pandora.getInfo(op))
            .build()
        ));

        final boolean canWitdraw = op.getDataInt(Data.RIL) >= MissionManager.getMin(op);
        content.set(1, 5, ClickableItem.of(new ItemBuilder(ItemType.GOLD_INGOT)
            .name(op.eng ? "§6Finances" : "§6Финансы")
            .lore("")
            .lore(op.eng ? "§fAt your disposal:" : "§fВ твоем распоряжении:")
            .lore((op.eng ? "§2Loni: <green>" : "§2Лони: <green>") + op.getDataInt(Data.LONI) + Ostrov.L)
            .lore(op.eng ? "§aLoni §7- the server's in-game currency." : "§aЛони §7- внутриигровая валюта на проекте.")
            .lore(op.eng ? "§7It's used for trading, upgrades, etc." : "§7Используются для торговли, прокачки, и т.д.")
            .lore("")
            .lore((op.eng ? "<amber>Ril: <yellow>" : "<amber>Рил: <yellow>") + op.getDataInt(Data.RIL) + Ostrov.R)
            .lore(op.eng ? "§eRil §7- a real money equivalent. Use it to" : "§eРил §7- счёт, приравненный к рублёвому. Используй его")
            .lore(op.eng ? "§7buy ranks, or withdraw to your phone / card." : "§7для покупки привилегий, или выведи на .")
            .lore("")
            .lore(op.eng ? "§aLoni §7- in-game currency" : "§aЛони §7- внутриигровая валюта")
            .lore(op.eng ? "§7on the project. Is used for" : "§7на проекте. Используется для")
            .lore(op.eng ? "§7turnover, game actions, etc." : "§7товарооборота, игровых действий и т.д.")
            .lore("")
            .lore(op.eng ? "§eRil §7- real money equivalent." : "§eРил §7- счёт, приравненный к рублёвому.")
            .lore(op.eng ? "§7You can buy privileges with Ryl," : "§7За Рил можно купить привилегии,")
            .lore(op.eng ? "§7or withdraw to your phone or card." : "§7или вывести на телефон или карту.")
            .lore(op.eng ? "§7*(subject to certain conditions)" : "§7*(при соблюдении ряда условий)")
            .lore(op.eng ? "§7Ril can be earned by completing tasks!" : "§7Рил можно заработать, выполняя задания!")
            .lore("")
            .lore(op.eng ? "§7LMB - §ftop up your account §eRil" : "§7ЛКМ - §fпополнить счёт §eРил")
            .lore(op.eng ? "§7RMB - §fwithdrawal requests journal" : "§7ПКМ - §fжурнал заявок на вывод")
            .lore(canWitdraw ? (op.eng ? "Key.Q - §forder withdrawal" : "§6Клав.Q - §fзаказать вывод") : (op.eng ? "" : "§8§mКлав.Q - заказать вывод"))
            .lore(canWitdraw ? (op.eng ? "§7Completed withdraws: §f" : "§7Выполнено выводов: §f") + op.getStat(Stat.WD_c) : (op.eng ? "" : "§5Вывод средств возможен от §b") + MissionManager.getMin(op) + " рил")
            .lore(canWitdraw ? "" : (op.eng ? "§5Calc:  §3(1 + withdraws amm.)*5" : "§5Расчёт:  §3(1 + кол-во выводов)*5"))
            .lore("")
            .build(), e -> {
            switch (e.getClick()) {

                case LEFT:
                    p.closeInventory();
                    ApiOstrov.executeBungeeCmd(p, "money add");
                    break;

                case RIGHT:
                    pm.openWithdrawalRequest(p, true);
                    break;

                case DROP:
                    if (op.getDataInt(Data.RIL) >= MissionManager.getMin(op)) {
                        SmartInventory
                            .builder()
                            .id(op.nik + "Миссии")
                            .type(InventoryType.HOPPER)
                            .provider(new ProfileWithdrawMenu(op.getDataInt(Data.RIL)))
                            .title("Новая заявка на вывод Рил")
                            .build()
                            .open(p);
                    } else {
                        PM.soundDeny(p);
                        p.sendMessage("§6Накопите не менее §b" + MissionManager.getMin(op) + " рил§6, чтобы заказать вывод средств!");
                    }
                    break;

                default:
                    break;
            }
        }));


        content.set(1, 7, ClickableItem.of(new ItemBuilder(ItemType.BEACON)
            .name(op.eng ? "§bGroups and perms" : "§bГруппы и права")
            .lore(op.eng ? "§7Detailed information" : "§7Подробная информация")
            .lore(op.eng ? "§7about your groups" : "§7о ваших группах")
            .lore(op.eng ? "§7and personal perms." : "§7и личных правах.")
            .lore((op.eng ? "§fActive groups found: §a" : "§fНайдено активных групп: §a") + op.getGroups().size())
            .lore(op.eng ? "§7LMB - get data from the DB" : "§7ЛКМ - показать данные из БД")
            .lore("")
            .lore(op.eng ? "§6Show permissions" : "§6Показать права (пермишены)")
            .lore(op.eng ? "§7loaded for" : "§7загруженные для")
            .lore(op.eng ? "§7this server" : "§7этого сервера")
            .lore(op.eng ? "§7RMB - details" : "§7ПКМ - подробно")
            .lore("")
            .build(), e -> {
            if (e.isLeftClick()) {
                pm.openGroupsAndPermsDB(p, 0);
            } else if (e.isRightClick()) {
                pm.openPerms(p, 0);
            }
        }));


        final int repu_base = op.getDataInt(Data.REPUTATION);
        final int playDay = op.getStat(Stat.PLAY_TIME) / 86400;
        final int passFill = StatManager.getPassportFill(op);
        final int statFill = op.getStatFill();
        final int groupCounter = StatManager.getGroupCounter(op);
        final int reportCounter = op.getDataInt(Data.REPORT_C) + op.getDataInt(Data.REPORT_P);
        final int friendCounter = op.friends.size();

        content.set(2, 2, ClickableItem.empty(new ItemBuilder(ItemType.NETHERITE_CHESTPLATE)
                    .name(op.eng ? "§bReputation" : "§bРепутация")
                    .flags(ItemFlag.HIDE_ATTRIBUTES)
                    .lore("")
                    .lore(op.eng ? "§bReputation indicate trust to you." : "§bРепутация - показатель доверия к Вам.")
                    .lore("")
                    .lore((op.eng ? "§fNow your reputation : " : "§fСейчас Ваша репутация : ") + op.getReputationDisplay())
                    .lore("§7")
                    .lore(op.eng ? "§7Calc Reputation:" : "§7Расчёт репутации:")
                    .lore((op.eng ? "§6Base value: " : "§6Базовое значение: ") + (repu_base == 0 ? "§80" : (repu_base > 0 ? "§a" + repu_base : "§c" + repu_base)))
                    .lore((op.eng ? "§7Play days: " : "§7Игровые дни: ") + (playDay > 0 ? "§a+" + playDay : "§80"))
                    .lore((op.eng ? "§7Passport fullness: " : "§7Наполненность паспорта: ") + (passFill > 0 ? "§a+" + passFill : "§80"))
                    .lore((op.eng ? "§7Stats fullness: " : "§7Наполненность статистики: ") + (statFill > 0 ? "§a+" + statFill : "§80"))
                    .lore((op.eng ? "§7Groups: " : "§7Группы: ") + (groupCounter > 0 ? "§a+" + groupCounter : "§80"))
                    .lore((op.eng ? "§7Friends: " : "§7Друзья: ") + (friendCounter > 0 ? "§a+" + friendCounter : "§80"))
                    .lore((op.eng ? "§7Reports: " : "§7Репорты: ") + (reportCounter > 0 ? "§c-" + reportCounter : "§80"))
                    .lore((op.eng ? "'§fTrust§7': " : "'§fДоверие§7': ") + (p.hasPermission("ostrov.trust") ? "§a+200" : "§5нет"))
                    .lore("")
                    .lore(op.eng ? "§7Yoyr features on the server" : "§7От репутаци зависят Ваши")
                    .lore(op.eng ? "§7depend on reputation." : "§7возможности на сервере.")
                    .build()
                //, e-> {
                //        op.getPlayer().sendMessage("ppp");
                //    }
            )
        );

        final int karma_base = op.getDataInt(Data.KARMA);

        content.set(2, 4, ClickableItem.empty(new ItemBuilder(ItemType.GLOW_BERRIES)
                    .name(op.eng ? "§bKarma" : "§bКарма")
                    .lore("")
                    .lore(op.eng ? "§bKarma - how successful are you?." : "§bКарма - насколько Вы успешны.")
                    .lore("")
                    .lore((op.eng ? "§fNow your karma : " : "§fСейчас Ваша карма : ") + op.getKarmaDisplay())
                    .lore("§7")
                    .lore(op.eng ? "§7Calc karma:" : "§7Расчёт кармы:")
                    .lore((op.eng ? "§6Base value: " : "§6Базовое значение: ") + (karma_base == 0 ? "§7нет" : (karma_base > 0 ? "§a" + karma_base : "§c" + karma_base)))
                    .lore((op.eng ? "§2Wins: §a+" : "§2Победы: §a+") + op.getKarmaModifier(Stat.KarmaChange.ADD))
                    .lore((op.eng ? "§4Loose: §c-" : "§4Поражения: §c-") + op.getKarmaModifier(Stat.KarmaChange.SUB))
                    .lore("")
                    .lore(op.eng ? "§7Karma cal help understand," : "§7Карма поможет понять,")
                    .lore(op.eng ? "§7how useful is the player" : "§7стоит ли иметь дело с игроком")
                    .lore(op.eng ? "§7(fight, join a team, etc.)" : "§7(сражаться, принимать в команду и т.д.)")
                    .build()
                //, e-> {
                //        op.getPlayer().sendMessage("ppp");
                //    }
            )
        );


        if (GM.GAME.type == ServerType.LOBBY) {
            content.set(2, 6, ClickableItem.of(new ItemBuilder(ItemType.PITCHER_PLANT)
                .name(op.eng ? "§5Change skin" : "§5Сменить скин")
                .lore("")
                .lore(op.eng ? "§7LMB - open skin menu" : "§7ЛКМ - открыть меню скинов")
                .lore("")
                .build(), e -> {
                SkinRestorerHook.openGui(p, 0);
            }));
        } else {
            content.set(2, 6, ClickableItem.empty(new ItemBuilder(ItemType.PITCHER_PLANT)
                .name(op.eng ? "§5Change skin" : "§5Сменить скин")
                .lore("")
                .lore(op.eng ? "§7Go to lobby" : "§eДля смены скина перейдите в лобби")
                .lore("")
                .build()
            ));
        }


        content.set(3, 1, ClickableItem.of(new ItemBuilder(ItemType.BOOKSHELF)
                    .name(op.eng ? "§7Journal" : "§7Журнал")
                    .lore("")
                    .lore(op.eng ? "§7LMB - §fview gere" : "§7ЛКМ - §fпросмотр здесь")
                    .lore("")
                    .lore(op.eng ? "§7RMB - §fview in chat" : "§7ПКМ - §fпросмотр в чате")
                    .lore(op.eng ? "§7(faster)" : "§7(работает быстрее)")
                    .lore("")
                    .build()
                , e -> {
                    if (e.isLeftClick()) {
                        pm.openJournal(p, 0);
                    } else if (e.isRightClick()) {
                        p.closeInventory();
                        ApiOstrov.executeBungeeCmd(p, "journal");
                    }

                }
            )
        );


        content.set(3, 3, ClickableItem.of(new ItemBuilder(ItemType.PAPER)
            .name(op.eng ? "§6Reports" : "§6Репорты")
            .lore("")
            .lore(op.eng ? "§7LMB - §сYour jambs." : "§7ЛКМ - §сВаши косяки.")
            .lore(op.eng ? "§7Appeal is possible" : "§7Обжалование возможно")
            .lore(op.eng ? "§7by request in a group." : "§7по заявке в группе.")
            .lore("")
            .lore(op.eng ? "§7RMB - §eView recent" : "§7ПКМ - §eПросмотр свежих")
            .lore(op.eng ? "§7Shows all reports," : "§7Покажет все репорты,")
            .lore(op.eng ? "submitted to anyone." : "поданные на кого-либо.")
            .lore("")
            .lore(op.eng ? "§eMake report" : "§eПодать Жалобу")
            .lore(op.eng ? "§fyou can use the command" : "§fможно командой")
            .lore(op.eng ? "§e/report name essence" : "§e/report ник жалоба")
            .build(), e -> {
            if (e.isLeftClick()) {
                ReportCmd.openPlayerReports(p, op, p.getName(), 0);
            } else if (e.isRightClick()) {
                ReportCmd.openAllReports(p, op, 0);
            }
        }));


        content.set(3, 5, ClickableItem.of(new ItemBuilder(ItemType.WITHER_SKELETON_SKULL)
                    .name(op.eng ? "§7Ignore - list" : "§7Игнор - лист")
                    .lore("")
                    .lore(op.eng ? "§7You can add" : "§7Вы можете добавить")
                    .lore(op.eng ? "§7annoying player" : "§7надоедливого игрока")
                    .lore(op.eng ? "§7into a blacklist" : "§7в Чёрный список")
                    .lore(op.eng ? "§7by command §b/ignore add <name>" : "§7командой §b/ignore add <ник>")
                    .lore(op.eng ? "§7Remove from blacklist - command" : "§7Удалить из ЧС - команда")
                    .lore(op.eng ? "§b/ignore del <name>" : "§b/ignore del <ник>")
                    .lore(op.eng ? "§7or in the menu on this button." : "§7или в меню на этой кнопке.")
                    .lore("")
                    .lore(op.getBlackListed().isEmpty() ? (op.eng ? "§8blacklist is empty" : "§8Список пуст") : (op.eng ? "§7LMB - §fedit" : "§7ЛКМ - §fредактировать"))
                    .lore("")
                    .build()
                , e -> {
                    if (op.getBlackListed().isEmpty()) {
                        PM.soundDeny(p);
                    } else {
                        pm.openIgnoreList(p);
                    }
                }
            )
        );


        content.set(3, 7, ClickableItem.of(new ItemBuilder(ItemType.NAME_TAG)
            .name(op.eng ? "§7Accaunts" : "§7Учётные данные")
            .lore("")
            .lore(op.eng ? "§7LMB - find others" : "§7ЛКМ - найти другие")
            .lore(op.eng ? "§7accounts for your IP," : "§7аккаунты для вашего IP,")
            .lore(op.eng ? "§7specify how much more" : "§7уточнить сколько еще")
            .lore(op.eng ? "§7can be created." : "§7можно создать.")
            .lore("")
            .build(), e -> {
            pm.openAkkauntsDB(p);
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
