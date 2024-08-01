package ru.komiss77.listener;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.*;

import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.*;
import org.bukkit.entity.Boat.Type;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Config;
import ru.komiss77.Initiable;
import ru.komiss77.Ostrov;
import ru.komiss77.Timer;
import ru.komiss77.enums.Module;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.translate.Lang;
import ru.komiss77.utils.*;
import ru.komiss77.utils.EntityUtil.EntityGroup;
import ru.komiss77.utils.inventory.*;


public final class LimiterLst implements Initiable, Listener {

    private static final OstrovConfig cfg;
    private static final Listener interactLst, chunkLst, spawnLst, blockLst;
    private static final EnumMap<limiterFlag, Boolean> flags;
    private static final EnumMap<EntityGroup, Integer> groupsLimit;
    private static final EnumMap<EntityType, Integer> entityTypeLimit;
    //private static final Inth<EntityType, Integer> entityTypeLimit;

    static {
        flags = new EnumMap<>(limiterFlag.class);
        for (final limiterFlag f : limiterFlag.values()) {
            flags.put(f, false);
        }
        groupsLimit = new EnumMap<>(EntityGroup.class);
        entityTypeLimit = new EnumMap<>(EntityType.class);
        interactLst = new interactLst();
        chunkLst = new chunkLst();
        spawnLst = new spawnLst();
        blockLst = new blockLst();
        cfg = Config.manager.getNewConfig("spawn_limiter.yml", new String[]{"", "Настройки лимитера", "ТУТ НИЧЕГО НЕ МЕНЯТЬ, РАБОТАТЬ ЧЕРЕЗ МЕНЮ!", ""});
        if (cfg.getConfigurationSection("mob_limiter") != null) { //вычистить старый конфиг
            cfg.set("mob_limiter", null);
            saveConfig();
        }
    }

    public LimiterLst() {
        reload();
    }

    public static boolean enabled() {
        return flags.get(limiterFlag.enable);
    }


    public enum limiterFlag {
        enable, oneMinecartPerPlayer, oneBoatPerPlayer, checkChunkOnLoad, blockStateLimiter, watchHandingSpawn;
    }

    @Override
    public void postWorld() { //обход модулей после загрузки миров, т.к. не всё можно сделать onEnable
    }

    @Override
    public void onDisable() {
    }

    @Override
    public void reload() {
        HandlerList.unregisterAll(interactLst);
        HandlerList.unregisterAll(blockLst);
        HandlerList.unregisterAll(chunkLst);
        HandlerList.unregisterAll(spawnLst);

        if (cfg.getConfigurationSection("flags") != null) {
            cfg.getConfigurationSection("flags").getKeys(false).stream().forEach((f) -> {
                try {
                    flags.put(limiterFlag.valueOf(f), cfg.getBoolean("flags." + f));
                } catch (IllegalArgumentException | NullPointerException ex) {
                    Ostrov.log_err("LimiterLst reload flags : " + ex.getMessage());
                }
            });
        }

        if (!flags.get(limiterFlag.enable)) {
            Ostrov.log_ok("§bЛимитер выключен.");
            return;
        }

        groupsLimit.clear();
        if (cfg.getConfigurationSection("groupsLimit") != null) {
            cfg.getConfigurationSection("groupsLimit").getKeys(false).stream().forEach((g) -> {
                try {
                    groupsLimit.put(EntityGroup.valueOf(g), cfg.getInt("groupsLimit." + g));
                } catch (IllegalArgumentException | NullPointerException ex) {
                    Ostrov.log_err("LimiterLst reload groupsLimit : " + ex.getMessage());
                }
            });
        }

        entityTypeLimit.clear();
        if (cfg.getConfigurationSection("entityTypeLimit") != null) {
            cfg.getConfigurationSection("entityTypeLimit").getKeys(false).stream().forEach((t) -> {
                try {
                    entityTypeLimit.put(EntityType.valueOf(t), cfg.getInt("entityTypeLimit." + t));
                } catch (IllegalArgumentException | NullPointerException ex) {
                    Ostrov.log_err("LimiterLst reload entityTypeLimit : " + ex.getMessage());
                }
            });
        }

        if (cfg.getConfigurationSection("blockStateTypeLimit") != null) {
            cfg.getConfigurationSection("blockStateTypeLimit").getKeys(false).stream().forEach((t) -> {
                try {
                    BlockStateType.valueOf(t).limit = cfg.getInt("blockStateTypeLimit." + t);
                } catch (IllegalArgumentException | NullPointerException ex) {
                    Ostrov.log_err("LimiterLst reload entityTypeLimit : " + ex.getMessage());
                }
            });
        }

        Bukkit.getPluginManager().registerEvents(interactLst, Ostrov.getInstance());
        if (flags.get(limiterFlag.watchHandingSpawn)) {
            Bukkit.getPluginManager().registerEvents(spawnLst, Ostrov.getInstance());
        }
        if (flags.get(limiterFlag.checkChunkOnLoad)) {
            Bukkit.getPluginManager().registerEvents(chunkLst, Ostrov.getInstance());
        }
        if (flags.get(limiterFlag.blockStateLimiter)) {
            Bukkit.getPluginManager().registerEvents(blockLst, Ostrov.getInstance());
        }
        Ostrov.log_ok("§2Лимитер активен!");
    }

