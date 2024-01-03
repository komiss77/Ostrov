package ru.komiss77.modules.player.profile;


import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.Perm;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InputButton;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;




public class HomeMenu implements InventoryProvider {
    
    
    
    private final Oplayer op;
    private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Section.ВОЗМОЖНОСТИ.glassMat).name("§8.").build());

    
    public HomeMenu(final Oplayer op) {
        this.op = op;
    }
    
    
    
    @Override
    public void init(final Player p, final InventoryContent content) {
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
        
        //final Oplayer op = PM.getOplayer(p);
        //final ProfileManager pm = op.menu;
        
        //линия - разделитель
        content.fillRow(4, fill);
        
        //выставить иконки внизу
        for (Section section:Section.values()) {
            content.set(section.slot, Section.getMenuItem(section, op));
        }

        
        
        
        
        for (final String homeName : op.homes.keySet()) {
            
            final ItemStack homeIcon = new ItemBuilder(Material.GRAY_BED)
                .name(homeName)
                .addLore("")
                .addLore("§7ЛКМ - §aперейти")
                .addLore("§7Шифт+ПКМ - §6переустановить")
                .addLore("§7Клав.Q - §cУдалить дом")
                .addLore("")
                .build();
            
            content.add(ClickableItem.of(homeIcon, e-> {
                switch (e.getClick()) {
                    case LEFT:
                        p.closeInventory();
                        p.performCommand("home "+homeName);
                        break;
                    case SHIFT_RIGHT:
                        p.closeInventory();
                        p.performCommand("sethome "+homeName);
                        break;
                    case DROP:
                        p.performCommand("delhome "+homeName);
                        reopen(p, content);
                        break; 
                    default:
                        break;
                }
                }
            ));
        }



        final int limit = Perm.getLimit(op, "home");
        
        if ( op.homes.size()>=limit ) {
            content.add(ClickableItem.empty(new ItemBuilder(Material.REDSTONE)
                .name("§7Добавить дом")
                .addLore("")
                .addLore("")
                .addLore("§cВы не можете добавть" )
                .addLore("§cновые дома.")
                .build()
            ));
        } else {
            content.add( new InputButton(InputButton.InputType.ANVILL,  new ItemBuilder(Material.EMERALD)
                .name("§7Добавить дом")
                .build(), "название", newName -> {
                    
                    p.closeInventory();
                    p.performCommand("sethome "+newName);
                    reopen(p, content);
                    
            }));
        }
        
        

    }
    
    
    
    
    
    
    @Override
    public void onClose(final Player p, final InventoryContent content) {
        PM.getOplayer(p).menu.current = null;
    }

    
    
    
}
