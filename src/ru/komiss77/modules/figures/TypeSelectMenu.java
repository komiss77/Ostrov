package ru.komiss77.modules.figures;

import java.util.ArrayList;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import ru.komiss77.modules.games.GM;
import ru.komiss77.modules.games.GameInfo;
import ru.komiss77.objects.Figure;
import ru.komiss77.objects.Figure.FigureType;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.Pagination;
import ru.komiss77.utils.inventory.SlotIterator;
import ru.komiss77.utils.inventory.SlotPos;
import ru.komiss77.utils.inventory.SmartInventory;


public class TypeSelectMenu implements InventoryProvider {


    private final Figure figure;


    public TypeSelectMenu(final Figure figure) {
        this.figure = figure;
    }


    @Override
    public void init(final Player player, final InventoryContent contents) {
        player.playSound(player.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
        //contents.fillBorders(ClickableItem.empty(TypeSelectMenu.fill));
        final Pagination pagination = contents.pagination();


        final ArrayList<ClickableItem> menuEntry = new ArrayList<>();


        menuEntry.add(ClickableItem.of(new ItemBuilder(Material.COMMAND_BLOCK)
            .name("§fкоманда")
            .lore("")
            .lore("§7При ЛКМ и ПКМ на фигуру")
            .lore("§7будут выполняться команды.")
            .lore("")
            .build(), e -> {
            if (e.isLeftClick()) {
                figure.setType(FigureType.COMMAND, null);
                FigureManager.saveFigure(player, figure);
                SmartInventory.builder().id("FigureMenu" + player.getName()).provider(new MenuSetup(figure)).size(6, 9).title("§fНастройка фигуры").build().open(player);
            }

        }));
        menuEntry.add(ClickableItem.of(new ItemBuilder(Material.COMMAND_BLOCK_MINECART)
            .name("§fкоманда с подтверждением")
            .lore("")
            .lore("§7При ЛКМ и ПКМ на фигуру")
            .lore("§7будут выполняться команды.")
            .lore("§7с меню подтверждения действия.")
            .lore("§7Полезно для телепортов, удалений")
            .lore("┘и т.д.")
            .lore("")
            .build(), e -> {
            if (e.isLeftClick()) {
                figure.setType(FigureType.COMMAND_CONFIRM, null);
                FigureManager.saveFigure(player, figure);
                SmartInventory.builder().id("FigureMenu" + player.getName()).provider(new MenuSetup(figure)).size(6, 9).title("§fНастройка фигуры").build().open(player);
            }

        }));

        menuEntry.add(ClickableItem.of(new ItemBuilder(Material.FLETCHING_TABLE)
            .name("§fэвент")
            .lore("")
            .lore("§7При ЛКМ и ПКМ на фигуру")
            .lore("§7будет вызываться FigureClickEvent.")
            .lore("")
            .build(), e -> {
            if (e.isLeftClick()) {
                figure.setType(FigureType.EVENT, null);
                FigureManager.saveFigure(player, figure);
                SmartInventory.builder().id("FigureMenu" + player.getName()).provider(new MenuSetup(figure)).size(6, 9).title("§fНастройка фигуры").build().open(player);
            }

        }));


        if (GM.getGames().isEmpty()) {

            menuEntry.add(ClickableItem.empty(new ItemBuilder(Material.GLASS_BOTTLE)
                .name("§fТаблица серверов пуста!")
                .lore("§7Скорее всего, в настройках Острова")
                .lore("§7выключена загрузка данных серверов.")
                .build()));

        } else {

            for (final GameInfo gi : GM.getGames()) {
                menuEntry.add(ClickableItem.of(new ItemBuilder(Material.matchMaterial(gi.game.mat))
                    .name("§f" + gi.game.displayName)
                    .lore("")
                    .lore(gi.game.description)
                    .lore("")
                    .build(), e -> {
                    if (e.isLeftClick()) {
                        figure.setType(FigureType.SERVER, gi.game);
                        FigureManager.saveFigure(player, figure);
                        SmartInventory.builder().id("FigureMenu" + player.getName()).provider(new MenuSetup(figure)).size(6, 9).title("§fНастройка фигуры").build().open(player);
                    }

                }));
            }

        }

        //addEntry (menuEntry, "command", "§акоманда", Material.COMMAND_BLOCK);


        pagination.setItems(menuEntry.toArray(new ClickableItem[menuEntry.size()]));
        pagination.setItemsPerPage(45);


        contents.set(5, 4, ClickableItem.of(new ItemBuilder(Material.OAK_DOOR).name("закрыть").build(), e ->
            player.closeInventory()
        ));


        if (!pagination.isLast()) {
            contents.set(5, 8, ClickableItem.of(ItemUtil.nextPage, e
                -> contents.getHost().open(player, pagination.next().getPage()))
            );
        }

        if (!pagination.isFirst()) {
            contents.set(5, 0, ClickableItem.of(ItemUtil.previosPage, e
                -> contents.getHost().open(player, pagination.previous().getPage()))
            );
        }

        pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, SlotPos.of(0, 0)).allowOverride(false));


    }


}
