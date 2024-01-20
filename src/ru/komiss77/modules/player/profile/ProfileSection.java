package ru.komiss77.modules.player.profile;

import ru.komiss77.modules.player.mission.ProfileWithdrawMenu;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemFlag;
import ru.komiss77.ApiOstrov;
import ru.komiss77.commands.ReportCmd;
import ru.komiss77.enums.Data;
import ru.komiss77.enums.Settings;
import ru.komiss77.modules.player.PM;
import ru.komiss77.enums.Stat;
import ru.komiss77.modules.Pandora;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.mission.MissionManager;
import ru.komiss77.modules.translate.Lang;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.SmartInventory;


public class ProfileSection implements InventoryProvider {
            
   private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Section.ПРОФИЛЬ.glassMat).name("§8.").build());
    
    
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
        content.fillRow(4, fill);
        
        //выставить иконки внизу
        for (Section section:Section.values()) {
            content.set(section.slot, Section.getMenuItem(section, op));
        }
        
        final boolean justGame = op.hasSettings(Settings.JustGame);
        content.set( 0,4, ClickableItem.of(new ItemBuilder(op.eng ? Material.RIB_ARMOR_TRIM_SMITHING_TEMPLATE : Material.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE)
                .name(op.eng ? "§7JustGame Menu Mode" : "§7Режим простого меню")
                .addLore("") //0
                .addLore(op.eng ? "§7Now: "+(justGame?"§6ON":"§aOFF") : "§7Сейчас: "+(justGame?"§6Включен":"§aВыключен")) //1
                .addLore(op.eng ? "§7Click - change" : "§7ЛКМ - изменить") //2
                .addLore("")
                .addLore("§7С режимом JustGame")
                .addLore("§7будут отключены квесты и")
                .addLore("§7будут сразы выдаваться все")
                .addLore("§7в лобби")
                .addLore("")
                .addFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ITEM_SPECIFICS)
                .build()
                    , e-> {
                        op.setSettings(Settings.JustGame, !justGame);
                        reopen(p, content);
                    } 
            )
        );        

        content.set( 1,1, ClickableItem.empty(new ItemBuilder(Material.CLOCK)
                .name(op.eng ? "§7Play time" : "§7Игровое время")
                .addLore("") //0
                .addLore(op.eng ? "§7Total play time since register," : "§7Общее игровое время с момента регистрации,") //1
                .addLore(op.eng ? "§7and daily growth." : "§7и ежедневный прирост.") //2
                .addLore("") //3
                .addLore( Lang.t(p, Stat.PLAY_TIME.desc)+ApiOstrov.secondToTime(op.getStat(Stat.PLAY_TIME)) ) //4 игровое время, обновляется каждую секунду, если наиграл меньше недели!!
                .addLore("") //5 наиграно за сегодня, обновляется каждую секунду
                .addLore("")
                .addLore( Pandora.getInfo(op) )
                .addLore("")
                .build()
           // , e-> {
            //        op.getPlayer().sendMessage("ppp");
            //    } 
            )
        );

        content.set(1, 3, ClickableItem.empty(new ItemBuilder(Material.EXPERIENCE_BOTTLE)
                .name(op.eng ? "§7Level" : "§7Уровень")
                .addLore("")
                .addLore(op.eng ? "§7Islander level displays" : "§7Уровень островитянина отображает")
                .addLore(op.eng ? "§7your skill. " : "§7ваше мастерство. ")
                .addLore(op.eng ? "§7The higher the level," : "§7Чем выше уровень,")
                .addLore(op.eng ? "§7the more opportunities for you." : "§7тем больше для Вас возможностей.")
                .addLore("")
                .addLore( Lang.t(p, Stat.LEVEL.desc)+op.getStat(Stat.LEVEL) +"  "+ApiOstrov.getPercentBar(op.getStat(Stat.LEVEL)*25, op.getStat(Stat.EXP), true)  )
                .addLore((op.eng ? "§fLevel increase today : " : "§fПрирост уровня за сегодня : ") + (op.getDaylyStat(Stat.LEVEL)>0 ? "§e+"+op.getDaylyStat(Stat.LEVEL) : "§7нет" ) )
                .addLore( (op.eng ? "§fExp to the next level : §a§l" : "§fОпыта до следующего уровня : §a§l")+(op.getStat(Stat.LEVEL)*25-op.getStat(Stat.EXP)+1) )
                //.addLore( ApiOstrov.getPercentBar(op.getStat(Stat.LEVEL)*25, op.getStat(Stat.EXP), true) )
                .addLore("")
                .build()
            //, e-> {
            //        op.getPlayer().sendMessage("ppp");
            //    } 
            )
        );
        
        final boolean canWitdraw = op.getDataInt(Data.RIL)>=MissionManager.getMin(op);
        content.set(1, 5, ClickableItem.of(new ItemBuilder(Material.GOLD_INGOT)
                .name(op.eng ? "§7Finance" : "§7Финансы")
                .addLore("")
                .addLore(op.eng ? "§fAt your disposal:" : "§fВ Вашем распоряжении:")
                .addLore("§f"+op.getDataInt(Data.LONI)+(op.eng ? " §aLoni" : " §aЛони"))
                .addLore("§f"+op.getDataInt(Data.RIL)+(op.eng ? " §eRil" : " §eРил"))
                .addLore("")
                .addLore(op.eng ? "§aLoni §7- in-game currency" : "§aЛони §7- внутриигровая валюта")
                .addLore(op.eng ?  "§7on the project. Is used for" : "§7на проекте. Используется для")
                .addLore(op.eng ? "§7turnover, game actions, etc." : "§7товарооборота, игровых действий и т.д.")
                .addLore("")
                .addLore(op.eng ? "§eRil §7- real money equivalent." : "§eРил §7- счёт, приравненный к рублёвому.")
                .addLore(op.eng ? "§7You can buy privileges with Ryl," : "§7За Рил можно купить привилегии,")
                .addLore(op.eng ? "§7or withdraw to your phone or card." : "§7или вывести на телефон или карту.")
                .addLore(op.eng ? "§7*(subject to certain conditions)" : "§7*(при соблюдении ряда условий)")
                .addLore(op.eng ? "§7Ril can be earned by completing tasks!" : "§7Рил можно заработать, выполняя задания!")
                .addLore("")
                .addLore(op.eng ? "§7LMB - §ftop up your account §eRil" : "§7ЛКМ - §fпополнить счёт §eРил")
                .addLore(op.eng ? "§7RMB - §fwithdrawal requests journal" : "§7ПКМ - §fжурнал заявок на вывод")
                .addLore(canWitdraw ? (op.eng ? "Key.Q - §forder withdrawal" : "§6Клав.Q - §fзаказать вывод") : (op.eng ? "" : "§8§mКлав.Q - заказать вывод"))
                .addLore(canWitdraw ? (op.eng ? "§7Completed withdraws: §f" : "§7Выполнено выводов: §f")+op.getStat(Stat.WD_c) : (op.eng ? "" : "§5Вывод средств возможен от §b")+MissionManager.getMin(op)+" рил")
                .addLore(canWitdraw ? "" : (op.eng ? "§5Calc:  §3(1 + withdraws amm.)*5" : "§5Расчёт:  §3(1 + кол-во выводов)*5"))
                .addLore("")
                .build()
                , e-> {
                    switch (e.getClick()) {
                        
                        case LEFT:
                            p.closeInventory();
                            ApiOstrov.executeBungeeCmd(p, "money add");
                            break;
                            
                        case RIGHT:
                            pm.openWithdrawalRequest(p, true);
                            break;
                            
                        case DROP:
                            if (op.getDataInt(Data.RIL)>=MissionManager.getMin(op)) {
                                SmartInventory
                                .builder()
                                .id(op.nik+"Миссии")
                                .type(InventoryType.HOPPER)
                                .provider(new ProfileWithdrawMenu(op.getDataInt(Data.RIL)))
                                .title("Новая заявка на вывод Рил")
                                .build()
                                .open(p);
                            } else {
                                PM.soundDeny(p);
                                p.sendMessage("§6Накопите не менее §b"+MissionManager.getMin(op)+" рил§6, чтобы заказать вывод средств!");
                            }
                            break;
                            
                        default:
                            break;
                    }
                } 
            )
        );
        
        
        
        content.set(1 ,7, ClickableItem.of(new ItemBuilder(Material.BEACON)
            .name(op.eng ? "§7Groups and perms" : "§7Группы и права")
            .addLore("")
            .addLore(op.eng ? "§7Detailed information" : "§7Подробная информация")
            .addLore(op.eng ? "§7about your groups" : "§7о ваших группах")
            .addLore(op.eng ? "§7and personal perms." : "§7и личных правах.")
            .addLore("")
            .addLore((op.eng ? "§7Active groups found: " : "§7Найдено активных групп: ")+op.getGroups().size())
            .addLore("")
            .addLore(op.eng ? "§7LMB - get data from the DB" : "§7ЛКМ - данные из БД")
            .addLore("")
            .build(), e-> {
                pm.openGroupsAndPermsDB(p, 0);
            }));


        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        final int repu_base = op.getDataInt(Data.REPUTATION);
        final int playDay = op.getStat(Stat.PLAY_TIME) / 86400;
        final int passFill = StatManager.getPassportFill(op);
        final int statFill = op.getStatFill();
        final int groupCounter = StatManager.getGroupCounter(op);
        final int reportCounter = op.getDataInt(Data.REPORT_C)+op.getDataInt(Data.REPORT_P);
        final int friendCounter = op.friends.size();
        
        content.set(2, 2, ClickableItem.empty (new ItemBuilder(Material.NETHERITE_CHESTPLATE)
                .name(op.eng ? "§bReputation" : "§bРепутация")
                .addFlags(ItemFlag.HIDE_ATTRIBUTES)
                .addLore("")
                .addLore(op.eng ? "§bReputation indicate trust to you." : "§bРепутация - показатель доверия к Вам.")
                .addLore("")
                .addLore((op.eng ?  "§fNow your reputation : " : "§fСейчас Ваша репутация : ")+ op.getReputationDisplay())
                .addLore("§7")
                .addLore(op.eng ? "§7Calc Reputation:" : "§7Расчёт репутации:")
                .addLore((op.eng ? "§6Base value: " : "§6Базовое значение: ")+  (repu_base==0 ? "§80" : (repu_base>0 ? "§a"+repu_base:"§c"+repu_base)))
                .addLore((op.eng ? "§7Play days: " : "§7Игровые дни: ") + (playDay>0 ? "§a+"+playDay : "§80") )
                .addLore((op.eng ? "§7Passport fullness: " : "§7Наполненность паспорта: ")+ (passFill>0 ? "§a+"+passFill : "§80") )
                .addLore((op.eng ? "§7Stats fullness: " : "§7Наполненность статистики: ")+ (statFill>0 ? "§a+"+statFill : "§80") )
                .addLore((op.eng ? "§7Groups: " : "§7Группы: ")+ (groupCounter>0 ? "§a+"+groupCounter : "§80") )
                .addLore((op.eng ? "§7Friends: " : "§7Друзья: ")+ (friendCounter>0 ? "§a+"+friendCounter : "§80") )
                .addLore((op.eng ? "§7Reports: " : "§7Репорты: ")+ (reportCounter>0 ? "§c-"+reportCounter : "§80") )
                .addLore((op.eng ? "'§fTrust§7': " : "'§fДоверие§7': ")+ (p.hasPermission("ostrov.trust") ? "§a+200" : "§5нет"))
                .addLore("")
                .addLore(op.eng ? "§7Yoyr features on the server" : "§7От репутаци зависят Ваши")
                .addLore(op.eng ? "§7depend on reputation." : "§7возможности на сервере.")
                .build()
            //, e-> {
            //        op.getPlayer().sendMessage("ppp");
            //    } 
            )
        );
                
        final int karma_base = op.getDataInt(Data.KARMA);
        
        content.set(2, 4, ClickableItem.empty (new ItemBuilder(Material.GLOW_BERRIES)
                .name(op.eng ? "§bKarma" : "§bКарма")
                .addLore("")
                .addLore(op.eng ?"§bKarma - how successful are you?."  : "§bКарма - насколько Вы успешны.")
                .addLore("")
                .addLore((op.eng ? "§fNow your karma : " : "§fСейчас Ваша карма : ")+ op.getKarmaDisplay()   )
                .addLore("§7")
                .addLore(op.eng ? "§7Calc karma:" : "§7Расчёт кармы:")
                .addLore((op.eng ? "§6Base value: " : "§6Базовое значение: ")+  (karma_base==0 ? "§7нет" : (karma_base>0 ? "§a"+karma_base:"§c"+karma_base)))
                .addLore((op.eng ?  "§2Wins: §a+" : "§2Победы: §a+")+ op.getKarmaModifier(Stat.KarmaChange.ADD))
                .addLore((op.eng ? "§4Loose: §c-" : "§4Поражения: §c-")+ op.getKarmaModifier(Stat.KarmaChange.SUB))
                .addLore("")
                .addLore(op.eng ?  "§7Karma cal help understand," : "§7Карма поможет понять,")
                .addLore(op.eng ? "§7how useful is the player" : "§7стоит ли иметь дело с игроком")
                .addLore(op.eng ? "§7(fight, join a team, etc.)" : "§7(сражаться, принимать в команду и т.д.)")
                .build()
            //, e-> {
            //        op.getPlayer().sendMessage("ppp");
            //    } 
            )
        );
                
                
        content.set(2 ,6, ClickableItem.of(new ItemBuilder(Material.LIME_DYE)
            .name(op.eng ? "§7Check permissions" : "§7Проверить права")
            .addLore("")
            .addLore(op.eng ? "§7Show permissions" : "§7Показать права (пермишены)")
            .addLore(op.eng ? "§7loaded for" : "§7загруженные для")
            .addLore(op.eng ? "§7this server" : "§7этого сервера")
            .addLore("")
            .addLore((op.eng ? "§7Records found: §6" : "§7Найдено записей: §6")+op.user_perms.size())
            .addLore("")
            .addLore(op.eng ? "§7LMB - details" : "§7ЛКМ - подробно")
            .addLore("")
            .build(), e-> {
                pm.openPerms(p, 0);
            }));

        

        


        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        

        
        
        
        content.set(3, 1, ClickableItem.of (new ItemBuilder(Material.BOOKSHELF)
                .name(op.eng ? "§7Journal" : "§7Журнал")
                .addLore("")
                .addLore(op.eng ?  "§7LMB - §fview gere" : "§7ЛКМ - §fпросмотр здесь")
                .addLore("")
                .addLore(op.eng ? "§7RMB - §fview in chat" : "§7ПКМ - §fпросмотр в чате")
                .addLore(op.eng ? "§7(faster)" : "§7(работает быстрее)")
                .addLore("")
                .build()
            , e-> {
                if (e.isLeftClick()) {
                    pm.openJournal(p, 0);
                } else if (e.isRightClick()) {
                    p.closeInventory();
                    ApiOstrov.executeBungeeCmd(p, "journal");
                }
                    
                } 
            )
        );
        
        
        
        
        
        
        
        
        
        
            content.set(3,3, ClickableItem.of(new ItemBuilder(Material.PAPER)
            .name(op.eng ? "§6Reports" : "§6Репорты")
            .addLore("")
            .addLore(op.eng ? "§7LMB - §сYour jambs." : "§7ЛКМ - §сВаши косяки.")
            .addLore(op.eng ? "§7Appeal is possible" : "§7Обжалование возможно")
            .addLore(op.eng ? "§7by request in a group." : "§7по заявке в группе.")
            .addLore("")
            .addLore(op.eng ? "§7RMB - §eView recent" : "§7ПКМ - §eПросмотр свежих")
            .addLore(op.eng ? "§7Shows all reports," : "§7Покажет все репорты,")
            .addLore(op.eng ? "submitted to anyone." : "поданные на кого-либо.")
            .addLore("")
            .addLore(op.eng ? "§eMake report" : "§eПодать Жалобу")
            .addLore(op.eng ? "§fyou can use the command" : "§fможно командой")
            .addLore(op.eng ? "§e/report name essence" : "§e/report ник жалоба")
            .build(), e-> {
                if (e.isLeftClick()) {
                    ReportCmd.openPlayerReports(p, op, p.getName(), 0);
                } else if (e.isRightClick()) {
                    ReportCmd.openAllReports(p, op, 0);
                }
            }));

    
        
        
        
        
        
        
        content.set(3, 5, ClickableItem.of (new ItemBuilder(Material.WITHER_SKELETON_SKULL)
                .name(op.eng ?  "§7Ignore - list" : "§7Игнор - лист")
                .addLore("")
                .addLore(op.eng ? "§7You can add" : "§7Вы можете добавить")
                .addLore(op.eng ? "§7annoying player" : "§7надоедливого игрока")
                .addLore(op.eng ? "§7into a blacklist" : "§7в Чёрный список")
                .addLore(op.eng ? "§7by command §b/ignore add <name>" : "§7командой §b/ignore add <ник>")
                .addLore(op.eng ? "§7Remove from blacklist - command" : "§7Удалить из ЧС - команда")
                .addLore(op.eng ? "§b/ignore del <name>" : "§b/ignore del <ник>")
                .addLore(op.eng ? "§7or in the menu on this button." : "§7или в меню на этой кнопке.")
                .addLore("")
                .addLore(op.getBlackListed().isEmpty() ? (op.eng ? "§8blacklist is empty" : "§8Список пуст") : (op.eng ? "§7LMB - §fedit" : "§7ЛКМ - §fредактировать"))
                .addLore("")
                .build()
            , e-> {
                    if (op.getBlackListed().isEmpty()) {
                        PM.soundDeny(p);
                    } else {
                        pm.openIgnoreList(p);
                    }
                } 
            )
        );
        
        
        
        content.set(3 ,7, ClickableItem.of(new ItemBuilder(Material.NAME_TAG)
            .name(op.eng ? "§7Accaunts" : "§7Учётные данные")
            .addLore("")
            .addLore(op.eng ? "§7LMB - find others" : "§7ЛКМ - найти другие")
            .addLore(op.eng ? "§7accounts for your IP," : "§7аккаунты для вашего IP,")
            .addLore(op.eng ? "§7specify how much more" : "§7уточнить сколько еще")
            .addLore(op.eng ? "§7can be created." : "§7можно создать.")
            .addLore("")
            .build(), e-> {
                pm.openAkkauntsDB(p);
            }));

        


                
        
        
        
        
        
        
              
            
        
        





        

        /*
        
        content.set( 5, 8, ClickableItem.of( new ItemBuilder(Material.OAK_DOOR).name("Закрыть").build(), e -> 
        {
            p.closeInventory();
        }
        ));
        
*/

        

    }


    
    
    
    
    
    
    
    
    
}
