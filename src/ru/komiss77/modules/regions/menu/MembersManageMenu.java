package ru.komiss77.modules.regions.menu;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.ArrayList;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import ru.komiss77.modules.regions.RM;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.Pagination;
import ru.komiss77.utils.inventory.SlotIterator;
import ru.komiss77.utils.inventory.SlotPos;


public class MembersManageMenu implements InventoryProvider {
  private static final ItemStack fill;
  private final ProtectedRegion region;

  static {
    fill = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name(" ").build();
  }

  public MembersManageMenu(final ProtectedRegion region) {
    this.region = region;
  }

  @Override
  public void init(final Player p, final InventoryContent contents) {
    p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
    contents.fillBorders(ClickableItem.empty(MembersManageMenu.fill));
    final Pagination pagination = contents.pagination();

    final ArrayList<ClickableItem> menuEntry = new ArrayList<>();

    //головы с удалением
    for (String name : region.getMembers().getPlayers()) {
      final ItemStack head = new ItemBuilder(Material.PLAYER_HEAD)
          .name("§f" + name)
          .lore("§7Клик - удалить " + name)
          .build();
      final SkullMeta skullmeta = (SkullMeta) head.getItemMeta();
      skullmeta.hasOwner();
      skullmeta.setOwner(name);
      head.setItemMeta((ItemMeta) skullmeta);

      menuEntry.add(ClickableItem.of(head, e -> {
        region.getMembers().removePlayer(name);
        contents.getHost().open(p, pagination.getPage());
        p.playSound(p.getLocation(), Sound.BLOCK_LEVER_CLICK, 2, 2);
        p.sendMessage("§e" + name + " больше не пользователь региона.");
      }));
    }


    pagination.setItems(menuEntry.toArray(new ClickableItem[menuEntry.size()]));
    pagination.setItemsPerPage(18);


    contents.set(4, 4, ClickableItem.of(new ItemBuilder(Material.OAK_DOOR).name("§fвернуться").build(), p1
        -> RM.openRegionOwnerMenu(p, region)));

    contents.set(4, 6, ClickableItem.of(ItemUtil.nextPage, p4
        -> contents.getHost().open(p, pagination.next().getPage()))
    );

    contents.set(4, 2, ClickableItem.of(ItemUtil.previosPage, p4
        -> contents.getHost().open(p, pagination.previous().getPage()))
    );

    pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, SlotPos.of(1, 1)).allowOverride(false));

  }


}
