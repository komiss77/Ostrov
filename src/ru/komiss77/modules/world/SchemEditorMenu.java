package ru.komiss77.modules.world;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.builder.SetupMode;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.inventory.*;


public class SchemEditorMenu implements InventoryProvider {

    public static final ItemStack fill = new ItemBuilder(Material.GREEN_STAINED_GLASS_PANE).build();
    ;

    // private final String schemName;// private final String schemName;


    public SchemEditorMenu() {
        //this.schemName = schemName;
    }


    @Override
    public void init(final Player p, final InventoryContent contents) {

        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);

        // contents.fillRect(SlotPos.of(0), SlotPos.of(53), ClickableItem.empty(fill));
        //contents.fillRow(4, ClickableItem.empty(fill));

        final SetupMode sm = PM.getOplayer(p).setup;

        //SetupManager.setPosition(p, style.getPos1(p.getWorld().getName()), style.getPos2(p.getWorld().getName()));

        if (sm.getCuboid() == null) {
            contents.set(0, 4, ClickableItem.empty(new ItemBuilder(Material.PAPER)
                .name("§7Схематик §f" + sm.schemName)
                .lore("")
                .lore("§7Создайте кубоид точками диагоналей.")

                .lore("")
                .build()));
        } else {
            contents.set(0, 4, ClickableItem.empty(new ItemBuilder(Material.PAPER)
                .name("§7Схематик §f" + sm.schemName)
                .lore("")
                .lore("§7Размер: §b" + sm.getCuboid().sizeX() + "§7x§b" + sm.getCuboid().sizeY() + "§7x§b" + sm.getCuboid().sizeZ())
                //.addLore("§7Размер: §b"+(sm.getCuboid().getSizeX()+1)+"§7x§b"+(sm.getCuboid().getSizeY()+1)+"§7x§b"+(sm.getCuboid().getSizeZ()+1))
                .lore("§7Полный Объём: §e" + sm.getCuboid().volume())
                //сколько блоков в схематике с воздухом или без ?
                .lore("")
                .build()));
        }


        final boolean selected = sm.min != null && sm.max != null
            && sm.min.getWorld().getUID().equals(sm.max.getWorld().getUID())
            && p.getWorld().getUID().equals(sm.min.getWorld().getUID());


        if (selected) contents.fillRect(1, 1, 4, 4, ClickableItem.empty(fill));


