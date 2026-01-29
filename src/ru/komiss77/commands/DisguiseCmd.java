package ru.komiss77.commands;

import java.util.*;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.*;
import org.bukkit.block.BlockType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemType;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Cfg;
import ru.komiss77.Ostrov;
import ru.komiss77.Timer;
import ru.komiss77.boot.OStrap;
import ru.komiss77.builder.menu.EntitySetup;
import ru.komiss77.commands.tools.Resolver;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.*;
import ru.komiss77.utils.inventory.*;


public class DisguiseCmd implements OCommand {

  @Override
  public LiteralCommandNode<CommandSourceStack> command() {

    final String type = "type";

    return Commands.literal("disguise").executes(cntx -> {
          final CommandSender cs = cntx.getSource().getSender();
          if (!(cs instanceof final Player p)) {
            cs.sendMessage("§eНе консольная команда!");
            return 0;
          }
          if (!ApiOstrov.isLocalBuilder(cs, true) && !cs.hasPermission("ostrov.disguise")) {
            cs.sendMessage("§eНет права ostrov.disguise!");
            return 0;
          }
          //без параметров
          SmartInventory.builder()
              .id("DisguiseSelect" + p.getName())
              .provider(new DisguiseEntitySelect())
              .size(6, 9)
              .title("§2Выбор маскировки").build()
              .open(p);
          return Command.SINGLE_SUCCESS;
        })

        //c вводом EntityType
        .then(Resolver.string(type).suggests((cntx, sb) -> {
              //final CommandSender cs = cntx.getSource().getSender();
              sb.suggest("off");
              sb.suggest("cancel");
              sb.suggest("block");
              final String input = sb.getRemaining().toUpperCase();
              EntityUtil.EntityGroup g;
              for (EntityType et : EntityType.values()) {
                g = EntityUtil.group(et);
                if (g == EntityUtil.EntityGroup.TILE || g == EntityUtil.EntityGroup.TICKABLE_TILE ||
                    g == EntityUtil.EntityGroup.UNDEFINED) continue;
                if (et.name().startsWith(input)) {
                  sb.suggest(et.name().toLowerCase());
                }
              }
              return sb.buildFuture();
            })
            .executes(cntx -> {
              final CommandSender cs = cntx.getSource().getSender();
              if (!(cs instanceof final Player p)) {
                cs.sendMessage("§eНе консольная команда!");
                return 0;
              }
              if (!ApiOstrov.isLocalBuilder(cs, true) && !cs.hasPermission("ostrov.disguise")) {
                cs.sendMessage("§eНет права ostrov.disguise!");
                return 0;
              }
              final String typeName = Resolver.string(cntx, type);
              final Oplayer op = PM.getOplayer(p);
              if (typeName.equalsIgnoreCase("off") || typeName.equalsIgnoreCase("cancel")) {
                op.disguise.unDisguise();
                p.closeInventory();
                p.sendMessage("§6Маскировка снята");
                return 0;
              } else if (typeName.equalsIgnoreCase("block")) {
                DisguiseBlockSelect.open(p);
                return 0;
              }
              EntityType et = null;
              for (EntityType t : EntityType.values()) {
                if (t.name().equalsIgnoreCase(typeName)) {
                  et = t;
                  break;
                }
              }
              if (et == null) {
                p.sendMessage("§cНет EntityType " + typeName);
                return 0;
              }
              EntityUtil.EntityGroup g = EntityUtil.group(et);
              if (g == EntityUtil.EntityGroup.TILE || g == EntityUtil.EntityGroup.TICKABLE_TILE ||
                  g == EntityUtil.EntityGroup.UNDEFINED) {
                p.sendMessage("§cНельзя маскироваться в группу " + g);
                return 0;
              }
              op.disguise.disguise(p, et);
              p.sendMessage("disguise as " + et);

              return Command.SINGLE_SUCCESS;
            }))
        .build();
  }

  @Override
  public Set<String> aliases() {
    return Set.of("");
  }

  @Override
  public String description() {
    return "Маскировка";
  }


}


class DisguiseEntitySelect implements InventoryProvider {

