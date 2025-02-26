package ru.komiss77.modules.signProtect;

import org.bukkit.Sound;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import ru.komiss77.ApiOstrov;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.world.BVec;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.LocUtil;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;


public class AccesEdit implements InventoryProvider {

    private static final ItemStack fill = new ItemBuilder(ItemType.BLACK_STAINED_GLASS_PANE).name("§8.").build();
    private static final int USER_DST = 30;


    private final Sign sign;
    private final ProtectionData pd;


    public AccesEdit(final Sign sign, final ProtectionData pd) {
        this.sign = sign;
        this.pd = pd;
    }


    @Override
    public void init(final Player p, final InventoryContent contents) {
        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_BAMBOO_WOOD_PRESSURE_PLATE_CLICK_ON, 5, 0.5f);
        contents.fillBorders(ClickableItem.empty(AccesEdit.fill));
        contents.fillRow(3, ClickableItem.empty(AccesEdit.fill));


        for (final String name : pd.users) {

            contents.add(ClickableItem.of(
                new ItemBuilder(ItemType.PLAYER_HEAD)
                    .name(name)
                    .lore("§7")
                    .lore("§7ЛКМ - §cУдалить")
                    .lore("§7")
                    .build(), e -> {
                    if (e.isLeftClick() && pd.users.remove(name)) {
                        SignProtect.updateSign(sign, pd);
                        reopen(p, contents);
                    }
                }
            ));

        }

        if (pd.users.size() < 14) {
            contents.add(ClickableItem.of(
                new ItemBuilder(ItemType.PLAYER_HEAD)
                    .name("§aДобавить")
                    .lore("")
                    .lore("§2Разрешить доступ игроку рядом")
                    .headTexture(ItemUtil.Texture.add)
                    .build(), e -> {
                    if (!e.isLeftClick()) return;
                    final Player find = LocUtil.getNearPl(BVec.of(p), USER_DST,
                        pl -> p.getEntityId() != pl.getEntityId() && !pd.users.contains(pl.getName()));
                    if (find == null) {
                        p.sendMessage("§6Рядом никого не найдено!");
                        return;
                    }
                    pd.users.add(find.getName());// - List.of immutebleб в него не добавить!!
                    SignProtect.updateSign(sign, pd);
                    reopen(p, contents);
                }
            ));
        }


        if (ApiOstrov.isLocalBuilder(p, false) && pd.valid > 0) {

            contents.set(4, 1, ClickableItem.of(new ItemBuilder(ItemType.FIREWORK_ROCKET)
                    .name("§bПометить постоянным")
                    .lore("§7")
                    .lore("§7ЛКМ - бессрочно")
                    .lore("§7")
                    .build(), e -> {
                    if (e.isLeftClick()) {
                        pd.valid = -1;
                        SignProtect.updateSign(sign, pd);
                        reopen(p, contents);
                    }
                }
            ));

        }

        final Oplayer op = PM.getOplayer(p);
        int curr = 1;
        if (op.mysqlData.containsKey("signProtect")) {
            curr = Integer.parseInt(op.mysqlData.get("signProtect"));
        }

        contents.set(4, 4, ClickableItem.empty(new ItemBuilder(ItemType.PAPER)
            .name("§bЛимит табличек")
            .lore("§7")
            .lore("§7Найдено активных: " + curr)
            .lore("§7Можно поставить: " + (SignProtect.LIMIT - curr))
            .build()
        ));


        contents.set(4, 7, ClickableItem.of(new ItemBuilder(ItemType.OAK_DOOR).name("закрыть").build(), e ->
            p.closeInventory()
        ));


    }


}



