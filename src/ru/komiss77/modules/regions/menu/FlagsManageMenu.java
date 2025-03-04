package ru.komiss77.modules.regions.menu;

import java.util.LinkedList;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.regions.FlagSetting;
import ru.komiss77.modules.regions.RM;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.inventory.*;


public class FlagsManageMenu implements InventoryProvider {

  private static final ItemStack fill = new ItemBuilder(ItemType.BLACK_STAINED_GLASS_PANE).name(" ").build();
  private final ProtectedRegion region;

  public FlagsManageMenu(final ProtectedRegion region) {
    this.region = region;
  }


  @Override
  public void init(Player p, InventoryContent content) {

    content.fillRow(4, ClickableItem.empty(fill));
    p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);

    LinkedList<ClickableItem> menuEntryList = new LinkedList<>();

    RM.flags.entrySet().stream().filter(es -> es.getValue().enabled).forEach(es -> {
      //if (p.hasPermission(flagSetting.getPermission()) || p.hasPermission("region.flagmenu.all")) {
      menuEntryList.add(FlagSetting.button(p, es.getKey(), region, content));
      //}
    });


    Pagination pg = content.pagination();
    pg.setItems(menuEntryList.toArray(new ClickableItem[0]));
    pg.setItemsPerPage(36);
    pg.addToIterator(content.newIterator(SlotIterator.Type.HORIZONTAL, 0, 0).allowOverride(false));

    content.set(4, 4, ClickableItem.of(new ItemBuilder(ItemType.OAK_DOOR).name("гл.меню").build(),
        e -> RM.openRegionOwnerMenu(p, region)));

    if (!pg.isLast()) {
      content.set(4, 6, ClickableItem.of(ItemUtil.nextPage,
          e -> content.getHost().open(p, pg.next().getPage()))
      );
    }


    if (!pg.isFirst()) {
      content.set(4, 2, ClickableItem.of(ItemUtil.previosPage,
          e -> content.getHost().open(p, pg.previous().getPage()))
      );
    }


  }


}
