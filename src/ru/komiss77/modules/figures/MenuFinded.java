package ru.komiss77.modules.figures;

import java.util.ArrayList;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import ru.komiss77.modules.games.GM;
import ru.komiss77.modules.games.GameInfo;
import ru.komiss77.objects.Figure;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.ConfirmationGUI;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.Pagination;
import ru.komiss77.utils.inventory.SlotIterator;
import ru.komiss77.utils.inventory.SlotPos;
import ru.komiss77.utils.inventory.SmartInventory;


public class MenuFinded implements InventoryProvider {


    private final Set<Figure> figures;


    public MenuFinded(final Set<Figure> figures) {
        this.figures = figures;
    }


    @Override
    public void init(final Player p, final InventoryContent contents) {
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
        //contents.fillBorders(ClickableItem.empty(TypeSelectMenu.fill));
        final Pagination pagination = contents.pagination();


        final ArrayList<ClickableItem> menuEntry = new ArrayList<>();


        if (figures.isEmpty()) {
            contents.set(1, 4, ClickableItem.empty(new ItemBuilder(Material.GLASS_BOTTLE)
                .name("§7Ничего не найдено!")
                .build()));

        }


        //Entity entity;

        for (final Figure figure : figures) {
            //final Figure figure = FigureManager.getFigure(id);
            if (figure == null) continue;
            //final Entity entity = figure.entityLink==null ? null : figure.entityLink.get();
            ItemBuilder builder;

//System.out.println("MenuFinded fig="+figure.name+" type="+figure.type+" game="+figure.getGame());           // if (figure.getType()!=null) {
            switch (figure.getType()) {

                case SERVER -> {
                    if (figure.game == null) {
                        builder = new ItemBuilder(Material.BARRIER);
                        builder.name("§cнет игры " + figure.getName());
                        break;
                    }
                    final GameInfo gi = GM.getGameInfo(figure.game);
                    if (gi == null) {
                        builder = new ItemBuilder(Material.matchMaterial(figure.game.mat));
                        builder.name(figure.game.displayName);
                        builder.lore("§cИнформация по игре не загружена");
                        builder.lore("");
                        break;
                    }
                    builder = new ItemBuilder(Material.matchMaterial(figure.game.mat));
                    builder.name(figure.game.displayName);
                    builder.lore("");
                    builder.lore("");
                    builder.lore("");
                }

                case COMMAND -> {
                    builder = new ItemBuilder(Material.COMMAND_BLOCK);
                    builder.lore("");
                    builder.lore("§7ИД: " + figure.getId());
                    builder.lore("§7Имя: " + figure.getName());
                    builder.name("§7Реакция: §fкоманда");
                    builder.lore("§7ЛКМ: §e" + figure.leftclickcommand);
                    builder.lore("§7ПКМ: §e" + figure.rightclickcommand);
                }

                case EVENT -> {
                    builder = new ItemBuilder(Material.FLETCHING_TABLE);
                    builder.lore("");
                    builder.lore("§7ИД: " + figure.getId());
                    builder.lore("§7Имя: " + figure.getName());
                    builder.name("§7Реакция: §fэвент");
                    builder.lore("§7Тэг: §e" + figure.getTag());
                }

                default -> {
                    builder = new ItemBuilder(Material.BARRIER);
                    builder.name("§4тип не определён");
                }
            }

            builder.lore("");
            builder.lore("§7Сущность: §5" + figure.getEntityType());

            final Location loc = figure.getSpawnLocation();

            if (loc == null) {

                builder.lore("§cЛокация недоступна!");

            } else if (!loc.getChunk().isLoaded() || !loc.getChunk().isEntitiesLoaded()) {

                builder.lore(loc.getChunk().isLoaded() ? "" : "§cЧанк фигуры выгружен");
                builder.lore(loc.getChunk().isEntitiesLoaded() ? "" : "§cЭнтити чанка выгружены");
                builder.lore("§eфигура неактивна.");
                builder.lore("§7ЛКМ - ТП к в локацию (чанк загрузится)");

            } else {

                //builder.addLore(loc==null ? "§cЛокация недоступна!" : "§7ЛКМ - ТП к фигуре");
                builder.lore(figure.entity == null ? "" : "§7Шифт+ЛКМ - поставить 'к ноге'");
                builder.lore("");
                builder.lore(figure.entity == null ? "§cне заспавнена" : "§7заспавнена, entityId = " + figure.entity.getEntityId());
                builder.lore(figure.entity == null ? "" : figure.entity.isDead() ? "§cдохлая" : "§aживая");
                builder.lore(figure.entity == null ? "" : figure.isInSameBlock(loc) ? "§aна локации" : "§cне на локации");
                builder.lore(loc.getBlock().getType().isSolid() && loc.getBlock().getRelative(BlockFace.UP).getType().isSolid() ? "§cзамурована" : "§aне замурована");
                //builder.addLore(figure.isValid() ? "§aПроблем не найдено":"§cНайдены проблемы!" );

            }

            builder.lore("");
            builder.lore("§7ПКМ - меню фигуры");
            builder.lore("§7Клав.Q - §cудалить обработчик");
            builder.lore("");


            menuEntry.add(ClickableItem.of(builder.build(), e -> {

                switch (e.getClick()) {
                    case LEFT -> {
                        //System.out.println("MenuFinded.teleport loc="+figure.getLocation());
                        if (loc != null) {
                            p.teleport(figure.getSpawnLocation());
                        }
                    }
                    case SHIFT_LEFT -> //System.out.println("MenuFinded.teleport loc="+figure.getLocation());
                        FigureManager.setNewPosition(p, figure);

                    case RIGHT ->
                        SmartInventory.builder().id("FigureMenu" + p.getName()).provider(new MenuSetup(figure)).size(6, 9).title("§fНастройка фигуры").build().open(p);

                    case DROP -> ConfirmationGUI.open(p, "§4Удалить обработчик ?", result -> {
                        if (result) {
                            p.playSound(p.getLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1, 5);
                            figures.remove(figure);
                            FigureManager.deleteFigure(figure);
                            reopen(p, contents);
                        } else {
                          p.playSound(p.getLocation(), Sound.ENTITY_CAT_HISS, 0.5f, 0.85f);
                        }
                        reopen(p, contents);
                    });

                    default -> {
                    }

                }


            }));

        }


        //addEntry (menuEntry, "command", "§акоманда", Material.COMMAND_BLOCK);


        pagination.setItems(menuEntry.toArray(new ClickableItem[menuEntry.size()]));
        pagination.setItemsPerPage(45);


        contents.set(5, 4, ClickableItem.of(new ItemBuilder(Material.OAK_DOOR).name("назад").build(), e ->
            p.performCommand("figure")
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


}