        if (sm.max == null) {
            contents.set(1, 4, ClickableItem.of(new ItemBuilder(Material.BARRIER)
                .name("§7Верхняя точка кубоида")
                .lore("§7")
                .lore("§7Клик - установить")
                .build(), e -> {
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 5);
                sm.max = p.getLocation().getBlock().getRelative(BlockFace.DOWN).getLocation(); //блок под ногами, или получается на 1 выше чем стоишь
                sm.checkPosition(p);
                //p.sendBlockChange(p.getLocation(), Material.EMERALD_BLOCK.createBlockData());
                reopen(p, contents);
            }));
        } else {
            //p.sendBlockChange(sm.pos2, Material.EMERALD_BLOCK.createBlockData());
            contents.set(1, 4, ClickableItem.of(new ItemBuilder(Material.OAK_FENCE)
                .name("§7Верхняя точка кубоида")
                .lore("§7")
                .lore("§7ЛКМ - установить")
                .lore("§7ПКМ - тп")
                .build(), e -> {
                if (e.isRightClick()) {
                    p.teleport(sm.max.clone().add(0.5, 1, 0.5)); //тп над блоком, чтобы выделение было под ногами
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 1, 5);
                } else if (e.isLeftClick()) {
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 5);
                    //p.sendBlockChange(sm.pos2, Material.AIR.createBlockData());
                    sm.max = p.getLocation().getBlock().getRelative(BlockFace.DOWN).getLocation(); //блок под ногами, или получается на 1 выше чем стоишь
                    sm.checkPosition(p);
                    //p.sendBlockChange(sm.pos2, Material.EMERALD_BLOCK.createBlockData());
                    reopen(p, contents);
                }
            }));
        }


        if (selected) {

            contents.set(2, 3, ClickableItem.of(new ItemBuilder(Material.SHULKER_SHELL)
                .name("§7Повернуть на 90град")
                .lore("§7ЛКМ - повернуть сетку")
                .lore("§7ПКМ - повернуть с содержимым")
                .build(), e -> {
                if (e.getClick() == ClickType.LEFT) {
                    sm.rotate(p, Schematic.Rotate.r90, false);
                } else if (e.getClick() == ClickType.RIGHT) {
                    sm.rotate(p, Schematic.Rotate.r90, true);
                }
            }));

            contents.set(3, 3, ClickableItem.of(new ItemBuilder(Material.SHULKER_SHELL)
                .name("§7Повернуть на 180град")
                .lore("§7ЛКМ - повернуть сетку")
                .lore("§7ПКМ - повернуть с содержимым")
                .build(), e -> {
                if (e.getClick() == ClickType.LEFT) {
                    sm.rotate(p, Schematic.Rotate.r180, false);
                } else if (e.getClick() == ClickType.RIGHT) {
                    sm.rotate(p, Schematic.Rotate.r180, true);
                }
            }));

            contents.set(3, 2, ClickableItem.of(new ItemBuilder(Material.SHULKER_SHELL)
                .name("§7Повернуть на 270град")
                .lore("§7ЛКМ - повернуть сетку")
                .lore("§7ПКМ - повернуть с содержимым")
                .build(), e -> {
                if (e.getClick() == ClickType.LEFT) {
                    sm.rotate(p, Schematic.Rotate.r270, false);
                } else if (e.getClick() == ClickType.RIGHT) {
                    sm.rotate(p, Schematic.Rotate.r270, true);
                }
            }));


//Ostrov.log(sm.spawnPoint==null ? "spawnPoint=null" : "contains?"+sm.cuboid.contains(sm.spawnPoint));
            if (sm.spawnPoint != null && !sm.cuboid.contains(sm.spawnPoint)) {
                sm.spawnPoint = null; //принудительный сброс если изменил кубоид
                p.sendMessage("§cточка спавна схематика сброшена: за пределами кубоида.");
            }

            if (sm.spawnPoint == null) {

                contents.set(1, 6, ClickableItem.of(new ItemBuilder(Material.BARRIER)
                    .name("§7Точка спавна кубоида")
                    .lore("§7(не обязательно)")
                    .lore("§cне установлена")
                    .lore("§7Клик - установить")
                    .build(), e -> {
                    if (!sm.cuboid.contains(p.getLocation())) {
                        p.sendMessage("§cДля установки точки надо быть в кубоиде");
                        PM.soundDeny(p);
                    } else {
                        sm.setSpawn(p);
                        reopen(p, contents);
                    }
                }));

            } else {

                contents.set(1, 6, ClickableItem.of(new ItemBuilder(Material.ENDER_EYE)
                    .name("§7Точка спавна кубоида")
                    .lore("§7(не обязательно)")
                    .lore("§7ЛКМ - установить")
                    .lore("§7ПКМ - тп")
                    .build(), e -> {
                    if (e.isRightClick()) {
                        p.teleport(sm.spawnPoint.clone().add(0.5, 0, 0.5));
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 1, 5);
                    } else if (e.isLeftClick()) {
                        sm.setSpawn(p);
                        reopen(p, contents);
                    } else {
                        PM.soundDeny(p);
                    }
                }));

                //кнопка тест сравнения с местностью с засечкой времени
            }

        }


        if (sm.min == null) {
            contents.set(4, 1, ClickableItem.of(new ItemBuilder(Material.BARRIER)
                .name("§7Нижняя точка кубоида")
                .lore("§7")
                .lore("§7Клик - установить")
                .build(), e -> {
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 5);
                sm.min = p.getLocation();
                sm.checkPosition(p);
                //p.sendBlockChange(p.getLocation(), Material.EMERALD_BLOCK.createBlockData());
                reopen(p, contents);
            }));
        } else {
            //p.sendBlockChange(style.getPos1(p.getWorld().getName()), Material.EMERALD_BLOCK.createBlockData());
            contents.set(4, 1, ClickableItem.of(new ItemBuilder(Material.OAK_FENCE)
                .name("§7Нижняя точка кубоида")
                .lore("§7")
                .lore("§7ЛКМ - установить")
                .lore("§7ПКМ - тп")
                .build(), e -> {
                if (e.isRightClick()) {
                    p.teleport(sm.min);
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 1, 5);
                } else if (e.isLeftClick()) {
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 5);
                    //p.sendBlockChange(sm.pos1, Material.AIR.createBlockData());
                    sm.min = p.getLocation();
                    sm.checkPosition(p);
                    //p.sendBlockChange(sm.pos1, Material.EMERALD_BLOCK.createBlockData());
                    reopen(p, contents);
                }
            }));
        }


        contents.set(2, 6, new InputButton(InputButton.InputType.ANVILL, new ItemBuilder(Material.ACACIA_SIGN)
            .name("§fРедактировать строку параметров")
            .lore("§7Сейчас:")
            .lore(sm.param)
            .build(), "параметры", param -> {
            sm.param = param;
            reopen(p, contents);
        }));
        

        
        
        
 
        
        
        
        
     
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        


