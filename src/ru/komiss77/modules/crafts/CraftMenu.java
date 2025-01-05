package ru.komiss77.modules.crafts;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.*;
import ru.komiss77.OStrap;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.crafts.Crafts.Craft;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.TCUtil;
import ru.komiss77.utils.inventory.*;



public class CraftMenu implements InventoryProvider {

    private static final ItemStack[] invIts;
    private static final int rad = 3;

    private final String key;
    private final boolean view;

    private ItemType tp;

    static {
        invIts = new ItemStack[27];
        for (int i = 0; i < 27; i++) {
            switch (i) {
                case 13:
                    invIts[13] = new ItemBuilder(ItemType.IRON_NUGGET).name("§7->").build();
                    break;
                case 9:
                    invIts[9] = new ItemBuilder(ItemType.CHEST).name("§dФормированый").build();
                    break;
                default:
                    invIts[i] = new ItemBuilder(ItemType.LIGHT_GRAY_STAINED_GLASS_PANE).name("§0.").build();
                    break;
            }
        }
    }


    public CraftMenu(final String key, final boolean view) {
        this.key = key;
        this.view = view;
        final Recipe rc = Crafts.getRecipe(new NamespacedKey(OStrap.space, key), Recipe.class);
        tp = switch (rc) {
            case ShapelessRecipe ignored -> ItemType.ENDER_CHEST;
            case FurnaceRecipe ignored -> ItemType.FURNACE;
            case SmokingRecipe ignored -> ItemType.SMOKER;
            case BlastingRecipe ignored -> ItemType.BLAST_FURNACE;
            case CampfireRecipe ignored -> ItemType.CAMPFIRE;
            case SmithingRecipe ignored -> ItemType.SMITHING_TABLE;
            case StonecuttingRecipe ignored -> ItemType.STONECUTTER;
            case null, default -> ItemType.CHEST;
        };
    }

