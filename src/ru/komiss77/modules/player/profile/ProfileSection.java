package ru.komiss77.modules.player.profile;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.commands.ReportCmd;
import ru.komiss77.enums.Data;
import ru.komiss77.enums.Stat;
import ru.komiss77.modules.Pandora;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.player.mission.MissionManager;
import ru.komiss77.modules.player.mission.ProfileWithdrawMenu;
import ru.komiss77.modules.translate.Lang;
import ru.komiss77.utils.PlayerInput;
import ru.komiss77.utils.StringUtil;
import ru.komiss77.utils.TCUtil;
import ru.komiss77.utils.TimeUtil;
import ru.komiss77.utils.inventory.*;


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
        content.set(0, 0, ClickableItem.of(new ItemBuilder(ItemType.RECOVERY_COMPASS)
            .name(op.eng ? "§eAccount Data" : "§eИгровые Данные")
            .lore("")
            .lore(Lang.t(p, Stat.LEVEL.desc) + totalLvl + "  " + StringUtil.getPercentBar(totalLvl * 25, exp, true))
            .lore((op.eng ? "<mithril>Increased today by: " : "<mithril>Прирост за сегодня: ") + (dailyLvl > 0 ? "§e+" + dailyLvl : "§80"))
            .lore((op.eng ? "<mithril>Exp needed for next level: §a§l" : "<mithril>Опыта до след. уровня: §a§l") + (totalLvl * 25 - exp + 1))
            .lore("")
            .lore(Lang.t(p, Stat.PLAY_TIME.desc) + TimeUtil.secondToTime(op.getStat(Stat.PLAY_TIME)))
            .lore((op.eng ? "<mithril>Play time today: §a" : "<mithril>Сегодня наиграно: §a")
                + TimeUtil.secondToTime(op.getDailyStat(Stat.PLAY_TIME)))
            .lore("")
            .lore((op.eng ? "§bReputation: " : "§bРепутация: ") + op.getReputationDisplay())
            .lore(op.eng ? "<mithril>This indicates account trust" : "<mithril>Это показатель доверия к аккаунту")
            .lore("")
            .lore(op.eng ? "§7LMB - §9other accounts tied to your IP" : "§7ЛКМ - §9Другие аккаунты с твоего IP")
            .lore(Pandora.getInfo(op))
            .build(), e -> pm.openAkkauntsDB(p)));

        final int from = MissionManager.getMin(op) - op.getDataInt(Data.RIL);
        content.set(0, 1, ClickableItem.of(new ItemBuilder(ItemType.RAW_GOLD)
            .name(op.eng ? "§6Finances" : "§6Финансы")
            .lore(op.eng ? "§fAt your disposal:" : "§fВ твоем распоряжении:")
            .lore("")
            .lore((op.eng ? "§2Loni: <green>" : "§2Лони: <green>") + op.getDataInt(Data.LONI) + Ostrov.L)
            .lore(op.eng ? "§aLoni <mithril>- the server's in-game currency." : "§aЛони <mithril>- внутриигровая валюта на проекте.")
            .lore(op.eng ? "<mithril>It's used for trading, upgrades, etc." : "<mithril>Используются для торговли, прокачки, и т.д.")
            .lore("")
            .lore((op.eng ? "<amber>Ril: <yellow>" : "<amber>Рил: <yellow>") + op.getDataInt(Data.RIL) + Ostrov.R)
            .lore(op.eng ? "§eRil <mithril>- a real money equivalent. Use it to" : "§eРил <mithril>- счёт, приравненный к рублёвому. Используй его")
            .lore(op.eng ? "<mithril>buy ranks, or withdraw to your phone / card." : "<mithril>для покупки привилегий, или вывода на телефон / карту.")
            .lore(op.eng ? "<gold>Complete missions to earn §eRil" : "<gold>Их можно заработать, выполняя миссии!")
            .lore("")
            .lore(op.eng ? "§7LMB - §fPurchase more §eRil" : "§7ЛКМ - §fПополнить счёт §eРил")
            .lore(from > 0 ? (op.eng ? "§8You need <amber>" + from + " §8more <amber>Ril §8to withdraw"
                : "§8Тебе нужно еще <amber>" + from + " Рил §8для вывода")
                : (op.eng ? "§7RMB - §fRequest a §eRil payout" : "§7ПКМ - §fЗаказать вывод §eРил"))
            .lore(op.eng ? "§7Shift+RMB - §fYour §eRil §7transactions" : "§7Шифт+ПКМ - §fЖурнал операций §eРил")
            .lore("<gray>" + TCUtil.bind(TCUtil.Input.DROP) + " - "
                + (op.eng ? "§fExchange §eRil §7for §aLoni" : "§fПоменять §eРил §fна §aЛони"))
            .lore("§81" + Ostrov.R + " -> 50" + Ostrov.L).build(), e -> {
            final int from_in = MissionManager.getMin(op) - op.getDataInt(Data.RIL);
            switch (e.getClick()) {
                case LEFT:
                    p.closeInventory();
                    ApiOstrov.executeBungeeCmd(p, "money add");
                    break;
                case RIGHT:
                    if (op.getDataInt(Data.RIL) >= MissionManager.getMin(op)) {
                        SmartInventory.builder().id(op.nik + "Payout").type(InventoryType.HOPPER)
                            .provider(new ProfileWithdrawMenu(op.getDataInt(Data.RIL)))
                            .title("<gold><b>Заявка на вывод <yellow>Рил").build().open(p);
                    } else {
                        PM.soundDeny(p);
                        p.sendMessage("§6Накопи еще§e" + from_in + " рил§6, чтобы заказать вывод!");
                    }
                    break;
                case SHIFT_RIGHT:
                    pm.openWithdrawalRequest(p, true);
                    break;
                case DROP:
                    PlayerInput.get(p, 10, 1, 1000,
                        amt -> p.performCommand("money change " + amt));
                    break;
                default:
                    break;
            }
        }));


        content.set(0, 2, ClickableItem.of(new ItemBuilder(ItemType.EMERALD)
            .name(op.eng ? "§bGroups and perms" : "§bГруппы и Права")
            .lore("")
            .lore(op.eng ? "<mithril>Detailed information about" : "<mithril>Подробная информация о твоих")
            .lore(op.eng ? "<mithril>your groups on this server" : "<mithril>группах на этом сервере")
            .lore((op.eng ? "§7LMB - §fShow all groups §3(" : "§7ЛКМ - §fПоказать группы §3(") + op.getGroups().size() + ")")
            .lore(op.eng ? "§7RMB - §fShow your permissions" : "§7ПКМ - §fПоказать права (пермы)")
            .build(), e -> {
            if (e.isLeftClick()) {
                pm.openGroupsAndPermsDB(p, 0);
            } else if (e.isRightClick()) {
                pm.openPerms(p, 0);
            }
        }));


        /*final int repu_base = op.getDataInt(Data.REPUTATION);
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
        );*/

        /*final int karma_base = op.getDataInt(Data.KARMA);

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
        );*/

        //TODO скин меню
        /*if (GM.GAME.type == ServerType.LOBBY) {
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
        }*/


        content.set(0, 3, ClickableItem.of(new ItemBuilder(ItemType.KNOWLEDGE_BOOK)
            .name(op.eng ? "<beige>Journal" : "<beige>Журнал")
            .lore("")
            .lore(op.eng ? "<mithril>This is a log of your important actions!" : "<mithril>Сдесь записаны твои важные действия!")
            .lore(op.eng ? "<dark_gray>(online sessions, buying groups, etc.)" : "<dark_gray>(онлайн сесии, покупка привилегий, т.д.)")
            .lore("")
            .lore(op.eng ? "§7LMB - §fView in menu" : "§7ЛКМ - §fПоказать в меню")
            .lore(op.eng ? "§7RMB - §fView in chat" : "§7ПКМ - §fПоказать в чате")
            .lore(op.eng ? "§8(loads faster)" : "§8(работает быстрее)")
            .build(), e -> {
            if (e.isLeftClick()) {
                pm.openJournal(p, 0);
            } else if (e.isRightClick()) {
                p.closeInventory();
                ApiOstrov.executeBungeeCmd(p, "journal");
            }
        }));


        content.set(0, 4, ClickableItem.of(new ItemBuilder(ItemType.PAPER)
            .name(op.eng ? "§6Reports" : "§6Репорты")
            .lore("")
            .lore(op.eng ? "§7LMB - §сYour jambs." : "§7ЛКМ - §сПроверить свои косяки")
            .lore(op.eng ? "<mithril>Appeal them in /discord or /telegram"
                : "<mithril>Обжалуй их в /discord или /telegram")
            .lore("")
            .lore(op.eng ? "§7RMB - §eView recent" : "§7ПКМ - §eРепорты на других игроков")
            .lore(op.eng ? "<gray>Shift+Click - §6Submit a report" : "<gray>Шифт+Клик - §6Подать жалобу")
            .build(), e -> {
            switch (e.getClick()) {
                case LEFT:
                    ReportCmd.openPlayerReports(p, op, p.getName(), 0);
                    break;
                case RIGHT:
                    ReportCmd.openAllReports(p, op, 0);
                    break;
                case SHIFT_LEFT, SHIFT_RIGHT:
                    PlayerInput.get(InputButton.InputType.ANVILL, p,
                        name -> PlayerInput.get(InputButton.InputType.ANVILL, p,
                            reason -> p.performCommand("report " + name + " " + reason),
                            "Жалоба"), "Ник");
                    break;
            }
        }));


        content.set(0, 5, ClickableItem.of(new ItemBuilder(ItemType.WITHER_SKELETON_SKULL)
            .name(op.eng ? "<stale>Ignore - List" : "<stale>Игнор - Список")
            .lore("")
            .lore(op.eng ? "<mithril>You can add annoyoing players" : "<mithril>Можешь добавлять сюда надоедливых")
            .lore(op.eng ? "<mithril>that spam your chat / dms here!" : "<mithril>игроков, которые спамят в чат / лс!")
            .lore(op.getBlackListed().isEmpty() ? (op.eng ? "§8Blacklist's empty" : "§8Список пуст")
                : (op.eng ? "§7LMB - §fOpen" : "§7ЛКМ - §fОткрыть"))
            .lore(op.eng ? "<stale>The blacklist lasts" : "<stale>Чёрный список длится до")
            .lore(op.eng ? "<stale>until you log off" : "<stale>твоего выхода с сервера")
            .lore("")
            .lore(op.eng ? "§7Add player - §b/ignore <name>" : "§7Добавить - §b/ignore <ник>")
            .build(), e -> {
            if (op.getBlackListed().isEmpty()) {
                PM.soundDeny(p);
                return;
            }
            pm.openIgnoreList(p);
        }));
    }
}