/*
        contents.set(5, 1, ClickableItem.of( new ItemBuilder(Material.WOODEN_AXE)
            .name("§7Вставить")
            .addLore("§6Вставить заготовку и наполнить сундуки")
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

        if (sm.undo != null) {
            contents.set(5, 0, ClickableItem.of(new ItemBuilder(Material.NAUTILUS_SHELL)
                .name("§aОтмена последней вставки")
                .lore("§7")
                .lore("§7ЛКМ - отменить")
                .lore("§7")
                .build(), e -> {
                if (e.isLeftClick()) {
                    p.closeInventory();
                    sm.undo.paste(p, sm.undoLoc, Schematic.Rotate.r0, true);
                    sm.undo = null;
                    sm.undoLoc = null;
                    //sm.undoRotate = null;
                }
            }));
        }

        if (selected) {

            contents.set(5, 2, ClickableItem.of(new ItemBuilder(Material.STONECUTTER)
                .name("§7Очистить выделение")
                .lore("§6Стирает всё в выделенной области")
                .build(), e -> {

                p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1, 5);
                ConfirmationGUI.open(p, "§4Стереть ?", result -> {
                    if (result) {
                        sm.clearArea();
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 5);
                    } else {
                        reopen(p, contents);
                      p.playSound(p.getLocation(), Sound.ENTITY_CAT_HISS, 0.5f, 0.85f);
                    }
                });
                //reopen(p, contents);
            }));


            contents.set(5, 6, ClickableItem.of(new ItemBuilder(Material.JUKEBOX)
                .name("§2Сохранить")
                .build(), e -> {

                if (sm.getCuboid().volume() > 8_000_000 || sm.getCuboid().sizeX() > 400 || sm.getCuboid().sizeY() > 400 || sm.getCuboid().sizeZ() > 400) {
                    p.sendMessage("§cВыделение слишком большое! Объём не более 8млн блоков, каждая сторона не более 400!");
                    PM.soundDeny(p);
                    return;
                }
                if (sm.spawnPoint != null && !sm.cuboid.contains(sm.spawnPoint)) {
                    p.sendMessage("§cточка за пределами кубоида.");
                    PM.soundDeny(p);
                    return;
                }

                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 5);
                WE.save(p, sm);

            }));


        }


        contents.set(5, 4, ClickableItem.of(new ItemBuilder(Material.OAK_DOOR).name("назад").build(), e
            -> sm.openSchemMainMenu(p)
        ));


    }


}
