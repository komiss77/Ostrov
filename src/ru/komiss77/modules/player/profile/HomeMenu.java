package ru.komiss77.modules.player.profile;

import org.bukkit.DyeColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import ru.komiss77.Cfg;
import ru.komiss77.modules.DelayTeleport;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.player.Perm;
import ru.komiss77.modules.translate.Lang;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.LocUtil;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InputButton;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;


public class HomeMenu implements InventoryProvider {

    private final Oplayer op;
    private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Section.ВОЗМОЖНОСТИ.glassMat).name("§8.").build());

    public HomeMenu(final Oplayer op) {
        this.op = op;
    }

    @Override
    public void init(final Player p, final InventoryContent content) {
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);

        //линия - разделитель
        content.fillRow(1, fill);

        //выставить иконки внизу
        for (Section section : Section.values()) {
          content.set(2, section.column, Section.getMenuItem(section, op));
        }

        if (!Cfg.home_command) {
            content.set(4, ClickableItem.empty(new ItemBuilder(ItemType.TRIAL_KEY)
                .name(Lang.t(p, "§cДома отключены на этом сервере!"))
                .build()));
            return;
        }

        for (final String homeName : op.homes.keySet()) {

            final ItemStack homeIcon = new ItemBuilder(ItemType.GRAY_BED)
                .name(homeName)
                .lore("")
                .lore("§7ЛКМ - §aперейти")
                .lore("§7Шифт+ПКМ - §6переустановить")
                .lore("§7Клав.Q - §cУдалить дом")
                .lore("")
                .build();

            content.add(ClickableItem.of(homeIcon, e -> {
                    switch (e.getClick()) {
                        case LEFT -> {
                            p.closeInventory();
                            DelayTeleport.tp(p, LocUtil.stringToLoc(op.homes.get(homeName), false, false),
                                3, "§2Дом, милый дом", true, true, DyeColor.GREEN);
                        }
                        case SHIFT_RIGHT -> {
                            p.closeInventory();
                            op.homes.put(homeName, LocUtil.toString(p.getLocation()));//PM.OP_SetHome(p, home);
                            op.mysqlData.put("homes", null); //пометить на сохранение
                            p.sendMessage("§2" + Lang.t(p, "Установлена новая позиция для дома ") + homeName);
                        }
                        case DROP -> {
                            op.homes.remove(homeName);
                            op.mysqlData.put("homes", null); //пометить на сохранение
                            p.sendMessage("§4" + Lang.t(p, "Точка дома удалена!"));
                            reopen(p, content);
                        }
                        default -> {
                        }
                    }
                }
            ));
        }


        final int limit = Perm.getLimit(op, "home");

        if (op.homes.size() >= limit) {

            content.add(ClickableItem.empty(new ItemBuilder(ItemType.TRIAL_KEY)
                .name("§cДобавить дом")
                .lore("")
                .lore("§cВы не можете добавть")
                .lore("§cновые дома:")
                .lore("§7домов: §5" + op.homes.size() + "§7, лимит: §6" + limit)
                .build()
            ));

        } else {

            content.add(new InputButton(InputButton.InputType.ANVILL, new ItemBuilder(ItemUtil.add)
                .name("§7Добавить дом")
                .build(), "название", newName -> {

                p.closeInventory();
                op.homes.put(newName, LocUtil.toString(p.getLocation()));//PM.OP_SetHome(p, home);
                op.mysqlData.put("homes", null); //пометить на сохранение
                //if (home.equals("home")) p.setBedSpawnLocation(p.getLocation());
                p.sendMessage("§2" + Lang.t(p, "Дом ") + ((newName.equals("home")) ? "" : newName) + Lang.t(p, " установлен!"));
            }));

        }
    }

    @Override
    public void onClose(final Player p, final InventoryContent content) {
        PM.getOplayer(p).menu.current = null;
    }

}