package ru.komiss77.modules.player.profile;


import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import ru.komiss77.ApiOstrov;
import ru.komiss77.enums.Data;
import ru.komiss77.modules.player.PM;
import ru.komiss77.enums.Stat;
import ru.komiss77.modules.Pandora;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;





public class ProfileSection implements InventoryProvider {
    
    
    
    private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Material.WHITE_STAINED_GLASS_PANE).name("§8.").build());
    

    
    public ProfileSection() {
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
        
        

        content.set( 1,1, ClickableItem.empty(new ItemBuilder(Material.CLOCK)
                .name("§7Игровое время")
                .lore("") //0
                .lore("§7Общее игровое время с момента регистрации,") //1
                .lore("§7и ежедневный прирост.") //2
                .lore("") //3
                .lore("") //4 игровое время, обновляется каждую секунду.lore( Stat.PLAY_TIME.desc+ApiOstrov.secondToTime(op.getStat(Stat.PLAY_TIME))) //4
                .lore("") //5 наиграно за сегодня, обновляется каждую секунду
                .lore("")
                .lore( Pandora.getInfo(op) )
                .lore("")
               // .lore( op.getDataInt(Data.REPORT_C)>0 ? Data.REPORT_C.desc+op.getDataInt(Data.REPORT_C) : "" )
              //  .lore( op.getDataInt(Data.REPORT_P)>0 ? Data.REPORT_P.desc+op.getDataInt(Data.REPORT_P) : "" )
               // .lore("")
                //.lore("§8флаги="+Integer.toBinaryString(op.getStat(Stat.FLAGS)))
                //.lore("§8(dayly="+Integer.toBinaryString(op.getDaylyStat(Stat.FLAGS))+")")
                .build()
           // , e-> {
            //        op.getPlayer().sendMessage("ppp");
            //    } 
            )
        );

        content.set(1, 3, ClickableItem.empty(new ItemBuilder(Material.EXPERIENCE_BOTTLE)
                .name("§7Уровень")
                .lore("")
                .lore("§7Уровень островитянина отображает")
                .lore("§7ваше мастерство. ")
                .lore("§7Чем выше уровень,")
                .lore("§7тем больше для Вас возможностей.")
                .lore("")
                .lore( Stat.LEVEL.desc+op.getStat(Stat.LEVEL) +"  "+ApiOstrov.getPercentBar(op.getStat(Stat.LEVEL)*25, op.getStat(Stat.EXP), true)  )
                .lore("§fПрирост уровня за сегодня : " + (op.getDaylyStat(Stat.LEVEL)>0 ? "§e+"+op.getDaylyStat(Stat.LEVEL) : "§7нет" ) )
                .lore( "§fОпыта до следующего уровня : §a§l"+(op.getStat(Stat.LEVEL)*25-op.getStat(Stat.EXP)+1) )
                //.lore( ApiOstrov.getPercentBar(op.getStat(Stat.LEVEL)*25, op.getStat(Stat.EXP), true) )
                .lore("")
                .build()
            //, e-> {
            //        op.getPlayer().sendMessage("ppp");
            //    } 
            )
        );
                
        content.set(1, 5, ClickableItem.empty (new ItemBuilder(Material.GOLD_INGOT)
                .name("§7Финансы")
                .lore("")
                .lore("§fВ Вашем распоряжении:")
                .lore("§f"+op.getDataInt(Data.LONI)+" §aЛони")
                .lore("§f"+op.getDataInt(Data.RIL)+" §eРил")
                .lore("")
                .lore("§aЛони §7- внутриигровая валюта")
                .lore("§7на проекте. Используется для")
                .lore("§7товарооборота, игровых действий и т.д.")
                .lore("")
                .lore("§eРил §7- счёт, приравненный к рублёвому.")
                .lore("§7За Рил можно купить привилегии,")
                .lore("§7или вывести на телефон или карту.")
                .lore("§7*(при соблюдении ряда условий)")
                .lore("§7Рил можно заработать, выполняя задания!")
                .lore("")
                .lore("§7ЛКМ - §fпополнить счёт §eРил")
                .lore("§7ПКМ - §fподробнее о выводе")
                .lore("")
                .build()
            //, e-> {
            //        op.getPlayer().sendMessage("ppp");
            //    } 
            )
        );
                
                
                









        
        
        
        
        
        
        
        
              
            
        
        





        

        
        
        content.set( 5, 8, ClickableItem.of( new ItemBuilder(Material.OAK_DOOR).name("Закрыть").build(), e -> 
        {
            p.closeInventory();
        }
        ));
        


        

    }


    
    
    
    
    
    
    
    
    
}
