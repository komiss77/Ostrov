package builder.menu;


import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.Listener.PlayerListener;
import ru.komiss77.Managers.PM;
import ru.komiss77.menu.ViewPerm.SelectPlayer;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.SmartInventory;





public class BuilderMain implements InventoryProvider {
    
    
    
    private static final ItemStack fill = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name("§8.").build();;
    

    
    public BuilderMain() {
    }
    
    
    
    @Override
    public void init(final Player p, final InventoryContent contents) {
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
        contents.fillRect(0,0,  5,8, ClickableItem.empty(fill));
        
        
        
        
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




        contents.set(1,1, ClickableItem.of(new ItemBuilder(Material.GRASS_BLOCK)
            .name("§7Миры")
            .lore("§7Управление")
            .lore("")
            .lore("§7- перемещение в миры")
            .lore("§7- настройки миров")
            .lore("")
            .lore("§6Вы находитесь в биоме:")
            .lore("§e"+p.getWorld().getBiome(p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ()))
            .lore("")
            .lore("§7ЛКМ - открыть")
            .lore("")
            .build(), e-> {
                p.performCommand("world");
            }));


        contents.set(1,2, ClickableItem.of(new ItemBuilder(Material.ENDER_PEARL)
            .name("§7места")
            .lore("§7Управление")
            .lore("")
            .lore("§7Настройка варпов")
            .lore("")
            .lore( "§7ЛКМ - открыть" )
            .lore("")
            .build(), e-> {
                p.performCommand("warp");
            }));




        contents.set(1,3, ClickableItem.of(new ItemBuilder(Material.COW_SPAWN_EGG)
            .name("§7Сущности")
            .lore("§7Управление")
            .lore("")
            .lore("§7поиск, просмотр и удаление")
            .lore("§7сущностей")
            .lore("")
            .lore("§7ЛКМ - открыть")
            .lore("")
            .build(), e-> {
                p.performCommand("entity");
            }));


        contents.set(1,4, ClickableItem.of(new ItemBuilder(Material.ARMOR_STAND)
            .name("§7Фигуры")
            .lore("§7Управление")
            .lore("")
            .lore("§7Открыть главное")
            .lore("§7меню фигур")
            .lore("")
            .lore("§7ЛКМ - получить")
            .lore("")
            .build(), e-> {
                p.performCommand("figure");
            }));

        contents.set(1,5, ClickableItem.of(new ItemBuilder(Material.CARTOGRAPHY_TABLE)
            .name("§7Схематики")
            .lore("")
            .lore("§7Создание/редактирование/удаление")
            .lore("§7схематиков")
            .lore("")
            .lore("§7ЛКМ - открыть")
            .lore("")
            .build(), e-> {
                PM.getOplayer(p).setup.openSchemMainMenu(p);
            }));













        
        
        
        
        
        
        
        
        
        
        
        
        
        
        contents.set(2,1, ClickableItem.of(new ItemBuilder(Material.PAPER)
            .name("§6Репорты")
            .lore("§7Модерация")
            .lore("")
            .lore("§7Просмотр репортов")
            .lore("")
            .lore("§7ЛКМ - открыть")
            .lore("")
            .build(), e-> {
                p.performCommand("report");
            }));



        contents.set(2,2, ClickableItem.of(new ItemBuilder(Material.ENDER_EYE)
            .name("§7шпионаж")
            .lore("§7Модерация")
            .lore("")
            .lore("§7Скрытый контроль")
            .lore("§7действий игроков.")
            .lore("")
            .lore(p.hasPermission("ostrov.spy") ? "§7ЛКМ - открыть" : "§cv=нет права ostrov.spy")
            .lore("")
            .build(), e-> {
                p.performCommand("spy");
            }));


        contents.set(2,3, ClickableItem.of(new ItemBuilder(Material.LIME_DYE)
            .name("§7Проверить права")
            .lore("§7Показать загруженные")
            .lore("§7пермишены для")
            .lore("§7этого сервера")
            //.lore("§7ЛКМ - открыть")
            .lore("")
            .build(), e-> {
                 SmartInventory.builder()
                .id("Чьи права показать")
                .provider(new SelectPlayer())
                .size(6, 9)
                .title("Чьи права показать?")
                .build()
                .open(p);
            }));



        
        
        
        
        
        
        
        
        
        
        
        
        
       
        contents.set(3,1, ClickableItem.of(new ItemBuilder(Material.GOLDEN_SWORD)
            .name("§6Наборы")
            .lore("§7Настройки")
            .lore("")
            .lore("§7ЛКМ - получение")
            .lore("§7ПКМ - редактировать")
            .lore("")
            .build(), e-> {
                if (e.isLeftClick()) {
                    p.performCommand("kit");
                } else if (e.isRightClick()) {
                    p.performCommand("kit admin");
                }
            }));

        
        
        
        








        

        contents.set(4,1, ClickableItem.of(new ItemBuilder(Material.NOTE_BLOCK)
            .name("§7Прослушивание звуков")
            .lore("§7Утилита")
            .lore("")
            .lore("§7ЛКМ - открыть")
            .lore("")
            .build(), e-> {
                p.performCommand("sound");
            }));

                
        contents.set(4,2, ClickableItem.of(new ItemBuilder(Material.WARPED_SIGN)
            .name("§7Таблички")
            .lore("§7Утилита")
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

                


                




                
                
            
            
        
        





        

        
        
        contents.set( 5, 4, ClickableItem.of( new ItemBuilder(Material.CRIMSON_FENCE).name("Закрыть режим строителя").build(), e -> 
        {
            p.performCommand("builder end");
        }
        ));
        


        

    }


    
    
    
    
    
    
    
    
    
}
