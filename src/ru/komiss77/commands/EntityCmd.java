package ru.komiss77.commands;

import java.util.*;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemType;
import ru.komiss77.ApiOstrov;
import ru.komiss77.builder.SetupMode;
import ru.komiss77.builder.menu.EntitySetup;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.translate.Lang;
import ru.komiss77.objects.ValueSortedMap;
import ru.komiss77.utils.EntityUtil;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.NumUtil;
import ru.komiss77.utils.inventory.*;

public class EntityCmd implements Listener, OCommand {

    @Override
    public LiteralCommandNode<CommandSourceStack> command() {
        return Commands.literal("entity").executes(cntx -> {
            final CommandSender cs = cntx.getSource().getSender();
            if (!(cs instanceof final Player p)) {
                cs.sendMessage("§eНе консольная команда!");
                return 0;
            }

            if (!ApiOstrov.isLocalBuilder(p)) {//просмотр доступен всем - зачем? это нигде не используется вроде
              SmartInventory          //скайблок,ванблок,аркаим, да и на других может пригодиться
                  .builder()
                  .id("EntityMain" + p.getName())
                  .provider(new EntityWorldView(List.of(p.getWorld()), -1))
                  .size(3, 9)
                  .title("§2Сущности " + p.getWorld().getName())
                  .build()
                  .open(p);
              //p.sendMessage("§cДоступно только персоналу!");
              return 0;
            }

          final Oplayer op = PM.getOplayer(p);
          if (op.setup == null) {
            op.setup = new SetupMode(p);
          }

          PM.getOplayer(p).setup.openEntityWorldMenu(p, p.getWorld(), -1);

            return Command.SINGLE_SUCCESS;
        }).build();
    }

  //для скайблока - перенос в скайблок пж, не мусорим Остров  //там есть нмс вещи, остров для этого и нужен
    public static void openByWorldList(final Player p, final List<World> worlds) {
        SmartInventory
            .builder()
            .id("EntityMain" + p.getName())
            .provider(new EntityWorldView(worlds, -1))
            .size(3, 9)
            .title("§2Сущности миров")
            .build()
            .open(p);
    }

    @Override
    public Set<String> aliases() {
        return Set.of();
    }

    @Override
    public String description() {
        return "Прoсмотр сущностей";
    }
}

class EntityServerView implements InventoryProvider {

