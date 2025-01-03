package ru.komiss77.builder.menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.NumUtil;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InputButton;
import ru.komiss77.utils.inventory.InputButton.InputType;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;


public class CustomModelData implements InventoryProvider {


    private int page;
    private final Material mat;

    public CustomModelData(final int page, final Material mat) {
        this.page = page;
        this.mat = mat;
    }


    @Override
    public void init(final Player p, final InventoryContent content) {

        int from = page * 44;
        int to = page * 44 + 45;

        for (int i = from; i < to; i++) {

            content.add(ClickableItem.empty(new ItemBuilder(mat)
                .name("§7" + i)
                .modelData(i)
                .build()
            ));

        }


        content.set(5, 4, ClickableItem.of(new ItemBuilder(Material.OAK_DOOR).name("закрыть").build(), e ->
            p.closeInventory()
        ));

        content.set(5, 2, new InputButton(InputType.ANVILL, new ItemBuilder(Material.FLOWER_BANNER_PATTERN)
            .name("§7Перейти к ИД ....")
            .build(), "" + from, input -> {

            if (!NumUtil.isInt(input)) {
                p.sendMessage("§cДолжно быть число!");
                return;
            }
            final int r = Integer.parseInt(input);
            if (r < 0) {
                p.sendMessage("§cот 0 до " + Integer.MAX_VALUE);
                return;
            }
            page = r / 44;
            reopen(p, content);
        }));


        if (to < Integer.MAX_VALUE) {
            content.set(5, 8, ClickableItem.of(ItemUtil.nextPage, e
                    -> {
                    page++;
                    reopen(p, content);
                })
            );
        }

        if (page > 0) {
            content.set(5, 0, ClickableItem.of(ItemUtil.previosPage, e
                    -> {
                    page--;
                    reopen(p, content);
                })
            );
        }


    }


}
