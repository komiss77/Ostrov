package ru.komiss77.builder.menu;

import java.util.Map;

import net.minecraft.server.level.ChunkHolder;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import ru.komiss77.ApiOstrov;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.world.Cuboid;
import ru.komiss77.objects.IntHashMap;
import ru.komiss77.objects.ValueSortedMap;
import ru.komiss77.utils.EntityUtil;
import ru.komiss77.utils.EntityUtil.EntityGroup;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.LocationUtil;
import ru.komiss77.utils.inventory.*;
import ru.komiss77.utils.inventory.InputButton.InputType;


public class EntityWorldMenu implements InventoryProvider {

    private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Material.NETHER_SPROUTS).name("§8.").build());
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
                map.put(LocationUtil.cLoc(chunk), chunk);

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

            for (ChunkHolder visibleChunk : io.papermc.paper.chunk.system.ChunkSystem.getVisibleChunkHolders(((CraftWorld) world).getHandle())) {
                net.minecraft.world.level.chunk.LevelChunk lc = visibleChunk.getTickingChunk();
                if (lc == null) {
                    continue;
                }
                cLoc = LocationUtil.cLoc("", lc.locX, lc.locZ);
                if (!map.containsKey(cLoc)) {
                    continue;
                }
                tile += lc.blockEntities.size();
            }

            for (TickingBlockEntity tbe : ((CraftWorld) world).getHandle().blockEntityTickers) {
                cLoc = LocationUtil.cLoc("", tbe.getPos().getX() >> 4, tbe.getPos().getZ() >> 4);
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
                    .addLore("")
                    .addLore("§e" + en.getValue())
                    //.addLore("§7")
                    .addLore("§7Лимит в настройках мира: §b" + (worldLimit > 0 ? worldLimit : "--"))
                    .addLore("§7")
                    .addLore("§7ЛКМ - показать по чанкам")
                    .addLore("§7ПКМ - показать по типу")
                    .addLore("§7")
                    .build(), e -> {
                if (e.isLeftClick()) {
                    group = en.getKey();
                    reopen(p, contents);
            /*SmartInventory.builder()
              .id("EntityByGroup" + p.getName())
              .provider(new EntityChunkMenu(world, radius, en.getKey()))
              .size(6, 9)
              .title("§2" + world.getName() + " " + en.getKey().displayName + " §1r=" + radius)
              .build()
              .open(p);*/
                } else {
                    SmartInventory.builder()
                            .id("EntityByGroup" + p.getName())
                            .provider(new EntityGroupMenu(p.getLocation(), radius, en.getKey()))
                            .size(6, 9)
                            .title("§2" + world.getName() + " " + en.getKey().displayName + " §1r=" + radius)
                            .build()
                            .open(p);
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
                    chunks.put(LocationUtil.cLoc(ch), ch);
                }
            } else {
                for (Chunk ch : world.getLoadedChunks()) {
                    chunks.put(LocationUtil.cLoc(ch), ch);
                }
            }

            if (group == EntityGroup.TILE) {

                for (ChunkHolder visibleChunk : io.papermc.paper.chunk.system.ChunkSystem.getVisibleChunkHolders(((CraftWorld) world).getHandle())) {
                    net.minecraft.world.level.chunk.LevelChunk lc = visibleChunk.getTickingChunk();
                    if (lc == null) {
                        continue;
                    }
                    int cLoc = LocationUtil.cLoc("", lc.locX, lc.locZ);
                    if (!chunks.containsKey(cLoc)) {
                        continue;
                    }
                    count2.put(cLoc, lc.blockEntities.size());
                }

            } else if (group == EntityGroup.TICKABLE_TILE) {

                for (TickingBlockEntity tbe : ((CraftWorld) world).getHandle().blockEntityTickers) {
                    int cLoc = LocationUtil.cLoc("", tbe.getPos().getX() >> 4, tbe.getPos().getZ() >> 4);
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
                    int cLoc = LocationUtil.cLoc(chunk);
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
                        .name("§bЧанк §f" + LocationUtil.getChunkX(entry.getKey()) + " §8x §f" + LocationUtil.getChunkZ(entry.getKey()))
                        .setAmount(entry.getValue() > 64 ? 64 : entry.getValue())
                        .addLore(group.displayName)
                        .addLore("§7Найдено: §e" + entry.getValue())
                        .addLore("")
                        .addLore("§7ЛКМ - ТП в чанк")
                        .addLore("§7ПКМ - показать группы в чанке")
                        .addLore("")
                        .addLore(group != EntityGroup.TILE && group != EntityGroup.TICKABLE_TILE ? "§7Шифт+ПКМ - удалить " + group.displayName + " в чанке" : "")
                        .build(), e -> {
                    if (e.isLeftClick()) {

                        EntityGroupMenu.toChunk(p, world, entry.getKey());

                    } else if (e.getClick() == ClickType.RIGHT) {
                        final Chunk c = LocationUtil.getChunk(world.getName(), entry.getKey());
                        final IntHashMap<Chunk> map = new IntHashMap<>();
                        map.put(LocationUtil.cLoc(c), c);
                        SmartInventory.builder()
                                .id("EntityByGroup" + p.getName())
                                .provider(new EntityGroupMenu(map, group))
                                .size(6, 9)
                                .title("§bЧанк §f" + LocationUtil.getChunkX(entry.getKey()) + " §8x §f" + LocationUtil.getChunkZ(entry.getKey()))
                                .build()
                                .open(p);

                    } else if (e.getClick() == ClickType.SHIFT_RIGHT) {

                        final Chunk c = LocationUtil.getChunk(world.getName(), entry.getKey());
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
                .addLore("§7")
                .addLore("§fЛКМ - §bуказать радиус")
                .addLore("§7(0 - весь мир)")
                .addLore("§7")
                .build(), "" + radius, imput -> {
            if (!ApiOstrov.isInteger(imput)) {
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
                .addLore("")
                .addLore("§fШифт+ЛКМ - §судалить")
                .addLore("")
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









/*




        contents.set(1, 1, ClickableItem.of(new ItemBuilder(Material.ZOMBIE_HEAD)
            .name(EntityGroup.MONSTER.displayName)
            .addLore("§7")
            .addLore("§f"+  (count.containsKey(EntityGroup.MONSTER) ? "§e"+count.get(EntityGroup.MONSTER) : "не найдено") )
            .addLore("§7")
            .addLore("§7Лимит в настройках мира: §b" + (world.getSpawnLimit(SpawnCategory.MONSTER)>0 ? world.getSpawnLimit(SpawnCategory.MONSTER) : "--"))
            .addLore("§7")
            .addLore("§7ЛКМ - группу подробно")
            .addLore(ApiOstrov.isLocalBuilder(p, false) ? "§7Шифт+ПКМ - удалить всех мобов этой группы" : "")
            .addLore("§7")
            .build(), e -> {
                if (e.isLeftClick()) {
                    SmartInventory.builder()
                            .id("EntityByGroup"+p.getName())
                            . provider(new EntityByGroup(world, radius, EntityGroup.MONSTER))
                            . size(6, 9)
                            . title("§2"+world.getName()+" "+EntityGroup.MONSTER.displayName+" §1r="+radius).build()
                            .open(p);
                } else if (e.getClick()==ClickType.SHIFT_RIGHT && ApiOstrov.isLocalBuilder(p, false)) {
                    for (final Entity entity : world.getEntities()) {
                        if (EntityUtil.group(entity)==EntityGroup.MONSTER) {
                            entity.remove();
                        }
                    }
                    reopen(p, contents);
                }
            }));




        contents.set(1, 2, ClickableItem.of(new ItemBuilder(Material.LEATHER_HORSE_ARMOR)
            .name(EntityGroup.CREATURE.displayName)
            .addLore("§7")
            .addLore("§f"+  (count.containsKey(EntityGroup.CREATURE) ? "§e"+count.get(EntityGroup.CREATURE) : "не найдено") )
            .addLore("§7")
            .addLore("§7Лимит в настройках мира: §b" + (world.getSpawnLimit(SpawnCategory.ANIMAL)>0 ? world.getSpawnLimit(SpawnCategory.ANIMAL) : "--"))
            .addLore("§7")
            .addLore("§7ЛКМ - группу подробно")
            .addLore(ApiOstrov.isLocalBuilder(p, false) ? "§7Шифт+ПКМ - удалить всех мобов этой группы" : "")
            .addLore("§7")
            .build(), e -> {
                if (e.isLeftClick()) {
                    SmartInventory.builder().id("EntityByGroup"+p.getName()). provider(new EntityByGroup(world, radius, EntityGroup.CREATURE)). size(6, 9). title("§2"+world.getName()+" "+EntityGroup.CREATURE.displayName+" §1r="+radius).build() .open(p);
                } else if (e.getClick()==ClickType.SHIFT_RIGHT && ApiOstrov.isLocalBuilder(p, false)) {
                    for (final Entity entity : world.getEntities()) {
                        if (EntityUtil.group(entity)==EntityGroup.CREATURE) {
                            entity.remove();
                        }
                    }
                    reopen(p, contents);
                }
            }));


        contents.set(1, 3, ClickableItem.of(new ItemBuilder(Material.NAUTILUS_SHELL)
            .name(EntityGroup.WATER_CREATURE.displayName)
            .addLore("§7")
            .addLore("§f"+  (count.containsKey(EntityGroup.WATER_CREATURE) ? "§e"+count.get(EntityGroup.WATER_CREATURE) : "не найдено") )
            .addLore("§7")
            .addLore("§7Лимит в настройках мира: §b" + (world.getSpawnLimit(SpawnCategory.WATER_ANIMAL)>0 ? world.getSpawnLimit(SpawnCategory.WATER_ANIMAL) : "--"))
            .addLore("§7")
            .addLore("§7ЛКМ - группу подробно")
            .addLore(ApiOstrov.isLocalBuilder(p, false) ? "§7Шифт+ПКМ - удалить всех мобов этой группы" : "")
            .addLore("§7")
            .build(), e -> {
                if (e.isLeftClick()) {
                    SmartInventory.builder().id("EntityByGroup"+p.getName()). provider(new EntityByGroup(world, radius, EntityGroup.WATER_CREATURE)). size(6, 9). title("§2"+world.getName()+" "+EntityGroup.WATER_CREATURE.displayName+" §1r="+radius).build() .open(p);
                } else if (e.getClick()==ClickType.SHIFT_RIGHT && ApiOstrov.isLocalBuilder(p, false)) {
                    for (final Entity entity : world.getEntities()) {
                        if (EntityUtil.group(entity)==EntityGroup.WATER_CREATURE) {
                            entity.remove();
                        }
                    }
                    reopen(p, contents);
                }
            }));




        contents.set(1, 4, ClickableItem.of(new ItemBuilder(Material.COAL)
            .name(EntityGroup.AMBIENT.displayName)
            .addLore("§7")
            .addLore("§f"+  (count.containsKey(EntityGroup.AMBIENT) ? "§e"+count.get(EntityGroup.AMBIENT) : "не найдено") )
            .addLore("§7")
            .addLore("§7Лимит в настройках мира: §b" + (world.getSpawnLimit(SpawnCategory.AMBIENT)>0 ? world.getSpawnLimit(SpawnCategory.AMBIENT) : "--"))
            .addLore("§7")
            .addLore("§7ЛКМ - группу подробно")
            .addLore(ApiOstrov.isLocalBuilder(p, false) ? "§7Шифт+ПКМ - удалить всех мобов этой группы" : "")
            .addLore("§7")
            .build(), e -> {
                if (e.isLeftClick()) {
                    SmartInventory.builder().id("EntityByGroup"+p.getName()). provider(new EntityByGroup(world, radius, EntityGroup.AMBIENT)). size(6, 9). title("§2"+world.getName()+" "+EntityGroup.AMBIENT.displayName+" §1r="+radius).build() .open(p);
                } else if (e.getClick()==ClickType.SHIFT_RIGHT && ApiOstrov.isLocalBuilder(p, false)) {
                    for (final Entity entity : world.getEntities()) {
                        if (EntityUtil.group(entity)==EntityGroup.AMBIENT) {
                            entity.remove();
                        }
                    }
                    reopen(p, contents);
                }
            }));


        contents.set(1, 5, ClickableItem.of(new ItemBuilder(Material.TROPICAL_FISH)
            .name(EntityGroup.WATER_AMBIENT.displayName)
            .addLore("§7")
            .addLore("§f"+  (count.containsKey(EntityGroup.WATER_AMBIENT) ? "§e"+count.get(EntityGroup.WATER_AMBIENT) : "не найдено") )
            .addLore("§7")
            .addLore("§7Лимит в настройках мира: §b" + (world.getSpawnLimit(SpawnCategory.WATER_AMBIENT)>0 ? world.getSpawnLimit(SpawnCategory.WATER_AMBIENT) : "--"))
            .addLore("§7")
            .addLore("§7ЛКМ - группу подробно")
            .addLore(ApiOstrov.isLocalBuilder(p, false) ? "§7Шифт+ПКМ - удалить всех мобов этой группы" : "")
            .addLore("§7")
            .build(), e -> {
                if (e.isLeftClick()) {
                    SmartInventory.builder().id("EntityByGroup"+p.getName()). provider(new EntityByGroup(world, radius, EntityGroup.WATER_AMBIENT)). size(6, 9). title("§2"+world.getName()+" "+EntityGroup.WATER_AMBIENT.displayName+" §1r="+radius).build() .open(p);
                } else if (e.getClick()==ClickType.SHIFT_RIGHT && ApiOstrov.isLocalBuilder(p, false)) {
                    for (final Entity entity : world.getEntities()) {
                        if (EntityUtil.group(entity)==EntityGroup.WATER_AMBIENT) {
                            entity.remove();
                        }
                    }
                    reopen(p, contents);
                }
            }));


        contents.set(1, 7, ClickableItem.of(new ItemBuilder(Material.ARMOR_STAND)
            .name(EntityGroup.UNDEFINED.displayName)
            .addLore("§7")
            .addLore("§f"+  (count.containsKey(EntityGroup.UNDEFINED) ? "§e"+count.get(EntityGroup.UNDEFINED) : "не найдено") )
            .addLore("§7")
            .addLore("§7ЛКМ - группу подробно")
            .addLore(ApiOstrov.isLocalBuilder(p, false) ? "§7Шифт+ПКМ - удалить всех мобов этой группы" : "")
            .addLore("§7")
            .build(), e -> {
                if (e.isLeftClick()) {
                    SmartInventory.builder().id("EntityByGroup"+p.getName()). provider(new EntityByGroup(world, radius, EntityGroup.UNDEFINED)). size(6, 9). title("§2"+world.getName()+" "+EntityGroup.UNDEFINED.displayName+" §1r="+radius).build() .open(p);
                } else if (e.getClick()==ClickType.SHIFT_RIGHT && ApiOstrov.isLocalBuilder(p, false)) {
                    for (final Entity entity : world.getEntities()) {
                        if (EntityUtil.group(entity)==EntityGroup.UNDEFINED) {
                            if (entity.getType()!=EntityType.PLAYER) {
                                entity.remove();
                            }
                        }
                    }
                    reopen(p, contents);
                }
            }));









 */
