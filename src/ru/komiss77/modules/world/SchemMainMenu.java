package ru.komiss77.modules.world;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import ru.komiss77.Ostrov;
import ru.komiss77.builder.BuilderCmd;
import ru.komiss77.builder.SetupMode;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.world.Schematic.Rotate;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.StringUtil;
import ru.komiss77.utils.inventory.*;


public class SchemMainMenu implements InventoryProvider {


    @Override
    public void init(final Player p, final InventoryContent contents) {
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
        contents.fillRow(4, ClickableItem.empty(BuilderCmd.fill));

        final SetupMode sm = PM.getOplayer(p).setup;


        final Pagination pagination = contents.pagination();


        final ArrayList<ClickableItem> menuEntry = new ArrayList<>();

        final FileFilter schemFilter = (final File file) -> file.isFile() && file.getName().endsWith(".schem");

        final File schemFolder = new File(Ostrov.instance.getDataFolder() + "/schematics");


        if (schemFolder.exists() && schemFolder.isDirectory()) {
            for (final File schemFile : schemFolder.listFiles(schemFilter)) {

                final String schemName = schemFile.getName().replaceFirst(".schem", "");

                menuEntry.add(ClickableItem.of(new ItemBuilder(Material.BOOKSHELF)
                    .name(schemName)
                    .lore("§7Размер: " + (schemFile.length() < 1000 ? schemFile.length() + " байт" : schemFile.length() / 1000 + "кб"))
                    .lore("")
                    .lore("§7ЛКМ - §fменю загрузки")
                    .lore("§5Откроется подМеню выбора")
                    .lore("§5поворота схематика")
                    .lore("§5для вставки")
                    .lore("")
                    .lore("§7ПКМ - §fтест Compare")
                    .lore("§5Точка спавна схематика")
                    .lore("§5будет совмещена")
                    .lore("§5с локацией ног и выполнится")
                    .lore("§5поиск отличий с местностью.")
                    .lore("")
                    .lore("клав.Q - §cудалить")
                    .lore("")
                    .build(), e -> {

                    switch (e.getClick()) {
                        case LEFT:
                            SmartInventory.builder()
                                .type(InventoryType.HOPPER)
                                .id(schemName)
                                .provider(new SchemPasteMenu(schemName))
                                .title("§fСхематик " + schemName)
                                .build()
                                .open(p);
                            break;

                        case RIGHT:
                            SmartInventory.builder()
                                .type(InventoryType.HOPPER)
                                .id(schemName)
                                .provider(new SchemCompareMenu(schemName))
                                .title("§aCompare " + schemName)
                                .build()
                                .open(p);
                            break;

                        case DROP:
                            schemFile.delete();
                            p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1, 5);
                            reopen(p, contents);
                            return;
                        default:
                            break;

                    }
                    PM.soundDeny(p);
                }));
            }
        }


        if (sm.undo != null) {
            contents.set(5, 0, ClickableItem.of(new ItemBuilder(Material.NAUTILUS_SHELL)
                .name("§aОтмена последней вставки")
                .lore("§7")
                .lore("§7ЛКМ - отменить")
                .lore("§7")
                .build(), e -> {
                if (e.isLeftClick()) {
                    p.closeInventory();
                    sm.undo.paste(p, sm.undoLoc, Rotate.r0, true);
                    sm.undo = null;
                    sm.undoLoc = null;
                    //sm.undoRotate = null;
                }
            }));
        }


