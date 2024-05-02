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
import ru.komiss77.enums.Module;
import ru.komiss77.listener.InteractLst;
import ru.komiss77.modules.displays.DisplayMenu;
import ru.komiss77.modules.items.menu.ItemMenu;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.player.mission.MissionManager;
import ru.komiss77.modules.player.profile.StatManager;
import ru.komiss77.modules.signProtect.SignProtectLst;
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

    private static final ItemStack fill = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name("§8.").build();

    ;
    
    
    @Override
    public void init(final Player p, final InventoryContent content) {
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
        content.fillRect(0, 0, 5, 8, ClickableItem.empty(fill));

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
        content.set(0, 4, ClickableItem.of(new ItemBuilder(Material.ANVIL)
                .name("§6Меню локального сервера")
                .build(), e -> {
                    PM.getOplayer(p).setup.lastEdit = "LocalGame";
                    PM.getOplayer(p).setup.openLocalGameMenu(p);
                }));

        
        
        
        
        
        
        
        
        
        
        content.set(1, 1, ClickableItem.of(new ItemBuilder(Material.PAPER)
                .name("§6Репорты")
                .addLore("§7Модерация")
                .addLore("")
                .addLore("§7Просмотр репортов")
                .addLore("")
                .addLore("§7ЛКМ - открыть")
                .addLore("")
                .build(), e -> {
                    p.performCommand("report");
                }));

        content.set(1, 2, ClickableItem.of(new ItemBuilder(Material.ENDER_EYE)
                .name("§7шпионаж")
                .addLore("§7Модерация")
                .addLore("")
                .addLore("§7Скрытый контроль")
                .addLore("§7действий игроков.")
                .addLore("")
                .addLore(p.hasPermission("ostrov.spy") ? "§7ЛКМ - открыть" : "§cv=нет права ostrov.spy")
                .addLore("")
                .build(), e -> {
                    p.performCommand("spy");
                }));

        content.set(1, 3, ClickableItem.of(new ItemBuilder(Material.LIME_DYE)
                .name("§7Пермишены")
                .addLore("")
                .addLore("§7ЛКМ - Проверить права игрока")
                .addLore("")
                .addLore("§7ПКМ - Показать загруженные")
                .addLore("§7группы и пермишены для")
                .addLore("§7этого сервера")
                //.addLore("§7ЛКМ - открыть")
                .addLore("")
                .build(), e -> {
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

        content.set(1, 4, ClickableItem.of(new ItemBuilder(Material.GOLD_INGOT)
                .name("§7Редактор миссий")
          .addLore("§7ЛКМ - редактор")
          //.addLore("§7ПКМ - debug addCustomStat")
                .addLore(StatManager.DEBUG ? "§7ПКМ - §cdisable §7addCustomStat debug" : "§7ПКМ - §aenable §7addCustomStat debug")
                .build(), e -> {
                  if (e.isLeftClick()) {
                    MissionManager.openMissionsEditMenu(p);
                  } else if (e.isRightClick()) {
                    StatManager.DEBUG = !StatManager.DEBUG;
                    reopen(p, content);
                  }

                }));

        content.set(1, 5, ClickableItem.of(new ItemBuilder(Material.TURTLE_HELMET)
                .name("§7Аналитика регистраций")
                //.addLore("§7ЛКМ - открыть")
                .addLore("")
                .build(), e -> {
                    p.performCommand("analytics");
                }));

































        content.set(2, 1, ClickableItem.of(new ItemBuilder(Material.GRASS_BLOCK)
                .name("§7Миры")
                .addLore("§7Управление")
                .addLore("")
                .addLore("§7- перемещение в миры")
                .addLore("§7- настройки миров")
                .addLore("")
                .addLore("§6Вы находитесь в биоме:")
                .addLore("§e" + p.getWorld().getBiome(p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ()))
                .addLore("")
                .addLore("§7ЛКМ - открыть")
                .addLore("")
                .build(), e -> {
                    p.performCommand("world");
                }));

        content.set(2, 2, ClickableItem.of(new ItemBuilder(Material.ENDER_PEARL)
                .name("§7места")
                .addLore("§7Управление")
                .addLore("")
                .addLore("§7Настройка варпов")
                .addLore("")
                .addLore("§7ЛКМ - открыть")
                .addLore("")
                .build(), e -> {
                    p.performCommand("warp");
                }));

        content.set(2, 3, ClickableItem.of(new ItemBuilder(Material.GOLDEN_SWORD)
                .name("§6Наборы")
                .addLore("§7Настройки")
                .addLore("")
                .addLore("§7ЛКМ - получение")
                .addLore("§7ПКМ - редактировать")
                .addLore("")
                .build(), e -> {
                    if (e.isLeftClick()) {
                        p.performCommand("kit");
                    } else if (e.isRightClick()) {
                        p.performCommand("kit admin");
                    }
                }));

        content.set(2, 4, ClickableItem.of(new ItemBuilder(Material.COW_SPAWN_EGG)
                .name("§7Сущности")
                .addLore("§7Управление")
                .addLore("")
                .addLore("§7поиск, просмотр и удаление")
                .addLore("§7сущностей")
                .addLore("§fЛКМ - открыть")
                .addLore("")
                .addLore("§fБИЛДЕР!! §eПрисесть + ПКМ")
                .addLore("§eна сущность - §bнастроить§e!")
                .addLore("")
                .build(), e -> {
                  PM.getOplayer(p).setup.openEntityWorldMenu(p, p.getWorld(), -1);
                }));

        content.set(2, 5, ClickableItem.of(new ItemBuilder(Material.ARMOR_STAND)
                .name("§7Фигуры")
                .addLore("§7Управление")
                .addLore("")
                .addLore("§7Открыть главное")
                .addLore("§7меню фигур")
                .addLore("")
                .addLore("§7ЛКМ - получить")
                .addLore("")
                .build(), e -> {
                    p.performCommand("figure");
                }));

        content.set(2, 6, ClickableItem.of(new ItemBuilder(Material.LEATHER_HORSE_ARMOR)
                .name("§7Просмотр Model Data")
                .addLore("§7Перетащи сюда предмет,")
                .addLore("§7будут показаны все его")
                .addLore("§7вариации,")
                .addLore("§7или ЛКМ - показать для")
                .addLore("§7кожаной конской брони.")
                .build(), e -> {
                    if (e.getCursor() != null && e.getCursor().getType() != Material.AIR) {
                        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                        SmartInventory.builder()
                                .id("CustomModelData" + p.getName())
                                .provider(new CustomModelData(0, e.getCursor().getType()))
                                .size(6, 9)
                                .title("§6" + e.getCursor().getType().name())
                                .build()
                                .open(p);
                        e.getView().setCursor(new ItemStack(Material.AIR));
                    } else {
                        SmartInventory.builder()
                                .id("CustomModelData" + p.getName())
                                .provider(new CustomModelData(0, Material.LEATHER_HORSE_ARMOR))
                                .size(6, 9)
                                .title("§6" + Material.LEATHER_HORSE_ARMOR.name())
                                .build()
                                .open(p);
                    }
                }));

        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        content.set(3, 1, ClickableItem.of(new ItemBuilder(Material.NOTE_BLOCK)
                .name("§7Прослушивание звуков")
                .addLore("§7Утилита")
                .addLore("")
                .addLore("§7ЛКМ - открыть")
                .addLore("")
                .build(), e -> {
                    p.performCommand("sound");
                }));

        content.set(3, 2, ClickableItem.of(new ItemBuilder(Material.WARPED_SIGN)
                .name("§7Таблички")
                .addLore("§7Утилита")
                .addLore("")
                .addLore("§7Получить предмет")
                .addLore("§7помогающий в работе")
                .addLore("§7с табличками")
                .addLore("")
                .addLore("§7ЛКМ - получить")
                .addLore("")
                .build(), e -> {
                    p.getInventory().addItem(InteractLst.signEdit.clone());
                }));

        content.set(3, 3, ClickableItem.of(new ItemBuilder(Material.CRIMSON_SIGN)
                .name("§7Серверные таблички")
                .addLore("§7Утилита")
                .addLore("")
                .addLore("§7Получить предмет")
                .addLore("§7для настройки")
                .addLore("§7серверных табличек")
                .addLore("")
                .addLore("§7ЛКМ - получить")
                .addLore("")
                .build(), e -> {
                    p.getInventory().addItem(InteractLst.gameSignEdit.clone());
                }));

        content.set(3, 4, ClickableItem.of(new ItemBuilder(Material.CARTOGRAPHY_TABLE)
                .name("§7Схематики")
                .addLore("")
                .addLore("§7Создание/редактирование/удаление")
                .addLore("§7схематиков")
                .addLore("")
                .addLore("§7ЛКМ - открыть")
                .addLore("")
                .build(), e -> {
                    PM.getOplayer(p).setup.openSchemMainMenu(p);
                }));

        content.set(3, 5, ClickableItem.of(new ItemBuilder(Material.SMITHING_TABLE)
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



        content.set(3, 6, ClickableItem.of(new ItemBuilder(Material.RIB_ARMOR_TRIM_SMITHING_TEMPLATE)
                .name("§7Редактор переводиков")
                .addLore("§7Утилита")
                .addLore("§7В редакторе сортировка")
                .addLore("§7по свежести")
                .build(), e -> {
                    LangEditor.edit(p, 0);
                }));

































      if (Config.displays) {
        content.set(4, 1, ClickableItem.of(new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
          .name("§7Дисплеи §aВКЛЮЧЕНЫ")
          .addLore("§7Утилита")
          .addLore("§7Q - §cВЫКЛЮЧИТЬ")
          .addLore("")
          .addLore("§7ЛКМ - настройка ближайшего дисплея")
          .addLore("§7Шифт + ЛКМ - тп дисплей рядом")
          .addLore("§7ПКМ - создать дисплей")
          .addLore("§7Шифт + ПКМ - клон дисплея рядом")
          .build(), e -> {
          final Location loc = p.getLocation();

          Display tds = null;
          switch (e.getClick()) {
            case DROP -> {
              Config.displays = false;
              Config.getConfig().set("modules.displays", false);
              Config.getConfig().saveConfig();
              reopen(p, content);
              return;
            }
            case LEFT -> {
              tds = LocationUtil.getClsChEnt(new WXYZ(loc), 100, Display.class, en -> true);
              if (tds == null) {
                p.closeInventory();
                p.sendMessage("§6Дисплея рядом не найдено!");
                return;
              }
            }
            case SHIFT_LEFT -> {
              tds = LocationUtil.getClsChEnt(new WXYZ(loc), 100, Display.class, en -> true);
              if (tds != null) {
                tds.teleport(new WXYZ(loc).getCenterLoc());
              } else {
                p.closeInventory();
                p.sendMessage("§6Дисплея рядом не найдено!");
              }
              return;
            }
            case RIGHT -> {
              tds = p.getWorld().spawn(new WXYZ(p.getLocation()).getCenterLoc(), TextDisplay.class);
              ((TextDisplay) tds).text(TCUtils.format("§оКекст"));
            }
            case SHIFT_RIGHT -> {
              final Display oldDis = LocationUtil.getClsChEnt(new WXYZ(loc), 100, Display.class, en -> true);
              if (oldDis != null) {
                switch (oldDis.getType()) {
                  case BLOCK_DISPLAY -> {
                    tds = p.getWorld().spawn(new WXYZ(p.getLocation()).getCenterLoc(), BlockDisplay.class);
                    ((BlockDisplay) tds).setBlock(((BlockDisplay) oldDis).getBlock());
                  }
                  case ITEM_DISPLAY -> {
                    tds = p.getWorld().spawn(new WXYZ(p.getLocation()).getCenterLoc(), ItemDisplay.class);
                    ((ItemDisplay) tds).setItemStack(((ItemDisplay) oldDis).getItemStack());
                    ((ItemDisplay) tds).setItemDisplayTransform(((ItemDisplay) oldDis).getItemDisplayTransform());
                  }
                  case TEXT_DISPLAY -> {
                    tds = p.getWorld().spawn(new WXYZ(p.getLocation()).getCenterLoc(), TextDisplay.class);
                    ((TextDisplay) tds).text(((TextDisplay) oldDis).text());
                    ((TextDisplay) tds).setSeeThrough(((TextDisplay) oldDis).isSeeThrough());
                    ((TextDisplay) tds).setShadowed(((TextDisplay) oldDis).isShadowed());
                    ((TextDisplay) tds).setLineWidth(((TextDisplay) oldDis).getLineWidth());
                  }
                  default -> tds = p.getWorld().spawn(new WXYZ(p.getLocation()).getCenterLoc(), TextDisplay.class);
                }
                tds.setPersistent(true);
                tds.setBillboard(oldDis.getBillboard());
                final Transformation atr = oldDis.getTransformation();
                tds.setTransformation(new Transformation(atr.getTranslation(),
                  atr.getLeftRotation(), atr.getScale(), atr.getRightRotation()));
              } else  {
                p.closeInventory();
                p.sendMessage("§6Дисплея для клонирования рядом не найдено!");
                return;
              }
              return;
            }
          }

          SmartInventory.builder()
            .id(p.getName() + " Display")
            .title("      §яНастройки Дисплея")
            .provider(new DisplayMenu(tds))
            .size(3, 9)
            .build().open(p);
        }));
      } else {
        content.set(4, 1, ClickableItem.of(new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
          .name("§7Дисплеи §сВЫКЛЮЧЕНЫ")
          .addLore("§7Утилита")
          .addLore("")
          .addLore("§7ЛКМ - §aВКЛЮЧИТТЬ")
          .addLore("")
          .build(), e -> {
          if (e.isLeftClick()) {
            Config.displays = true;
            Config.getConfig().set("modules.displays", true);
            Config.getConfig().saveConfig();
            reopen(p, content);
          }
        }));
      }


      if (SignProtectLst.enable) {
        content.set(4, 2, ClickableItem.of(new ItemBuilder(Material.WARPED_HANGING_SIGN)
          .name("§7SignProtect §aВКЛЮЧЕН")
          .addLore("")
          .addLore("§7ПКМ - §cВЫКЛЮЧИТЬ")
          .addLore("")
          .build(), e -> {
          if (e.isRightClick()) {
            SignProtectLst.enable = false;
            Config.getConfig().set("modules.signProtect", false);
            Config.getConfig().saveConfig();
            Ostrov.getModule(Module.signProtect).onDisable();
            reopen(p, content);
          }
        }));
      } else {
        content.set(4, 2, ClickableItem.of(new ItemBuilder(Material.CRIMSON_HANGING_SIGN)
          .name("§7SignProtect §сВЫКЛЮЧЕН")
          .addLore("")
          .addLore("§7ЛКМ - §aВКЛЮЧИТТЬ")
          .addLore("")
          .build(), e -> {
          if (e.isLeftClick()) {
            SignProtectLst.enable = true;
            Config.getConfig().set("modules.signProtect", true);
            Config.getConfig().saveConfig();
            Ostrov.getModule(Module.signProtect).reload();
            reopen(p, content);
          }
        }));
      }
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        content.set(5, 4, ClickableItem.of(new ItemBuilder(Material.CRIMSON_FENCE).name("Закрыть режим строителя").build(), e
                -> {
            p.performCommand("builder end");
        }
        ));

    }

}
