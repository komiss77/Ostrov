package ru.komiss77.builder.menu;

import java.util.ArrayList;
import java.util.List;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.GameRule;
import org.bukkit.GameRules;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemType;
import ru.komiss77.Ostrov;
import ru.komiss77.boot.OStrap;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.LocUtil;
import ru.komiss77.utils.NumUtil;
import ru.komiss77.utils.inventory.*;


public class WorldSettings implements InventoryProvider {


    private static final List<GameRule<?>> RULES = OStrap.getAll(RegistryKey.GAME_RULE);
    private final World world;
    
    public WorldSettings(final World world) {
        this.world = world;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void init(final Player p, final InventoryContent contents) {
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
        //contents.fillRect(0,0,  2,8, ClickableItem.empty(fill));
      final Pagination pagination = contents.pagination();
      final ArrayList<ClickableItem> menuEntry = new ArrayList<>();

        //"setwordspawn", "delete", "backup", "restore"


        //p.teleport( Bukkit.getWorld(itemname).getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);

        for (final GameRule rule : RULES) {
          try {
            if (rule.getType() == Boolean.class) {

              final boolean on = (boolean) world.getGameRuleValue(rule);

              menuEntry.add(ClickableItem.of(new ItemBuilder(getRuleMat(rule, on))
                        .name(rule.getKey().value())
                        .lore("")
                        .lore(on ? "§7сейчас §aвключено" : "§7сейчас §cвыключено")
                        .lore("")
                        .lore(on ? "§7ПКМ - §4выкл." : "§7ЛКМ - §2вкл.")
                        .lore("")
                        .build(), e -> {
                switch (e.getClick()) {
                  case LEFT:
                    if (!on) {
                      world.setGameRule(rule, true);
                      p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.5f, 1);
                      reopen(p, contents);
                    }
                    break;
                  case RIGHT:
                    if (on) {
                      world.setGameRule(rule, false);
                      p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.5f, 1);
                      reopen(p, contents);
                    }
                    break;
                  default:
                    break;

                }
              }));

            } else if (rule.getType() == Integer.class) {

              final int value = (int) world.getGameRuleValue(rule);

              menuEntry.add(new InputButton(InputButton.InputType.ANVILL, new ItemBuilder(ItemType.NAME_TAG)
                        .name(rule.getKey().value())
                        .lore("")
                        .lore("§7сейчас: " + value)
                        .lore("")
                        .lore("§7ЛКМ - §eизменить")
                        .lore("")
                        .build(), String.valueOf(value), msg -> {

                if (!NumUtil.isInt(msg)) {
                  p.sendMessage("§cДолжно быть число!");
                  PM.soundDeny(p);
                  return;
                }
                final int amount = Integer.parseInt(msg);
                if (amount < 1 || amount > 1_000_000) {
                  p.sendMessage("§cот 1 до 1000000");
                  PM.soundDeny(p);
                  return;
                }

                world.setGameRule(rule, amount);
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.5f, 1);
                reopen(p, contents);
                //return;
              }));


            }
          } catch (IllegalArgumentException ex) { //.IllegalArgumentException: Tried to access invalid game rule
            Ostrov.log_warn("GameRule " + rule.getKey().value() + " is @MinecraftExperimental");
            }

        }


      contents.set(5, 1, ClickableItem.of(new ItemBuilder(ItemType.ENDER_EYE)
                .name("Точка СПАВНА мира")
                .lore("")
                .lore("§7сейчас: " + LocUtil.toString(world.getSpawnLocation()))
                .lore("")
                .lore("§7Здесь будут появляться все игроки,")
                .lore("§7впервые зашедшие на сервер.")
                .lore("")
                .lore("§7ЛКМ - §eустановить")
                .lore("")
                .build(), e -> {
            if (e.isLeftClick()) {
                world.setSpawnLocation(p.getLocation());
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.5f, 1);
                reopen(p, contents);
            }
        }));


      contents.set(5, 2, ClickableItem.of(new ItemBuilder(ItemType.CAKE)
                .name("Центр ГРАНИЦЫ мира")
                .lore("")
                .lore("§7сейчас: " + LocUtil.toString(world.getWorldBorder().getCenter()))
                .lore("")
                .lore("§7ЛКМ - §eустановить")
                .lore("")
                .build(), e -> {
            if (e.isLeftClick()) {
                world.getWorldBorder().setCenter(p.getLocation());
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.5f, 1);
                reopen(p, contents);
            }
        }));


      contents.set(5, 3, new InputButton(InputButton.InputType.ANVILL, new ItemBuilder(ItemType.BEACON)
                .name("§6Размер §eГРАНИЦЫ §6мира")
                .lore("§7")
                .lore("§7сейчас: " + world.getWorldBorder().getSize())
                .lore("§7")
                .lore("§fЛКМ - §bустановить")
                //.addLore(ApiOstrov.isLocalBuilder(p, false) ? "§eПКМ - показать все миры" : "")
                .lore("§7")
                .build(), "" + world.getWorldBorder().getSize(), imput -> {

            if (!NumUtil.isInt(imput)) {
                p.sendMessage("§cДолжно быть число!");
                return;
            }
            final int r = Integer.parseInt(imput);
            if (r < 0 || r > 100000) {
                p.sendMessage("§cот 0 до 100000!");
                return;
            }
            world.getWorldBorder().setSize(r);
            reopen(p, contents);
        }));


      pagination.setItems(menuEntry.toArray(new ClickableItem[0]));
      pagination.setItemsPerPage(45);


      contents.set(5, 4, ClickableItem.of(new ItemBuilder(ItemType.OAK_DOOR).name("назад").build(), e ->
          p.performCommand("world")//WorldManagerCmd.openWorldMenu(p)
      ));


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

    private ItemType getRuleMat(final GameRule<?> rule, final boolean on) {
        if (rule == GameRules.SHOW_ADVANCEMENT_MESSAGES) {
            return ItemType.FLOWER_BANNER_PATTERN;
        } else if (rule == GameRules.COMMAND_BLOCK_OUTPUT) {
            return ItemType.COMMAND_BLOCK;
        } else if (rule == GameRules.ELYTRA_MOVEMENT_CHECK) {
            return ItemType.ELYTRA;
        } else if (rule == GameRules.RAIDS) {
            return ItemType.IRON_HORSE_ARMOR;
        } else if (rule == GameRules.ADVANCE_TIME) {
            return ItemType.SUNFLOWER;
        } else if (rule == GameRules.ENTITY_DROPS) {
            return ItemType.HOPPER;
        } else if (rule == GameRules.FIRE_DAMAGE) {
            return ItemType.BLAZE_POWDER;
        }
        return on ? ItemType.REDSTONE_TORCH : ItemType.LEVER;
    }


}
