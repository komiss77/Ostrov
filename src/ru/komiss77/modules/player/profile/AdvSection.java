package ru.komiss77.modules.player.profile;

import java.util.ArrayList;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.modules.player.PM;
import ru.komiss77.enums.Stat;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.translate.Lang;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.Pagination;
import ru.komiss77.utils.inventory.SlotIterator;
import ru.komiss77.utils.inventory.SlotPos;

public class AdvSection implements InventoryProvider {
    
   private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Section.ДОСТИЖЕНИЯ.glassMat).name("§8.").build());

     
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
        content.fillRow(4, fill);
        
        //выставить иконки внизу
        for (Section section:Section.values()) {
            content.set(section.slot, Section.getMenuItem(section, op));
        }

        
        
        
        final Pagination pagination = content.pagination();
        final ArrayList<ClickableItem> menuEntry = new ArrayList<>();        
        

        
        Material mat;
        int level;
        
        for (Stat st : Stat.values()) {
            if (st.achiv==null) continue;
            
            level = StatManager.getLevel(st, op.getStat(st));
            mat = switch (level) {
                case 5 -> Material.DIAMOND_HELMET;
                case 4 -> Material.GOLDEN_HELMET;
                case 3 -> Material.IRON_HELMET;
                case 2 -> Material.CHAINMAIL_HELMET;
                case 1 -> Material.TURTLE_HELMET;
                default -> Material.LEATHER_HELMET;
            };
            
            final ItemStack adv_item = new ItemBuilder(mat)
                .name(Lang.t(p, st.game.displayName)+" : "+Lang.t(p, st.desc))
                .addFlags(ItemFlag.HIDE_ATTRIBUTES)
                .addLore("")
                .addLore(level>=5 ? "§6✪ §e"+StatManager.topAdv(st)+" §6✪" : "")
                .addLore(Lang.t(p, "§fНакоплено : §6")+op.getStat(st))
                .addLore(level==0 ? Lang.t(p, "§5Пока нечем гордиться") :    level >=5 ? Lang.t(p, "§8Предел достижения") : Lang.t(p, "§fУровень достижения : §b")+level )
                .addLore(level>=5 ? "" : Lang.t(p, "До след. уровня: §f")+StatManager.getLeftToNextLevel(st, op.getStat(st)))
                .addLore("")
                .addLore(Lang.t(p, "§7Опыта за каждый уровень: §e")+st.exp_per_point)
                .build();
            
            
            menuEntry.add(ClickableItem.empty(adv_item));
            
        }                
                
                









        
        
        
        
        
        
        
        
              
            
        
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




        

        /*
        
        content.set( 5, 8, ClickableItem.of( new ItemBuilder(Material.OAK_DOOR).name("Закрыть").build(), e -> 
        {
            p.closeInventory();
        }
        ));
        */


        

    }


    

    
    
    
    
    
}
