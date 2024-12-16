package ru.komiss77.builder.menu;

import java.util.*;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.ApiOstrov;
import ru.komiss77.utils.EntityUtil;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.NumUtil;
import ru.komiss77.utils.inventory.*;
import ru.komiss77.utils.inventory.InputButton.InputType;


public class EntityTypeMenu implements InventoryProvider {


    private static final ItemStack fill = new ItemBuilder(Material.YELLOW_STAINED_GLASS_PANE).name("§8.").build();

    private final World world;
    private int radius;
    private final EntityType type;


    public EntityTypeMenu(final World world, final int radius, final EntityType type) {
        this.world = world;
        this.radius = radius;
        this.type = type;
    }


    @Override
    public void init(final Player p, final InventoryContent contents) {
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
        contents.fillRow(4, ClickableItem.empty(EntityTypeMenu.fill));

        final Pagination pagination = contents.pagination();
        final ArrayList<ClickableItem> menuEntry = new ArrayList<>();


        final Map<Entity, Integer> entitys = new HashMap<>();
        double d;

        if (radius > 0) {

            for (final Entity e : p.getNearbyEntities(radius, radius, radius)) {
                if (e.getType() == type) {
                    d = p.getLocation().distance(e.getLocation());
                    //entitys.put(e, LocationUtil.getDistance(p.getLocation(), e.getLocation()));
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


        for (final int dist : distances) {
            for (final Entity entity : entitys.keySet()) {
                if (entitys.get(entity) == dist) {

                    menuEntry.add(ClickableItem.of(new ItemBuilder(Material.ENDER_EYE)
                        .name("§f" + entity.getLocation().getBlockX() + " §7: §f" + entity.getLocation().getBlockY() + " §7: §f" + entity.getLocation().getBlockZ())
                        .lore("§7Дистанция: " + (entitys.get(entity) == -1 ? "§8другой мир" : "§b" + entitys.get(entity)))
                        .lore("§7")
                        .lore("§7ЛКМ - ТП к сущности")
                        .lore("§7ПКМ - изменить характеристики")
                        .lore("§7Шифт+ЛКМ - призвать")
                        .lore("§7Шифт+ПКМ - удалить")
                        .lore("§7")
                        .build(), e -> {
//Ostrov.log("CLICK="+e.getClick());
                        if (!ApiOstrov.isLocalBuilder(p, true)) return;
                        switch (e.getClick()) {
                            case LEFT -> p.teleport(entity);
                            case RIGHT -> {
                                EntitySetup.openSetupMenu(p, entity);
                            }
                            case SHIFT_LEFT -> entity.teleport(p);
                            case SHIFT_RIGHT -> entity.remove();

                        }

                        if (e.getClick() != ClickType.RIGHT) reopen(p, contents);
                    }));
                }
            }
        }


        pagination.setItems(menuEntry.toArray(new ClickableItem[menuEntry.size()]));
        pagination.setItemsPerPage(36);


        contents.set(5, 2, new InputButton(InputType.ANVILL, new ItemBuilder(Material.FLOWER_BANNER_PATTERN)
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
                .provider(new EntityGroupMenu(p.getLocation(), radius, EntityUtil.group(type)))
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
