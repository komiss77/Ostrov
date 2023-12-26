package ru.komiss77.modules.player.profile;


import ru.komiss77.modules.player.mission.MoneyWithdrawMenu;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemFlag;
import ru.komiss77.ApiOstrov;
import ru.komiss77.commands.ReportCmd;
import ru.komiss77.enums.Data;
import ru.komiss77.modules.player.PM;
import ru.komiss77.enums.Stat;
import ru.komiss77.modules.Pandora;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.mission.MissionManager;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.SmartInventory;





public class ProfileSection implements InventoryProvider {
    
    
    
   private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Section.ПРОФИЛЬ.glassMat).name("§8.").build());
    

    
    public ProfileSection() {
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
        final ProfileManager pm = op.menu;

        //линия - разделитель
        content.fillRow(4, fill);
        
        //выставить иконки внизу
        for (Section section:Section.values()) {
            content.set(section.slot, Section.getMenuItem(section, op));
        }
        
        
        content.set( 0,4, ClickableItem.of(new ItemBuilder(op.eng ? Material.RIB_ARMOR_TRIM_SMITHING_TEMPLATE : Material.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE)
                .name(op.eng ? "§7Change Language" : "§7Сменить язык")
                .addLore("") //0
                .addLore(op.eng ? "§7Now: §fEnglish" : "§7Сейчас: §fРусский") //1
                .addLore(op.eng ? "§7Click - set Russian" : "§7ЛКМ - установить Английский") //2
                .addLore("")
                .addFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ITEM_SPECIFICS)
                .build()
            , e-> {
                        op.setData(Data.LANG, (op.eng ? 0 : 1));
                        reopen(p, content);
                } 
            )
        );        

        content.set( 1,1, ClickableItem.empty(new ItemBuilder(Material.CLOCK)
                .name("§7Игровое время")
                .addLore("") //0
                .addLore("§7Общее игровое время с момента регистрации,") //1
                .addLore("§7и ежедневный прирост.") //2
                .addLore("") //3
                .addLore( Stat.PLAY_TIME.desc+ApiOstrov.secondToTime(op.getStat(Stat.PLAY_TIME)) ) //4 игровое время, обновляется каждую секунду, если наиграл меньше недели!!
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
                .name("§7Уровень")
                .addLore("")
                .addLore("§7Уровень островитянина отображает")
                .addLore("§7ваше мастерство. ")
                .addLore("§7Чем выше уровень,")
                .addLore("§7тем больше для Вас возможностей.")
                .addLore("")
                .addLore( Stat.LEVEL.desc+op.getStat(Stat.LEVEL) +"  "+ApiOstrov.getPercentBar(op.getStat(Stat.LEVEL)*25, op.getStat(Stat.EXP), true)  )
                .addLore("§fПрирост уровня за сегодня : " + (op.getDaylyStat(Stat.LEVEL)>0 ? "§e+"+op.getDaylyStat(Stat.LEVEL) : "§7нет" ) )
                .addLore( "§fОпыта до следующего уровня : §a§l"+(op.getStat(Stat.LEVEL)*25-op.getStat(Stat.EXP)+1) )
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
                .name("§7Финансы")
                .addLore("")
                .addLore("§fВ Вашем распоряжении:")
                .addLore("§f"+op.getDataInt(Data.LONI)+" §aЛони")
                .addLore("§f"+op.getDataInt(Data.RIL)+" §eРил")
                .addLore("")
                .addLore("§aЛони §7- внутриигровая валюта")
                .addLore("§7на проекте. Используется для")
                .addLore("§7товарооборота, игровых действий и т.д.")
                .addLore("")
                .addLore("§eРил §7- счёт, приравненный к рублёвому.")
                .addLore("§7За Рил можно купить привилегии,")
                .addLore("§7или вывести на телефон или карту.")
                .addLore("§7*(при соблюдении ряда условий)")
                .addLore("§7Рил можно заработать, выполняя задания!")
                .addLore("")
                .addLore("§7ЛКМ - §fпополнить счёт §eРил")
                .addLore("§7ПКМ - §fжурнал заявок на вывод")
                .addLore(canWitdraw ? "§6Клав.Q - §fзаказать вывод" : "§8§mКлав.Q - заказать вывод")
                .addLore(canWitdraw ? "§7Выполнено выводов: §f"+op.getStat(Stat.WD_c) : "§5Вывод средств возможен от §b"+MissionManager.getMin(op)+" рил")
                .addLore(canWitdraw ? "" : "§5Расчёт:  §3(1 + кол-во выводов)*5")
                .addLore("")
                .build()
                , e-> {
                    switch (e.getClick()) {
                        
                        case LEFT:
                            p.closeInventory();
                            ApiOstrov.executeBungeeCmd(p, "money add");
                            break;
                            
                        case RIGHT:
                            pm.openWithdrawalRequest(p);
                            break;
                            
                        case DROP:
                            if (op.getDataInt(Data.RIL)>=MissionManager.getMin(op)) {
                                SmartInventory
                                .builder()
                                .id(op.nik+"Миссии")
                                .type(InventoryType.HOPPER)
                                .provider(new MoneyWithdrawMenu(op.getDataInt(Data.RIL)))
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
            .name("§7Группы и права")
            .addLore("")
            .addLore("§7Подробная информация")
            .addLore("§7о ваших группах")
            .addLore("§7и личных правах.")
            .addLore("")
            .addLore("§7Найдено активных групп: "+op.getGroups().size())
            .addLore("")
            .addLore("§7ЛКМ - данные из БД")
            .addLore("")
            .build(), e-> {
                pm.openGroupsAndPermsDB(p, 0);
            }));


        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        final int repu_base = op.getDataInt(Data.REPUTATION);
        final int playDay = ((int)op.getStat(Stat.PLAY_TIME)/86400);
        final int passFill = StatManager.getPassportFill(op);
        final int statFill = op.getStatFill();
        final int groupCounter = StatManager.getGroupCounter(op);
        final int reportCounter = op.getDataInt(Data.REPORT_C)+op.getDataInt(Data.REPORT_P);
        final int friendCounter = op.friends.size();
        
        content.set(2, 2, ClickableItem.empty (new ItemBuilder(Material.NETHERITE_CHESTPLATE)
                .name("§bРепутация")
                .addFlags(ItemFlag.HIDE_ATTRIBUTES)
                .addLore("")
                .addLore("§bРепутация - показатель доверия к Вам.")
                .addLore("")
                .addLore("§fСейчас Ваша репутация : "+ op.getReputationDisplay())
                .addLore("§7")
                .addLore("§7Расчёт репутации:")
                .addLore("§6Базовое значение: "+  (repu_base==0 ? "§80" : (repu_base>0 ? "§a"+repu_base:"§c"+repu_base)))
                .addLore("§7Игровые дни: " + (playDay>0 ? "§a+"+playDay : "§80") )
                .addLore("§7Наполненность паспорта: "+ (passFill>0 ? "§a+"+passFill : "§80") )
                .addLore("§7Наполненность статистики: "+ (statFill>0 ? "§a+"+statFill : "§80") )
                .addLore("§7Группы: "+ (groupCounter>0 ? "§a+"+groupCounter : "§80") )
                .addLore("§7Друзья: "+ (friendCounter>0 ? "§a+"+friendCounter : "§80") )
                .addLore("§7Репорты: "+ (reportCounter>0 ? "§c-"+reportCounter : "§80") )
                .addLore("'§fДоверие§7': "+ (p.hasPermission("ostrov.trust") ? "§a+200" : "§5нет"))
                .addLore("§7")
                .addLore("§7От репутаци зависят Ваши")
                .addLore("§7возможности на сервере.")
                .build()
            //, e-> {
            //        op.getPlayer().sendMessage("ppp");
            //    } 
            )
        );
                
        final int karma_base = op.getDataInt(Data.KARMA);
        
        content.set(2, 4, ClickableItem.empty (new ItemBuilder(Material.GLOW_BERRIES)
                .name("§bКарма")
                .addLore("§7")
                .addLore("§bКарма - насколько Вы успешны.")
                .addLore("")
                .addLore("§fСейчас Ваша карма : "+ op.getKarmaDisplay()   )
                .addLore("§7")
                .addLore("§7Расчёт кармы:")
                .addLore("§6Базовое значение: "+  (karma_base==0 ? "§7нет" : (karma_base>0 ? "§a"+karma_base:"§c"+karma_base)))
                .addLore("§2Победы: §a+"+ op.getKarmaModifier(Stat.KarmaChange.ADD))
                .addLore("§4Поражения: §c-"+ op.getKarmaModifier(Stat.KarmaChange.SUB))
                .addLore("")
                .addLore("§7Карма поможет понять,")
                .addLore("§7стоит ли иметь дело с игроком")
                .addLore("§7(сражаться, принимать в команду и т.д.)")
                .build()
            //, e-> {
            //        op.getPlayer().sendMessage("ppp");
            //    } 
            )
        );
                
                
        content.set(2 ,6, ClickableItem.of(new ItemBuilder(Material.LIME_DYE)
            .name("§7Проверить права")
            .addLore("")
            .addLore("§7Показать права (пермишены)")
            .addLore("§7загруженные для")
            .addLore("§7этого сервера")
            .addLore("")
            .addLore("§7Найдено записей: §6"+op.user_perms.size())
            .addLore("")
            .addLore("§7ЛКМ - подробно")
            .addLore("")
            .build(), e-> {
                pm.openPerms(p, 0);
            }));

        

        


        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        

        
        
        
        content.set(3, 1, ClickableItem.of (new ItemBuilder(Material.BOOKSHELF)
                .name("§7Журнал")
                .addLore("")
                .addLore("§7ЛКМ - §fпросмотр в меню")
                .addLore("")
                .addLore("§7ПКМ - §fтекстовый просмотр")
                .addLore("§7(работает быстрее)")
                //.addLore("§7Просматрикать журнал")
                //.addLore("§7так же можно командой")
                //.addLore("§b/journal")
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
            .name("§6Репорты")
            .addLore("")
            .addLore("§7ЛКМ - §сВаши косяки.")
            .addLore("§7Обжалование возможно")
            .addLore("§7по заявке в группе.")
            .addLore("")
            .addLore("§7ПКМ - §eПросмотр свежих")
            .addLore("§7Покажет все репорты,")
            .addLore("поданные на коо-либо.")
            .addLore("")
            .addLore("§eПодать Жалобу")
            .addLore("§fможно командой")
            .addLore("§e/report ник жалоба")
            .build(), e-> {
                if (e.isLeftClick()) {
                    ReportCmd.openPlayerReports(p, op, p.getName(), 0);
                } else if (e.isRightClick()) {
                    ReportCmd.openAllReports(p, op, 0);
                }
            }));

    
        
        
        
        
        
        
        content.set(3, 5, ClickableItem.of (new ItemBuilder(Material.WITHER_SKELETON_SKULL)
                .name("§7Игнор - лист")
                .addLore("")
                .addLore("§7Вы можете добавить")
                .addLore("§7надоедливого игрока")
                .addLore("§7в Чёрный список")
                .addLore("§7командой §b/ignore add ник")
                .addLore("§7Удалить из ЧС - команда")
                .addLore("§b/ignore del ник")
                .addLore("§7или в меню на этой кнопке.")
                .addLore("")
                .addLore(op.getBlackListed().isEmpty() ? "§8Список пуст" : "§7ЛКМ - §fредактировать")
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
            .name("§7Учётные данные")
            .addLore("")
            .addLore("§7ЛКМ - найти другие")
            .addLore("§7аккаунты для вашего IP,")
            .addLore("§7уточнить сколько еще")
            .addLore("§7можно создать - ")
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
