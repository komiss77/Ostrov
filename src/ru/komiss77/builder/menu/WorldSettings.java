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

//https://minecraft.wiki/w/Game_rule

public class WorldSettings implements InventoryProvider {
    private static final List<GameRule<?>> RULES = OStrap.getAll(RegistryKey.GAME_RULE);
    private final World world;

  public WorldSettings(final World world) {
        this.world = world;
    }


  private ItemType getRuleMat(final GameRule<?> rule, final boolean on) {

    //GameRules.ADVANCE_TIME

    return switch (rule.getKey().value().toUpperCase()) {
      case "ADVANCE_TIME" -> ItemType.CLOCK;
      case "ADVANCE_WEATHER" -> ItemType.WIND_CHARGE;
      case "ALLOW_ENTERING_NETHER_USING_PORTALS" -> ItemType.NETHERRACK;
      case "BLOCK_DROPS" -> ItemType.STONE_PICKAXE;
      case "BLOCK_EXPLOSION_DROP_DECAY" ->
          ItemType.TNT; //выпадает ли добыча из всех блоков или случайным образом в зависимости от расстояния блока до центра взрыва
      case "COMMAND_BLOCK_OUTPUT" -> ItemType.COMMAND_BLOCK_MINECART;
      case "COMMAND_BLOCKS_WORK" -> ItemType.COMMAND_BLOCK;
      case "DROWNING_DAMAGE" -> ItemType.SALMON_BUCKET; //утопление
      case "ELYTRA_MOVEMENT_CHECK" -> ItemType.ELYTRA;
      case "ENDER_PEARLS_VANISH_ON_DEATH" ->
          ItemType.ENDER_PEARL; //исчезают ли брошенные жемчужины Края после смерти игрока
      case "ENTITY_DROPS" -> ItemType.SPIDER_EYE;
      case "FALL_DAMAGE" -> ItemType.LADDER;
      case "FIRE_DAMAGE" -> ItemType.BLAZE_POWDER;
      case "FIRE_SPREAD_RADIUS_AROUND_PLAYER" -> ItemType.BLAZE_POWDER;
      case "FORGIVE_DEAD_PLAYERS" ->
          ItemType.PITCHER_PLANT; //Заставляет разгневанных нейтральных мобов в пределах 65 x 21 x 65 блоков, центрированных на целевом игроке, перестать злиться после смерти игрока.
      case "FREEZE_DAMAGE" -> ItemType.PACKED_ICE;
      case "GLOBAL_SOUND_EVENTS" ->
          ItemType.GOAT_HORN; //Слышат ли все игроки, независимо от местоположения, звук появления иссушителя, звук смерти дракона Края и звук активации портала в Край.
      case "IMMEDIATE_RESPAWN" ->
          ItemType.TOTEM_OF_UNDYING; //Игроки мгновенно возрождаются, без отображения экрана смерти.
      case "KEEP_INVENTORY" -> ItemType.BLACK_BUNDLE;
      case "LAVA_SOURCE_CONVERSION" -> ItemType.LAVA_BUCKET; //Будет ли разрешено образование новых источников лавы.
      case "LIMITED_CRAFTING" ->
          ItemType.CRAFTING_TABLE; //Можно ли игрокам создавать только те предметы по разблокированным рецептам?
      case "LOG_ADMIN_COMMANDS" -> ItemType.RECOVERY_COMPASS;
      case "MAX_BLOCK_MODIFICATIONS" ->
          ItemType.CHAIN_COMMAND_BLOCK; //Управляет максимальным количеством блоков, изменяемых при использовании команд /clone, /fill или /fillbiome.
      case "MAX_COMMAND_FORKS" -> ItemType.REPEATING_COMMAND_BLOCK;
      case "MAX_COMMAND_SEQUENCE_LENGTH" -> ItemType.CHAIN_COMMAND_BLOCK;
      case "MAX_ENTITY_CRAMMING" ->
          ItemType.STICKY_PISTON; //Максимальное количество объектов, которые может сдвинуть моб или игрок, прежде чем получить 6 единиц урона за полсекунды. Установка значения 0 отключает это правило. Урон действует на игроков в режиме выживания или приключений, а также на всех мобов, кроме летучих мышей. К сдвигаемым объектам относятся игроки, не находящиеся в режиме наблюдателя, любые мобы, кроме летучих мышей, а также лодки и вагонетки.
      case "MAX_MINECART_SPEED" -> ItemType.MINECART;
      case "MAX_SNOW_ACCUMULATION_HEIGHT" ->
          ItemType.WHITE_CARPET; //Максимальное количество слоев снега, которое может накопиться на каждом блоке.
      case "MOB_DROPS" -> ItemType.EXPERIENCE_BOTTLE;
      case "MOB_GRIEFING" -> ItemType.CREEPER_SPAWN_EGG;
      case "PLAYER_MOVEMENT_CHECK" -> ItemType.LEATHER_BOOTS;
      case "PLAYERS_NETHER_PORTAL_CREATIVE_DELAY" -> ItemType.OBSIDIAN;
      case "PLAYERS_NETHER_PORTAL_DEFAULT_DELAY" -> ItemType.OBSIDIAN;
      case "PLAYERS_SLEEPING_PERCENTAGE" -> ItemType.RED_BED;
      case "PROJECTILES_CAN_BREAK_BLOCKS" -> ItemType.SPECTRAL_ARROW;
      case "PVP" -> ItemType.DIAMOND_SWORD;
      case "RAIDS" -> ItemType.CROSSBOW;
      case "RANDOM_TICK_SPEED" -> ItemType.WHEAT;
      case "REDUCED_DEBUG_INFO" ->
          ItemType.WRITABLE_BOOK; //Определяет, отображает ли отладочный экран всю или сокращенную информацию; и отображаются ли эффекты сочетаний клавиш F3+B (области попадания объектов) и F3+G (границы блоков).
      case "RESPAWN_RADIUS" -> ItemType.ENDER_EYE;
      case "SEND_COMMAND_FEEDBACK" ->
          ItemType.COMMAND_BLOCK; //Определяет, должны ли в чате отображаться результаты выполнения команд игроком. Также влияет на поведение по умолчанию, определяющее, сохраняют ли командные блоки свой выводимый текст.
      case "SHOW_ADVANCEMENT_MESSAGES" -> ItemType.FLOWER_BANNER_PATTERN;
      case "SHOW_DEATH_MESSAGES" -> ItemType.BORDURE_INDENTED_BANNER_PATTERN;
      case "SPAWN_MOBS" -> ItemType.COW_SPAWN_EGG;
      case "SPAWN_MONSTERS" -> ItemType.ZOMBIE_SPAWN_EGG;
      case "SPAWN_PATROLS" -> ItemType.CROSSBOW;
      case "SPAWN_PHANTOMS" -> ItemType.PHANTOM_SPAWN_EGG;
      case "SPAWN_WANDERING_TRADERS" -> ItemType.WANDERING_TRADER_SPAWN_EGG;
      case "SPAWN_WARDENS" -> ItemType.WARDEN_SPAWN_EGG;
      case "SPAWNER_BLOCKS_WORK" -> ItemType.SPAWNER; //Включить или отключить блоки спавнера монстров.
      case "SPECTATORS_GENERATE_CHUNKS" -> ItemType.SPYGLASS;
      case "SPREAD_VINES" -> ItemType.VINE;
      case "TNT_EXPLODES" -> ItemType.TNT;
      case "TNT_EXPLOSION_DROP_DECAY" ->
          ItemType.TNT; //выпадает ли добыча из всех блоков или случайным образом в зависимости от расстояния блока до центра взрыва
      case "UNIVERSAL_ANGER" ->
          ItemType.PIGLIN_HEAD; //Заставляет разгневанных нейтральных мобов атаковать любого находящегося поблизости игрока, а не только того, кто их разозлил. Лучше всего работает, если параметр forgive_dead_players отключен.
      case "WATER_SOURCE_CONVERSION" -> ItemType.WATER_BUCKET;


      default -> rule.getType() == Integer.class ? ItemType.NAME_TAG : on ? ItemType.REDSTONE_TORCH : ItemType.LEVER;

    };

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
              ItemType it = getRuleMat(rule, on);

              menuEntry.add(ClickableItem.of(new ItemBuilder(it)
                        .name(rule.getKey().value())
                  .glint(on && it != ItemType.REDSTONE_TORCH)
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

              menuEntry.add(new InputButton(InputButton.InputType.ANVILL, new ItemBuilder(getRuleMat(rule, true))
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
    pagination.setItemsPerPage(36);


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


}