    @Override
    public void init(final Player p, final InventoryContent its) {
        final Inventory inv = its.getInventory();
        if (inv != null) inv.setContents(invIts);
        final Recipe rc = Crafts.getRecipe(new NamespacedKey(OStrap.space, key), Recipe.class);
//        p.sendMessage("k=" + new NamespacedKey(Crafts.space, key) + ", f=" + rc + ", " + Crafts.crafts.write());
        its.set(9, rc == null ? ClickableItem.of(makeIcon(tp), e -> {
            if (ItemType.ENDER_CHEST.equals(tp)) {
                tp = ItemType.FURNACE;
            } else if (ItemType.FURNACE.equals(tp)) {
                tp = ItemType.SMOKER;
            } else if (ItemType.SMOKER.equals(tp)) {
                tp = ItemType.BLAST_FURNACE;
            } else if (ItemType.BLAST_FURNACE.equals(tp)) {
                tp = ItemType.CAMPFIRE;
            } else if (ItemType.CAMPFIRE.equals(tp)) {
                tp = ItemType.SMITHING_TABLE;
            } else if (ItemType.SMITHING_TABLE.equals(tp)) {
                tp = ItemType.STONECUTTER;
            } else if (ItemType.STONECUTTER.equals(tp)) {
                tp = ItemType.CHEST;
            } else {//if ItemType.CHEST.equals(tp)
                tp = ItemType.ENDER_CHEST;
            }
            reopen(p, its);
        }) : ClickableItem.empty(makeIcon(tp)));
        its.set(16, view ? ClickableItem.empty(ItemType.LIGHT_GRAY_STAINED_GLASS_PANE.createItemStack()) :
            ClickableItem.from(new ItemBuilder(ItemType.GREEN_CONCRETE_POWDER).name("§aГотово!").build(), e -> {
                if (e.getEvent() instanceof InventoryClickEvent) {
                    ((InventoryClickEvent) e.getEvent()).setCancelled(true);
                }
                final ItemStack rst = inv.getItem(14);
                if (ItemUtil.isBlank(rst, false)) {
                    p.sendMessage("§cСначала закончите крафт!");
                    return;
                }

                //запоминание крафта
                final YamlConfiguration craftConfig = YamlConfiguration.loadConfiguration(new File(Ostrov.instance.getDataFolder().getAbsolutePath() + "/crafts/craft.yml"));
                craftConfig.set(key, null);
                craftConfig.set(key + ".result", ItemUtil.write(rst));
                //craftConfig.set(key + ".world", Ostrov.subServer.write());
                craftConfig.set(key + ".type", getRecType(tp));
                final ConfigurationSection cs = craftConfig.getConfigurationSection(key);
                final NamespacedKey nKey = new NamespacedKey(OStrap.space, key);
                final Recipe nrc;
                final ItemStack it;
                final String[] shp;
                if (ItemType.FURNACE.equals(tp)) {
                    it = inv.getItem(11);
                    if (ItemUtil.isBlank(it, false)) {
                        p.sendMessage("§cСначала закончите крафт!");
                        return;
                    }
                    cs.set("recipe.a", ItemUtil.write(it));
                    nrc = new FurnaceRecipe(nKey, rst, IdChoice.of(it), 0.5f, 200);
                    Bukkit.removeRecipe(nKey);
                    Bukkit.addRecipe(nrc);
                } else if (ItemType.SMOKER.equals(tp)) {
                    it = inv.getItem(11);
                    if (ItemUtil.isBlank(it, false)) {
                        p.sendMessage("§cСначала закончите крафт!");
                        return;
                    }
                    cs.set("recipe.a", ItemUtil.write(it));
                    nrc = new SmokingRecipe(nKey, rst, IdChoice.of(it), 0.5f, 100);
                    Bukkit.removeRecipe(nKey);
                    Bukkit.addRecipe(nrc);
                } else if (ItemType.BLAST_FURNACE.equals(tp)) {
                    it = inv.getItem(11);
                    if (ItemUtil.isBlank(it, false)) {
                        p.sendMessage("§cСначала закончите крафт!");
                        return;
                    }
                    cs.set("recipe.a", ItemUtil.write(it));
                    nrc = new BlastingRecipe(nKey, rst, IdChoice.of(it), 0.5f, 100);
                    Bukkit.removeRecipe(nKey);
                    Bukkit.addRecipe(nrc);
                } else if (ItemType.CAMPFIRE.equals(tp)) {
                    it = inv.getItem(11);
                    if (ItemUtil.isBlank(it, false)) {
                        p.sendMessage("§cСначала закончите крафт!");
                        return;
                    }
                    cs.set("recipe.a", ItemUtil.write(it));
                    nrc = new CampfireRecipe(nKey, rst, IdChoice.of(it), 0.5f, 500);
                    Bukkit.removeRecipe(nKey);
                    Bukkit.addRecipe(nrc);
                } else if (ItemType.SMITHING_TABLE.equals(tp)) {
                    it = inv.getItem(10);
                    final ItemStack scd = inv.getItem(12);
                    final ItemStack tpl = inv.getItem(2);
                    if (ItemUtil.isBlank(it, false) || ItemUtil.isBlank(scd, false)) {
                        p.sendMessage("§cСначала закончите крафт!");
                        return;
                    }
                    cs.set("recipe.a", ItemUtil.write(it));
                    cs.set("recipe.b", ItemUtil.write(scd));
                    cs.set("recipe.c", ItemUtil.write(tpl));
                    nrc = new SmithingTransformRecipe(nKey, rst, IdChoice.of(tpl), IdChoice.of(it), IdChoice.of(scd), false);
                    Bukkit.removeRecipe(nKey);
                    Bukkit.addRecipe(nrc);
                } else if (ItemType.STONECUTTER.equals(tp)) {
                    it = inv.getItem(11);
                    if (ItemUtil.isBlank(it, false)) {
                        p.sendMessage("§cСначала закончите крафт!");
                        return;
                    }
                    cs.set("recipe.a", ItemUtil.write(it));
                    nrc = new StonecuttingRecipe(nKey, rst, IdChoice.of(it));
                    Bukkit.removeRecipe(nKey);
                    Bukkit.addRecipe(nrc);
                } else if (ItemType.ENDER_CHEST.equals(tp)) {
                    final ShapelessRecipe lrs = new ShapelessRecipe(nKey, rst);
                    shp = new String[]{"abc", "def", "ghi"};
                    for (byte cy = 0; cy < 3; cy++) {
                        for (byte cx = 1; cx < 4; cx++) {
                            final ItemStack ti = inv.getItem(cy * 9 + cx);
                            if (!ItemUtil.isBlank(ti, false)) {
                                lrs.addIngredient(IdChoice.of(ti));
                                cs.set("recipe." + shp[cy].charAt(cx - 1), ItemUtil.write(ti));
                            }
                        }
                    }
                    nrc = lrs;
                    Bukkit.removeRecipe(nKey);
                    Bukkit.addRecipe(nrc);
                } else {//if ItemType.CHEST.equals(tp) - тоже магия
                    final ShapedRecipe srs = new ShapedRecipe(nKey, rst);
                    final ItemStack[] rcs = new ItemStack[rad * rad];
                    int xMin = -1, xMax = -1, yMin = -1, yMax = -1;
                    for (int cx = 0; cx < rad; cx++) {
                        for (int cy = 0; cy < rad; cy++) {
                            final ItemStack ti = inv.getItem(cy * 9 + cx + 1);
                            if (!ItemUtil.isBlank(ti, false)) {
                                if (xMin == -1 || xMin > cx) xMin = cx;
                                if (yMin == -1 || yMin > cy) yMin = cy;
                                if (xMax < cx) xMax = cx;
                                if (yMax < cy) yMax = cy;
                            }
                            rcs[cy * rad + cx] = ti;
                        }
                    }

                    if (xMin == -1 || yMin == -1) {
                        p.sendMessage("§cСначала закончите крафт!");
                        return;
                    }

                    shp = makeShape(xMax + 1 - xMin, yMax + 1 - yMin);
                    final StringBuilder sb = new StringBuilder(shp.length * (xMax + 1 - xMin));
                    for (final String s : shp) sb.append(":").append(s);
                    cs.set("shape", sb.substring(1));
                    srs.shape(shp);

                    for (int cx = xMax; cx >= xMin; cx--) {
                        for (int cy = yMax; cy >= yMin; cy--) {
                            final ItemStack ti = rcs[cy * rad + cx];
                            if (!ItemUtil.isBlank(ti, false)) {
                                srs.setIngredient(shp[cy - yMin].charAt(cx - xMin), IdChoice.of(ti));
                                cs.set("recipe." + shp[cy - yMin].charAt(cx - xMin), ItemUtil.write(ti));
                            }
                        }
                    }
                    nrc = srs;
                    Bukkit.removeRecipe(nKey);
                    Bukkit.addRecipe(srs);
                }

                Crafts.crafts.put(nKey, new Craft(nrc, pl -> true));

                try {
                    craftConfig.save(new File(Ostrov.instance.getDataFolder().getAbsolutePath() + "/crafts/craft.yml"));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                p.sendMessage(TCUtil.form(Ostrov.PREFIX + "§7Крафт §к" + key + " §7завершен!"));
                p.closeInventory();
            }));
        //final ClickableItem cl = ClickableItem.from(ItemUtil.air, e -> e.setCurrentItem(e.getCursor().asOne()));
        final Consumer<ItemClickData> canEdit = e -> {
            if (e.getEvent() instanceof InventoryClickEvent)
                ((InventoryClickEvent) e.getEvent()).setCancelled(view);
        };

        final Set<ItemType> COOKING = Set.of(ItemType.FURNACE, ItemType.SMOKER, ItemType.BLAST_FURNACE, ItemType.CAMPFIRE);
        if (COOKING.contains(tp)) {
            if (rc == null) {
                setEditSlot(SlotPos.of(1, 2), null, its, canEdit);

                setEditSlot(SlotPos.of(1, 5), null, its, canEdit);
            } else {
                setEditSlot(SlotPos.of(1, 2), ((IdChoice) ((CookingRecipe<?>) rc).getInputChoice()).getItemStack(), its, canEdit);

                setEditSlot(SlotPos.of(1, 5), rc.getResult(), its, canEdit);
            }
        } else if (ItemType.SMITHING_TABLE.equals(tp)) {
            if (rc == null) {
                setEditSlot(SlotPos.of(0, 2), null, its, canEdit);
                setEditSlot(SlotPos.of(1, 1), null, its, canEdit);
                setEditSlot(SlotPos.of(1, 3), null, its, canEdit);

                setEditSlot(SlotPos.of(1, 5), null, its, canEdit);
            } else {
                setEditSlot(SlotPos.of(0, 2), ((IdChoice) ((SmithingTransformRecipe) rc).getTemplate()).getItemStack(), its, canEdit);
                setEditSlot(SlotPos.of(1, 1), ((IdChoice) ((SmithingTransformRecipe) rc).getBase()).getItemStack(), its, canEdit);
                setEditSlot(SlotPos.of(1, 3), ((IdChoice) ((SmithingTransformRecipe) rc).getAddition()).getItemStack(), its, canEdit);

                setEditSlot(SlotPos.of(1, 5), rc.getResult(), its, canEdit);
            }
        } else if (ItemType.STONECUTTER.equals(tp)) {
            if (rc == null) {
                setEditSlot(SlotPos.of(1, 2), null, its, canEdit);

                setEditSlot(SlotPos.of(1, 5), null, its, canEdit);
            } else {
                setEditSlot(SlotPos.of(1, 2), ((IdChoice) ((StonecuttingRecipe) rc).getInputChoice()).getItemStack(), its, canEdit);

                setEditSlot(SlotPos.of(1, 5), rc.getResult(), its, canEdit);
            }
        } else if (ItemType.ENDER_CHEST.equals(tp)) {
            if (rc == null) {
                setEditSlot(SlotPos.of(0, 1), null, its, canEdit);
                setEditSlot(SlotPos.of(0, 2), null, its, canEdit);
                setEditSlot(SlotPos.of(0, 3), null, its, canEdit);
                setEditSlot(SlotPos.of(1, 1), null, its, canEdit);
                setEditSlot(SlotPos.of(1, 2), null, its, canEdit);
                setEditSlot(SlotPos.of(1, 3), null, its, canEdit);
                setEditSlot(SlotPos.of(2, 1), null, its, canEdit);
                setEditSlot(SlotPos.of(2, 2), null, its, canEdit);
                setEditSlot(SlotPos.of(2, 3), null, its, canEdit);

                setEditSlot(SlotPos.of(1, 5), null, its, canEdit);
            } else {
                final Iterator<RecipeChoice> rci = ((ShapelessRecipe) rc).getChoiceList().iterator();
                setEditSlot(SlotPos.of(0, 1), rci.hasNext() ? ((IdChoice) rci.next()).getItemStack() : null, its, canEdit);
                setEditSlot(SlotPos.of(0, 2), rci.hasNext() ? ((IdChoice) rci.next()).getItemStack() : null, its, canEdit);
                setEditSlot(SlotPos.of(0, 3), rci.hasNext() ? ((IdChoice) rci.next()).getItemStack() : null, its, canEdit);
                setEditSlot(SlotPos.of(1, 1), rci.hasNext() ? ((IdChoice) rci.next()).getItemStack() : null, its, canEdit);
                setEditSlot(SlotPos.of(1, 2), rci.hasNext() ? ((IdChoice) rci.next()).getItemStack() : null, its, canEdit);
                setEditSlot(SlotPos.of(1, 3), rci.hasNext() ? ((IdChoice) rci.next()).getItemStack() : null, its, canEdit);
                setEditSlot(SlotPos.of(2, 1), rci.hasNext() ? ((IdChoice) rci.next()).getItemStack() : null, its, canEdit);
                setEditSlot(SlotPos.of(2, 2), rci.hasNext() ? ((IdChoice) rci.next()).getItemStack() : null, its, canEdit);
                setEditSlot(SlotPos.of(2, 3), rci.hasNext() ? ((IdChoice) rci.next()).getItemStack() : null, its, canEdit);

                setEditSlot(SlotPos.of(1, 5), rc.getResult(), its, canEdit);
            }
        } else {//if ItemType.CHEST.equals(tp)
            if (rc == null) {
                setEditSlot(SlotPos.of(0, 1), null, its, canEdit);
                setEditSlot(SlotPos.of(0, 2), null, its, canEdit);
                setEditSlot(SlotPos.of(0, 3), null, its, canEdit);
                setEditSlot(SlotPos.of(1, 1), null, its, canEdit);
                setEditSlot(SlotPos.of(1, 2), null, its, canEdit);
                setEditSlot(SlotPos.of(1, 3), null, its, canEdit);
                setEditSlot(SlotPos.of(2, 1), null, its, canEdit);
                setEditSlot(SlotPos.of(2, 2), null, its, canEdit);
                setEditSlot(SlotPos.of(2, 3), null, its, canEdit);

                setEditSlot(SlotPos.of(1, 5), null, its, canEdit);
            } else {
                final String[] shp = ((ShapedRecipe) rc).getShape();
                final Map<Character, RecipeChoice> rcm = ((ShapedRecipe) rc).getChoiceMap();
                for (int r = 0; r < rad; r++) {
                    final String sr = shp.length > r ? shp[r] : "";
                    for (int c = 0; c < rad; c++) {
                        final RecipeChoice chs = rcm.get(sr.length() > c ? sr.charAt(c) : 'w');
                        setEditSlot(SlotPos.of(r, c + 1), chs == null ? ItemUtil.air : ((IdChoice) chs).getItemStack(), its, canEdit);
                    }
                }

                setEditSlot(SlotPos.of(1, 5), rc.getResult(), its, canEdit);
            }
        }
    }

    private static final String dsp = "abcdefghi";
    private static String[] makeShape(final int dX, final int dY) {
        final String[] sp = new String[dY];
        for (int i = 0; i < dY; i++) {
            sp[i] = dsp.substring(i * dX, i * dX + dX);
        }
        return sp;
    }


    private void setEditSlot(final SlotPos slot, final ItemStack it, final InventoryContent its, Consumer<ItemClickData> canEdit) {
        its.set(slot, ClickableItem.from(ItemUtil.isBlank(it, false) ? ItemUtil.air : it, canEdit));
        its.setEditable(slot, !view);
    }

    private ItemStack makeIcon(final ItemType mt) {
        if (ItemType.ENDER_CHEST.equals(tp)) return new ItemBuilder(ItemType.ENDER_CHEST).name("§5Безформенный").build();
        if (ItemType.FURNACE.equals(tp)) return new ItemBuilder(ItemType.FURNACE).name("§6Печевой").build();
        if (ItemType.SMOKER.equals(tp)) return new ItemBuilder(ItemType.SMOKER).name("§cЗапекающий").build();
        if (ItemType.BLAST_FURNACE.equals(tp)) return new ItemBuilder(ItemType.BLAST_FURNACE).name("§7Плавильный").build();
        if (ItemType.CAMPFIRE.equals(tp)) return new ItemBuilder(ItemType.CAMPFIRE).name("§eКостерный").build();
        if (ItemType.SMITHING_TABLE.equals(tp)) return new ItemBuilder(ItemType.SMITHING_TABLE).name("§fКующий").build();
        if (ItemType.STONECUTTER.equals(tp)) return new ItemBuilder(ItemType.STONECUTTER).name("§7Режущий").build();
        /*if ItemType.CHEST.equals(tp)*/ return new ItemBuilder(ItemType.CHEST).name("§dФормированый").build();
    }

    private String getRecType(final ItemType m) {
        if (ItemType.ENDER_CHEST.equals(tp)) return "noshape";
        if (ItemType.FURNACE.equals(tp)) return "furnace";
        if (ItemType.SMOKER.equals(tp)) return "smoker";
        if (ItemType.BLAST_FURNACE.equals(tp)) return "blaster";
        if (ItemType.CAMPFIRE.equals(tp)) return "campfire";
        if (ItemType.SMITHING_TABLE.equals(tp)) return "smith";
        if (ItemType.STONECUTTER.equals(tp)) return "cutter";
        /*if ItemType.CHEST.equals(tp)*/ return "shaped";
    }


}
