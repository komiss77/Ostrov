package ru.komiss77.builder.menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.block.BlockType;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.translate.Lang;
import ru.komiss77.modules.world.Cuboid;
import ru.komiss77.objects.IntHashMap;
import ru.komiss77.objects.ValueSortedMap;
import ru.komiss77.utils.*;
import ru.komiss77.utils.EntityUtil.EntityGroup;
import ru.komiss77.utils.inventory.*;
import ru.komiss77.utils.inventory.InputButton.InputType;


public class EntityGroupMenu implements InventoryProvider {

    private static final ItemStack fill = new ItemBuilder(Material.YELLOW_STAINED_GLASS_PANE).name("§8.").build();
    //java.lang.NoClassDefFoundError: ca/spottedleaf/moonrise/paper/util/BaseChunkSystemHooks
    // private static final BaseChunkSystemHooks CHUNK_HOLD = new BaseChunkSystemHooks() {
    //    @Override
    //    public boolean screenEntity(final ServerLevel serverLevel, final net.minecraft.world.entity.Entity entity, final boolean b, final boolean b1) {
    //         return false;
    //     }
    // };

    private final IntHashMap<Chunk> chunks;
    private final World world;
    private int radius;
    private final EntityGroup group;


    public EntityGroupMenu(final Location loc, final int radius, final EntityGroup group) {
        chunks = getChunks(loc, radius);
        this.world = loc.getWorld();
        this.group = group;
    }

    public EntityGroupMenu(final IntHashMap<Chunk> chunks, final EntityGroup group) {
        this.world = chunks.values().stream().findAny().get().getWorld();
        this.chunks = chunks;
        this.group = group;
    }

    public static IntHashMap<Chunk> getChunks(final Location loc, final int radius) {
        final IntHashMap<Chunk> chunks = new IntHashMap<>();
        if (radius > 0) {
            final Cuboid c = new Cuboid(loc, radius * 2, 1, radius * 2);
            c.allign(loc);
            for (Chunk ch : c.getChunks(loc.getWorld())) {
                chunks.put(LocUtil.cLoc(ch), ch);
            }
        } else {
            for (Chunk ch : loc.getWorld().getLoadedChunks()) {
                chunks.put(LocUtil.cLoc(ch), ch);
            }
        }
        return chunks;
    }

    public static void toChunk(final Player p, final World world, final int cloc) {
        final Chunk c = LocUtil.getChunk(world.getName(), cloc);
        toChunk(p, c);
    }

    public static void toChunk(final Player p, final Chunk chunk) {
        p.teleport(chunk.getWorld().getHighestBlockAt(chunk.getX() * 16 + 8, chunk.getZ() * 16 + 8).getLocation());
    }