    public static void saveConfig() {
        cfg.set("flags", null);
        for (Map.Entry<limiterFlag, Boolean> e : flags.entrySet()) {
            cfg.set("flags." + e.getKey().name(), e.getValue());
        }
        cfg.set("groupsLimit", null);
        for (Map.Entry<EntityGroup, Integer> e : groupsLimit.entrySet()) {
            cfg.set("groupsLimit." + e.getKey().name(), e.getValue());
        }
        cfg.set("entityTypeLimit", null);
        for (Map.Entry<EntityType, Integer> e : entityTypeLimit.entrySet()) {
            cfg.set("entityTypeLimit." + e.getKey().name(), e.getValue());
        }
        cfg.set("blockStateTypeLimit", null);
        for (BlockStateType bst : BlockStateType.values()) {
            if (bst.limit >= 0) cfg.set("blockStateTypeLimit." + bst.name(), bst.limit);
        }
        cfg.saveConfig();
    }


    static class interactLst implements Listener {
        @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
        public void onInteract(PlayerInteractEvent e) {
            if (e.getAction() == Action.PHYSICAL || e.getItem() == null) {
                return;
            }
            final Material mat = e.getItem().getType();

            if (e.getAction() == Action.RIGHT_CLICK_BLOCK && (mat == Material.FIREWORK_ROCKET || mat == Material.FIREWORK_STAR)) {
                if (Timer.has(e.getPlayer(), "firework")) {
                    e.setUseItemInHand(Event.Result.DENY);
                    ApiOstrov.sendActionBarDirect(e.getPlayer(), "§eЧуть помедленнее!");
                } else {
                    Timer.add(e.getPlayer(), "firework", 1);
                }
            }

            if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                final Player p = e.getPlayer();
                if (ApiOstrov.isLocalBuilder(p, false)) {
                    return;
                }
                final Oplayer op = PM.getOplayer(e.getPlayer());

                if (flags.get(limiterFlag.watchHandingSpawn) && ItemUtils.isSpawnEgg(mat)) {
                    final EntityType et = EntityUtil.typeFromEgg(mat);
                    final String res = fastCheck(e.getClickedBlock().getLocation(), et);
                    if (res != null) {
                        e.setUseItemInHand(Event.Result.DENY);
                        p.sendMessage(res);
                    }
                    return;
                }

                Entity entity = null;
//Ostrov.log_warn("isMineCart?"+ItemUtils.isMineCart(mat));
                if (flags.get(limiterFlag.oneMinecartPerPlayer) && ItemUtils.isMineCart(mat)) {
                    e.setUseItemInHand(Event.Result.DENY);
                    if (Timer.has(p, "vehicle")) {
                        ApiOstrov.sendActionBarDirect(p, Lang.t(p, "§eПодождите ") + Timer.getLeft(p, "vehicle") + Lang.t(p, " сек.!"));
                        return;
                    }
                    Timer.add(p, "vehicle", 2);

                    EntityType mineType = EntityType.MINECART;
                    switch (mat) {
                        case CHEST_MINECART -> mineType = EntityType.CHEST_MINECART;
                        case FURNACE_MINECART -> mineType = EntityType.FURNACE_MINECART;
                        case TNT_MINECART -> mineType = EntityType.TNT_MINECART;
                        case HOPPER_MINECART -> mineType = EntityType.HOPPER_MINECART;
                        //MINECART_COMMAND и MINECART_MOB_SPAWNER будут обычными MINECART
                    }
                    //if (Tag.RAILS.isTagged(e.getClickedBlock().getType())) {
                    entity = p.getWorld().spawnEntity(e.getClickedBlock().getRelative(BlockFace.UP).getLocation(), mineType, SpawnReason.CUSTOM);
                    if (op.minecart != null && op.minecart.get() != null) {
                        op.minecart.get().remove();
                    }
                    op.minecart = new WeakReference<>(entity);

                } else if (flags.get(limiterFlag.oneBoatPerPlayer) && (Tag.ITEMS_BOATS.isTagged(mat) || Tag.ITEMS_CHEST_BOATS.isTagged(mat))) {
                    e.setUseItemInHand(Event.Result.DENY);
                    if (Timer.has(p, "vehicle")) {
                        ApiOstrov.sendActionBarDirect(p, Lang.t(p, "§eПодождите ") + Timer.getLeft(p, "vehicle") + Lang.t(p, " сек.!"));
                        return;
                    }
                    Timer.add(p, "vehicle", 2);

                    EntityType boatType = Tag.ITEMS_CHEST_BOATS.isTagged(mat) ? EntityType.CHEST_BOAT : EntityType.BOAT;
                    entity = p.getWorld().spawnEntity(e.getClickedBlock().getRelative(BlockFace.UP).getLocation(), boatType, SpawnReason.CUSTOM);
                    switch (mat) {
                        case OAK_BOAT -> ((Boat) entity).setBoatType(Type.OAK);
                        case DARK_OAK_BOAT -> ((Boat) entity).setBoatType(Type.DARK_OAK);
                        case ACACIA_BOAT -> ((Boat) entity).setBoatType(Type.ACACIA);
                        case BIRCH_BOAT -> ((Boat) entity).setBoatType(Type.BIRCH);
                        case SPRUCE_BOAT -> ((Boat) entity).setBoatType(Type.SPRUCE);
                        case JUNGLE_BOAT -> ((Boat) entity).setBoatType(Type.JUNGLE);
                        case CHERRY_BOAT -> ((Boat) entity).setBoatType(Type.CHERRY);
                        case MANGROVE_BOAT -> ((Boat) entity).setBoatType(Type.MANGROVE);
                    }
                    if (op.boat != null && op.boat.get() != null) {
                        op.boat.get().remove();
                    }
                    op.boat = new WeakReference<>(entity);
                }

                if (entity != null) {
                    if (p.getInventory().getItemInMainHand().getType() == mat) {
                        p.getInventory().setItemInMainHand(ItemUtils.air);
                    } else {
                        p.getInventory().setItemInOffHand(ItemUtils.air);
                    }
                }
            } //end RIGHT_CLICK_BLOCK
        }

