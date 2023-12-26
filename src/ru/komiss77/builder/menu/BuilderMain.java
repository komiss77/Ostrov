package ru.komiss77.builder.menu;


import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;

import ru.komiss77.Config;
import ru.komiss77.Ostrov;
import ru.komiss77.builder.menu.ViewPerm.SelectPlayer;
import ru.komiss77.listener.PlayerLst;
import ru.komiss77.modules.displays.DisplayMenu;
import ru.komiss77.modules.items.ItemMenu;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.player.mission.MissionManager;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.LocationUtil;
import ru.komiss77.utils.TCUtils;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.SmartInventory;



public class BuilderMain implements InventoryProvider {
    
    private static final ItemStack fill = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name("§8.").build();;
    
    
    @Override
    public void init(final Player p, final InventoryContent contents) {
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
        contents.fillRect(0,0,  4,8, ClickableItem.empty(fill));
        
        
        
        
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
        
        
        contents.set(0,4, ClickableItem.of(new ItemBuilder(Material.ANVIL)
            .name("§6Меню локального сервера")
            .build(), e-> {
                PM.getOplayer(p).setup.lastEdit = "LocalGame";
                PM.getOplayer(p).setup.openLocalGameMenu(p);
            }));
        
        
        
        contents.set(1,1, ClickableItem.of(new ItemBuilder(Material.PAPER)
            .name("§6Репорты")
            .addLore("§7Модерация")
            .addLore("")
            .addLore("§7Просмотр репортов")
            .addLore("")
            .addLore("§7ЛКМ - открыть")
            .addLore("")
            .build(), e-> {
                p.performCommand("report");
            }));



        contents.set(1,2, ClickableItem.of(new ItemBuilder(Material.ENDER_EYE)
            .name("§7шпионаж")
            .addLore("§7Модерация")
            .addLore("")
            .addLore("§7Скрытый контроль")
            .addLore("§7действий игроков.")
            .addLore("")
            .addLore(p.hasPermission("ostrov.spy") ? "§7ЛКМ - открыть" : "§cv=нет права ostrov.spy")
            .addLore("")
            .build(), e-> {
                p.performCommand("spy");
            }));


        contents.set(1,3, ClickableItem.of(new ItemBuilder(Material.LIME_DYE)
            .name("§7Пермишены")
            .addLore("")
            .addLore("§7ЛКМ - Проверить права игрока")
            .addLore("")
            .addLore("§7ПКМ - Показать загруженные")
            .addLore("§7группы и пермишены для")
            .addLore("§7этого сервера")
            //.addLore("§7ЛКМ - открыть")
            .addLore("")
            .build(), e-> {
                if (e.isLeftClick()) {
                    SmartInventory.builder()
                    .id("Чьи права показать")
                    .provider(new SelectPlayer())
                    .size(6, 9)
                    .title("Чьи права показать?")
                    .build()
                    .open(p);
                } else if (e.isRightClick()) {
                    SmartInventory.builder()
                    .id("Загружанные группы")
                    .provider(new ViewGroups())
                    .size(6, 9)
                    .title("Загружанные группы")
                    .build()
                    .open(p);                 
                }
                
            }));


       contents.set(1,4, ClickableItem.of(new ItemBuilder(Material.GOLD_INGOT)
            .name("§7Редактор миссий")
            //.addLore("§7ЛКМ - открыть")
            .addLore("")
            .build(), e-> {
                MissionManager.openMissionsEditMenu(p);
            }));

       contents.set(1,5, ClickableItem.of(new ItemBuilder(Material.TURTLE_HELMET)
            .name("§7Аналитика регистраций")
            //.addLore("§7ЛКМ - открыть")
            .addLore("")
            .build(), e-> {
                p.performCommand("analytics");
            }));
        
        
        
        
        
        
       
       
       
       
       
       
       
       
       
       
       
       
       
       
       



        contents.set(2,1, ClickableItem.of(new ItemBuilder(Material.GRASS_BLOCK)
            .name("§7Миры")
            .addLore("§7Управление")
            .addLore("")
            .addLore("§7- перемещение в миры")
            .addLore("§7- настройки миров")
            .addLore("")
            .addLore("§6Вы находитесь в биоме:")
            .addLore("§e"+p.getWorld().getBiome(p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ()))
            .addLore("")
            .addLore("§7ЛКМ - открыть")
            .addLore("")
            .build(), e-> {
                p.performCommand("world");
            }));


        contents.set(2,2, ClickableItem.of(new ItemBuilder(Material.ENDER_PEARL)
            .name("§7места")
            .addLore("§7Управление")
            .addLore("")
            .addLore("§7Настройка варпов")
            .addLore("")
            .addLore( "§7ЛКМ - открыть" )
            .addLore("")
            .build(), e-> {
                p.performCommand("warp");
            }));


        
        
       
        contents.set(2,3, ClickableItem.of(new ItemBuilder(Material.GOLDEN_SWORD)
            .name("§6Наборы")
            .addLore("§7Настройки")
            .addLore("")
            .addLore("§7ЛКМ - получение")
            .addLore("§7ПКМ - редактировать")
            .addLore("")
            .build(), e-> {
                if (e.isLeftClick()) {
                    p.performCommand("kit");
                } else if (e.isRightClick()) {
                    p.performCommand("kit admin");
                }
            }));

        
        
        

        contents.set(2,4, ClickableItem.of(new ItemBuilder(Material.COW_SPAWN_EGG)
            .name("§7Сущности")
            .addLore("§7Управление")
            .addLore("")
            .addLore("§7поиск, просмотр и удаление")
            .addLore("§7сущностей")
            .addLore("")
            .addLore("§7ЛКМ - открыть")
            .addLore("")
            .build(), e-> {
                p.performCommand("entity");
            }));


        contents.set(2,5, ClickableItem.of(new ItemBuilder(Material.ARMOR_STAND)
            .name("§7Фигуры")
            .addLore("§7Управление")
            .addLore("")
            .addLore("§7Открыть главное")
            .addLore("§7меню фигур")
            .addLore("")
            .addLore("§7ЛКМ - получить")
            .addLore("")
            .build(), e-> {
                p.performCommand("figure");
            }));



        

        contents.set(2,6, ClickableItem.of(new ItemBuilder(Material.LEATHER_HORSE_ARMOR)
            .name("§7Просмотр Model Data")
            .addLore("§7Перетащи сюда предмет,")
            .addLore("§7будут показаны все его")
            .addLore("§7вариации,")
            .addLore("§7или ЛКМ - показать для")
            .addLore("§7кожаной конской брони.")
            .build(), e-> {
                if (e.getCursor() != null && e.getCursor().getType() != Material.AIR) {
                    p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                    SmartInventory.builder()
                        .id("CustomModelData"+p.getName())
                        .provider(new CustomModelData(0, e.getCursor().getType()))
                        .size(6, 9)
                        .title("§6"+e.getCursor().getType().name())
                        .build()
                        .open(p);
                    e.getView().setCursor(new ItemStack(Material.AIR));
                } else {
                    SmartInventory.builder()
                        .id("CustomModelData"+p.getName())
                        .provider(new CustomModelData(0, Material.LEATHER_HORSE_ARMOR))
                        .size(6, 9)
                        .title("§6"+Material.LEATHER_HORSE_ARMOR.name())
                        .build()
                        .open(p);
                }
            }));



        
        








        
        
        
        
        
        
        
        
        

        
        
        
        
        

        








        

        contents.set(3,1, ClickableItem.of(new ItemBuilder(Material.NOTE_BLOCK)
            .name("§7Прослушивание звуков")
            .addLore("§7Утилита")
            .addLore("")
            .addLore("§7ЛКМ - открыть")
            .addLore("")
            .build(), e-> {
                p.performCommand("sound");
            }));

                
        contents.set(3,2, ClickableItem.of(new ItemBuilder(Material.WARPED_SIGN)
            .name("§7Таблички")
            .addLore("§7Утилита")
            .addLore("")
            .addLore("§7Получить предмет")
            .addLore("§7помогающий в работе")
            .addLore("§7с табличками")
            .addLore("")
            .addLore("§7ЛКМ - получить")
            .addLore("")
            .build(), e-> {
                p.getInventory().addItem(PlayerLst.signEdit.clone());
            }));

        contents.set(3,3, ClickableItem.of(new ItemBuilder(Material.CRIMSON_SIGN)
            .name("§7Серверные таблички")
            .addLore("§7Утилита")
            .addLore("")
            .addLore("§7Получить предмет")
            .addLore("§7для настройки")
            .addLore("§7серверных табличек")
            .addLore("")
            .addLore("§7ЛКМ - получить")
            .addLore("")
            .build(), e-> {
                p.getInventory().addItem(PlayerLst.gameSignEdit.clone());
            }));

                
                

        contents.set(3,4, ClickableItem.of(new ItemBuilder(Material.CARTOGRAPHY_TABLE)
            .name("§7Схематики")
            .addLore("")
            .addLore("§7Создание/редактирование/удаление")
            .addLore("§7схематиков")
            .addLore("")
            .addLore("§7ЛКМ - открыть")
            .addLore("")
            .build(), e-> {
                PM.getOplayer(p).setup.openSchemMainMenu(p);
            }));

        contents.set(3, 5, ClickableItem.of(new ItemBuilder(Material.SMITHING_TABLE)
                .name("§7Создание предмета")
                .addLore("§7Утилита")
                .addLore("")
                .addLore("§7Клик - выдать / изменить")
                .addLore("§7предмет в левой руке")
                .addLore("")
                .build(), e -> {
                    final ItemStack it = p.getInventory().getItemInOffHand();
                    if (ItemUtils.isBlank(it, false)) {
                        p.sendMessage(Ostrov.PREFIX + "§cНужно держать что-то в левой руке!");
                        p.closeInventory();
                    } else {
                        SmartInventory.builder().id("Item " + p.getName()).provider(new ItemMenu(it.hasItemMeta() ? it : new ItemStack(it)))
                            .size(3, 9).title("      §6Создание Предмета").build().open(p);
                    }
                }));

        if (Config.displays) {
            contents.set(3, 6, ClickableItem.of(new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
                    .name("§7Дисплей")
                    .addLore("§7Утилита")
                    .addLore("")
                    .addLore("§7ЛКМ - найти дисплей рядом")
                    .addLore("§7Шифт + ЛКМ - тп дисплей рядом")
                    .addLore("§7ПКМ - создать дисплей")
                    .addLore("§7Шифт + ПКМ - клон дисплея рядом")
                    .addLore("")
                    .build(), e -> {
                    	final Display tds;
                    	final Location loc = p.getLocation();
                    	if (e.isLeftClick()) {
                			tds = LocationUtil.getClsChEnt(new WXYZ(loc), 100, Display.class, en -> true);
                    		if (tds != null && e.isShiftClick()) tds.teleport(new WXYZ(loc).getCenterLoc());
                    	} else {
                    		if (e.isShiftClick()) {
                    			final Display ods = LocationUtil.getClsChEnt(new WXYZ(loc), 100, Display.class, en -> true);
                    			if (ods != null) {
                    				switch (ods.getType()) {
    								case BLOCK_DISPLAY:
    									tds = p.getWorld().spawn(new WXYZ(p.getLocation()).getCenterLoc(), BlockDisplay.class);
    									((BlockDisplay) tds).setBlock(((BlockDisplay) ods).getBlock());
    									break;
    								case ITEM_DISPLAY:
    									tds = p.getWorld().spawn(new WXYZ(p.getLocation()).getCenterLoc(), ItemDisplay.class);
    									((ItemDisplay) tds).setItemStack(((ItemDisplay) ods).getItemStack());
    									((ItemDisplay) tds).setItemDisplayTransform(((ItemDisplay) ods).getItemDisplayTransform());
    									break;
    								case TEXT_DISPLAY:
    									tds = p.getWorld().spawn(new WXYZ(p.getLocation()).getCenterLoc(), TextDisplay.class);
    									((TextDisplay) tds).text(((TextDisplay) ods).text());
    									((TextDisplay) tds).setSeeThrough(((TextDisplay) ods).isSeeThrough());
    									((TextDisplay) tds).setShadowed(((TextDisplay) ods).isShadowed());
    									((TextDisplay) tds).setLineWidth(((TextDisplay) ods).getLineWidth());
    									break;
    								default:
    									tds = p.getWorld().spawn(new WXYZ(p.getLocation()).getCenterLoc(), TextDisplay.class);
    									break;
    								}
    								tds.setPersistent(true);
    								tds.setBillboard(ods.getBillboard());
    								final Transformation atr = ods.getTransformation();
    								tds.setTransformation(new Transformation(atr.getTranslation(), 
    									atr.getLeftRotation(), atr.getScale(), atr.getRightRotation()));
                    			} else tds = null;
                    		} else {
                    			tds = p.getWorld().spawn(new WXYZ(p.getLocation()).getCenterLoc(), TextDisplay.class);
                    			((TextDisplay) tds).text(TCUtils.format("§оКекст"));
                    		}
                    	}
                    	
                    	if (tds == null) {
                    		p.sendMessage(Ostrov.PREFIX + "Дисплей не найден!");
                    		p.closeInventory();
                    		return;
                    	}
                    	
                		SmartInventory.builder().id(p.getName() + " Display").title("      §яНастройки Дисплея")
                			.provider(new DisplayMenu(tds)).size(3, 9).build().open(p);
                    }));
        }



                




                
                
            
            
        
        





        

        
        
        contents.set( 4, 4, ClickableItem.of( new ItemBuilder(Material.CRIMSON_FENCE).name("Закрыть режим строителя").build(), e -> 
        {
            p.performCommand("builder end");
        }
        ));
        


        

    }


    
    
    
    
    
    
    
    
    
}
