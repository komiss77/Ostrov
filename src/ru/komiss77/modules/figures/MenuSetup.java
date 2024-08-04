package ru.komiss77.modules.figures;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.games.GM;
import ru.komiss77.modules.games.GameInfo;
import ru.komiss77.objects.Figure;
import ru.komiss77.objects.Figure.FigureType;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.TCUtil;
import ru.komiss77.utils.inventory.*;


public class MenuSetup implements InventoryProvider {


    private final Figure figure;


    public MenuSetup(final Figure figure) {
        this.figure = figure;
    }


    @Override
    public void init(final Player player, final InventoryContent contents) {
        player.playSound(player.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
        //contents.fillBorders(ClickableItem.empty(FigureMenu.fill));


        ItemBuilder builder = null;

        if (figure.getType() != null) {
            switch (figure.getType()) {
                case COMMAND:
                    builder = new ItemBuilder(Material.COMMAND_BLOCK);
                    builder.name("§fкоманда");
                    builder.lore("");
                    builder.lore("§7ЛКМ - сменить тип");
                    builder.lore("");
                    break;
                case COMMAND_CONFIRM:
                    builder = new ItemBuilder(Material.COMMAND_BLOCK_MINECART);
                    builder.name("§fкоманда с подтверждением");
                    builder.lore("");
                    builder.lore("§7ЛКМ - сменить тип");
                    builder.lore("");
                    break;
                case EVENT:
                    builder = new ItemBuilder(Material.FLETCHING_TABLE);
                    builder.name("§fэвент");
                    builder.lore("");
                    builder.lore("§7ЛКМ - сменить тип");
                    builder.lore("");
                    break;

                case SERVER:
                    if (figure.game != null && GM.getGameInfo(figure.game) != null) {
                        final GameInfo gi = GM.getGameInfo(figure.game); //в этом меню тип не будет пустой, не даст в FigureListener
                        builder = new ItemBuilder(Material.matchMaterial(gi.game.mat));
                        //builder.setType(gi.item.getType());
                        builder.name(gi.game.displayName);
                        builder.lore("");
                        builder.lore("§7ЛКМ - сменить тип");
                        builder.lore("");
                        builder.lore("");
                    }
                default:
                    break;
            }
        }

        if (builder == null) {
            builder = new ItemBuilder(Material.BARRIER);
            builder.name("§cНеизвестный тип");
        }

        contents.set(2, 4, ClickableItem.of(builder.build(), e -> {
            if (e.isLeftClick()) {
                SmartInventory.builder().id("TypeSelectMenu" + player.getName()).provider(new TypeSelectMenu(figure)).size(6, 9).title("§fВыбор типа").build().open(player);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 5);
            }
        }));

        //}


        contents.set(0, 4, new InputButton(InputButton.InputType.ANVILL, new ItemBuilder(Material.NAME_TAG)
            .name("§fНазвание")
            .lore("§7")
            .lore("§7Сейчас:")
            .lore("§e" + figure.name)
            .lore("§7")
            .lore("§fЛКМ - изменить")
            .lore("§7Можно использовать цвета с §f&")
            .lore("§7")
            .lore("§7Для фигур - команд имя")
            .lore("§7будет неизменно,")
            .lore("§7для фигур- -серверов")
            .lore("§7после названия будет добавлен")
            .lore("§7общий онлайн на игре.")
            .lore("§7")
            .lore("§7")

            .lore("§7")
            .build(), figure.name, msg -> {
            figure.setName(msg.replaceAll("&", "§"));
            FigureManager.saveFigure(player, figure);
            reopen(player, contents);
        }));


        if (figure.getType() == FigureType.EVENT) {
            contents.set(0, 5, new InputButton(InputButton.InputType.ANVILL, new ItemBuilder(Material.NAME_TAG)
                .name("§fТэг")
                .lore("§7")
                .lore("§7Сейчас:")
                .lore("§e" + figure.getTag())
                .lore("§7")
                .lore("§fЛКМ - изменить")
                .lore("§7цветовые коды будут обрезаны.")
                .lore("§7")
                .lore("§7Тэг никак не отображается,")
                .lore("§7но будет передан в Эвент")
                .lore("§7при клике на фигуру")
                .lore("§7(например, 'открыть меню прокачки')")
                .lore("§7и поможет плагину понять,")
                .lore("§7что нужно сделать.")
                .lore("§7")
                .build(), figure.getTag(), msg -> {
                figure.setTag(TCUtil.strip(msg));
                FigureManager.saveFigure(player, figure);
                reopen(player, contents);
            }));
        }


        if (figure.getType() == FigureType.COMMAND || figure.getType() == FigureType.COMMAND_CONFIRM) {

            contents.set(1, 1, new InputButton(InputButton.InputType.ANVILL, new ItemBuilder(Material.FLOWER_BANNER_PATTERN)
                .name("§fКоманда при ЛКМ")
                .lore("§7")
                .lore("§7При ЛКМ на фигуру игроком")
                .lore(figure.getType() == FigureType.COMMAND_CONFIRM ? "§7команда с подтверждением:" : "§7будет выполнена команда:")
                .lore("§e" + figure.leftclickcommand)
                .lore("§7")
                .lore("§fЛКМ - изменить")
                .lore("§7Можно использовать цвета с §f&")
                .lore("§7")
                .lore("§7Возможные переменные:")
                .lore("§f@p §7заменится на ник игрока.")
                .lore("§7")
                .lore("§7Если команда начинается с §f@c")
                .lore("§7команда будет выполнена от имени")
                .lore("§7консоли.")
                .lore("§7")
                .build(), figure.leftclickcommand, msg -> {
                if (msg.contains("bossbar ") || msg.contains("op ")) {
                    player.kick(TCUtil.form("В следующий раз вылет с должности"));
                    Ostrov.log_err("Попытка ОП через фигуру: " + player.getName());
                } else {
                    figure.leftclickcommand = msg;
                    FigureManager.saveFigure(player, figure);
                    reopen(player, contents);
                }
            }));

            contents.set(1, 7, new InputButton(InputButton.InputType.ANVILL, new ItemBuilder(Material.FLOWER_BANNER_PATTERN)
                .name("§fКоманда при ПКМ")
                .lore("§7")
                .lore("§7При ПКМ на фигуру игроком")
                .lore(figure.getType() == FigureType.COMMAND_CONFIRM ? "§7команда с подтверждением:" : "§7будет выполнена команда:")
                .lore("§e" + figure.rightclickcommand)
                .lore("§7")
                .lore("§fЛКМ - изменить")
                .lore("§7Можно использовать цвета с §f&")
                .lore("§7")
                .lore("§7Возможные переменные:")
                .lore("§f@p §7заменится на ник игрока.")
                .lore("§7")
                .lore("§7Если команда начинается с §f@c")
                .lore("§7команда будет выполнена от имени")
                .lore("§7консоли.")
                .lore("§7")
                .build(), figure.rightclickcommand, msg -> {
                if (msg.contains("bossbar ") || msg.contains("op ")) {
                    player.kick(TCUtil.form("В следующий раз вылет с должности"));
                    Ostrov.log_err("Попытка ОП через фигуру: " + player.getName());
                } else {
                    figure.rightclickcommand = msg;
                    FigureManager.saveFigure(player, figure);
                    reopen(player, contents);
                }
            }));

        }


        contents.set(4, 2, ClickableItem.of(new ItemBuilder(Material.ENDER_PEARL)
            .name("§7Поставить на новое место")
            .lore("§7")
            .lore("§7фигура переместится на вашу")
            .lore("§7позицию и сохранится.")
            .lore("§7")
            .lore("§4Шифт + ЛКМ - переместить")
            .lore("§7")
            .build(), e -> {
            if (e.getClick() == ClickType.SHIFT_LEFT) {
                FigureManager.setNewPosition(player, figure);
            }
        }));


        contents.set(4, 8, ClickableItem.of(new ItemBuilder(Material.SOUL_SAND)
            .name("§cУдалить обработчик фигуры")
            .lore("§7")
            .lore("§cУдалится только обработчик")
            .lore("§7фигуры, стойка останется!")
            .lore("§7")
            .lore("§4Шифт + ПКМ - удалить")
            .lore("§7")
            .build(), e -> {
            if (e.getClick() == ClickType.SHIFT_RIGHT) {
                ConfirmationGUI.open(player, "§4Удалить обработчик ?", result -> {
                    player.closeInventory();
                    if (result) {
                        player.playSound(player.getLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1, 5);
                        FigureManager.deleteFigure(figure);
                    } else {
                        player.playSound(player.getLocation(), Sound.ENTITY_LEASH_KNOT_PLACE, 0.5f, 0.85f);
                    }
                });
            }
        }));
    
        
            
        
        /*

        if (figure.changed) {
            contents.set(5, 7, ClickableItem.of(new ItemBuilder(Material.JUKEBOX)
                .name("§aСохранить изменения")
                .addLore("§7")
                .addLore("§7Вы внесли изменения,")
                .addLore("§7рекомендуется сохранение.")
                .addLore("§7")
                .addLore("§cБез сохранения все изменения будут")
                .addLore("§cутеряны после перезагрузки сервера!")
                .addLore("§7")
                .build(), e -> {
                    FigureManager.saveFigure(player, figure);
                    reopen(player, contents);
                }));
        }*/


    }


}