  private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Material.YELLOW_STAINED_GLASS_PANE).name("§8.").build());

  @Override
  public void init(final Player p, final InventoryContent contents) {
    p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
    contents.fillRow(4, fill);
    final Oplayer op = PM.getOplayer(p);

    final Pagination pagination = contents.pagination();
    final ArrayList<ClickableItem> menuEntry = new ArrayList<>();


    EntityUtil.EntityGroup g;
    for (EntityType et : EntityType.values()) {
      g = EntityUtil.group(et);
      if (g == EntityUtil.EntityGroup.TILE || g == EntityUtil.EntityGroup.TICKABLE_TILE ||
          g == EntityUtil.EntityGroup.UNDEFINED) continue;

      ItemType tp = OStrap.get(Key.key(et.name().toLowerCase() + "_spawn_egg"), ItemType.AIR);
      if (tp == ItemType.AIR) tp = ItemType.TURTLE_SPAWN_EGG;

      menuEntry.add(ClickableItem.of(new ItemBuilder(tp)
          //.name("§f" + entity.getLocation().getBlockX() + " §7: §f" + entity.getLocation().getBlockY() + " §7: §f" + entity.getLocation().getBlockZ())
          .build(), e -> {
        p.closeInventory();
        op.disguise.disguise(p, et);
      }));
    }

    pagination.setItems(menuEntry.toArray(new ClickableItem[0]));
    pagination.setItemsPerPage(36);


    contents.set(5, 0, ClickableItem.of(new ItemBuilder(ItemType.ACACIA_LOG)
            .name("Маскировка в блок")
            .build(), e -> {
          DisguiseBlockSelect.open(p);
        }
    ));

    if (op.disguise.type != null) {
      contents.set(5, 4, ClickableItem.of(new ItemBuilder(ItemType.REDSTONE)
              .name("Убрать маскировку")
              .build(), e -> {
            op.disguise.unDisguise();
            p.closeInventory();
          }
      ));
    }


    if (!pagination.isLast()) {
      contents.set(5, 8, ClickableItem.of(ItemUtil.nextPage, e
          -> contents.getHost().open(p, pagination.next().getPage()))
      );
    }

    if (!pagination.isFirst()) {
      contents.set(5, 0, ClickableItem.of(ItemUtil.previosPage, e
          -> contents.getHost().open(p, pagination.previous().getPage()))
      );
    }

    pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, SlotPos.of(0, 0)).allowOverride(false));

  }

}

class DisguiseBlockSelect implements InventoryProvider {

  private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Material.YELLOW_STAINED_GLASS_PANE).name("§8.").build());

  public static void open(Player p) {
    SmartInventory.builder()
        .id("DisguiseSelect" + p.getName())
        .provider(new DisguiseBlockSelect())
        .size(6, 9)
        .title("§2Блок для маскировки").build()
        .open(p);
  }

  @Override
  public void init(final Player p, final InventoryContent contents) {
    p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
    contents.fillRow(4, fill);
    final Oplayer op = PM.getOplayer(p);

    final Pagination pagination = contents.pagination();
    final ArrayList<ClickableItem> menuEntry = new ArrayList<>();

    for (BlockType bt : Ostrov.registries.BLOCKS) {
      if (!bt.hasItemType()) continue;
      menuEntry.add(ClickableItem.of(new ItemBuilder(bt.getItemType())
          //.name("§f" + entity.getLocation().getBlockX() + " §7: §f" + entity.getLocation().getBlockY() + " §7: §f" + entity.getLocation().getBlockZ())
          .build(), e -> {
        p.closeInventory();
        op.disguise.disguise(p, bt);
      }));
    }

    pagination.setItems(menuEntry.toArray(new ClickableItem[0]));
    pagination.setItemsPerPage(36);


    if (op.disguise.type != null) {
      contents.set(5, 4, ClickableItem.of(new ItemBuilder(Material.REDSTONE)
              .name("Убрать маскировку")
              .build(), e -> {
            op.disguise.unDisguise();
            p.closeInventory();
          }
      ));
    }


    if (!pagination.isLast()) {
      contents.set(5, 8, ClickableItem.of(ItemUtil.nextPage, e
          -> contents.getHost().open(p, pagination.next().getPage()))
      );
    }

    if (!pagination.isFirst()) {
      contents.set(5, 0, ClickableItem.of(ItemUtil.previosPage, e
          -> contents.getHost().open(p, pagination.previous().getPage()))
      );
    }

    pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, SlotPos.of(0, 0)).allowOverride(false));

  }

}