        contents.set(5, 2, new InputButton(InputButton.InputType.ANVILL, new ItemBuilder(Material.BOOK)
            .name("§fCоздать схематик")
            .build(), "название", newName -> {

            if (newName.isEmpty() || newName.length() > 16 || !StringUtil.checkString(newName, true, true)) {
                p.sendMessage("§cНедопустимое название!");
                PM.soundDeny(p);
                return;
            }
            final File schemFile = new File(Ostrov.instance.getDataFolder() + "/schematics", newName + ".schem");
            if (schemFile.exists() && schemFile.isFile()) {
                p.sendMessage("§cФайл с таким названием уже есть, будет перезаписан при сохранении!");
                //PM.soundDeny(p);
                //return;
            }
            //sm.resetCuboid();
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


        final PasteJob pj = WE.JOBS.get(WE.currentTask);

        contents.set(5, 6, ClickableItem.of(new ItemBuilder(Material.COMMAND_BLOCK_MINECART)
            .name("§7Процессы вставки")
            .lore("§7")
            .lore("§7Создано процессов: §b" + WE.JOBS.size())
            .lore("§7")
            .lore(pj == null ? "" : "§7Выполняется: §a" + pj.getSchemName())
            .lore(pj == null ? "" : "§7Текущий блок: §f" + pj.getCurrentXYZ())
            //.addLore(pj==null ? "" : "§7Локация 2: §f"+LocationUtil.StringFromLoc(pj.getPos2()))
            .lore(pj == null ? "" : "§7Прогресс: §e" + pj.percent + "%")
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


        pagination.setItems(menuEntry.toArray(new ClickableItem[0]));
        pagination.setItemsPerPage(36);


        contents.set(5, 4, ClickableItem.of(new ItemBuilder(Material.OAK_DOOR).name("гл.меню").build(), e
            -> sm.openMainSetupMenu(p)
        ));


        if (!pagination.isLast()) {
            contents.set(5, 8, ClickableItem.of(ItemUtil.nextPage, e
                -> contents.getHost().open(p, pagination.next().getPage()))
            );
        }

        if (!pagination.isFirst()) {
            contents.set(5, 0, ClickableItem.of(ItemUtil.previosPage, e
                -> contents.getHost().open(p, pagination.previous().getPage()))
            );
        }

        pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, SlotPos.of(0, 0)).allowOverride(false));


    }


    public static class SchemPasteMenu implements InventoryProvider {

        private final String schemName;

        public SchemPasteMenu(final String schemName) {
            this.schemName = schemName;
        }


        @Override
        public void init(final Player p, final InventoryContent contents) {

            p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, .5f, 1);
            //final SetupMode sm = PM.getOplayer(p).setup;

            //final Schematic schem = WE.getSchematic(p, schemName);
            // if (schem==null) {
            //     p.sendMessage("схематик=null!");
            //     return;
            //  }

            //schem.paste(p, p.getLocation(), true);
            //sm.setCuboid(p, new Cuboid (  p.getLocation(), schem.getCuboid().getSizeX(), schem.getCuboid().getSizeY(), schem.getCuboid().getSizeZ() ));
            //sm.openSchemEditMenu(p, schem.getName());
            //p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, .5f, 2);


            contents.set(0, ClickableItem.of(new ItemBuilder(Material.SPRUCE_SAPLING)
                .name("§bБез поворота")
                .lore("§eзагрузить, вставить и выделить")
                .lore("§7ЛКМ - §fc воздухом")
                .lore("§7ПКМ - §fпропускать воздух")
                .lore("")
                .lore("§5Отмена последней вставки")
                .lore("§5возможна в главном меню")
                .lore("§5выбора схематиков.")
                //.addLore("§5Убедитесь, что рядом нет")
                //.addLore("§5ценных построек,")
                //.addLore("§5действие не отменить.")
                .build(), e -> {
                if (e.isLeftClick()) {
                    paste(p, Rotate.r0, true);
                } else if (e.isRightClick()) {
                    paste(p, Rotate.r0, false);
                }
            }));


            contents.set(1, ClickableItem.of(new ItemBuilder(Material.BIRCH_SAPLING)
                .name("§bПовернуть на 90 град.")
                .lore("§eзагрузить, вставить и выделить")
                .lore("§7ЛКМ - §fc воздухом")
                .lore("§7ПКМ - §fпропускать воздух")
                .lore("")
                .lore("§5Отмена последней вставки")
                .lore("§5возможна в главном меню")
                .lore("§5выбора схематиков.")
                .build(), e -> {
                if (e.isLeftClick()) {
                    paste(p, Rotate.r90, true);
                } else if (e.isRightClick()) {
                    paste(p, Rotate.r90, false);
                }
            }));


            contents.set(2, ClickableItem.of(new ItemBuilder(Material.JUNGLE_SAPLING)
                .name("§bПовернуть на 180 град.")
                .lore("§eзагрузить, вставить и выделить")
                .lore("§7ЛКМ - §fc воздухом")
                .lore("§7ПКМ - §fпропускать воздух")
                .lore("")
                .lore("§5Отмена последней вставки")
                .lore("§5возможна в главном меню")
                .lore("§5выбора схематиков.")
                .build(), e -> {
                if (e.isLeftClick()) {
                    paste(p, Rotate.r180, true);
                } else if (e.isRightClick()) {
                    paste(p, Rotate.r180, false);
                }
            }));


            contents.set(3, ClickableItem.of(new ItemBuilder(Material.ACACIA_SAPLING)
                .name("§bПовернуть на 270 град.")
                .lore("§eзагрузить, вставить и выделить")
                .lore("§7ЛКМ - §fc воздухом")
                .lore("§7ПКМ - §fпропускать воздух")
                .lore("")
                .lore("§5Отмена последней вставки")
                .lore("§5возможна в главном меню")
                .lore("§5выбора схематиков.")
                .build(), e -> {
                if (e.isLeftClick()) {
                    paste(p, Rotate.r270, true);
                } else if (e.isRightClick()) {
                    paste(p, Rotate.r270, false);
                }
            }));


        }

        private void paste(final Player p, final Rotate rotate, final boolean pasteAir) {
            final Schematic schem = WE.getSchematic(p, schemName);
            if (schem == null) {
                p.sendMessage("схематик=null!");
                return;
            }
            final SetupMode sm = PM.getOplayer(p).setup;
            p.closeInventory();
            final Cuboid cuboid = schem.paste(p, BVec.of(p.getLocation()), rotate, pasteAir); //вставка начнётся через тик!
            sm.undo = new Schematic(null, p.getName() + "_undo", "", cuboid, p.getWorld(), false);
            sm.undoLoc = new WXYZ(p.getLocation());
            sm.setCuboid(p, cuboid);//checkPosition(p);
            sm.openSchemEditMenu(p, schem.getName());
            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, .5f, 2);
        }


    }


