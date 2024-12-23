package ru.komiss77.builder.menu;

import java.util.Map;
import ca.spottedleaf.moonrise.paper.util.BaseChunkSystemHooks;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import org.bukkit.*;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.world.Cuboid;
import ru.komiss77.objects.IntHashMap;
import ru.komiss77.objects.ValueSortedMap;
import ru.komiss77.utils.EntityUtil;
import ru.komiss77.utils.EntityUtil.EntityGroup;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.LocUtil;
import ru.komiss77.utils.NumUtil;
import ru.komiss77.utils.inventory.*;
import ru.komiss77.utils.inventory.InputButton.InputType;
import ru.komiss77.version.Craft;


public class EntityWorldMenu implements InventoryProvider {

    private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Material.NETHER_SPROUTS).name("§8.").build());
    private static final BaseChunkSystemHooks CHUNK_HOLD = new BaseChunkSystemHooks() {
        @Override
        public boolean screenEntity(final ServerLevel serverLevel, final net.minecraft.world.entity.Entity entity, final boolean b, final boolean b1) {
            return false;
        }
    };
    private final World world;
    private int radius;

    private EntityGroup group;

    public EntityWorldMenu(final World world, final int radius) {
        this.world = world;
        this.radius = radius;
    }


    @Override
    public void init(final Player p, final InventoryContent contents) {
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
        contents.fillRow(4, fill);
        contents.fillRow(1, fill);


        final ValueSortedMap<EntityGroup, Integer> count = new ValueSortedMap<>(true);

        int tile = 0;
        int tickingTile = 0;
        EntityGroup g;

        if (radius > 0) {

            final Cuboid c = new Cuboid(p.getLocation(), radius * 2, 1, radius * 2);
            c.allign(p.getLocation());
            final IntHashMap<Chunk> map = new IntHashMap<>();

            for (final Chunk chunk : c.getChunks(p.getWorld())) {
                if (!chunk.isLoaded() || !chunk.isEntitiesLoaded()) continue;
                map.put(LocUtil.cLoc(chunk), chunk);

                for (final Entity e : chunk.getEntities()) {
                    if (e.getType() == EntityType.PLAYER) continue;
                    g = EntityUtil.group(e);
                    if (count.containsKey(g)) {
                        count.replace(g, count.get(g) + 1);
                    } else {
                        count.put(g, 1);
                    }
                }


                //for (final BlockState bs : chunk.getTileEntities()) {
                //  group = EntityUtil.group(e);
                //  if (count.containsKey(group)) {
                //    count.replace(group, count.get(group)+1);
                //  } else {
                //    count.put(group, 1);
                //  }
                // }

            }

            int cLoc;

            for (ChunkHolder visibleChunk : CHUNK_HOLD.getVisibleChunkHolders(Craft.toNMS(world))) {
                net.minecraft.world.level.chunk.LevelChunk lc = visibleChunk.getTickingChunk();
                if (lc == null) {
                    continue;
                }
                cLoc = LocUtil.cLoc("", lc.locX, lc.locZ);
                if (!map.containsKey(cLoc)) {
                    continue;
                }
                tile += lc.blockEntities.size();
            }

            for (TickingBlockEntity tbe : ((CraftWorld) world).getHandle().blockEntityTickers) {
                cLoc = LocUtil.cLoc("", tbe.getPos().getX() >> 4, tbe.getPos().getZ() >> 4);
                if (!map.containsKey(cLoc)) {
                    continue;
                }
                tickingTile++;
            }
            /*for (final Entity e : p.getNearbyEntities(radius, radius, radius)) {
                if (e.getType()==EntityType.PLAYER) continue;
                group = EntityUtil.group(e);
                if (count.containsKey(group)) {
                    count.replace(group, count.get(group)+1);
                } else {
                    count.put(group, 1);
                }
            } */
        } else {

            for (final Entity e : world.getEntities()) {
                if (e.getType() == EntityType.PLAYER) continue;
                g = EntityUtil.group(e);
                if (count.containsKey(g)) {
                    count.replace(g, count.get(g) + 1);
                } else {
                    count.put(g, 1);
                }
            }
            tile = world.getTileEntityCount();
            tickingTile = world.getTickableTileEntityCount();


        }

        if (tile > 0) count.put(EntityGroup.TILE, tile);
        if (tickingTile > 0) count.put(EntityGroup.TICKABLE_TILE, tickingTile);


        //1 строка - численность по группам

        int worldLimit;
        for (final Map.Entry<EntityGroup, Integer> en : count.entrySet()) {
            worldLimit = EntityGroup.getWorldSpawnLimit(world, en.getKey());
            contents.add(ClickableItem.of(new ItemBuilder(en.getKey().displayMat)
                .name(en.getKey().displayName)
                .lore("")
                .lore("§e" + en.getValue())
                //.addLore("§7")
                .lore("§7Лимит в настройках мира: §b" + (worldLimit > 0 ? worldLimit : "--"))
                .lore("§7")
                .lore("§7ЛКМ - показать по типу")
                .lore("§7ПКМ - показать по чанкам")
                .lore("§7")
                .build(), e -> {
                if (e.isLeftClick()) {
                    SmartInventory.builder()
                        .id("EntityByGroup" + p.getName())
                        .provider(new EntityGroupMenu(p.getLocation(), radius, en.getKey()))
                        .size(6, 9)
                        .title("§2" + world.getName() + " " + en.getKey().displayName + " §1r=" + radius)
                        .build()
                        .open(p);
                } else {
                    group = en.getKey();
                    reopen(p, contents);
                }
            }));
        }


        //2 строки - кратко по чанкам
        if (group != null) {
            int pos = 18;
            final ValueSortedMap<Integer, Integer> count2 = new ValueSortedMap<>(true);

            final IntHashMap<Chunk> chunks = new IntHashMap<>();

            if (radius > 0) {
                final Cuboid c = new Cuboid(p.getLocation(), radius * 2, 1, radius * 2);
                c.allign(p.getLocation());
                for (Chunk ch : c.getChunks(world)) {
                    chunks.put(LocUtil.cLoc(ch), ch);
                }
            } else {
                for (Chunk ch : world.getLoadedChunks()) {
                    chunks.put(LocUtil.cLoc(ch), ch);
                }
            }

            if (group == EntityGroup.TILE) {

                for (ChunkHolder visibleChunk : CHUNK_HOLD.getVisibleChunkHolders(Craft.toNMS(world))) {
                    net.minecraft.world.level.chunk.LevelChunk lc = visibleChunk.getTickingChunk();
                    if (lc == null) {
                        continue;
                    }
                    int cLoc = LocUtil.cLoc("", lc.locX, lc.locZ);
                    if (!chunks.containsKey(cLoc)) {
                        continue;
                    }
                    count2.put(cLoc, lc.blockEntities.size());
                }

            } else if (group == EntityGroup.TICKABLE_TILE) {

                for (TickingBlockEntity tbe : ((CraftWorld) world).getHandle().blockEntityTickers) {
                    int cLoc = LocUtil.cLoc("", tbe.getPos().getX() >> 4, tbe.getPos().getZ() >> 4);
                    if (!chunks.containsKey(cLoc)) {
                        continue;
                    }
                    if (count2.containsKey(cLoc)) {
                        count2.replace(cLoc, count2.get(cLoc) + 1);
                    } else {
                        count2.put(cLoc, 1);
                    }
                }

            } else {

                for (final Chunk chunk : chunks.values()) {
                    int cLoc = LocUtil.cLoc(chunk);
                    for (final Entity e : chunk.getEntities()) {
                        if (EntityUtil.group(e.getType()) == group) {
                            if (count2.containsKey(cLoc)) {
                                count2.replace(cLoc, count2.get(cLoc) + 1);
                            } else {
                                count2.put(cLoc, 1);
                            }
                        }
                    }
                }
            }

            for (final Map.Entry<Integer, Integer> entry : count2.entrySet()) {

                contents.set(pos, ClickableItem.of(new ItemBuilder(Material.SCULK_CATALYST)
                    .name("§bЧанк §f" + LocUtil.getChunkX(entry.getKey()) + " §8x §f" + LocUtil.getChunkZ(entry.getKey()))
                    .amount(entry.getValue() > 64 ? 64 : entry.getValue())
                    .lore(group.displayName)
                    .lore("§7Найдено: §e" + entry.getValue())
                    .lore("")
                    .lore("§7ЛКМ - показать группы в чанке")
                    .lore("§7ПКМ - ТП в чанк")
                    .lore("")
                    .lore(group != EntityGroup.TILE && group != EntityGroup.TICKABLE_TILE ? "§7Шифт+ПКМ - удалить " + group.displayName + " в чанке" : "")
                    .build(), e -> {
                    if (e.isLeftClick()) {
                        final Chunk c = LocUtil.getChunk(world.getName(), entry.getKey());
                        final IntHashMap<Chunk> map = new IntHashMap<>();
                        map.put(LocUtil.cLoc(c), c);
                        SmartInventory.builder()
                            .id("EntityByGroup" + p.getName())
                            .provider(new EntityGroupMenu(map, group))
                            .size(6, 9)
                            .title("§bЧанк §f" + LocUtil.getChunkX(entry.getKey()) + " §8x §f" + LocUtil.getChunkZ(entry.getKey()))
                            .build()
                            .open(p);
                    } else if (e.getClick() == ClickType.RIGHT) {

                        EntityGroupMenu.toChunk(p, world, entry.getKey());

                    } else if (e.getClick() == ClickType.SHIFT_RIGHT) {

                        final Chunk c = LocUtil.getChunk(world.getName(), entry.getKey());
                        for (final Entity en : c.getEntities()) {
                            if (EntityUtil.group(en) == group) {
                                en.remove();
                            }
                        }
                        reopen(p, contents);

                    }
                }));

                pos++;
                if (pos == 35) {
                    break;
                }

            }


        }


        contents.set(5, 2, ClickableItem.of(new ItemBuilder(Material.HEART_OF_THE_SEA)
            .name("§eПоказать все миры")
            .build(), e -> {
            if (e.isLeftClick()) {
                SmartInventory
                    .builder()
                    .id("EntityWorlds" + p.getName())
                    .provider(new EntityServerMenu(Bukkit.getWorlds()))
                    .size(6, 9)
                    .title("§2Сущности миров")
                    .build()
                    .open(p);
            }
        }));


        contents.set(5, 4, new InputButton(InputType.ANVILL, new ItemBuilder(Material.FLOWER_BANNER_PATTERN)
            .name("§7Сущности в мире §a" + world.getName() + (radius > 0 ? " §7в радиусе §a" + radius : ""))
            .lore("§7")
            .lore("§fЛКМ - §bуказать радиус")
            .lore("§7(0 - весь мир)")
            .lore("§7")
            .build(), "" + radius, imput -> {
            if (!NumUtil.isInt(imput)) {
                p.sendMessage("§cДолжно быть число!");
                return;
            }
            final int r = Integer.parseInt(imput);
            if (r < 0 || r > 100000) {
                p.sendMessage("§cот 0 до 100000!");
                return;
            }
            radius = r;
            reopen(p, contents);
        }));


        contents.set(5, 6, ClickableItem.of(new ItemBuilder(Material.REDSTONE)
            .name("§cУдалить всех энтити в мире")
            .lore("")
            .lore("§fШифт+ЛКМ - §судалить")
            .lore("")
            .build(), e -> {
            if (e.isShiftClick()) {
                if (radius > 0) {
                    for (final Entity entity : p.getNearbyEntities(radius, radius, radius)) {
                        if (entity.getType() != EntityType.PLAYER) {
                            entity.remove();
                        }
                    }
                } else {
                    for (final Entity entity : p.getWorld().getEntities()) {
                        if (entity.getType() != EntityType.PLAYER) {
                            entity.remove();
                        }
                    }
                }
                reopen(p, contents);
            }
        }));


        contents.set(5, 8, ClickableItem.of(new ItemBuilder(Material.OAK_DOOR).name("назад").build(), e
            -> PM.getOplayer(p).setup.openMainSetupMenu(p)
        ));


    }


}

