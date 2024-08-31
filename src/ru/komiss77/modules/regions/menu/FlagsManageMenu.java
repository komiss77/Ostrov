package ru.komiss77.modules.regions.menu;

import java.util.LinkedList;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import ru.komiss77.modules.regions.FlagSetting;
import ru.komiss77.modules.regions.RM;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.*;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.Pagination;
import ru.komiss77.utils.inventory.SlotIterator;
import ru.komiss77.utils.inventory.SlotPos;


public class FlagsManageMenu implements InventoryProvider {

  private static final ItemStack fill = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name(" ").build();
  private final ProtectedRegion region;

  public FlagsManageMenu(final ProtectedRegion region) {
    this.region = region;
  }


  @Override
  public void init(Player p, InventoryContent content) {

    content.fillRow(4, ClickableItem.empty(fill));
    p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);

    LinkedList<ClickableItem> menuEntryList = new LinkedList();

    RM.flags.entrySet().stream().filter(es -> es.getValue().enabled).forEach(es -> {
      //if (p.hasPermission(flagSetting.getPermission()) || p.hasPermission("region.flagmenu.all")) {
      menuEntryList.add(FlagSetting.button(p, es.getKey(), region, content));
      //}
    });


    Pagination pg = content.pagination();
    pg.setItems(menuEntryList.toArray(new ClickableItem[0]));
    pg.setItemsPerPage(36);
    pg.addToIterator(content.newIterator(SlotIterator.Type.HORIZONTAL, 0, 0).allowOverride(false));


    content.set(4, 4, ClickableItem.of(new ItemBuilder(Material.OAK_DOOR).name("гл.меню").build(),
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
