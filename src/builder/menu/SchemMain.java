package builder.menu;

import builder.PasteJob;
import builder.Schematic;
import builder.SetupMode;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Commands.Builder;
import ru.komiss77.Managers.Cuboid;
import ru.komiss77.Managers.PM;
import ru.komiss77.Managers.WE;
import ru.komiss77.Ostrov;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.LocationUtil;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InputButton;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.Pagination;
import ru.komiss77.utils.inventory.SlotIterator;
import ru.komiss77.utils.inventory.SlotPos;



public class SchemMain implements InventoryProvider {

    
    public SchemMain() {
    }

    
    
    
    
    
    
    @Override
    public void init(final Player p, final InventoryContent contents) {
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
        contents.fillRow(4, ClickableItem.empty(Builder.fill));
        
        final SetupMode sm = PM.getOplayer(p).setup;
        
        
        final Pagination pagination = contents.pagination();
        
        
        
        final ArrayList<ClickableItem> menuEntry = new ArrayList<>();
        
        final FileFilter schemFilter  = (final File file) -> file.isFile() && file.getName().endsWith(".schem");   
        
        final File schemFolder = new File (Ostrov.instance.getDataFolder() + "/schematics");
        
        
        if (schemFolder.exists() && schemFolder.isDirectory()) {
            for (final File schemFile : schemFolder.listFiles(schemFilter)) {
                
                final String schemName = schemFile.getName().replaceFirst(".schem", "");
                        
                menuEntry.add(ClickableItem.of(new ItemBuilder( Material.BOOKSHELF )
                    .name(schemName)
                    .lore("§7Размер: "+(schemFile.length()<1000 ? schemFile.length()+" байт" : schemFile.length()/1000+"кб"))
                    .lore("")
                    .lore("§7ЛКМ - §fзагрузить, вставить и выделить")
                    .lore("§5Убедитесь, что рядом нет")
                    .lore("§5ценных построек,")
                    .lore("§5действие не отменить.")
                    .lore("")
                    .lore("клав.Q - §cудалить")
                    .lore("")
                    .build(), e -> {
                        
                
                if (e.isLeftClick()) {
                    p.closeInventory();
                    final Schematic schem = WE.getSchematic(p, schemName);
                    if (schem!=null) {
                        schem.paste(p, p.getLocation(), true);
                        //final Schematic schem = new Schematic(p, schemFile, false);
    //System.out.println("Schematic size="+schem.sizeX+" "+schem.sizeY+" "+schem.sizeZ);
                        //WE.paste(p, p.getLocation(), schem, true);
                        sm.setCuboid(p, new Cuboid (  p.getLocation(), schem.sizeX, schem.sizeY, schem.sizeZ ));
                        sm.openSchemEditMenu(p, schem.getName());
                        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, .5f, 2);
                        return;
                    }
                    
                }  else if (e.getClick()==ClickType.DROP) {
                    schemFile.delete();
                    p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1, 5);
                    reopen(p, contents);
                    return;
                } 
                PM.soundDeny(p);
            }));
            }
        }
        
        
        
        
        
        
        /*

        for (final String isStyleName : IslandStyleManager.islansStyles.keySet()) {
            final IslandStyle isStyle = IslandStyleManager.islansStyles.get(isStyleName);
            
                
                final ItemStack icon = new ItemBuilder( isStyle.logo )
                    .name(isStyle.displayName)
                    .lore("")
                    .lore("§7ЛКМ - изменить")
                    //.lore( isStyleName.equals("обычный") ? "§eВстроенный, выключение невозможно." : (isStyle.enabled?"§2включен":"§4выключен"))
                    //.lore( isStyleName.equals("обычный")?"§eВстроенный, удаление невозможно.":"§cshift+ПКМ - удалить")
                    .lore("")
                    .lore(isStyle.changed ? "§cИзменения не сохранены на диск!" : "")
                    .lore("")
                    .build();
                
            
            menuEntry.add(ClickableItem.of(icon, e -> {
                p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1, 5);
                if (e.isLeftClick()) {
                    
                    sm.setPosition(p, isStyle.getPos1(p.getWorld().getName()), isStyle.getPos2(p.getWorld().getName()));
                    
                    SmartInventory.builder().id("StyleEdit"+isStyleName). provider(new StyleEditor(isStyle)). size(6, 9). title("§6Заготовка островка "+isStyleName).build().open(player);
                    
                }  else if (e.isShiftClick()) {
                    
                    if (!isStyleName.equals("обычный")) IslandStyleManager.islansStyles.remove(isStyleName);
                    reopen(p, contents);
                    
                } //else if (e.isRightClick()) {
                    
                  //  if (!isStyleName.equals("обычный")) isStyle.enabled = !isStyle.enabled;
                 //   reopen(player, contents);
                    
               // }

            }));            
        }
        
        
        
        
   
        
        contents.set(5, 2 , new InputButton( new ItemBuilder(Material.BOOK)
                .name("§fCоздать новый стиль")
                .build(), "стиль", newName -> {
                    
                    
                    if(newName.length()>16 || !ApiOstrov.checkString(newName,true,true) ) {
                        p.sendMessage("§cНедопустимое название!");
                        PM.soundDeny(p);
                    } else if ( IslandStyleManager.islansStyles.containsKey(newName) ) {
                        p.sendMessage("§cТакой стиль уже есть!");
                        PM.soundDeny(p);
                    } else {
                        final IslandStyle ist = new IslandStyle(newName);
                        ist.changed = true;
                        ist.enabled = false;
                        ist.author = p.getName();
                        IslandStyleManager.islansStyles.put(newName, ist);
                        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                        reopen(p, contents);
                    }
        }));
        */
        contents.set(5, 2 , new InputButton( InputButton.InputType.ANVILL, new ItemBuilder(Material.BOOK)
            .name("§fCоздать схематик")
            .build(), "название", newName -> {

                if(newName.isEmpty() || newName.length()>16 || !ApiOstrov.checkString(newName,true,true) ) {
                    p.sendMessage("§cНедопустимое название!");
                    PM.soundDeny(p);
                    return;
                } 
                final File schemFile = new File (Ostrov.instance.getDataFolder() + "/schematics", newName+".schem");
                if (schemFile.exists() && schemFile.isFile()) {
                    p.sendMessage("§cФайл с таким названием уже есть, будет перезаписан при сохранении!");
                    //PM.soundDeny(p);
                    //return;
                }
                sm.resetCuboid();
                sm.openSchemEditMenu(p, newName);
//Bukkit.broadcastMessage("создание "+schemFile.getAbsolutePath());
                //p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                //reopen(p, contents);
                    
        }));
        
        if (!sm.schemName.isEmpty()) {
            contents.set(5, 3, ClickableItem.of(new ItemBuilder(Material.FEATHER)
                .name("§7Продолжить редактирование")
                .lore("§7")
                .lore("§7ЛКМ - открыть редактор")
                .lore("§7")
                .build(), e -> {
                    if (e.isLeftClick()) {
                        sm.openSchemEditMenu(p, sm.schemName);
                    }
                }));
        }
             
        
        final PasteJob pj = WE.jobs.get(WE.currentTask);
        
        contents.set(5, 6, ClickableItem.of(new ItemBuilder(Material.COMMAND_BLOCK_MINECART)
                .name("§7Процессы вставки")
                .lore("§7")
                .lore("§7Создано процессов: §b"+WE.jobs.size())
                .lore("§7")
                .lore(pj==null ? "" : "§7Выполняется: §a"+pj.getSchemName())
                .lore(pj==null ? "" : "§7Локация: §f"+LocationUtil.StringFromLoc(pj.loc))
                .lore(pj==null ? "" : "§7Прогресс: §e"+pj.percent+"%")
                .lore("§7")
                .lore("§7ЛКМ - управление")
                .lore("§7")
                .build(), e -> {
                    if (e.isLeftClick()) {
                        p.sendMessage("пока не доделано");
//Bukkit.broadcastMessage("управление Процессы вставки");
                        reopen(p, contents);
                    }
            //return;
        }));        
        
        
        
        
        
        pagination.setItems(menuEntry.toArray(new ClickableItem[menuEntry.size()]));
        pagination.setItemsPerPage(36);
        

        
        
        contents.set( 5, 4, ClickableItem.of( new ItemBuilder(Material.OAK_DOOR).name("гл.меню").build(), e 
                -> sm.openMainSetupMenu(p)
        ));
        
        
        if (!pagination.isLast()) {
            contents.set(5, 8, ClickableItem.of(ItemUtils.nextPage, e 
                    -> contents.getHost().open(p, pagination.next().getPage()) )
            );
        }

        if (!pagination.isFirst()) {
            contents.set(5, 0, ClickableItem.of(ItemUtils.previosPage, e 
                    -> contents.getHost().open(p, pagination.previous().getPage()) )
            );
        }
        
        pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, SlotPos.of(0, 0)).allowOverride(false));
        

        
        

    }
    
    
        
}
