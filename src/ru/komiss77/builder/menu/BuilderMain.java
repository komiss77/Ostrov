package ru.komiss77.builder.menu;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import ru.komiss77.Cfg;
import ru.komiss77.Ostrov;
import ru.komiss77.builder.menu.ViewPerm.SelectPlayer;
import ru.komiss77.enums.Module;
import ru.komiss77.listener.InteractLst;
import ru.komiss77.listener.LimiterLst;
import ru.komiss77.modules.displays.DisplayMenu;
import ru.komiss77.modules.items.menu.ItemMenu;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.player.mission.MissionManager;
import ru.komiss77.modules.player.profile.StatManager;
import ru.komiss77.modules.regions.RM;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.LocUtil;
import ru.komiss77.utils.TCUtil;
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
            .lore("§7Модерация")
            .lore("")
            .lore("§7Просмотр репортов")
            .lore("")
            .lore("§7ЛКМ - открыть")
            .lore("")
            .build(), e -> {
            p.performCommand("report");
        }));

        content.set(1, 2, ClickableItem.of(new ItemBuilder(Material.ENDER_EYE)
            .name("§7шпионаж")
            .lore("§7Модерация")
            .lore("")
            .lore("§7Скрытый контроль")
            .lore("§7действий игроков.")
            .lore("")
            .lore(p.hasPermission("ostrov.spy") ? "§7ЛКМ - открыть" : "§cv=нет права ostrov.spy")
            .lore("")
            .build(), e -> {
            p.performCommand("spy");
        }));

        content.set(1, 3, ClickableItem.of(new ItemBuilder(Material.LIME_DYE)
            .name("§7Пермишены")
            .lore("")
            .lore("§7ЛКМ - Проверить права игрока")
            .lore("")
            .lore("§7ПКМ - Показать загруженные")
            .lore("§7группы и пермишены для")
            .lore("§7этого сервера")
            //.addLore("§7ЛКМ - открыть")
            .lore("")
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
            .lore("§7ЛКМ - редактор")
            //.addLore("§7ПКМ - debug addCustomStat")
            .lore(StatManager.DEBUG ? "§7ПКМ - §cвыкл.отладки" : "§7ПКМ - §aвкл.отладки")
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
            .lore("")
            .build(), e -> {
            p.performCommand("analytics");
        }));


        content.set(2, 1, ClickableItem.of(new ItemBuilder(Material.GRASS_BLOCK)
            .name("§7Миры")
            .lore("§7Управление")
            .lore("")
            .lore("§7- перемещение в миры")
            .lore("§7- настройки миров")
            .lore("")
            .lore("§6Вы находитесь в биоме:")
            .lore("§e" + p.getWorld().getBiome(p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ()))
            .lore("")
            .lore("§7ЛКМ - открыть")
            .lore("")
            .build(), e -> {
            p.performCommand("world");
        }));

        content.set(2, 2, ClickableItem.of(new ItemBuilder(Material.ENDER_PEARL)
            .name("§7места")
            .lore("§7Управление")
            .lore("")
            .lore("§7Настройка варпов")
            .lore("")
            .lore("§7ЛКМ - открыть")
            .lore("")
            .build(), e -> {
            p.performCommand("warp");
        }));

        content.set(2, 3, ClickableItem.of(new ItemBuilder(Material.GOLDEN_SWORD)
            .name("§6Наборы")
            .lore("§7Настройки")
            .lore("")
            .lore("§7ЛКМ - получение")
            .lore("§7ПКМ - редактировать")
            .lore("")
            .build(), e -> {
            if (e.isLeftClick()) {
                p.performCommand("kit");
            } else if (e.isRightClick()) {
                p.performCommand("kit admin");
            }
        }));

        content.set(2, 4, ClickableItem.of(new ItemBuilder(Material.COW_SPAWN_EGG)
            .name("§7Сущности")
            .lore("§7Управление")
            .lore("")
            .lore("§7поиск, просмотр и удаление")
            .lore("§7сущностей")
            .lore("§fЛКМ - открыть")
            .lore("")
            .lore("§fБИЛДЕР!! §eПрисесть + ПКМ")
            .lore("§eна сущность - §bнастроить§e!")
            .lore("")
            .build(), e -> {
            PM.getOplayer(p).setup.openEntityWorldMenu(p, p.getWorld(), -1);
        }));

        content.set(2, 5, ClickableItem.of(new ItemBuilder(Material.ARMOR_STAND)
            .name("§7Фигуры")
            .lore("§7Управление")
            .lore("")
            .lore("§7Открыть главное")
            .lore("§7меню фигур")
            .lore("")
            .lore("§7ЛКМ - получить")
            .lore("")
            .build(), e -> {
            p.performCommand("figure");
        }));

        content.set(2, 6, ClickableItem.of(new ItemBuilder(Material.COMPARATOR)
            .name("§fЛимитер")
            .lore(LimiterLst.enabled() ? "§aАктивен" : "§cВыключен")
            .lore("")
            .lore("§7ЛКМ - настроить")
            .lore("")
            .build(), e -> {
            LimiterLst.openMenu(p);
        }));

        if (Ostrov.wg) {
            content.set(2, 7, ClickableItem.of(new ItemBuilder(Material.OAK_FENCE)
                .name("§fРегионы")
                .lore("")
                .lore("§7ЛКМ - редактор заготовок")
                .lore("§7ПКМ - редактор флагов")
                .lore(RM.regenOnDelete ? "§6Регенерация включена" : "§3Регенерация вылючена")
                .lore(RM.regenOnDelete ? "§7Q - §2выключить" : "§7Q - §4включить")
                .lore("")
                .build(), e -> {
                if (e.getClick() == ClickType.LEFT) {
                    RM.openTemplateAdmin(p);
                } else if (e.getClick() == ClickType.RIGHT) {
                    RM.openFlagAdmin(p);
                } else if (e.getClick() == ClickType.DROP) {
                    RM.switchRegen(p);
                    reopen(p, content);
                }
            }));
        } else {
            content.set(2, 7, ClickableItem.empty(new ItemBuilder(Material.OAK_FENCE)
                .name("§fРегионы")
                .lore("§c§mНет WG!")
                .build()
            ));
        }


















        content.set(3, 1, ClickableItem.of(new ItemBuilder(Material.NOTE_BLOCK)
            .name("§7Прослушивание звуков")
            .lore("§7Утилита")
            .lore("")
            .lore("§7ЛКМ - открыть")
            .lore("")
            .build(), e -> {
            p.performCommand("sound");
        }));

        content.set(3, 2, ClickableItem.of(new ItemBuilder(Material.WARPED_SIGN)
            .name("§7Таблички")
            .lore("§7Утилита")
            .lore("")
            .lore("§7Получить предмет")
            .lore("§7помогающий в работе")
            .lore("§7с табличками")
            .lore("")
            .lore("§7ЛКМ - получить")
            .lore("")
            .build(), e -> {
            p.getInventory().addItem(InteractLst.signEdit.clone());
        }));

        content.set(3, 3, ClickableItem.of(new ItemBuilder(Material.CRIMSON_SIGN)
            .name("§7Серверные таблички")
            .lore("§7Утилита")
            .lore("")
            .lore("§7Получить предмет")
            .lore("§7для настройки")
            .lore("§7серверных табличек")
            .lore("")
            .lore("§7ЛКМ - получить")
            .lore("")
            .build(), e -> {
            p.getInventory().addItem(InteractLst.gameSignEdit.clone());
        }));

        content.set(3, 4, ClickableItem.of(new ItemBuilder(Material.CARTOGRAPHY_TABLE)
            .name("§7Схематики")
            .lore("")
            .lore("§7Создание/редактирование/удаление")
            .lore("§7схематиков")
            .lore("")
            .lore("§7ЛКМ - открыть")
            .lore("")
            .build(), e -> {
            PM.getOplayer(p).setup.openSchemMainMenu(p);
        }));

        content.set(3, 5, ClickableItem.of(new ItemBuilder(Material.SMITHING_TABLE)
            .name("§7Создание предмета")
            .lore("§7Утилита")
            .lore("")
            .lore("§7Клик - выдать / изменить")
            .lore("§7предмет в левой руке")
            .lore("")
            .build(), e -> {
            final ItemStack it = p.getInventory().getItemInOffHand();
            if (ItemUtil.isBlank(it, false)) {
                p.sendMessage(Ostrov.PREFIX + "§cНужно держать что-то в левой руке!");
                reopen(p, content);
            } else {
                ItemMenu.open(p, it.clone());
            }
        }));


        content.set(3, 6, ClickableItem.of(new ItemBuilder(Material.RIB_ARMOR_TRIM_SMITHING_TEMPLATE)
            .name("§7Редактор переводиков")
            .lore("§7Утилита")
            .lore("§7В редакторе сортировка")
            .lore("§7по свежести")
            .build(), e -> {
            LangEditor.edit(p, 0);
        }));


        content.set(3, 7, ClickableItem.of(new ItemBuilder(Material.LEATHER_HORSE_ARMOR)
            .name("§7Частицы и эффекты")
            //.lore("§7Перетащи сюда предмет,")
            //.lore("§7будут показаны все его")
            //.lore("§7вариации,")
            //.lore("§7или ЛКМ - показать для")
            //.lore("§7кожаной конской брони.")
            .build(), e -> {

                /*if (e.getCursor().getType() != Material.AIR) {
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
                }*/
            }
        ));


        if (Cfg.displays) {
            content.set(4, 1, ClickableItem.of(new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
                .name("§7Дисплеи §aВКЛЮЧЕНЫ")
                .lore("§7Утилита")
                .lore("§7Q - §cВЫКЛЮЧИТЬ")
                .lore("")
                .lore("§7ЛКМ - настройка ближайшего дисплея")
                .lore("§7Шифт + ЛКМ - тп дисплей рядом")
                .lore("§7ПКМ - создать дисплей")
                .lore("§7Шифт + ПКМ - клон дисплея рядом")
                .build(), e -> {
                final Location loc = p.getLocation();

                Display tds = null;
                switch (e.getClick()) {
                    case DROP -> {
                        Cfg.displays = false;
                        Cfg.getConfig().set("modules.displays", false);
                        Cfg.getConfig().saveConfig();
                        reopen(p, content);
                        return;
                    }
                    case LEFT -> {
                        tds = LocUtil.getClsChEnt(new WXYZ(loc), 100, Display.class, en -> true);
                        if (tds == null) {
                            p.closeInventory();
                            p.sendMessage("§6Дисплея рядом не найдено!");
                            return;
                        }
                    }
                    case SHIFT_LEFT -> {
                        tds = LocUtil.getClsChEnt(new WXYZ(loc), 100, Display.class, en -> true);
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
                        ((TextDisplay) tds).text(TCUtil.form("§оКекст"));
                    }
                    case SHIFT_RIGHT -> {
                        final Display oldDis = LocUtil.getClsChEnt(new WXYZ(loc), 100, Display.class, en -> true);
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
                                default ->
                                    tds = p.getWorld().spawn(new WXYZ(p.getLocation()).getCenterLoc(), TextDisplay.class);
                            }
                            tds.setPersistent(true);
                            tds.setBillboard(oldDis.getBillboard());
                            final Transformation atr = oldDis.getTransformation();
                            tds.setTransformation(new Transformation(atr.getTranslation(),
                                atr.getLeftRotation(), atr.getScale(), atr.getRightRotation()));
                        } else {
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
                .lore("§7Утилита")
                .lore("")
                .lore("§7ЛКМ - §aВКЛЮЧИТТЬ")
                .lore("")
                .build(), e -> {
                if (e.isLeftClick()) {
                    Cfg.displays = true;
                    Cfg.getConfig().set("modules.displays", true);
                    Cfg.getConfig().saveConfig();
                    reopen(p, content);
                }
            }));
        }


        if (Cfg.signProtect) {
            content.set(4, 2, ClickableItem.of(new ItemBuilder(Material.WARPED_HANGING_SIGN)
                .name("§7SignProtect §aВКЛЮЧЕН")
                .lore("")
                .lore("§7ПКМ - §cВЫКЛЮЧИТЬ")
                .lore("")
                .build(), e -> {
                if (e.isRightClick()) {
                    Cfg.signProtect = false;
                    Cfg.getConfig().set("modules.signProtect", false);
                    Cfg.getConfig().saveConfig();
                    Ostrov.getModule(Module.signProtect).onDisable();
                    reopen(p, content);
                }
            }));
        } else {
            content.set(4, 2, ClickableItem.of(new ItemBuilder(Material.CRIMSON_HANGING_SIGN)
                .name("§7SignProtect §сВЫКЛЮЧЕН")
                .lore("")
                .lore("§7ЛКМ - §aВКЛЮЧИТТЬ")
                .lore("")
                .build(), e -> {
                if (e.isLeftClick()) {
                    Cfg.signProtect = true;
                    Cfg.getConfig().set("modules.signProtect", true);
                    Cfg.getConfig().saveConfig();
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
