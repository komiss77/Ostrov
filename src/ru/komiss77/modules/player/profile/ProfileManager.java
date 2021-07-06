package ru.komiss77.modules.player.profile;

import org.bukkit.entity.Player;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Timer;
import ru.komiss77.enums.Stat;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.inventory.InventoryManager;
import ru.komiss77.utils.inventory.SmartInventory;

//глобальные настроки - ред.данные
//группы
//журнал
//Администрация
//подать жалобу

//варпы, наборы, дома
//локальные настройки

public class ProfileManager {
    private static InventoryManager im = InventoryManager.get();
    public Oplayer op;
    public Section section = Section.ПРОФИЛЬ;
    
    //иконки, меняющие локе каждую секунду
    //public ClickableItem time;
    //public ClickableItem stat;
    
    
    
    
    public ProfileManager(final Oplayer op) {
        this.op = op;
    }

    
    
    public void openLastSection(final Player p) {
        open(p, section);
    }
    
    



    
    
    public void open(final Player p, final Section section) {
        
        this.section = section;
        
        switch (section) {
                
            
            case ПРОФИЛЬ:
          /*      time = ClickableItem.empty(new ItemBuilder(Material.CLOCK)
                    .name("§7Игровое время")
                    .lore("") //0
                    .lore("§7Общее игровое время с момента регистрации,") //1
                    .lore("§7и ежедневный прирост.") //2
                    .lore("") //3
                    .lore( Stat.PLAY_TIME.desc+ApiOstrov.secondToTime(op.getStat(Stat.PLAY_TIME))) //4
                    .lore("") //5 пустая сторка, обновляется каждую секунду
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
                );*/
                
                SmartInventory
                    .builder()
                    .id(op.nik+section.name())
                    .provider(new ProfileSection())
                    .size(6, 9)
                    .title("Профиль")
                    .build()
                    .open(p);
                
                break;
                
                
            case СТАТИСТИКА:
                SmartInventory
                    .builder()
                    .id(op.nik+section.name())
                    .provider(new StatSection())
                    .size(6, 9)
                    .title("Статистика")
                    .build()
                    .open(p);
                
                break;
                
            case ДОСТИЖЕНИЯ:
                SmartInventory
                    .builder()
                    .id(op.nik+section.name())
                    .provider(new AdvSection())
                    .size(6, 9)
                    .title("Достижения")
                    .build()
                    .open(p);
                
                break;
                
            }
    }
    
    
    
    
    
    
    
    
    
    
    
    public void tick(final Player p) {
//System.out.println("tick hasContent?"+im.hasContent(p));
        if (PM.im.hasContent(p)) {
            
            //поставить игровое время на иконке профиля
            setLine( p, Section.ПРОФИЛЬ.slot, 1, Stat.PLAY_TIME.desc+ApiOstrov.secondToTime(op.getStat(Stat.PLAY_TIME)) );
            
            //поставить время до сброса дневной статы на иконке статы
            setLine( p, Section.СТАТИСТИКА.slot, 3, "§3"+ApiOstrov.secondToTime(Timer.leftBeforeResetDayly()) );
            //setLine( p, Section.СТАТИСТИКА.slot, 4, "§3"+op.getOnlineSec() );
            
            switch (section) {
                
                case ПРОФИЛЬ:
                    setLine( p, 10, 4, Stat.PLAY_TIME.desc+ApiOstrov.secondToTime(op.getStat(Stat.PLAY_TIME)) );
                    setLine( p, 10, 5, "§fНаиграно за сегодня : §e"+ApiOstrov.secondToTime(op.getDaylyStat(Stat.PLAY_TIME)) );
                    break;
                
                case СТАТИСТИКА:
                    break;
                
            }
            
        }
    }

    
    
    
    
    private void setLine(final Player p, final int slot, final int line, final String value) {
        im.getContents(p).get().getInventory().setItem(slot, ItemUtils.setLoreLine(im.getContents(p).get().getInventory().getItem(slot), line, value));  //set(Section.СТАТИСТИКА.slot, im.getContents(p).get().g);
    }

    
    
    
}
