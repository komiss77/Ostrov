package builder.menu;

import builder.SetupMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.Managers.PM;
import ru.komiss77.Managers.WE;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.ConfirmationGUI;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;










public class SchemEditor implements InventoryProvider{

    public static final ItemStack fill = new ItemBuilder(Material.GREEN_STAINED_GLASS_PANE).build();;
    
   // private final String schemName;
    
    
    public SchemEditor() {
        //this.schemName = schemName;
    }
        
    
    
    @Override
    public void init(final Player p, final InventoryContent contents) {
        
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
        
       // contents.fillRect(SlotPos.of(0), SlotPos.of(53), ClickableItem.empty(fill));
        //contents.fillRow(4, ClickableItem.empty(fill));

        final SetupMode sm = PM.getOplayer(p).setup;
        
        //SetupManager.setPosition(p, style.getPos1(p.getWorld().getName()), style.getPos2(p.getWorld().getName()));

        if (sm.getCuboid()==null) {
            contents.set(0, 4, ClickableItem.empty(new ItemBuilder(Material.PAPER)
                .name("§7Схематик §f"+sm.schemName)
                .lore("")
                .lore("§7Создайте кубоид точками диагоналей.")

                .lore("")
                .build()));
        } else {
            contents.set(0, 4, ClickableItem.empty(new ItemBuilder(Material.PAPER)
                .name("§7Схематик §f"+sm.schemName)
                .lore("")
                .lore("§7Размер: §b"+sm.getCuboid().getSizeX()+"§7x§b"+sm.getCuboid().getSizeY()+"§7x§b"+sm.getCuboid().getSizeZ())
                .lore("§7Объём: §e"+sm.getCuboid().getSize())
                .lore("")
                .build()));
        }
        


    final boolean selected = sm.pos1!=null && sm.pos2!=null
                && sm.pos1.getWorld().getName().equals(sm.pos2.getWorld().getName())
                && p.getWorld().getName().equals(sm.pos1.getWorld().getName());
        
        
        
    if (selected) contents.fillRect(1,1, 4,4, ClickableItem.empty(fill));
        
        
        
        

       

        if (sm.pos2==null) {
             contents.set(1, 4, ClickableItem.of( new ItemBuilder(Material.BARRIER)
                .name("§7верхняя точка кубоида.")
                .lore("§7")
                .lore("§7Клик - установить.")
                .build(), e -> {
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 5);
                    sm.pos2=p.getLocation();
                    sm.checkPosition(p);
                    //p.sendBlockChange(p.getLocation(), Material.EMERALD_BLOCK.createBlockData());
                    reopen(p, contents);
                }));
        } else {
            //p.sendBlockChange(sm.pos2, Material.EMERALD_BLOCK.createBlockData());
            contents.set(1, 4, ClickableItem.of( new ItemBuilder(Material.OAK_FENCE)
                .name("§7верхняя точка кубоида.")
                .lore("§7")
                .lore("§7ЛКМ-тп")
                .lore("§7ПКМ-установить")
                .build(), e -> {
                    if (e.isLeftClick()) {
                        p.teleport(sm.pos2);
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 1, 5);
                    } else if (e.isRightClick()) {
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 5);
                        //p.sendBlockChange(sm.pos2, Material.AIR.createBlockData());
                        sm.pos2=p.getLocation();
                        sm.checkPosition(p);
                        //p.sendBlockChange(sm.pos2, Material.EMERALD_BLOCK.createBlockData());
                        reopen(p, contents);
                    }
                }));
        }




        
        

        

        
        
        if (sm.pos1==null) {
             contents.set(4, 1, ClickableItem.of( new ItemBuilder(Material.BARRIER)
                .name("§7нижняя точка кубоида.")
                .lore("§7")
                .lore("§7Клик - установить.")
                .build(), e -> {
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 5);
                    sm.pos1=p.getLocation();
                    sm.checkPosition(p);
                    //p.sendBlockChange(p.getLocation(), Material.EMERALD_BLOCK.createBlockData());
                    reopen(p, contents);
                }));
        } else {
            //p.sendBlockChange(style.getPos1(p.getWorld().getName()), Material.EMERALD_BLOCK.createBlockData());
            contents.set(4, 1, ClickableItem.of( new ItemBuilder(Material.OAK_FENCE)
                .name("§7нижняя точка кубоида.")
                .lore("§7")
                .lore("§7ЛКМ-тп")
                .lore("§7ПКМ-установить")
                .build(), e -> {
                    if (e.isLeftClick()) {
                        p.teleport(sm.pos1);
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 1, 5);
                    } else if (e.isRightClick()) {
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 5);
                        //p.sendBlockChange(sm.pos1, Material.AIR.createBlockData());
                        sm.pos1=p.getLocation();
                        sm.checkPosition(p);
                        //p.sendBlockChange(sm.pos1, Material.EMERALD_BLOCK.createBlockData());
                        reopen(p, contents);
                    }
                }));
        }

        
        
        
        
        
        
       
     
        

        
        
        
        
        
        
        
        
     
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        