    @Override
    public void init(final Player p, final InventoryContent contents) {
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
        contents.fillRow(4, ClickableItem.empty(EntityGroupMenu.fill));


        final Pagination pagination = contents.pagination();
        final ArrayList<ClickableItem> menuEntry = new ArrayList<>();


        if (group == EntityGroup.TILE) {

            final Map<BlockType, Integer> count = new HashMap<>();
            //??? java.lang.NoClassDefFoundError: ca/spottedleaf/moonrise/paper/util/BaseChunkSystemHooks
            //for (ChunkHolder visibleChunk : CHUNK_HOLD.getVisibleChunkHolders(Craft.toNMS(world))) {
            //    net.minecraft.world.level.chunk.LevelChunk lc = visibleChunk.getTickingChunk();
            //  if (lc == null) {
            //       continue;
            //    }
            //   int cLoc = LocUtil.cLoc("", lc.locX, lc.locZ);
            // if (!chunks.containsKey(cLoc)) {
            //      continue;
            //   }
            // for (BlockEntity be : lc.blockEntities.values()) {
            //    final BlockType mat = Craft.fromNMS(be.getBlockState().getBlock());
            //    if (count.containsKey(mat)) {
            //        count.replace(mat, count.get(mat) + 1);
            //   } else {
            //        count.put(mat, 1);
            //   }
            // }
            //}
            for (Chunk chunk : world.getLoadedChunks()) {
                int cLoc = LocUtil.cLoc("", chunk.getX(), chunk.getZ());
                if (!chunks.containsKey(cLoc)) {
                    continue;
                }
                for (BlockState bs : chunk.getTileEntities()) {
                    final BlockType mat = bs.getType().asBlockType();
                    if (count.containsKey(mat)) {
                        count.replace(mat, count.get(mat) + 1);
                    } else {
                        count.put(mat, 1);
                    }
                }
            }


            for (final Map.Entry<BlockType, Integer> entry : count.entrySet()) {
                final BlockType bt = entry.getKey();
                final ItemType mat = bt.hasItemType() ? bt.getItemType() : ItemType.STONE;
                menuEntry.add(ClickableItem.of(new ItemBuilder(mat)
                    .name(Lang.t(bt, p))
                    .amount(entry.getValue() > 64 ? 1 : entry.getValue())
                    .lore("§7Найдено: §e" + entry.getValue())
                    .lore(chunks.size() == 1 ? "§7ЛКМ - ТП в чанк" : "")
                    .build(), e -> {
                    toChunk(p, chunks.values().stream().findAny().get());
                }));
            }


        } else if (group == EntityGroup.TICKABLE_TILE) {

            final ValueSortedMap<String, Integer> count = new ValueSortedMap<>(true);

            for (TickingBlockEntity tbe : ((CraftWorld) world).getHandle().blockEntityTickers) {
                int cLoc = LocUtil.cLoc("", tbe.getPos().getX() >> 4, tbe.getPos().getZ() >> 4);
                if (!chunks.containsKey(cLoc)) {
                    continue;
                }
                if (count.containsKey(tbe.getType())) {
                    count.replace(tbe.getType(), count.get(tbe.getType()) + 1);
                } else {
                    count.put(tbe.getType(), 1);
                }
            }
            Material mat;
            for (final Map.Entry<String, Integer> entry : count.entrySet()) {
                mat = Material.matchMaterial(entry.getKey().substring(10));
                menuEntry.add(ClickableItem.of(new ItemBuilder(mat == null ? Material.ENDER_CHEST : mat)
                    .name(entry.getKey())
                    .amount(entry.getValue() > 64 ? 1 : entry.getValue())
                    .lore("§7Найдено: §e" + entry.getValue())
                    .lore(chunks.size() == 1 ? "§7ЛКМ - ТП в чанк" : "")
                    .build(), e -> {
                    toChunk(p, chunks.values().stream().findAny().get());

                }));
            }

        } else {

            final ValueSortedMap<EntityType, Integer> count = new ValueSortedMap<>(true);

            for (final Chunk chunk : chunks.values()) {
                if (!chunk.isLoaded() || !chunk.isEntitiesLoaded()) continue;
                for (final Entity e : chunk.getEntities()) {
                  if (e.getType() == EntityType.PLAYER) continue;
                    if (EntityUtil.group(e.getType()) == group) {
                        if (count.containsKey(e.getType())) {
                            count.replace(e.getType(), count.get(e.getType()) + 1);
                        } else {
                            count.put(e.getType(), 1);
                        }
                    }
                }
            }

            for (final Map.Entry<EntityType, Integer> entry : count.entrySet()) {
              final EntityType type = entry.getKey();
              menuEntry.add(ClickableItem.of(ItemUtil.buildEntityIcon(type)
                  .name(Lang.t(type, p)).maxStack(99).amount(entry.getValue())
                    .lore("§7")
                    .lore("§7Найдено: §e" + entry.getValue())
                    .lore("§7")
                    .lore("§7ЛКМ - подробно по типу")
                    .lore("§7")
                    .lore("§7Шифт+ПКМ - удалить всё этого типа")
                    .build(), e -> {
                    if (e.isLeftClick()) {
                        SmartInventory.builder()
                            .id("EntityByType" + p.getName())
                            .provider(new EntityTypeMenu(world, radius, type))
                            .size(6, 9)
                            .title("§2" + world.getName() + ", §6" + type + ", §1r=" + radius).build()
                            .open(p);
                    } else if (e.getClick() == ClickType.SHIFT_RIGHT) {
                        for (final Entity entity : world.getEntities()) {
                          if (entity.getType() == type) {
                                entity.remove();
                            }
                        }
                        reopen(p, contents);
                    }
                }));
            }
        }


        contents.set(5, 2, new InputButton(InputType.ANVILL, new ItemBuilder(Material.FLOWER_BANNER_PATTERN)
            .name("§7Группа " + group.toString() + " в мире §a" + world.getName() + (radius > 0 ? " §7r=§a" + radius : ""))
            .lore("§7")
            .lore("§7ЛКМ - изменить радиус")
            .lore("§7(0 - весь мир)")
            .lore("§7")
            .build(), "" + radius, input -> {

            if (!NumUtil.isInt(input)) {
                p.sendMessage("§cДолжно быть число!");
                return;
            }
            final int r = Integer.parseInt(input);
            if (r < 0 || r > 100000) {
                p.sendMessage("§cот 0 до 100000!");
                return;
            }
            radius = r;
            reopen(p, contents);
        }));


        contents.set(5, 4, ClickableItem.of(new ItemBuilder(Material.OAK_DOOR).name("назад").build(), e ->
            PM.getOplayer(p).setup.openEntityWorldMenu(p, world, radius)
        ));


        pagination.setItems(menuEntry.toArray(new ClickableItem[0]));
        pagination.setItemsPerPage(36);

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
