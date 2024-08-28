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
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.Pagination;
import ru.komiss77.utils.inventory.SlotIterator;
import ru.komiss77.utils.inventory.SlotPos;


public class MembersEditMenu implements InventoryProvider {
  private static final ItemStack fill;
  private final ProtectedRegion region;

  static {
    fill = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name(" ").build();
  }

  public MembersEditMenu(final ProtectedRegion region) {
    this.region = region;
  }

  @Override
  public void init(final Player player, final InventoryContent contents) {
    player.playSound(player.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
    contents.fillBorders(ClickableItem.empty(MembersEditMenu.fill));
    final Pagination pagination = contents.pagination();


    final ArrayList<ClickableItem> menuEntry = new ArrayList<>();

    //головы с удалением
    for (String name : this.region.getMembers().getPlayers()) {
      final ItemStack head = new ItemBuilder(Material.PLAYER_HEAD)
          .name("§f" + name)
          .lore("§7Клик - удалить " + name)
          .build();
      final SkullMeta skullmeta = (SkullMeta) head.getItemMeta();
      skullmeta.hasOwner();
      skullmeta.setOwner(name);
      head.setItemMeta((ItemMeta) skullmeta);

      menuEntry.add(ClickableItem.of(head, p6 -> {
        //  RegionRemoveMemberEvent regionRemoveMemberEvent = new RegionRemoveMemberEvent(player, this.claim.getTemplate(), offlinePlayer.getUniqueId());
        //  Bukkit.getPluginManager().callEvent((Event)regionRemoveMemberEvent);

        // if (regionRemoveMemberEvent.isCancelled()) {
        //    this.reOpen(player, contents);
        //contents.inventory().open(player, new String[] { "region" }, new Object[] { protectedRegion2 });
        //      return;
        //  }
        // else {

        region.getMembers().removePlayer(name);
        contents.getHost().open(player, pagination.getPage());
        player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 2, 2);
        player.sendMessage("§e" + name + " больше не пользователь региона.");
        //return;
        //  }
      }));
    }


    pagination.setItems((ClickableItem[]) menuEntry.toArray(new ClickableItem[menuEntry.size()]));
    pagination.setItemsPerPage(18);


    contents.set(4, 4, ClickableItem.of(new ItemBuilder(Material.OAK_DOOR).name("§fвернуться").build(), p1
        -> RM.openRegionOwnerMenu(player, region)));

    contents.set(4, 6, ClickableItem.of(new ItemBuilder(Material.MAP).name("§fдалее").build(), p4
        -> contents.getHost().open(player, pagination.next().getPage()))
    );

    contents.set(4, 2, ClickableItem.of(new ItemBuilder(Material.MAP).name("§fназад").build(), p4
        -> contents.getHost().open(player, pagination.previous().getPage()))
    );

    pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, SlotPos.of(1, 1)).allowOverride(false));
        
     /*   
        final Iterator iterator = this.region.getMembers().getUniqueIds().iterator();
        
        
        while (iterator.hasNext()) {
            final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer((UUID)iterator.next());
            final String s2 = offlinePlayer.hasPlayedBefore() ? offlinePlayer.getName() : "Unknown Name";
            final ItemStack build = new ItemBuilder(Material.PLAYER_HEAD).name("§f" + s2).lore(Language.INTERFACE_REMOVE_DESC.toString().replaceAll("%name%", s2)).build();
            
            if (offlinePlayer.hasPlayedBefore()) {
                final ItemStack itemStack = build;
                final SkullMeta itemMeta = (SkullMeta)itemStack.getItemMeta();
                itemMeta.hasOwner();
                itemMeta.setOwningPlayer(offlinePlayer);
                itemStack.setItemMeta((ItemMeta)itemMeta);
            }
            //final UUID target;
            //final RegionRemoveMemberEvent regionRemoveMemberEvent;
            //final ProtectedRegion protectedRegion2;
            //final Pagination pagination2;
            //final String replacement;
            
            list.add(ClickableItem.of(build, p6 -> {
              //  RegionRemoveMemberEvent regionRemoveMemberEvent = new RegionRemoveMemberEvent(player, this.claim.getTemplate(), offlinePlayer.getUniqueId());
              //  Bukkit.getPluginManager().callEvent((Event)regionRemoveMemberEvent);
                
               // if (regionRemoveMemberEvent.isCancelled()) {
                //    this.reOpen(player, contents);
                    //contents.inventory().open(player, new String[] { "region" }, new Object[] { protectedRegion2 });
              //      return;
              //  }
               // else {
                    
                    protectedRegion.getMembers().removePlayer(offlinePlayer.getName());
                    contents.inventory().open(player, pagination.getPage(), new String[] { "region" }, new Object[] { protectedRegion });
                    UtilPlayer.playSound(player, Sound.BLOCK_LEVER_CLICK);
                    player.sendMessage(Language.INTERFACE_REMOVE_SUCESSFULL.toChatString().replaceAll("%name%", offlinePlayer.getName()));
                    return;
              //  }
            }));
        }*/


  }


}