    public static class SchemCompareMenu implements InventoryProvider {

        private final String schemName;

        public SchemCompareMenu(final String schemName) {
            this.schemName = schemName;
        }


        @Override
        public void init(final Player p, final InventoryContent contents) {

            p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, .5f, 1);

            contents.set(0, ClickableItem.of(new ItemBuilder(Material.SPRUCE_SAPLING)
                .name("§bСравнить без поворота")
                .lore("§7ЛКМ - §fигнорировать воздух")
                .lore("§7ПКМ - §fучитывать воздух")
                .build(), e -> {
                if (e.isLeftClick()) {
                    compare(p, Rotate.r0, true);
                } else if (e.isRightClick()) {
                    compare(p, Rotate.r0, false);
                }
            }));


            contents.set(1, ClickableItem.of(new ItemBuilder(Material.BIRCH_SAPLING)
                .name("§bСравнить с поворотом на 90 град.")
                .lore("§7ЛКМ - §fигнорировать воздух")
                .lore("§7ПКМ - §fучитывать воздух")
                .build(), e -> {
                if (e.isLeftClick()) {
                    compare(p, Rotate.r90, true);
                } else if (e.isRightClick()) {
                    compare(p, Rotate.r90, false);
                }
            }));


            contents.set(2, ClickableItem.of(new ItemBuilder(Material.JUNGLE_SAPLING)
                .name("§bСравнить с поворотом на 180 град.")
                .lore("§7ЛКМ - §fигнорировать воздух")
                .lore("§7ПКМ - §fучитывать воздух")
                .build(), e -> {
                if (e.isLeftClick()) {
                    compare(p, Rotate.r180, true);
                } else if (e.isRightClick()) {
                    compare(p, Rotate.r180, false);
                }
            }));


            contents.set(3, ClickableItem.of(new ItemBuilder(Material.ACACIA_SAPLING)
                .name("§bСравнить с поворотом на 270 град.")
                .lore("§7ЛКМ - §fигнорировать воздух")
                .lore("§7ПКМ - §fучитывать воздух")
                .build(), e -> {
                if (e.isLeftClick()) {
                    compare(p, Rotate.r270, true);
                } else if (e.isRightClick()) {
                    compare(p, Rotate.r270, false);
                }
            }));


        }

        private void compare(final Player p, final Rotate rotate, final boolean ignoreAir) {
            final Schematic schem = WE.getSchematic(p, schemName);
            if (schem == null) {
                p.sendMessage("схематик=null!");
                return;
            }
            p.closeInventory();
            final Schematic.CompareResult cr = schem.compare(new WXYZ(p.getLocation()), rotate, ignoreAir);
            p.sendMessage("");
            p.sendMessage("§7============== §fРезультат сравнения §7================");
            p.sendMessage("§7Bремя: §5" + cr.ms + " мс.");
            p.sendMessage("§7Размер схематика §b: " + cr.cuboid.volume() + " §7(блоков: §6" + cr.blocksSize + "§7");
            if (cr.mustBe.isEmpty()) {
                p.sendMessage("§aПолное совпадение.");
            } else {
                p.sendMessage("§7Отличаются §c: " + cr.mustBe.size());
                Schematic.CompareResult.print(p, cr);
            }
            p.sendMessage("§7=====================================================");

            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, .5f, 2);
        }


    }


}
