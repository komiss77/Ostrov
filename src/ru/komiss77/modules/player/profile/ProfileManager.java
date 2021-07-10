package ru.komiss77.modules.player.profile;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Timer;
import ru.komiss77.enums.Game;
import ru.komiss77.enums.Stat;
import ru.komiss77.modules.player.Oplayer;
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

//по эвенту обновлять лоре онлайн

public class ProfileManager {
    private static InventoryManager im = InventoryManager.get();
    public Oplayer op;
    public Section section = Section.РЕЖИМЫ;
    public Game game = null;  //для динамической обновы. null значит открыты большие, или арены игры
    
    protected Inventory current;
    //иконки, меняющие локе каждую секунду
    //public ClickableItem time;
    //public ClickableItem stat;
    protected int gamePage;
    
    
    
    
    public ProfileManager(final Oplayer op) {
        this.op = op;
    }

    
    
    public void openLastSection(final Player p) {
        open(p, section);
    }
    
    



    
    
    public void open(final Player p, final Section section) {
        
        this.section = section;
        
        switch (section) {
                
            
            case РЕЖИМЫ:
                current = SmartInventory
                    .builder()
                    .id(op.nik+section.name())
                    .provider(new GameSection())
                    .size(6, 9)
                    .title("Режимы")
                    .build()
                    .open(p);
                
                break;

            case ПРОФИЛЬ:
                current = SmartInventory
                    .builder()
                    .id(op.nik+section.name())
                    .provider(new ProfileSection())
                    .size(6, 9)
                    .title("Профиль")
                    .build()
                    .open(p);
                
                break;
                
                
            case СТАТИСТИКА:
                current = SmartInventory
                    .builder()
                    .id(op.nik+section.name())
                    .provider(new StatSection())
                    .size(6, 9)
                    .title("Статистика")
                    .build()
                    .open(p);
                
                break;
                
            case ДОСТИЖЕНИЯ:
                current = SmartInventory
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
       // if (current!=null) { //if (PM.im.hasContent(p)) {
        if (current==null) return; //нет открытого раздела - ничего не делаем
        
        //поставить игровое время на иконке профиля, если меньше недели
        if (op.getStat(Stat.PLAY_TIME)<604800) setLine( p, Section.ПРОФИЛЬ.slot, 1, Stat.PLAY_TIME.desc+ApiOstrov.secondToTime(op.getStat(Stat.PLAY_TIME)) );

        //поставить время до сброса дневной статы на иконке статы
        setLine( p, Section.СТАТИСТИКА.slot, 3, "§3"+ApiOstrov.secondToTime(Timer.leftBeforeResetDayly()) );
        //setLine( p, Section.СТАТИСТИКА.slot, 4, "§3"+op.getOnlineSec() );

        switch (section) {


            case РЕЖИМЫ:
                if (game==null) {
//System.out.println("тик меню игр");
                } else {
//System.out.println("тик арен "+game);
                }
                break;



            case ПРОФИЛЬ:
                if (op.getStat(Stat.PLAY_TIME)<604800) setLine( p, 10, 4, Stat.PLAY_TIME.desc+ApiOstrov.secondToTime(op.getStat(Stat.PLAY_TIME)) );
                setLine( p, 10, 5, "§fНаиграно за сегодня : §e"+ApiOstrov.secondToTime(op.getDaylyStat(Stat.PLAY_TIME)) );
                break;

            case СТАТИСТИКА:
                break;

        }
            
        //}
    }

    
    
    
    
    private void setLine(final Player p, final int slot, final int line, final String value) {
        if (current.getContents().length<=slot) return;

        //im.getContents(p).get().getInventory().setItem(slot, ItemUtils.setLoreLine(im.getContents(p).get().getInventory().getItem(slot), line, value));  //set(Section.СТАТИСТИКА.slot, im.getContents(p).get().g);
        current.setItem(slot, ItemUtils.setLoreLine(im.getContents(p).get().getInventory().getItem(slot), line, value));  //set(Section.СТАТИСТИКА.slot, im.getContents(p).get().g);
    }

    
    
    
}