    /*@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent e) {
      if (e.getInventory().getType() == InventoryType.DISPENSER && flags.get(limiterFlag.block_egg_dispence) && e.getCurrentItem() != null) {
        if (e.getCurrentItem().getType().name().endsWith("_EGG")) {
          e.setCancelled(true);
          Lang.sendMessage((Player) e.getWhoClicked(), "§cНа данном сервере запрещён спавн мобов через раздатчик!");
        }
      }
    }*/

        @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
        public void onDispense(final BlockDispenseEvent e) {
            final Material mat = e.getItem().getType();
            boolean cancel = false;
            if (mat == Material.FIREWORK_ROCKET || mat == Material.FIREWORK_STAR) {
                final int dLoc = e.getBlock().getX() ^ e.getBlock().getY() ^ e.getBlock().getZ();
                if (Timer.has(dLoc)) {
                    cancel = true;//e.setCancelled(true);
                    //e.getItem().setType(Material.AIR);
                } else {
                    Timer.add(dLoc, 1);
                }
            } else if (ItemUtils.isSpawnEgg(mat)) {
                final String res = fastCheck(e.getBlock().getLocation(), EntityUtil.typeFromEgg(mat));
                if (res != null) {
                    cancel = true;//e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), e.getItem());
                    //e.setItem(ItemUtils.air);
                }
            } else if (flags.get(limiterFlag.oneMinecartPerPlayer) && ItemUtils.isMineCart(mat)) {
                cancel = true;//e.setCancelled(true);
            } else if (flags.get(limiterFlag.oneBoatPerPlayer) && (Tag.ITEMS_BOATS.isTagged(mat) || Tag.ITEMS_CHEST_BOATS.isTagged(mat))) {
                cancel = true;//e.setCancelled(true);
            }
            if (cancel) {
                e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), e.getItem());
                e.setItem(ItemUtils.air);
            }
        }
    }

    static class chunkLst implements Listener {
        @EventHandler
        public void onChunkLoadEvent(ChunkLoadEvent e) {
            //if (flags.get(limiterFlag.checkChunkOnLoad)) {по флагу подключается листенер
            checkChunk(null, e.getChunk());
            //checkBlockStates(e.getChunk()); - сносит сундуки!
            //}
        }
        //@EventHandler
        // public void onChunkUnloadEvent(ChunkUnloadEvent e) {
        //    if (ml_check_chunk_unload) {
        //       CheckEntyty(e.getChunk());
        //    }
        //  }
    }

    public static int checkChunk(@Nullable final Player p, @NotNull final Chunk chunk) {
        final EnumMap<EntityGroup, Integer> groups = new EnumMap(EntityGroup.class);
        final EnumMap<EntityType, Integer> types = new EnumMap(EntityType.class);

        EntityType type;
        EntityGroup group;
        Integer count, limit;//int count;
        int removed = 0;
        boolean r;

        for (final Entity entity : chunk.getEntities()) {
            type = entity.getType();
            group = EntityUtil.group(entity);
            if (type == EntityType.PLAYER) continue;
            r = false;

            limit = groupsLimit.get(group);
            if (limit != null) {
                count = groups.get(group);
                if (count == null) {
                    groups.put(group, 1);
                } else {
                    count++;
                    if (count > limit) {
                        entity.remove();
                        removed++;
                        r = true;
                    } else {
                        groups.put(group, count);
                    }
                }
            }
            if (!r) {
                limit = entityTypeLimit.get(type);
                if (limit != null) {
                    count = types.get(type);
                    if (count == null) {
                        types.put(type, 1);
                    } else {
                        count = types.get(type);
                        count++;
                        if (count > limit) {
                            entity.remove();
                            removed++;
                        } else {
                            types.put(type, count);
                        }
                    }
                }
            }
        }
        if (removed > 0) {
            final String result = "§3LimiterLst : В чанке " + chunk.getX() + "x" + chunk.getZ() + " удалено " + removed + " сущностей.";
            if (p != null) {
                p.sendMessage(result);
            } else {
                Ostrov.log_ok(result);
            }
        }
        return removed;
    }


    static class spawnLst implements Listener {
        @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
        public void onCreatureSpawnEvent(CreatureSpawnEvent e) {
            if (!flags.get(limiterFlag.watchHandingSpawn)) {
                return;
            }
            switch (e.getSpawnReason()) {
                case COMMAND, CUSTOM,
                     NATURAL, //When something spawns from natural means
                     JOCKEY, //When an entity spawns as a jockey of another entity (mostly spider jockeys)
                     LIGHTNING, //When a creature spawns because of a lightning strike
                     VILLAGE_DEFENSE, //When an iron golem is spawned to defend a village
                     VILLAGE_INVASION, //When a zombie is spawned to invade a village
                     BREEDING, //When an entity breeds to create a child, this also include Shulker and Allay
                     SLIME_SPLIT, //When a slime splits
                     REINFORCEMENTS, //When an entity calls for reinforcements
                     NETHER_PORTAL, //When a creature is spawned by nether portal
                     INFECTION, //When a zombie infects a villager
                     CURED, //When a villager is cured from infection
                     OCELOT_BABY, //When an ocelot has a baby spawned along with them
                     SILVERFISH_BLOCK, //When a silverfish spawns from a block
                     MOUNT, //When an entity spawns as a mount of another entity (mostly chicken jockeys)
                     TRAP, //When an entity spawns as a trap for players approaching
                     ENDER_PEARL, //When an entity is spawned as a result of ender pearl usage
                     SHOULDER_ENTITY,
                     //When an entity is spawned as a result of the entity it is being perched on jumping or being damaged
                     DROWNED, //When a creature is spawned by another entity drowning
                     SHEARED, //When a cow is spawned by shearing a mushroom cow
                     EXPLOSION,
                     //When an entity is spawned as a result of an explosion. Like an area effect cloud from a creeper or a dragon fireball
                     RAID, //When an entity is spawned as part of a raid
                     PATROL, //When an entity is spawned as part of a patrol
                     BEEHIVE, //When a bee is released from a beehive/bee nest
                     PIGLIN_ZOMBIFIED, //When a piglin is converted to a zombified piglin.
                     SPELL, //When an entity is created by a cast spell.
                     FROZEN, //When an entity is shaking in Powder Snow and a new entity spawns.
                     METAMORPHOSIS, //When a tadpole converts to a frog
                     DUPLICATION //When an Allay duplicate itself
                    -> {
                    return;
                }
                case EGG, BUILD_IRONGOLEM, BUILD_SNOWMAN, BUILD_WITHER,
                     DISPENSE_EGG, SPAWNER_EGG, SPAWNER, DEFAULT -> {
                    final String res = fastCheck(e.getLocation(), e.getEntityType());
                    if (res != null) {
                        e.setCancelled(true);
                        if (e.getSpawnReason().name().startsWith("BUILD_")) {
                            final Player find = LocationUtil.getNearestPlayer(e.getLocation(), 10);
                            if (find != null) {
                                find.sendMessage(res);
                            }
                        }
                    }
                }
            }
        }
    }


    static class blockLst implements Listener {
        @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
        public void onBlockPlace(BlockPlaceEvent e) {
//Ostrov.log_warn("onBlockPlace "+e.getBlock().getType());
            //e.setCancelled(!can(e.getPlayer(), e.getBlock().getChunk(), e.getBlock().getType()));
            final BlockStateType limittype = BlockStateType.getType(e.getBlock().getType());
//Ostrov.log_warn("can limittype="+limittype+(limittype==null?"":limittype.limit));
            if (limittype != null && limittype.limit >= 0) {
                if (ApiOstrov.isLocalBuilder(e.getPlayer(), false) || e.getPlayer().hasPermission("ostrov.limiter.ignore")) {
                    return;
                }
                if (limittype.limit == 0) {
                    e.getPlayer().sendMessage(limittype.displayName + " §cограничены!");
                    e.setCancelled(true);
                    return;
                }
                int count = 0;
                for (final BlockState blockState : e.getBlock().getChunk().getTileEntities()) {
//Ostrov.log_warn("can blockState="+blockState.getType()+":"+BlockStateType.getType(blockState.getType())+" count="+count);
                    if (BlockStateType.getType(blockState.getType()) == limittype) {
                        count++;
                        if (count > limittype.limit) {
                            e.getPlayer().sendMessage("§6Лимит " + limittype.displayName + " §6в чанке: §e" + limittype.limit);
                            e.setCancelled(true);
                            break;
//Ostrov.log_ok("§eBlockPlaceEvent cancel p=" + e.getPlayer().getName() + " size=" + e.getBlock().getChunk().getTileEntities().length + " mat=" + blockState.getType().toString() + " type=" + limittype.toString() + " count=" + count);
                        }
                    }
                }
            }
        }


        @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
        public void onHangingPlace(HangingPlaceEvent e) {
//Ostrov.log_warn("onHangingPlace "+e.getItemStack().getType());
            final BlockStateType limittype = BlockStateType.getType(e.getItemStack().getType());
//Ostrov.log_warn("can limittype="+limittype+(limittype==null?"":limittype.limit));
            if (limittype != null && limittype.limit > 0) {
                if (ApiOstrov.isLocalBuilder(e.getPlayer(), false) || e.getPlayer().hasPermission("ostrov.limiter.ignore")) {
                    return;
                }
                if (limittype.limit == 0) {
                    e.getPlayer().sendMessage("§6Лимит " + limittype.displayName + " §c: запрещены!");
                    e.setCancelled(true);
                    return;
                }
                int count = 0;
                for (final Entity en : e.getBlock().getChunk().getEntities()) {
//Ostrov.log_warn("can type="+en.getType()+":"+BlockStateType.getType(en.getType())+" count="+count);
                    if (BlockStateType.getType(en.getType()) == limittype) {
                        count++;
                        if (count > limittype.limit) {
                            e.getPlayer().sendMessage("§6Лимит " + limittype.displayName + " §6в чанке: §e" + limittype.limit);
                            e.setCancelled(true);
                            break;
//Ostrov.log_ok("§eBlockPlaceEvent cancel p=" + e.getPlayer().getName() + " size=" + e.getBlock().getChunk().getTileEntities().length + " mat=" + blockState.getType().toString() + " type=" + limittype.toString() + " count=" + count);
                        }
                    }
                }
            }

        }

   /*   private static boolean can (final Player p, final Chunk c, final Material mat) {
        final BlockStateType limittype = BlockStateType.getType(mat);
Ostrov.log_warn("can limittype="+limittype+(limittype==null?"":limittype.limit));
        if (limittype != null && limittype.limit > 0) {
          if (ApiOstrov.isLocalBuilder(p, false) || p.hasPermission("ostrov.limiter.ignore")) {
            return true;
          }
          int count = 0;
          for (final BlockState blockState : c.getTileEntities()) {
Ostrov.log_warn("can blockState="+blockState.getType()+":"+BlockStateType.getType(blockState.getType())+" count="+count);
            if (BlockStateType.getType(blockState.getType()) == limittype) {
              count++;
              if (count > limittype.limit) {
                p.sendMessage("§6Лимит " + limittype.displayName + " в чанке: §e" + limittype.limit);
                return false;
//Ostrov.log_ok("§eBlockPlaceEvent cancel p=" + e.getPlayer().getName() + " size=" + e.getBlock().getChunk().getTileEntities().length + " mat=" + blockState.getType().toString() + " type=" + limittype.toString() + " count=" + count);
              }
            }
          }
        }
        return true;
      }*/

    }


    private static String fastCheck(final Location loc, final EntityType type) {
        final EntityGroup group = EntityUtil.group(type);
//Ostrov.log_warn("SPAWN --------- "+group+" "+type);
        if (!groupsLimit.containsKey(group) && !entityTypeLimit.containsKey(type)) {
            return null;
        }
        int group_count = 0;
        int type_count = 0;
        final int group_limit = groupsLimit.getOrDefault(group, Integer.MAX_VALUE);
        if (group_limit == 0) {
            return "§6Cущности группы " + group.displayName + " §запрещены!";
        }
        final int type_limit = entityTypeLimit.getOrDefault(type, Integer.MAX_VALUE);
        if (type_limit == 0) {
            return "§6Cущности типа §3" + type.name() + " §запрещены!";
        }

        for (final Entity en : loc.getChunk().getEntities()) {
            if (en.getType() == type) {
                if (type_count++ > type_limit) {
                    return "§6Лимит сущности: §c" + type_limit + " §6" + type.name() + " на чанк.";
                }
            }
            if (EntityUtil.group(en) == group) {
                if (group_count++ > group_limit) {
                    return "§6Лимит сущности: §c" + group_limit + " §6" + group.name() + " на чанк.";
                }
            }
        }
//Ostrov.log_warn("en="+type+" group_count="+group_count+" limit="+groupsLimit.get(group)+" type_count="+type_count+" limit="+entityTypeLimit.get(type));
        return null;
    }


    public static enum BlockStateType {
        HEADS(-1, "§3Голов", Material.PLAYER_HEAD),
        BANNERS(-1, "§3Баннеров", Material.WHITE_BANNER),
        FRAMES(-1, "§3Рамок", Material.ITEM_FRAME),
        HOPPERS(-1, "§3Воронок", Material.HOPPER),
        DISPENSERS(-1, "§3Раздатчиков", Material.DISPENSER),
        CHESTS(-1, "§3Сундуков", Material.CHEST),

        ARMOR_STANDS(-1, "§3Стоек для брони", Material.ARMOR_STAND),
        ;

        public int limit;
        public final String displayName;
        public final Material displayMat;

        private BlockStateType(int limit, final String displayName, final Material displayMat) {
            this.limit = limit;
            this.displayName = displayName;
            this.displayMat = displayMat;
        }

        public static BlockStateType getType(final Material mat) {

            switch (mat) {

                case CHEST:
                case ENDER_CHEST:
                case TRAPPED_CHEST:
                case CHEST_MINECART:
                    return CHESTS;

                case DISPENSER:
                case DROPPER:
                    return DISPENSERS;

                case ITEM_FRAME: //при установке чекаем как предмет, при калькуляции- как entity!!
                    return FRAMES;

                case ARMOR_STAND: //при установке чекаем как предмет, при калькуляции- как entity!!
                    return ARMOR_STANDS;

                case HOPPER:
                case HOPPER_MINECART:
                    return HOPPERS;

                case SKELETON_SKULL:
                case WITHER_SKELETON_SKULL:
                case SKELETON_WALL_SKULL:
                case WITHER_SKELETON_WALL_SKULL:
                case PLAYER_HEAD:
                case ZOMBIE_HEAD:
                case CREEPER_HEAD:
                case DRAGON_HEAD:
                    return HEADS;
                default:
                    break;

            }

            if (Tag.BANNERS.isTagged(mat)) {
                return BANNERS;
            }

            return null;
        }

        public static BlockStateType getType(final EntityType entityType) {
            switch (entityType) {

                case ITEM_FRAME:
                    return FRAMES;

                case ARMOR_STAND:
                    return ARMOR_STANDS;

            }
            return null;
        }
    }

    public static void openMenu(final Player p) {
        SmartInventory.builder()
            .id("Limitersetup" + p.getName())
            .provider(new LimiterSetupMenu())
            .size(6, 9)
            .title("§fНастройки Лимитера")
            .build()
            .open(p);
    }

    private static class LimiterSetupMenu implements InventoryProvider {

        enum MenuMode {
            main, group, type, state;
        }

        private MenuMode mode = MenuMode.main;

        @Override
        public void init(final Player p, final InventoryContent content) {
            p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);

            if (!flags.get(limiterFlag.enable)) {

                final ItemStack is = new ItemBuilder(Material.REDSTONE_BLOCK)
                    .name("§8Модуль неактивен")
                    .lore("§aВключить")
                    .build();
                content.add(ClickableItem.of(is, e -> {
                        flags.put(limiterFlag.enable, true);
                        saveConfig();
                        (Ostrov.getModule(Module.limiter)).reload();
                        reopen(p, content);
                    }
                ));
                return;

            }

            if (mode == MenuMode.main) {
                final ItemStack is = new ItemBuilder(Material.EMERALD_BLOCK)
                    .name("§fМодуль активен")
                    .lore("§cВыключить")
                    .build();
                content.add(ClickableItem.of(is, e -> {
                        flags.put(limiterFlag.enable, false);
                        saveConfig();
                        (Ostrov.getModule(Module.limiter)).reload();
                        reopen(p, content);
                    }
                ));

                for (limiterFlag f : limiterFlag.values()) {
                    if (f == limiterFlag.enable) {
                        continue;
                    }
                    boolean b = flags.get(f);

                    content.add(ClickableItem.of(new ItemBuilder(b ? Material.LIME_DYE : Material.GRAY_DYE)
                            .name("§f" + f)
                            .lore(b ? "§cВыключить" : "§aВключить")
                            .build(), e -> {
                            flags.put(f, !b);
                            saveConfig();
                            (Ostrov.getModule(Module.limiter)).reload();
                            reopen(p, content);
                        }
                    ));
                }

                content.add(ClickableItem.of(new ItemBuilder(Material.STONECUTTER)
                        .name("§eПройтись по чанкам")
                        .lore("§cПрименить лимиты")
                        .lore("§eЛКМ - §cк текущему чанку")
                        .lore("§eКлав.Q - §cко всем в текущем мире")
                        .build(), e -> {
                        p.closeInventory();
                        if (e.getClick() == ClickType.LEFT) {
                            checkChunk(p, p.getChunk());
                        } else if (e.getClick() == ClickType.DROP) {
                            final long start = System.currentTimeMillis();
                            int removed = 0, chunk_count = 0;
                            for (final Chunk c : p.getWorld().getLoadedChunks()) {
                                removed += checkChunk(null, c);
                                chunk_count++;
                            }
                            p.sendMessage("§fОбработано чанков : §b" + chunk_count + " §f, удалено §c" + removed + " §fсущностей §3время: " + (System.currentTimeMillis() - start) + "ms.");
                        }
                    }
                ));

                int records = groupsLimit.size();
                content.set(1, 1, ClickableItem.of(new ItemBuilder(records == 0 ? Material.CLAY_BALL : Material.EGG)
                        .name("§bЛимит для чанка по ГРУППЕ")
                        .lore(records == 0 ? "§8нет ограничений" : "§6ограничений: §e" + records)
                        .lore("§7ЛКМ - настроить")
                        .build(), e -> {
                        mode = MenuMode.group;
                        reopen(p, content);
                    }
                ));

                records = entityTypeLimit.size();
                content.set(1, 2, ClickableItem.of(new ItemBuilder(records == 0 ? Material.CLAY_BALL : Material.PIGLIN_HEAD)
                        .name("§bЛимит для чанка по ТИПУ")
                        .lore(records == 0 ? "§8нет ограничений" : "§6ограничений: §e" + records)
                        .lore("§7ЛКМ - настроить")
                        .build(), e -> {
                        mode = MenuMode.type;
                        reopen(p, content);
                    }
                ));

                if (flags.get(limiterFlag.blockStateLimiter)) {
                    records = 0;
                    for (BlockStateType bst : BlockStateType.values()) {
                        if (bst.limit >= 0) {
                            records++;
                        }
                    }
                    content.set(1, 3, ClickableItem.of(new ItemBuilder(records == 0 ? Material.CLAY_BALL : Material.HOPPER)
                            .name("§bЛимит BLOCKSTATE для чанка")
                            .lore(records == 0 ? "§8нет ограничений" : "§6ограничений: §e" + records)
                            .lore("§7ЛКМ - настроить")
                            .build(), e -> {
                            mode = MenuMode.state;
                            reopen(p, content);
                        }
                    ));
                }
                return;
            }

            final Pagination pagination = content.pagination();
            final ArrayList<ClickableItem> menuEntry = new ArrayList<>();


            if (mode == MenuMode.group) {
                for (final EntityGroup g : EntityGroup.values()) {
                    if (g == EntityGroup.UNDEFINED || g == EntityGroup.TILE || g == EntityGroup.TICKABLE_TILE) continue;
                    final Integer limit = groupsLimit.get(g);
                    menuEntry.add(ClickableItem.of(new ItemBuilder(limit == null ? Material.CLAY_BALL : limit == 0 ? Material.RED_DYE : g.displayMat)
                        .name(g.displayName)
                        .lore(limit == null ? "§8не учитывается" : limit == 0 ? "§4==0 : Запрещены" : "§e" + limit + " §6на чанк")
                        .lore(limit == null ? "" : "§7ЛКМ §b+1")
                        .lore(limit == null ? "" : "§7Шифт+ЛКМ §3+10")
                        .lore(limit == null ? "" : "§7ПКМ §5-1")
                        .lore(limit == null ? "" : "§7Шифт+ПКМ §d-10")
                        .lore("")
                        .build(), e -> {
                        int i = limit == null ? -1 : limit;
                        switch (e.getClick()) {
                            case LEFT -> i++;
                            case SHIFT_LEFT -> i += 10;
                            case RIGHT -> i--;
                            case SHIFT_RIGHT -> i -= 10;
                        }
                        if (i < 0) {
                            groupsLimit.remove(g);
                        } else {
                            groupsLimit.put(g, i);
                        }
                        saveConfig();
                        reopen(p, content);
                    }));
                }
            } else if (mode == MenuMode.type) {
                EntityGroup g;
                for (final EntityType t : EntityType.values()) {
                    if (t == EntityType.PLAYER) continue;
                    g = EntityUtil.group(t);
                    if (g == EntityGroup.UNDEFINED || g == EntityGroup.TILE || g == EntityGroup.TICKABLE_TILE) continue;
                    final Integer limit = entityTypeLimit.get(t);
                    menuEntry.add(ClickableItem.of(ItemUtils.buildEntityIcon(t)
                        .setType(limit == null ? Material.CLAY_BALL : limit == 0 ? Material.RED_DYE : null)
                        .lore(limit == null ? "§8не учитывается" : limit == 0 ? "§4==0 : Запрещены" : "§e" + limit + " §6на чанк")
                        .lore(limit == null ? "" : "§7ЛКМ §b+1")
                        .lore(limit == null ? "" : "§7Шифт+ЛКМ §3+10")
                        .lore(limit == null ? "" : "§7ПКМ §5-1")
                        .lore(limit == null ? "" : "§7Шифт+ПКМ §d-10")
                        .lore("")
                        .build(), e -> {
                        int i = limit == null ? -1 : limit;
                        switch (e.getClick()) {
                            case LEFT -> i++;
                            case SHIFT_LEFT -> i += 10;
                            case RIGHT -> i--;
                            case SHIFT_RIGHT -> i -= 10;
                        }
                        if (i < 0) {
                            entityTypeLimit.remove(t);
                        } else {
                            entityTypeLimit.put(t, i);
                        }
                        saveConfig();
                        reopen(p, content);
                    }));
                }
            } else if (mode == MenuMode.state) {
                for (final BlockStateType bst : BlockStateType.values()) {
                    menuEntry.add(ClickableItem.of(new ItemBuilder(bst.limit < 0 ? Material.CLAY_BALL : bst.displayMat)
                        .name("§f" + bst.name())
                        .lore(bst.limit < 0 ? "§8не учитывается" : bst.limit == 0 ? "§4==0 : Запрещены" : "§e" + bst.limit + " §6на чанк")
                        .lore(bst.limit < 0 ? "" : "§7ЛКМ §b+1")
                        .lore(bst.limit < 0 ? "" : "§7Шифт+ЛКМ §3+10")
                        .lore(bst.limit < 0 ? "" : "§7ПКМ §5-1")
                        .lore(bst.limit < 0 ? "" : "§7Шифт+ПКМ §d-10")
                        .build(), e -> {
                        switch (e.getClick()) {
                            case LEFT -> bst.limit++;
                            case SHIFT_LEFT -> bst.limit += 10;
                            case RIGHT -> bst.limit--;
                            case SHIFT_RIGHT -> bst.limit -= 10;
                        }
                        if (bst.limit < 0) {
                            bst.limit = -1;
                        }
                        saveConfig();
                        reopen(p, content);
                    }));
                }
            }


            pagination.setItems(menuEntry.toArray(new ClickableItem[menuEntry.size()]));
            pagination.setItemsPerPage(45);

            content.set(5, 4, ClickableItem.of(new ItemBuilder(Material.OAK_DOOR).name("назад").build(), e -> {
                    mode = MenuMode.main;
                    openMenu(p);//reopen(p, content);
                }
            ));


            if (!pagination.isLast()) {
                content.set(5, 8, ClickableItem.of(ItemUtils.nextPage, e
                    -> content.getHost().open(p, pagination.next().getPage()))
                );
            }

            if (!pagination.isFirst()) {
                content.set(5, 0, ClickableItem.of(ItemUtils.previosPage, e
                    -> content.getHost().open(p, pagination.previous().getPage()))
                );
            }
//.ArrayIndexOutOfBoundsException: arraycopy: length -39 is negative если открывал вторую страничку
            pagination.addToIterator(content.newIterator(SlotIterator.Type.HORIZONTAL, SlotPos.of(0, 0)).allowOverride(false));

        }
    }


}







    /*
    private static void checkBlockStates(final Chunk chunk) {

        final Map <LimitType,Integer> counter = new HashMap<>();

        LimitType limittype;

            for (final BlockState blockState : chunk.getTileEntities()) {

                limittype = LimitType.getType(blockState.getType());

                if (limittype!=null && limittype.limit>0) {
                    if (counter.containsKey(limittype)) {
                        if (counter.get(limittype)>=limittype.limit) {
                            blockState.getBlock().setType(Material.AIR);
                            //if (p!=null) p.sendMessage("§cЛимит "+limittype.toString()+" в чанке: "+limittype.limit);
                            Ostrov.log_ok("§eremove blockState on ChunkLoad! size"+chunk.getTileEntities().length+" mat="+blockState.getType().toString()+" type="+limittype.toString()+" count="+counter.get(limittype) );
                        } else {
                            counter.put(limittype, counter.get(limittype)+1);
                        }
                    } else {
                        counter.put(limittype, 1);
                    }
                }

            }

        counter.clear();

    }*/




          /*  final String clickedBlock = e.getClickedBlock().getType().toString();
            switch (e.getItem().getType()) {
              case MINECART:
                e.setCancelled(true);
                if (clickedBlock.contains("RAIL")) {
                  entity = p.getWorld().spawn(e.getClickedBlock().getRelative(BlockFace.UP).getLocation(), Minecart.class);
                }
                break;
              case CHEST_MINECART:
                e.setCancelled(true);
                if (clickedBlock.contains("RAIL")) {
                  entity = p.getWorld().spawn(e.getClickedBlock().getRelative(BlockFace.UP).getLocation(), StorageMinecart.class);
                }
                break;
              case FURNACE_MINECART:
                e.setCancelled(true);
                if (clickedBlock.contains("RAIL")) {
                  entity = p.getWorld().spawn(e.getClickedBlock().getRelative(BlockFace.UP).getLocation(), PoweredMinecart.class);
                }
                break;
              case TNT_MINECART:
                e.setCancelled(true);
                if (clickedBlock.contains("RAIL")) {
                  entity = p.getWorld().spawn(e.getClickedBlock().getRelative(BlockFace.UP).getLocation(), ExplosiveMinecart.class);
                }
                break;
              case HOPPER_MINECART:
                e.setCancelled(true);
                if (clickedBlock.contains("RAIL")) {
                  entity = p.getWorld().spawn(e.getClickedBlock().getRelative(BlockFace.UP).getLocation(), HopperMinecart.class);
                }
                break;
              case COMMAND_BLOCK_MINECART:
                e.setCancelled(true);
                //if (e.getClickedBlock().getType().toString().contains("RAIL")) entity = p.getWorld().spawn(e.getClickedBlock().getRelative(BlockFace.UP).getLocation(), Minecart.class);
                break;

              case OAK_BOAT:
              case ACACIA_BOAT:
              case BIRCH_BOAT:
              case SPRUCE_BOAT:
              case JUNGLE_BOAT:
              case DARK_OAK_BOAT:
                entity = p.getWorld().spawn(e.getClickedBlock().getRelative(BlockFace.UP).getLocation(), Boat.class);
                switch (e.getItem().getType()) {
                  case OAK_BOAT:
                    ((Boat) entity).setBoatType(Type.OAK);
                    break;
                  case DARK_OAK_BOAT:
                    ((Boat) entity).setBoatType(Type.DARK_OAK);
                    break;
                  case ACACIA_BOAT:
                    ((Boat) entity).setBoatType(Type.ACACIA);
                    break;
                  case BIRCH_BOAT:
                    ((Boat) entity).setBoatType(Type.BIRCH);
                    break;
                  case SPRUCE_BOAT:
                    ((Boat) entity).setBoatType(Type.SPRUCE);
                    break;
                  case JUNGLE_BOAT:
                    ((Boat) entity).setBoatType(Type.JUNGLE);
                    break;
                  case CHERRY_BOAT:
                    ((Boat) entity).setBoatType(Type.CHERRY);
                    break;
                  case MANGROVE_BOAT:
                    ((Boat) entity).setBoatType(Type.MANGROVE);
                    break;
                  default:
                    break;
                }
                break;
              default:
                break;
            }
            if (entity != null) {
              if (p.getInventory().getItemInMainHand().getType() == e.getItem().getType()) {
                p.getInventory().setItemInMainHand(null);
              } else {
                p.getInventory().setItemInOffHand(null);
              }
              final Oplayer op = PM.getOplayer(e.getPlayer());

              if (entity.getType().toString().contains("MINECART")) {

                if (op.minecart != null && op.minecart.get() != null) {
                  op.minecart.get().remove();
                  op.minecart.clear();
                }
                op.minecart = new WeakReference<>(entity);

              } else if (entity.getType() == EntityType.BOAT) {
                if (op.boat != null && op.boat.get() != null) {
                  op.boat.get().remove();
                  op.boat.clear();
                }
                op.boat = new WeakReference<>(entity);
              }
          }
*/

  /*   private VehicleInfo getVehicleInfo(final Player p) {
        VehicleInfo vi = vehicleInfo.get(p.getName());
        if (vi==null) {
            vi = new VehicleInfo(p);
        }
        vehicleInfo.put(p.getName(), vi);
        return vi;
    }


    private VehicleInfo findByMinecartCoord(final String coord) {
        for (final VehicleInfo vi : vehicleInfo.values()) {
            if ( !vi.placeMinecartCoord.isEmpty() && vi.placeMinecartCoord.equals(coord) ) return vi;
        }
        return null;
    }

    private VehicleInfo findByBoatCoord(final String coord) {
        for (final VehicleInfo vi : vehicleInfo.values()) {
            if ( !vi.placeBoatCoord.isEmpty() && vi.placeBoatCoord.equals(coord)) return vi;
        }
        return null;
    }*/