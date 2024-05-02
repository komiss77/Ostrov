package ru.komiss77.modules.player.profile;


import java.util.EnumSet;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import ru.komiss77.enums.Settings;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;





public class PartySettings implements InventoryProvider {
    
    
    
    private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Section.ДРУЗЬЯ.glassMat).name("§8.").build());
    
    
    private static final EnumSet<Settings> partySettings = EnumSet.of(Settings.Party_LeaderTrackDeny, Settings.Party_SlaveDeny, Settings.Party_InviteFriendsDeny, Settings.Party_InviteOtherDeny);

     
    @Override
    public void onClose(final Player p, final InventoryContent content) {
        PM.getOplayer(p).menu.current = null;
    }

    
    @Override
    public void init(final Player p, final InventoryContent content) {
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
        //content.fillRect(0,0,  5,8, ClickableItem.empty(fill));
        
        final Oplayer op = PM.getOplayer(p);
        
        
        //линия - разделитель
        content.fillRow(4, fill);
        
        //выставить иконки внизу
        for (Section section:Section.values()) {
            content.set(section.slot, Section.getMenuItem(section, op));
        }

        
        

        
        
                     
                
                
            for (final Settings set : partySettings) {
                
                //if (set.tag>18) break;
                //if (set.tag<16) continue;
                final boolean locked = op.hasSettings(set);
                content.set(set.menuSlot, ClickableItem.of(new ItemBuilder( locked ? Material.RED_CONCRETE : Material.GREEN_CONCRETE)
                            .name(set.displayName)
                            .addLore(set.description)
                            .addLore("")
                            .addLore("§7Сейчас: "+ (locked ? "§4Нет" : "§2Да" ))
                            .addLore("")
                            .addLore("§7ЛКМ - менять")
                            .addLore("")
                            .build(), e-> {
                                if (e.isLeftClick()) {
                                    op.setSettings(set, !locked);
                                    reopen(p, content);
                                } else {
                                    PM.soundDeny(p);
                                }
                            }
                    ) 
                );                
                
                 
             }   
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                



        

    }


    
    
    
    
    
    
    
    
    
}
