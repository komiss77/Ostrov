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





public class ProfileWithdrawMenu implements InventoryProvider {
    
    
    
    private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Material.BROWN_STAINED_GLASS_PANE).name("§8.").build());
    //private final List<Integer>ids;
    private int ril;
    
    public ProfileWithdrawMenu(final int ril) {
        this.ril = ril;
    }
    

    @Override
    public void onClose(final Player p, final InventoryContent content) {
        PM.getOplayer(p).menu.current = null;
    }

    
    
    
    @Override
    public void init(final Player p, final InventoryContent content) {
        
        PM.getOplayer(p).menu.current = null; //или кидает ошибку
        
        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_AMETHYST_CLUSTER_BREAK, 5, 5);
        content.set(0,fill);
        content.set(1,fill);
        content.set(3,fill);
        content.set(4,fill);
//System.out.println("completed="+completed.toString());        
        
        final Oplayer op = PM.getOplayer(p);
        
            

        if (ril<MissionManager.getMin(op)) {
            
            content.set(2, ClickableItem.empty(new ItemBuilder(Material.REDSTONE)
                .name("§5Вывод средств возможен от §b"+MissionManager.getMin(op)+" рил")
                .build()
            ));
            return;

        } 

        if (GM.GAME!=Game.LOBBY ||op.getDataString(Data.PHONE).isEmpty() || op.getDataString(Data.NOTES).isEmpty()) {
            
            final boolean emptyNotes = op.getDataString(Data.NOTES).isEmpty();
            
            content.set(2, ClickableItem.empty(new ItemBuilder(Material.REDSTONE)
                .name("§5Вывод невозможен.")
                .addLore("§7")
                .addLore("§7Для заказа вывода средств")
                .addLore("§7должны быть выполнены условия:")
                .addLore(GM.GAME==Game.LOBBY ? "§a✔ §8Находиться в лобби" : "§4✕ §6Находиться в лобби")
                .addLore(op.getDataString(Data.PHONE).isEmpty() ? "§4✕ §6Номер телефона в профиле" :  "§a✔ §8Номер телефона в профиле" )
                .addLore(emptyNotes ? "§4✕ §6Способ вывода в примечаниях" : "§a✔ §8Способ вывода в примечаниях")
                .addLore("§7")
                .addLore(emptyNotes ? "§6Для описания способа вывода" : "§eОбратите внимание на технически" )
                .addLore(emptyNotes ? "§6в §3главном меню §6ПКМ на §3ПРОФИЛЬ §6-" : "§eдоступные способы перевода:" )
                .addLore(emptyNotes ? "§6откроется редактор §3Паспорта" : "§f- номер телефона на +7 (Россия)")
                .addLore(emptyNotes ? "§6Нажмите на иконку §3примечания" : "§f- номер банковской карты VISA,MasterCard,МИР")
                .addLore(emptyNotes ? "§6и опишите способ вывода" : "§f- Webmoney")
                .addLore(emptyNotes ? "§6(на телефон, карту или что-то ещё)" : "§f- Киви")
                .addLore(emptyNotes ? "" : "§f- Яндекс Кошелёк")
                .addLore("§7")
                .build()
            ));
            return;
            
        } else {
            final String ammountInfo = MissionManager.getMin(op)+" до "+ril;
            final ItemStack is = new ItemBuilder( Material.RAW_GOLD)
                .name("§e"+ril)
                .addLore("")
                .addLore("§7Вы можете изменить")
                .addLore("§7сумму для вывода по заявке.")
                .addLore("§7Когда готово, подтвердите")
                .addLore("§7зелёной кнопкой.")
                .addLore("§7Сумму будет снята с вашего")
                .addLore("§7баланса, и отправлена на реквизиты,")
                .addLore("§7указанные в профиле.")
                .addLore("")
                .addLore("§7ЛКМ - §6изменить сумму")
                .addLore("")
                .addLore("§7Вывод осуществляется в течении")
                .addLore("§33 дней§7, за процессом можно следить")
                .addLore("§7в §fЖурнале вывода.")
                .addLore("")
                .addLore("§eОбратите внимание на технически" )
                .addLore("§eдоступные способы перевода:" )
                .addLore("§f- номер телефона на +7 (Россия)")
                .addLore("§f- номер банковской карты VISA,MasterCard,МИР")
                .addLore("§f- Webmoney")
                .addLore("§f- Киви")
                .addLore("§f- Яндекс Кошелёк")
                .addLore("")
                .build();
            
            content.set(2, new InputButton(InputButton.InputType.ANVILL, is,  ammountInfo, msg -> {
                        if (!ApiOstrov.isInteger(msg)) {
                                p.sendMessage("§cДолжно быть число!");
                                PM.soundDeny(p);
                                return;
                            }
                            final int amount = Integer.valueOf(msg);
                            if (amount<MissionManager.getMin(op) || amount>MissionManager.WITHDRAW_MAX) {
                                p.sendMessage("§cот "+ammountInfo);
                                PM.soundDeny(p);
                                reopen(p, content);
                                return;
                            }
                            ril=amount;
                            reopen(p, content);
                    }));  

        }
        
 
        
               
        
        

        
        content.set(0, ClickableItem.of(new ItemBuilder( Material.RED_CANDLE)
            .name("§cОтмена")
            .build(), e-> {
                p.closeInventory();
            }
        ));
        
        content.set(4, ClickableItem.of(new ItemBuilder( Material.LIME_CANDLE)
            .name("§aПодтвердить")
            .build(), e-> {
                p.closeInventory();
                //final Oplayer op = PM.getOplayer(p);
                final int current = op.getDataInt(Data.RIL);
                if (current<ril) {
                    p.sendMessage("§cна счету нет "+ril);
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
            }
        ));
        
        
        


        

    }


    
    
    
    
    
    
    
    
    
}
