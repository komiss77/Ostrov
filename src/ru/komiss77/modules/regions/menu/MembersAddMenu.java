package ru.komiss77.modules.regions.menu;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.domains.PlayerDomain;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.ArrayList;
import ru.komiss77.modules.regions.RM;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.Pagination;
import ru.komiss77.utils.inventory.SlotIterator;
import ru.komiss77.ApiOstrov;
import ru.komiss77.modules.games.GM;
import ru.komiss77.utils.ItemBuilder;


public class MembersAddMenu implements InventoryProvider {
  private static final ItemStack fill;
  private final ProtectedRegion region;

  static {
    fill = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name(" ").build();
  }

  public MembersAddMenu(final ProtectedRegion region) {
    this.region = region;
  }

  @Override
  public void init(final Player p, final InventoryContent contents) {
    p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
    contents.fillBorders(ClickableItem.empty(MembersAddMenu.fill));

    final Pagination pagination = contents.pagination();
    final ArrayList<ClickableItem> menuEntry = new ArrayList<>();


    DefaultDomain domain = region.getMembers();
    PlayerDomain playerDomain = domain.getPlayerDomain();

    for (Player v : p.getWorld().getPlayers()) {

      if (!p.getName().equals(v.getName()) && !playerDomain.contains(v.getName()) && v.getLocation().distance(p.getLocation()) < 10) {

        final ItemStack head = new ItemBuilder(Material.PLAYER_HEAD)
            .name("§f" + v.getName())
            .lore("§7ЛКМ - добавить пользователем")
            .build();

        menuEntry.add(ClickableItem.of(head, p6 -> {

          playerDomain.addPlayer(v.getName());
          domain.setPlayerDomain(playerDomain);
          region.setMembers(domain);
          region.setDirty(true);

          contents.getHost().open(p, pagination.getPage());
          p.playSound(p.getLocation(), Sound.BLOCK_LEVER_CLICK, 2, 2);
          ApiOstrov.reachCustomStat(p, GM.GAME.name() + "_member", domain.size());
        }));

      }

    }

    pagination.setItems(menuEntry.toArray(new ClickableItem[menuEntry.size()]));
    pagination.setItemsPerPage(18);


    contents.set(4, 4, ClickableItem.of(new ItemBuilder(Material.OAK_DOOR).name("§fвернуться").build(), p1
        -> RM.openRegionOwnerMenu(p, region)));

    contents.set(4, 6, ClickableItem.of(ItemUtil.nextPage, e
        -> contents.getHost().open(p, pagination.next().getPage()))
    );

    contents.set(4, 2, ClickableItem.of(ItemUtil.previosPage, e
        -> contents.getHost().open(p, pagination.previous().getPage()))
    );

    pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 1, 1).allowOverride(false));

  }


}