/*
        contents.set(5, 1, ClickableItem.of( new ItemBuilder(Material.WOODEN_AXE)
            .name("§7Вставить")
            .lore("§6Вставить заготовку и наполнить сундуки")
            .build(), e -> {

                p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1, 5);
                ConfirmationGUI.open( p, "§4Вставить ?", result -> {
                    if (result) {
                        style.build(p.getWorld().getName());
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 5);
                    } else {
                        reopen(p, contents);
                        p.playSound(p.getLocation(), Sound.ENTITY_LEASH_KNOT_PLACE, 0.5f, 0.85f);
                    }
                });
                //reopen(p, contents);
            }));
        */
        
        
        if (selected) {
            
            contents.set(5, 2, ClickableItem.of( new ItemBuilder(Material.STONECUTTER)
                .name("§7Очистить выделение")
                .lore("§6Стирает всё в выделенной области")
                .build(), e -> {

                    p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1, 5);
                    ConfirmationGUI.open( p, "§4Стереть ?", result -> {
                        if (result) {
                            sm.reset();
                            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 5);
                        } else {
                            reopen(p, contents);
                            p.playSound(p.getLocation(), Sound.ENTITY_LEASH_KNOT_PLACE, 0.5f, 0.85f);
                        }
                    });
                    //reopen(p, contents);
                }));
            
            
            contents.set(5, 6, ClickableItem.of( new ItemBuilder(Material.JUKEBOX)
                .name("§2Сохранить")
                .build(), e -> {
                    
                    if (sm.getCuboid().getSize()>1_000_000 || sm.getCuboid().getSizeX()>100 || sm.getCuboid().getSizeY()>100 || sm.getCuboid().getSizeZ()>100) {
                        p.sendMessage("§cВыделение слишком большое! Объём не более 1млн блоков, каждая сторона не более 100!");
                        PM.soundDeny(p);
                        return;
                    }
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 5);
                    WE.save(p, sm.pos1, sm.pos2, sm.schemName);
                   // p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1, 5);
                   /* ConfirmationGUI.open( p, "§4Стереть ?", result -> {
                        if (result) {
                            sm.reset();
                            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 5);
                        } else {
                            reopen(p, contents);
                            p.playSound(p.getLocation(), Sound.ENTITY_LEASH_KNOT_PLACE, 0.5f, 0.85f);
                        }
                    });*/
                    //reopen(p, contents);
                }));
            
            
            
            
            }
        
        
        
        
        contents.set( 5, 4, ClickableItem.of( new ItemBuilder(Material.OAK_DOOR).name("назад").build(), e 
                -> sm.openSchemMainMenu(p)
        ));


        
       

        


    
    
    
    }
    

        


    


    
    
    
    
    
    
    
    
}