    private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(ItemType.YELLOW_STAINED_GLASS_PANE).name("§8.").build());
    private final List<World> worlds;

    //не менять, скайблок открывает со списком миров!!!
    public EntityServerView(final List<World> worlds) {
        this.worlds = worlds;
    }

    @Override
    public void init(final Player p, final InventoryContent contents) {//эта менюшка откроется только у билдера
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
        contents.fillRow(4, fill);

        final Pagination pagination = contents.pagination();
        final ArrayList<ClickableItem> menuEntry = new ArrayList<>();

        final Map<EntityUtil.EntityGroup, Integer> count = new HashMap<>();
        int total = 0;

        for (final World world : worlds) {

            EntityUtil.EntityGroup group;
            for (final Entity e : world.getEntities()) {
                if (e.getType() == EntityType.PLAYER) continue;
                group = EntityUtil.group(e);
                if (count.containsKey(group)) {
                    count.put(group, count.get(group) + 1);
                } else {
                    count.put(group, 1);
                }
                total++;
            }

            menuEntry.add(ClickableItem.of(new ItemBuilder(Material.HEART_OF_THE_SEA)
                .name("§f" + world.getName())
                .lore("§7")
                .lore("§7Найдено всего: §e" + total)
                .lore(EntityUtil.EntityGroup.MONSTER.displayName + " §7: §6" + (count.containsKey(EntityUtil.EntityGroup.MONSTER) ? "§e" + count.get(EntityUtil.EntityGroup.MONSTER) : "не найдено"))
                .lore(EntityUtil.EntityGroup.CREATURE.displayName + " §7: §6" + (count.containsKey(EntityUtil.EntityGroup.CREATURE) ? "§e" + count.get(EntityUtil.EntityGroup.CREATURE) : "не найдено"))
                .lore(EntityUtil.EntityGroup.WATER_CREATURE.displayName + " §7: §6" + (count.containsKey(EntityUtil.EntityGroup.WATER_CREATURE) ? "§e" + count.get(EntityUtil.EntityGroup.WATER_CREATURE) : "не найдено"))
                .lore(EntityUtil.EntityGroup.AMBIENT.displayName + " §7: §6" + (count.containsKey(EntityUtil.EntityGroup.AMBIENT) ? "§e" + count.get(EntityUtil.EntityGroup.AMBIENT) : "не найдено"))
                .lore(EntityUtil.EntityGroup.WATER_AMBIENT.displayName + " §7: §6" + (count.containsKey(EntityUtil.EntityGroup.WATER_AMBIENT) ? "§e" + count.get(EntityUtil.EntityGroup.WATER_AMBIENT) : "не найдено"))
                .lore(EntityUtil.EntityGroup.UNDEFINED.displayName + " §7: §6" + (count.containsKey(EntityUtil.EntityGroup.UNDEFINED) ? "§e" + count.get(EntityUtil.EntityGroup.UNDEFINED) : "не найдено"))
                .lore("§7")
                .lore("§7ЛКМ - подробно по миру")
                .lore("§7")
                .build(), e -> {
                if (e.isLeftClick()) {
                    SmartInventory
                        .builder()
                        .id("EntityMain" + p.getName())
                        .provider(new EntityWorldView(List.of(world), -1))
                        .size(3, 9)
                        .title("§2Сущности " + world.getName())
                        .build()
                        .open(p);
                }
            }));


            count.clear();
            total = 0;


        }

        pagination.setItems(menuEntry.toArray(new ClickableItem[0]));
        pagination.setItemsPerPage(36);

        contents.set(5, 4, ClickableItem.of(new ItemBuilder(Material.OAK_DOOR).name("назад").build(), e ->
            p.closeInventory()
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

class EntityWorldView implements InventoryProvider {
    private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Material.YELLOW_STAINED_GLASS_PANE).name("§8.").build());
    private int radius;
    private final List<World> worlds;

    public EntityWorldView(final List<World> worlds, final int radius) {
        this.radius = radius;
        this.worlds = worlds;
    }

    @Override
    public void init(final Player p, final InventoryContent contents) {
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
        contents.fillBorders(fill);

        final Map<EntityUtil.EntityGroup, Integer> count = new HashMap<>();

        EntityUtil.EntityGroup group;

        if (radius > 0) {

            for (final Entity e : p.getNearbyEntities(radius, radius, radius)) {
                if (e.getType() == EntityType.PLAYER) continue;
                group = EntityUtil.group(e);//.VM.getNmsEntitygroup().getEntityType(e);
                if (count.containsKey(group)) {
                    count.put(group, count.get(group) + 1);
                } else {
                    count.put(group, 1);
                }
            }

        } else {

            for (World w : worlds) {
                for (final Entity e : w.getEntities()) {
                    if (e.getType() == EntityType.PLAYER) continue;
                    group = EntityUtil.group(e);//group=VM.getNmsEntitygroup().getEntityType(e);
                    if (count.containsKey(group)) {
                        count.put(group, count.get(group) + 1);
                    } else {
                        count.put(group, 1);
                    }
                }
            }

        }


        contents.set(0, 2, ClickableItem.of(new ItemBuilder(Material.SUNFLOWER)
            .name("§eПКМ - показать все миры")
            .lore("")
            .lore(ApiOstrov.isLocalBuilder(p, false) ? "§eПКМ - показать все миры" : "§eВключите режим билдера!")
            .lore("")
            .build(), e -> {
            if (e.isLeftClick()) {
                if (ApiOstrov.isLocalBuilder(p, true)) {
                    SmartInventory
                        .builder()
                        .id("EntityWorlds" + p.getName())
                        .provider(new EntityServerView(Bukkit.getWorlds()))
                        .size(6, 9)
                        .title("§2Сущности миров")
                        .build()
                        .open(p);
                }
            }
        }));


        if (worlds.size() == 1) { //просмотр для одного мира
            final World world = worlds.getFirst();
            contents.set(0, 4, new InputButton(InputButton.InputType.ANVILL, new ItemBuilder(Material.FLOWER_BANNER_PATTERN)
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

            int worldLimit;
            for (EntityUtil.EntityGroup g : EntityUtil.EntityGroup.values()) {
                worldLimit = EntityUtil.EntityGroup.getWorldSpawnLimit(world, g);
                contents.add(ClickableItem.of(new ItemBuilder(g.displayMat)
                    .name(g.displayName)
                    .lore("§7")
                    .lore("§f" + (count.containsKey(g) ? "§e" + count.get(g) : "не найдено"))
                    .lore("§7")
                    .lore("§7Лимит в настройках мира: §b" + (worldLimit > 0 ? worldLimit : "--"))
                    .lore("§7")
                    .lore("§7ЛКМ - группу подробно")
                    .lore("§7")
                    .build(), e -> {
                    if (e.isLeftClick()) {
                        SmartInventory.builder()
                            .id("EntityByGroup" + p.getName())
                            .provider(new EntityGroupView(world, radius, g))
                            .size(6, 9)
                            .title("§2" + world.getName() + " " + g.displayName + " §1r=" + radius)
                            .build()
                            .open(p);
                    }
                }));
            }


        } else { //общая инфа для нескольких миров (скайблок!)

            List<Component> lore = new ArrayList<>();
            for (World w : worlds) {
                lore.add(Component.text("§7- " + w.getName()));
            }
            contents.set(0, 4, ClickableItem.empty(new ItemBuilder(Material.FLOWER_BANNER_PATTERN)
                .name("§7Сущности в мирах :")
                .lore(lore)
                .build()
            ));
            for (EntityUtil.EntityGroup g : EntityUtil.EntityGroup.values()) {
                contents.add(ClickableItem.of(new ItemBuilder(g.displayMat)
                    .name(g.displayName)
                    .lore("§7")
                    .lore("§f" + (count.containsKey(g) ? "§e" + count.get(g) : "не найдено"))
                    .lore("§7")
                    //.lore("§7Лимит в настройках мира: §b" + (worldLimit > 0 ? worldLimit : "--"))
                    .lore("§7")
                    .lore("§7ЛКМ - группу подробно")
                    .lore("§7в текущем мире!")
                    .build(), e -> {
                    if (e.isLeftClick()) {
                        SmartInventory.builder()
                            .id("EntityByGroup" + p.getName())
                            .provider(new EntityGroupView(p.getWorld(), radius, g))
                            .size(6, 9)
                            .title("§2" + p.getWorld().getName() + " " + g.displayName + " §1r=" + radius)
                            .build()
                            .open(p);
                    }
                }));
            }

        }



    }

}

class EntityGroupView implements InventoryProvider {
    private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Material.YELLOW_STAINED_GLASS_PANE).name("§8.").build());
    private final World world;
    private int radius;
    private final EntityUtil.EntityGroup group;

    public EntityGroupView(final World world, final int radius, final EntityUtil.EntityGroup group) {
        this.world = world;
        this.radius = radius;
        this.group = group;
    }

    @Override
    public void init(final Player p, final InventoryContent contents) {
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
        contents.fillRow(4, fill);

        final Pagination pagination = contents.pagination();
        final ArrayList<ClickableItem> menuEntry = new ArrayList<>();

        final ValueSortedMap<EntityType, Integer> count = new ValueSortedMap<>();

        if (radius > 0) {

            for (final Entity e : p.getNearbyEntities(radius, radius, radius)) {
                if (e.getType() == EntityType.PLAYER) continue;
                if (EntityUtil.group(e.getType()) == group) {
                    if (count.containsKey(e.getType())) {
                        count.put(e.getType(), count.get(e.getType()) + 1);
                    } else {
                        count.put(e.getType(), 1);
                    }
                }
            }

        } else {

            for (final Entity e : world.getEntities()) {
                if (e.getType() == EntityType.PLAYER) continue;
                if (EntityUtil.group(e.getType()) == group) {
                    if (count.containsKey(e.getType())) {
                        count.put(e.getType(), count.get(e.getType()) + 1);
                    } else {
                        count.put(e.getType(), 1);
                    }
                }
            }

        }

        int find;
        for (final EntityType type : count.keySet()) {
            find = count.get(type);
            menuEntry.add(ClickableItem.of(ItemUtil.buildEntityIcon(type)
                .name(Lang.t(type, p)).maxStack(99).amount(find)
                .lore("§7")
                .lore("§7Найдено: §e" + find)
                .lore("§7")
                .lore("§7ЛКМ - подробно по типу")
                .lore("§7")
                .lore(ApiOstrov.isLocalBuilder(p, false) ? "§7Шифт+ПКМ - удалить всех мобов этого типа" : "")
                .build(), e -> {
                if (e.isLeftClick()) {
                    SmartInventory.builder()
                        .id("EntityByType" + p.getName())
                        .provider(new EntityTypeView(world, radius, type))
                        .size(6, 9)
                        .title("§2" + world.getName() + ", §6" + type + ", §1r=" + radius).build()
                        .open(p);
                }
            }));
        }

        contents.set(5, 2, new InputButton(InputButton.InputType.ANVILL, new ItemBuilder(Material.FLOWER_BANNER_PATTERN)
            .name("§7Группа" + group.toString() + " в мире §a" + world.getName() + (radius > 0 ? " §7r=§a" + radius : ""))
            .lore("§7")
            .lore("§7ЛКМ - изменить радиус")
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

        contents.set(5, 4, ClickableItem.of(new ItemBuilder(Material.OAK_DOOR).name("назад").build(), e ->
                p.performCommand("entity")
            /*SmartInventory.builder()
                .id("EntityMain" + p.getName())
                .provider(new EntityWorldView(p, radius))
                .size(3, 9)
                .title("§2Сущности " + world.getName() + " §1r=" + radius)
                .build()
                .open(p)*/
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

class EntityTypeView implements InventoryProvider {


    private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Material.YELLOW_STAINED_GLASS_PANE).name("§8.").build());
    private final World world;
    private int radius;
    private final EntityType type;


    public EntityTypeView(final World world, final int radius, final EntityType type) {
        this.world = world;
        this.radius = radius;
        this.type = type;
    }

    @Override
    public void init(final Player p, final InventoryContent contents) {
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
        contents.fillRow(4, fill);

        final Pagination pagination = contents.pagination();
        final ArrayList<ClickableItem> menuEntry = new ArrayList<>();


        final Map<Entity, Integer> entitys = new HashMap<>();
        double d;

        if (radius > 0) {

            for (final Entity e : p.getNearbyEntities(radius, radius, radius)) {
                if (e.getType() == type) {
                    d = p.getLocation().distance(e.getLocation());
                    //entitys.put(e, LocUtil.getDistance(p.getLocation(), e.getLocation()));
                    entitys.put(e, (int) Math.floor(d));
                }
            }

        } else {

            for (final Entity e : world.getEntities()) {
                if (e.getType() == type) {
                    if (p.getWorld().getName().equals(e.getWorld().getName())) {
                        d = p.getLocation().distance(e.getLocation());
                        entitys.put(e, (int) Math.floor(d));
                    } else {
                        entitys.put(e, -1);
                    }

                }
            }

        }

        final SortedSet<Integer> distances = new TreeSet<>(entitys.values());
        final boolean bld = ApiOstrov.isLocalBuilder(p, true);
        for (final int dist : distances) {
            for (final Entity entity : entitys.keySet()) {
                if (entitys.get(entity) == dist) {
                    if (bld) {
                        menuEntry.add(ClickableItem.of(new ItemBuilder(Material.ENDER_EYE)
                            .name("§f" + entity.getLocation().getBlockX() + " §7: §f" + entity.getLocation().getBlockY() + " §7: §f" + entity.getLocation().getBlockZ())
                            .lore("§7Дистанция: " + (entitys.get(entity) == -1 ? "§8другой мир" : "§b" + entitys.get(entity)))
                            .lore("§7")
                            .lore("§7ЛКМ - ТП к сущности")
                            .lore("§7ПКМ - изменить характеристики")
                            .lore("§7Шифт+ЛКМ - призвать")
                            .lore("§7Шифт+ПКМ - удалить")
                            .build(), e -> {
//Ostrov.log("CLICK="+e.getClick());
                            switch (e.getClick()) {
                                case LEFT -> p.teleport(entity);
                                case RIGHT -> SmartInventory.builder()
                                    .provider(new EntitySetup(entity))
                                    .size(6, 9)
                                    .title("§2Характеристики сущности").build()
                                    .open(p);
                                case SHIFT_LEFT -> entity.teleport(p);
                                case SHIFT_RIGHT -> entity.remove();
                                default -> {
                                }
                            }

                            if (e.getClick() != ClickType.RIGHT) reopen(p, contents);
                        }));
                        continue;
                    }

                    menuEntry.add(ClickableItem.empty(new ItemBuilder(Material.ENDER_EYE)
                        .name("§f" + entity.getLocation().getBlockX() + " §7: §f" + entity.getLocation().getBlockY() + " §7: §f" + entity.getLocation().getBlockZ())
                        .lore("§7Дистанция: " + (entitys.get(entity) == -1 ? "§8другой мир" : "§b" + entitys.get(entity)))
                        .build()));
                }
            }
        }


        pagination.setItems(menuEntry.toArray(new ClickableItem[0]));
        pagination.setItemsPerPage(36);


        contents.set(5, 2, new InputButton(InputButton.InputType.ANVILL, new ItemBuilder(Material.FLOWER_BANNER_PATTERN)
            .name("§fРадиус: §e" + (radius > 0 ? radius : " весь мир"))
            .lore("§7")
            .lore("§7ЛКМ - изменить радиус")
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


        contents.set(5, 4, ClickableItem.of(new ItemBuilder(Material.OAK_DOOR).name("назад").build(), e ->
            SmartInventory.builder()
                .id("EntityByGroup" + p.getName())
                .provider(new EntityGroupView(world, radius, EntityUtil.group(type)))
                .size(6, 9)
                .title("§2" + world.getName() + " " + type + " §1r=" + radius).build().open(p)
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
