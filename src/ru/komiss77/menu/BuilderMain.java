package ru.komiss77.menu;


import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.Listener.PlayerListener;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;





public class BuilderMain implements InventoryProvider {
    
    
    
    private static final ItemStack fill = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name("§8.").build();;
    

    
    public BuilderMain() {
    }
    
    
    
    @Override
    public void init(final Player p, final InventoryContent contents) {
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
        contents.fillRect(0,0,  2,8, ClickableItem.empty(fill));
        
        
        
        
       /*
          seen:
            description: Информация об игроке
          passport:
            description: Просмотр паспорта игрока
        
          nbtfind:
            description: Просканировать инвентарь игроков на нбт-тэги <moder>
            usage: '/nbtfind [material] (all-любой)'
          nbtcheck:
            description: Просканировать предмет на нбт-тэги <moder>
            usage: '/nbtcheck'
          oreload:
            description: Перезагрузить конфиг <moder>
          biome:
            description: Узнать биом
          entity:
            description: Управление сущностями <moder>
          blockstate:
            description: Подсчитать blockstate <moder>
          operm:
            description: Список пермишенов <moder>
          wm:
            description: Менеджер миров

        */ 




        contents.set(0,0, ClickableItem.of(new ItemBuilder(Material.GRASS_BLOCK)
            .name("§7Миры")
            .lore("")
            .lore("§7- перемещение в миры")
            .lore("§7- настройки миров")
            .lore("")
            .lore("§7ЛКМ - открыть")
            .lore("")
            .build(), e-> {
                p.performCommand("world");
            }));




        contents.set(0,1, ClickableItem.of(new ItemBuilder(Material.COW_SPAWN_EGG)
            .name("§7Сущности")
            .lore("")
            .lore("§7поиск, просмотр и удаление")
            .lore("§7сущностей")
            .lore("")
            .lore("§7ЛКМ - открыть")
            .lore("")
            .build(), e-> {
                p.performCommand("entity");
            }));















        
        
        
        
        
        
        
        
        
        
        
        
        
        


        contents.set(1,0, ClickableItem.of(new ItemBuilder(Material.ENDER_EYE)
            .name("§7шпионаж")
            .lore("")
            .lore("§7Скрытый контроль")
            .lore("§7действий игроков.")
            .lore("")
            .lore(p.hasPermission("ostrov.spy") ? "§7ЛКМ - открыть" : "§cv=нет права ostrov.spy")
            .lore("")
            .build(), e-> {
                p.performCommand("spy");
            }));


        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        








        

        contents.set(4,0, ClickableItem.of(new ItemBuilder(Material.NOTE_BLOCK)
            .name("§7Прослушивание звуков")
            .lore("")
            .lore("§7ЛКМ - открыть")
            .lore("")
            .build(), e-> {
                p.performCommand("sound");
            }));

                
        contents.set(4,1, ClickableItem.of(new ItemBuilder(Material.WARPED_SIGN)
            .name("§7Таблички")
            .lore("")
            .lore("§7Получить предмет")
            .lore("§7помогающий в работе")
            .lore("§7с табличками")
            .lore("")
            .lore("§7ЛКМ - получить")
            .lore("")
            .build(), e-> {
                p.getInventory().addItem(PlayerListener.signEdit.clone());
            }));

                
        contents.set(4,2, ClickableItem.of(new ItemBuilder(Material.ARMOR_STAND)
            .name("§7Фигуры")
            .lore("")
            .lore("§7Открыть главное")
            .lore("§7меню фигур")
            .lore("")
            .lore("§7ЛКМ - получить")
            .lore("")
            .build(), e-> {
                p.performCommand("figure");
            }));


                




                
                
            
            
        
        





        

        
        
        contents.set( 5, 4, ClickableItem.of( new ItemBuilder(Material.CRIMSON_FENCE).name("Закрыть режим строителя").build(), e -> 
        {
            p.setGameMode(GameMode.SURVIVAL);
            p.closeInventory();
            PlayerListener.signCache.remove(p.getName());
        }
        ));
        


        

    }


    
    
    
    
    
    
    
    
    
}
