package ru.komiss77.modules.player.mission;


import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.OstrovDB;
import ru.komiss77.Timer;
import ru.komiss77.enums.Data;
import ru.komiss77.enums.Game;
import ru.komiss77.enums.GlobalLogType;
import ru.komiss77.enums.Stat;
import ru.komiss77.modules.games.GM;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InputButton;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;





public class MissionWithdrawCreateMenu implements InventoryProvider {
    
    private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Material.SCULK_VEIN).name("§8.").build());

    
    @Override
    public void init(final Player p, final InventoryContent content) {
        
        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_AMETHYST_CLUSTER_BREAK, 5, 5);
        content.fillRect(0,0, 4,8, fill);
        
        final Oplayer op = PM.getOplayer(p);

        final int ril = op.getDataInt(Data.RIL);
        
        
        boolean can = true;
        final int min = MissionManager.getMin(op);
        
        if (ril < min || ril > MissionManager.WITHDRAW_MAX) {
            
            content.set(2, 2, ClickableItem.empty(new ItemBuilder(Material.GRAY_DYE)
                .name("§5Вывод средств возможен от §b"+min+" до "+MissionManager.WITHDRAW_MAX+"рил")
                    .addLore("")
                    .addLore("§7Расчёт мин. суммы такой:")
                    .addLore("§7Колл-во предыдущих выводов * 5")
                    .addLore("§7Минимальняа сумма : §6"+min)
                    .addLore("§7Максимальная сумма")
                    .addLore("§7для одного вывода : §6"+MissionManager.WITHDRAW_MAX+"рил")
                .build()
            ));
            can = false;

        } else {
            
            content.set(2, 2, ClickableItem.empty(new ItemBuilder(Material.LIME_DYE)
                .name("§aРил для вывода достаточно")
                    .addLore("")
                    .addLore("§fУ Вас §e"+ril+" §fрил.")
                    .addLore("")
                .build()
            ));

        }

        
        if (GM.GAME!=Game.LOBBY ) {
            
            content.set(2, 3, ClickableItem.of(new ItemBuilder(Material.GRAY_DYE)
                .name("§4✕ §6Надо Находиться в лобби")
                    .addLore("")
                    .addLore("§7Перейдите в лобби")
                    .addLore("§7командной /hub")
                    .addLore("")
                    .addLore("§7ЛКМ - перейти")
                .build(), e-> {
                    p.performCommand("server lobby");
                }
            ));
            can = false;

        } else {
            
            content.set(2, 3, ClickableItem.empty(new ItemBuilder(Material.LIME_DYE)
                .name("§a✔ §8Находиться в лобби")
                .build()
            ));

        }

        
        if (op.getDataString(Data.PHONE).isEmpty()) {
            
            content.set(2, 4, ClickableItem.of(new ItemBuilder(Material.GRAY_DYE)
                .name("§4✕ §6Не указан номер телефона в профиле")
                    .addLore("")
                    .addLore("§7Оттедактируйте профиль")
                    .addLore("§7командной /passport edit")
                    .addLore("")
                    .addLore("§7ЛКМ - редактировать")
                .build(), e-> {
                    p.performCommand("passport edit");
                }
            ));
            can = false;

        } else {
            
            content.set(2, 4, ClickableItem.empty(new ItemBuilder(Material.LIME_DYE)
                .name("§a✔ §8Номер телефона указан")
                    .addLore("")
                    .addLore("§b"+op.getDataString(Data.PHONE))
                    .addLore("")
                .build()
            ));

        }

       if (op.getDataString(Data.NOTES).isEmpty()) {
            
            content.set(2, 5, ClickableItem.of(new ItemBuilder(Material.GRAY_DYE)
                .name("§4✕ §6Не указано куда переводить в примечаниях")
                    .addLore("")
                    .addLore("§7Оттедактируйте 'примечания'")
                    .addLore("§7в профиле")
                    .addLore("§7командной /passport edit")
                    .addLore("")
                    .addLore("§7ЛКМ - редактировать")
                .build(), e-> {
                    p.performCommand("passport edit");
                }
            ));
            can = false;

        } else {
            
            content.set(2, 5, ClickableItem.empty(new ItemBuilder(Material.LIME_DYE)
                .name("§a✔ §8Направление вывода указано")
                    .addLore("")
                    .addLore("§b"+op.getDataString(Data.NOTES))
                    .addLore("")
                .build()
            ));

        }

        
        if (!can) {
            content.set(2, 7,ClickableItem.empty(new ItemBuilder(Material.REDSTONE)
                .name("§сВывод невозможен.")
                .addLore("§5<<<<<<<<<<<<")
                .addLore("§7Для заказа вывода средств")
                .addLore("§7должны быть выполнены все условия!")
                .addLore("§5<<<<<<<<<<<<")
                .build()
            ));
            
        } else {
            
            
            final String ammountInfo = MissionManager.getMin(op)+" до "+ril;
            final ItemStack is = new ItemBuilder( Material.RAW_GOLD)
                .name("§e"+ril)
                .addLore("")
                .addLore("§fЛКМ - §6указать сумму")
                .addLore("§fи вывести.")
                .addLore("")
                .addLore("§7Сумма будет снята с вашего")
                .addLore("§7баланса РИЛ, и отправлена на")
                .addLore("§7реквизиты, указанные в профиле.")
                .addLore("§7Вывод осуществляется в течении")
                .addLore("§33 дней§7, за процессом можно следить")
                .addLore("§7в меню статуса заявок.")
                .addLore("")
                .addLore("§eОбратите внимание на технически" )
                .addLore("§eдоступные способы перевода:" )
                .addLore("§f- §нКиви")
                .addLore("§f- §нЯндекс Кошелёк")
                .addLore("§f- §нномер телефона на +7 (Россия)")
                .addLore("§f- §нномер банковской карты")
                .addLore("")
                .build();

            content.set(2, 7, new InputButton(InputButton.InputType.ANVILL, is,  ammountInfo, msg -> {
                        p.closeInventory();
                        if (!ApiOstrov.isInteger(msg)) {
                                p.sendMessage("§cДолжно быть число!");
                                PM.soundDeny(p);
                                return;
                            }
                            final int amount = Integer.parseInt(msg);
                            if (amount < min || amount > MissionManager.WITHDRAW_MAX) {
                                p.sendMessage("§cСумма для вывод от "+min+" до "+MissionManager.WITHDRAW_MAX+" рил!");
                                PM.soundDeny(p);
                                reopen(p, content);
                                return;
                            }
                            
                            
                            final int current = op.getDataInt(Data.RIL);
                            if (current<ril) {
                                p.sendMessage("§cНа счету нет "+amount+" рил!");
                                return;
                            }
                            
                            op.setData(Data.RIL, current-ril);
                            OstrovDB.executePstAsync(p, 
                                    "INSERT INTO `withdraw` (name,summ,time,passPhone,passNote) VALUES ('"+op.nik+"', '"+ril+"', '"+Timer.getTime()+"', '"+op.getDataString(Data.PHONE)+"', '"+op.getDataString(Data.NOTES)+"'); "
                            );
                            Ostrov.globalLog(GlobalLogType.WITHDRAW_RIL, op.nik, "заявка на вывод "+ril+". Было "+current+" стало "+op.getDataInt(Data.RIL));
                            p.sendMessage("§aЗаявка на вывод §b"+ril+" рил §aзарегистрирована.");
                            p.playSound(p.getLocation(), Sound.BLOCK_SMITHING_TABLE_USE, 1, 1);
                            ApiOstrov.addStat(p, Stat.WD_c);
                            ApiOstrov.addStat(p, Stat.WD_a, ril);
                            
                            
                    }));
            
        }
        



        

    }


    
    
    
    
    
    
    
    
    
}
