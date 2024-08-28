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
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.Pagination;
import ru.komiss77.utils.inventory.SlotIterator;
import ru.komiss77.utils.inventory.SlotPos;
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


    //добавление через чат
       /* contents.set(SlotPos.of(0, 4), ClickableItem.of(new ItemBuilder(Material.FEATHER).name("§7Добавить через чат").build(), p3 -> {
            p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
            
            p.closeInventory();
            p.sendMessage(Language.REGION_MESSAGE_CHATADDMEMBER.toChatString());
            PlayerInput.get(InputButton.InputType.CHAT, p, addName -> {
                

                        playerDomain.addPlayer(addName);
                        domain.setPlayerDomain(playerDomain);
                        region.setMembers(domain);
                        region.setDirty(true);
                        reopen(p, contents);
                        ApiOstrov.reachCustomStat(p, GM.GAME.name()+"_member", domain.size());
            }, "");

        }));*/


    //ClickableItem clickItem = ClickableItem.of( new ItemBuilder(Material.OAK_DOOR).name(Language.INTERFACE_BACK.toString()).build(), p1
    //        -> MenuOpener.openMain(p, region) );


    contents.set(4, 4, ClickableItem.of(new ItemBuilder(Material.OAK_DOOR).name("§fвернуться").build(), p1
        -> RM.openRegionOwnerMenu(p, region)));

    contents.set(4, 6, ClickableItem.of(new ItemBuilder(Material.MAP).name("§fдалее").build(), p4
        //-> contents.getHost().open(p, pagination.next().getPage(), new String[] { "region" }, new Object[] { this.region })));
        -> contents.getHost().open(p, pagination.next().getPage()))
    );

    contents.set(4, 2, ClickableItem.of(new ItemBuilder(Material.MAP).name("§fназад").build(), p4
        //-> contents.getHost().open(p, pagination.previous().getPage(), new String[] { "region" }, new Object[] { this.region })));
        -> contents.getHost().open(p, pagination.previous().getPage()))
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
              //  RegionRemoveMemberEvent regionRemoveMemberEvent = new RegionRemoveMemberEvent(p, this.claim.getTemplate(), offlinePlayer.getUniqueId());
              //  Bukkit.getPluginManager().callEvent((Event)regionRemoveMemberEvent);
                
               // if (regionRemoveMemberEvent.isCancelled()) {
                //    this.reOpen(p, contents);
                    //contents.inventory().open(p, new String[] { "region" }, new Object[] { protectedRegion2 });
              //      return;
              //  }
               // else {
                    
                    protectedRegion.getMembers().removePlayer(offlinePlayer.getName());
                    contents.inventory().open(p, pagination.getPage(), new String[] { "region" }, new Object[] { protectedRegion });
                    UtilPlayer.playSound(p, Sound.BLOCK_LEVER_CLICK);
                    p.sendMessage(Language.INTERFACE_REMOVE_SUCESSFULL.toChatString().replaceAll("%name%", offlinePlayer.getName()));
                    return;
              //  }
            }));
        }*/


  }


}
