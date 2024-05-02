package ru.komiss77.commands;


import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Config;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.builder.menu.WorldSetupMenu;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.inventory.*;

import java.util.ArrayList;


public class WorldCmd implements Listener, CommandExecutor {
    

    
    
    @Override
    public boolean onCommand(CommandSender cs, Command cmd, String string, String[] arg) {
        
        if ( ! (cs instanceof Player) ) {
            cs.sendMessage("§cНе консольная команда!");
            return true;
        }
        final Player p = (Player) cs;
        
        if (ApiOstrov.isLocalBuilder(cs, false)) {

            SmartInventory.builder()
                .id("Worlds"+p.getName())
                .provider(new WorldSetupMenu())
                .size(6, 9)
                .title("§2Миры сервера")
                .build().open(p);
            return true;

        }
        
        if ( Config.world_command ) {
            if ( p.hasPermission("ostrov.world")) {
                SmartInventory.builder()
                .id("Worlds"+p.getName())
                .provider(new WorldSelectMenu())
                .size(3, 9)
                .title("§2Миры сервера")
                .build().open(p);
            } else {
                p.sendMessage("§cУ Вас нет пава ostrov.world !");
            }
        } else {
            p.sendMessage( "§cСмена мира командой world отключён на этом сервере!");
        }
        //if (!ApiOstrov.isLocalBuilder(cs, true)) return false;


        return true;
    }
    

}





class WorldSelectMenu implements InventoryProvider {

  private static final ItemStack fill = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name("§8.").build();

  @Override
  public void init(final Player p, final InventoryContent contents) {
    p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
    //contents.fillRect(0,0,  2,8, ClickableItem.empty(fill));

    final Oplayer op = PM.getOplayer(p);

    final Pagination pagination = contents.pagination();
    final ArrayList<ClickableItem> menuEntry = new ArrayList<>();

      contents.fillRect(0, 0, 2, 8, ClickableItem.empty(fill));

      for (final World world : Bukkit.getWorlds()) {

        menuEntry.add(ClickableItem.of(new ItemBuilder(getWorldMat(world))
          .name(world.getName())
          .addLore(op.world_positions.containsKey(world.getName()) ? "§7ЛКМ - ТП на точку выхода" : "")
          .addLore("§7ПКМ - ТП на точку спавна мира")
          .addLore("")
          .build(), e -> {
          if (e.isLeftClick() && op.world_positions.containsKey(world.getName())) {
            final Location exit = ApiOstrov.locFromString(op.world_positions.get(world.getName()));
            ApiOstrov.teleportSave(p, exit, true);//p.teleport( world.getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
          } else {
            ApiOstrov.teleportSave(p, world.getSpawnLocation(), true);//p.teleport( world.getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
          }
        }));
      }

      pagination.setItems(menuEntry.toArray(new ClickableItem[0]));
      pagination.setItemsPerPage(9);

      contents.set(2, 4, ClickableItem.of(new ItemBuilder(Material.OAK_DOOR).name("назад").build(), e ->
        p.closeInventory()
      ));


      if (!pagination.isLast()) {
        contents.set(2, 8, ClickableItem.of(ItemUtils.nextPage, e
          -> contents.getHost().open(p, pagination.next().getPage()))
        );
      }

      if (!pagination.isFirst()) {
        contents.set(2, 0, ClickableItem.of(ItemUtils.previosPage, e
          -> contents.getHost().open(p, pagination.previous().getPage()))
        );
      }

    pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, SlotPos.of(0, 0)).allowOverride(false));

  }

  private Material getWorldMat(final World w) {
    return switch (w.getEnvironment()) {
      case NORMAL -> Material.SHORT_GRASS;
      case NETHER -> Material.NETHERRACK;
      case THE_END -> Material.END_STONE;
      default -> Material.WHITE_GLAZED_TERRACOTTA;
    };
  }


}
