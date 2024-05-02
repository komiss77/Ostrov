package ru.komiss77.modules.figures;


import java.util.HashSet;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import ru.komiss77.objects.Figure;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.SmartInventory;





public class MenuMain implements InventoryProvider {
    
    
    

    
    
    @Override
    public void init(final Player p, final InventoryContent contents) {
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
        //contents.fillBorders(ClickableItem.empty(MenuMain.fill));
        
        
        

        //меню открывается, только если нет клана!
        
        
        contents.set(0, 0, ClickableItem.of( new ItemBuilder(Material.STICK)
            .name("§eВыдать палку")
            .addLore("")
            .addLore("§7Держа палку в руке:")
            .addLore("")
            .addLore("§7ПКМ на фигуру - ")
            .addLore("§7настройки фигуры.")
            .addLore("")
            .addLore("§7ПКМ на моба или стойку -")
            .addLore("§7откроет меню создания")
            .addLore("§7обработчика.")
            .addLore("")
            .build(), e -> {
                if( e.isLeftClick()) {
                    p.closeInventory();
                    p.getInventory().addItem(FigureManager.stick.clone());
                    p.sendMessage("§aВыдана палка, смотри подсказки в Lore палки.");
                    //p.sendMessage(new Component);
                }
            }));    




        contents.set(0, 2, ClickableItem.of( new ItemBuilder(Material.HOPPER)
            .name("§fНайти в радиусе 10")
            .addLore("")
            .addLore("")
            .build(), e -> {
                if( e.isLeftClick()) {
                    //p.sendMessage("Ищем фигуры в радиусе из 10 блоков...");
                    final Set<Figure> list = new HashSet<>();
                    for (final Figure figure : FigureManager.getFigures()) {
                        if ( figure.worldName.equals((p.getWorld().getName())) && getDistance (p,figure) <=10) {
                            list.add(figure);
                        }
                    }
                    SmartInventory.builder()
                        .id("FugureSelectMenu"+p.getName())
                        .provider(new MenuFinded(list))
                        .size(6, 9)
                        .title("§fФигуры в радиусе 10")
                        .build()
                        .open(p);
                }
            }));    

        contents.set(0, 3, ClickableItem.of( new ItemBuilder(Material.HOPPER)
            .name("§fНайти в радиусе 100")
            .addLore("")
            .addLore("")
            .build(), e -> {
                if( e.isLeftClick()) {
                    //p.sendMessage("Ищем фигуры в радиусе из 10 блоков...");
                    final Set<Figure> list = new HashSet<>();
                    for (final Figure figure : FigureManager.getFigures()) {
                        if ( figure.worldName.equals((p.getWorld().getName())) && getDistance (p,figure) <=100) {
                            list.add(figure);
                        }
                    }                    
                    SmartInventory.builder()
                        .id("FugureSelectMenu"+p.getName())
                        .provider(new MenuFinded(list))
                        .size(6, 9)
                        .title("§fФигуры в радиусе 100")
                        .build()
                        .open(p);
                }
            }));    

        
        contents.set(0, 4, ClickableItem.of( new ItemBuilder(Material.HOPPER)
            .name("§fНайти все в мире")
            .addLore("")
            .addLore("")
            .build(), e -> {
                if( e.isLeftClick()) {
                    //p.sendMessage("Ищем фигуры в радиусе из 10 блоков...");
                    final Set<Figure> list = new HashSet<>();
                    for (final Figure figure : FigureManager.getFigures()) {
                        if (  figure.worldName.equals((p.getWorld().getName())) ) {
                            list.add(figure);
                        }
                    }                    
                    SmartInventory.builder()
                        .id("FugureSelectMenu"+p.getName())
                        .provider(new MenuFinded(list))
                        .size(6, 9)
                        .title("§fФигуры в мире")
                        .build()
                        .open(p);
                }
            }));    

        contents.set(0, 5, ClickableItem.of( new ItemBuilder(Material.HOPPER)
            .name("§fПоказать все")
            .addLore("")
            .addLore("")
            .build(), e -> {
                if( e.isLeftClick()) {
                    final Set<Figure> list = new HashSet<>(FigureManager.getFigures());
                    SmartInventory.builder()
                        .id("FugureSelectMenu"+p.getName())
                        .provider(new MenuFinded(list))
                        .size(6, 9)
                        .title("§fВсе фигуры")
                        .build()
                        .open(p);
                }
            }));    

        
        
        
        
        

    }
    
    
    
    
    
    
    private int getDistance(final Player p, final Figure figure) {
        return square(p.getLocation().getBlockX()-figure.x) + square(p.getLocation().getBlockY()-figure.y) + square(p.getLocation().getBlockZ()-figure.z);
    }
   
    private static int square(final int num) {
        return num * num;
    }

    
    
    
}
