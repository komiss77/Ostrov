package ru.komiss77.modules.player.profile;

import java.util.ArrayList;
import java.util.List;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.key.Key;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import ru.komiss77.boot.OStrap;
import ru.komiss77.enums.Game;
import ru.komiss77.enums.Stat;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.translate.Lang;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;

public class StatSection implements InventoryProvider {

//    private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Section.СТАТИСТИКА.glassMat).name("§8.").build());


    @Override
    public void onClose(final Player p, final InventoryContent content) {
        PM.getOplayer(p).menu.current = null;
    }


    @Override
    public void init(final Player p, final InventoryContent content) {
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
        //content.fillRect(0,0,  5,8, ClickableItem.empty(fill));

        final Oplayer op = PM.getOplayer(p);
        //final ProfileManager pm = op.menu;


        //линия - разделитель
//        content.fillRow(4, fill);

        //выставить иконки внизу
        for (Section section : Section.values()) {
          content.set(2, section.column, Section.getMenuItem(section, op));
        }


        //final Pagination pagination = content.pagination();
        //final ArrayList<ClickableItem> menuEntry = new ArrayList<>();        


        for (Game game : Game.values()) {
            final List<String> lore = new ArrayList<>();

            if (game == Game.GLOBAL) continue;

            for (Stat stat : Stat.values()) {
                if (stat.game == game) {
//System.out.println("- stat="+stat.toString()+" len="+op.getStat(stat).length()+" value="+op.getStat(stat));                   
                    lore.add(Lang.t(p, stat.desc) + op.getStat(stat) + (op.getDailyStat(stat) > 0 ? " §5(+" + op.getDailyStat(stat) + ")" : ""));
                }
            }


            final ItemStack stat_item = new ItemBuilder(OStrap.get(Key.key(game.mat.toLowerCase()), ItemType.GRAY_DYE))
                .name(Lang.t(p, game.displayName))
                .hide(DataComponentTypes.ATTRIBUTE_MODIFIERS)
                    .lore(lore)
                .build();

            content.set(game.statSlot, ClickableItem.empty(stat_item));

        }                
        

                
                
                









        
        
        
        
        
        
        
        
              
           /* 
        
        pagination.setItems(menuEntry.toArray(new ClickableItem[menuEntry.size()]));
        pagination.setItemsPerPage(36);    
        

        if (!pagination.isLast()) {
            content.set(4, 8, ClickableItem.of(ItemUtils.nextPage, e 
                    -> {
                content.getHost().open(p, pagination.next().getPage()) ;
            }
            ));
        }

        if (!pagination.isFirst()) {
            content.set(4, 0, ClickableItem.of(ItemUtils.previosPage, e 
                    -> {
                content.getHost().open(p, pagination.previous().getPage()) ;
               })
            );
        }
        
        pagination.addToIterator(content.newIterator(SlotIterator.Type.HORIZONTAL, SlotPos.of(0, 0)).allowOverride(false));
*/



        

        
        /*
        content.set( 5, 8, ClickableItem.of( new ItemBuilder(Material.OAK_DOOR).name("Закрыть").build(), e -> 
        {
            p.closeInventory();
        }
        ));
        */


    }


}
