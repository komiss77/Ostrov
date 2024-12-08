package ru.komiss77.modules.crafts;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Predicate;
import com.destroystokyo.paper.event.player.PlayerRecipeBookClickEvent;
import com.google.common.collect.Multimap;
import io.papermc.paper.event.player.PlayerStonecutterRecipeSelectEvent;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.block.Furnace;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.RecipeChoice.ExactChoice;
import org.bukkit.inventory.meta.ItemMeta;
import ru.komiss77.Cfg;
import ru.komiss77.Initiable;
import ru.komiss77.OStrap;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.items.SpecialItem;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.TCUtil;


public final class Crafts implements Initiable, Listener {

    public static final Map<NamespacedKey, Craft> crafts = new HashMap<>();

    public Crafts() {
        reload();
    }

    @Override
    public void postWorld() { //обход модулей после загрузки миров, т.к. не всё можно сделать onEnable
    }

    @Override
    public void reload() {
        HandlerList.unregisterAll(this);
        if (!Cfg.crafts) {
            Ostrov.log_ok("§6Крафты выключены!");
            return;
        }

        final Iterator<NamespacedKey> rki = Crafts.crafts.keySet().iterator();
        while (rki.hasNext()) {
            Bukkit.removeRecipe(rki.next());
            rki.remove();
        }

        Bukkit.getPluginManager().registerEvents(this, Ostrov.getInstance());
        Crafts.loadCrafts();
        new CraftCmd();

        Ostrov.log_ok("§2Крафты запущены!");
    }

    @Override
    public void onDisable() {
        if (!Cfg.crafts) {
            Ostrov.log_ok("§6Крафты выключены!");
            return;
        }

        Crafts.crafts.clear();
    }

    public static void loadCrafts() {
        //крафты
        final File dir = new File(Ostrov.instance.getDataFolder().getAbsolutePath() + "/crafts/");
        dir.mkdirs();
        try {
            new File(dir + File.separator + "craft.yml").createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (final File cfg : dir.listFiles()) {
            final ConsoleCommandSender css = Bukkit.getConsoleSender();
            final YamlConfiguration otherCrafts = YamlConfiguration.loadConfiguration(cfg);
            css.sendMessage("Found file " + cfg.getName());
            final Set<String> crfts = otherCrafts.getKeys(false);
            if (crfts.isEmpty()) {
                css.sendMessage("File empty...");
                return;
            }
            css.sendMessage("Найдено крафтов: " + crfts.size() + "!");
            for (final String key : crfts) {
                readCraft(otherCrafts.getConfigurationSection(key));
            }
        }
        //} catch (IOException ex) {
        //    Logger.getLogger(Crafts.class.name()).log(Level.SEVERE, null, ex);
        //}
    }

    public static void readCraft(final ConfigurationSection cs) {
        //ConfigurationSection cs = craftConfig.getConfigurationSection("crafts");
        final ItemStack resultItem = ItemUtil.parseItem(cs.getString("result"), "=");
        final NamespacedKey nsk = new NamespacedKey(OStrap.space, cs.getName());
        //cs = craftConfig.getConfigurationSection("crafts." + c + ".recipe");
        final Recipe recipe;
        final ItemStack it;
        switch (cs.getString("type")) {//(craftConfig.getString("crafts." + c + ".type")) {
            case "smoker":
                if (ItemUtil.isBlank((it = ItemUtil.parseItem(cs.getString("recipe.a"), "=")), false)) return;
                recipe = new SmokingRecipe(nsk, resultItem, CMDMatChoice.of(it), 0.5f, 100);
                break;
            case "blaster":
                if (ItemUtil.isBlank((it = ItemUtil.parseItem(cs.getString("recipe.a"), "=")), false)) return;
                recipe = new BlastingRecipe(nsk, resultItem, CMDMatChoice.of(it), 0.5f, 100);
                break;
            case "campfire":
                if (ItemUtil.isBlank((it = ItemUtil.parseItem(cs.getString("recipe.a"), "=")), false)) return;
                recipe = new CampfireRecipe(nsk, resultItem, CMDMatChoice.of(it), 0.5f, 500);
                break;
            case "furnace":
                if (ItemUtil.isBlank((it = ItemUtil.parseItem(cs.getString("recipe.a"), "=")), false)) return;
                recipe = new FurnaceRecipe(nsk, resultItem, CMDMatChoice.of(it), 0.5f, 200);
                break;
            case "cutter":
                if (ItemUtil.isBlank((it = ItemUtil.parseItem(cs.getString("recipe.a"), "=")), false)) return;
                recipe = new StonecuttingRecipe(nsk, resultItem, CMDMatChoice.of(it));
                break;
            case "smith":
                it = ItemUtil.parseItem(cs.getString("recipe.a"), "=");
                final ItemStack scd = ItemUtil.parseItem(cs.getString("recipe.b"), "=");
                if (ItemUtil.isBlank(it, false) || ItemUtil.isBlank(scd, false)) return;
                recipe = new SmithingTransformRecipe(nsk, resultItem, CMDMatChoice.of(
                    ItemUtil.parseItem(cs.getString("recipe.c"), "=")), CMDMatChoice.of(it), CMDMatChoice.of(scd), false);
                break;
            case "noshape":
                recipe = new ShapelessRecipe(nsk, resultItem);
                for (final String s : cs.getConfigurationSection("recipe").getKeys(false)) {
                    final ItemStack ii = ItemUtil.parseItem(cs.getString("recipe." + s), "=");
                    if (!ii.getType().isAir()) {
                        ((ShapelessRecipe) recipe).addIngredient(CMDMatChoice.of(ItemUtil.parseItem(cs.getString("recipe." + s), "=")));
                    }
                }
                break;
            case "shaped":
            default:
                recipe = new ShapedRecipe(nsk, resultItem);
                final String shp = cs.getString("shape");
                ((ShapedRecipe) recipe).shape(shp == null ? new String[]{"abc", "def", "ghi"} : shp.split(":"));
                for (final String s : cs.getConfigurationSection("recipe").getKeys(false)) {
                    ((ShapedRecipe) recipe).setIngredient(s.charAt(0), CMDMatChoice.of(ItemUtil.parseItem(cs.getString("recipe." + s), "=")));
                }
                break;
        }
        Bukkit.addRecipe(recipe);
        //final SubServer sv = SubServer.parseSubServer(cs.getString("world"));
        crafts.put(nsk, new Craft(recipe, p -> true));

    }

    @SuppressWarnings("unchecked")
    public static <G extends Recipe> G getRecipe(final NamespacedKey key, final Class<G> cls) {
        if (!key.getNamespace().equals(OStrap.space)) return null;
        final Craft rc = crafts.get(key);
        if (rc != null && cls.isAssignableFrom(rc.rec.getClass())) return (G) rc.rec;
        return null;
    }

    public static boolean rmvRecipe(final NamespacedKey key) {
        return crafts.remove(key) != null;
    }

    public static Recipe fakeRec(final Recipe rc) {
        if (rc instanceof Keyed) {
            final String ks = ((Keyed) rc).getKey().getKey();
            switch (rc) {
                case ShapedRecipe src:
                    final ShapedRecipe drc = new ShapedRecipe(new NamespacedKey(OStrap.space, ks), rc.getResult());
                    drc.shape(src.getShape());
                    for (final Entry<Character, RecipeChoice> en : src.getChoiceMap().entrySet()) {
                        if (en.getValue() == null) continue;
                        drc.setIngredient(en.getKey(), new ExactChoice(((CMDMatChoice) en.getValue()).getItemStack()));
                    }
                    return drc;
                case ShapelessRecipe src:
                    final ShapelessRecipe lrc = new ShapelessRecipe(new NamespacedKey(OStrap.space, ks), rc.getResult());
                    for (final RecipeChoice ch : src.getChoiceList()) {
                        if (ch == null) continue;
                        lrc.addIngredient(new ExactChoice(((CMDMatChoice) ch).getItemStack()));
                    }
                    return lrc;
                case final FurnaceRecipe src:
                    return new FurnaceRecipe(new NamespacedKey(OStrap.space, ks), src.getResult(),
                        new ExactChoice(((CMDMatChoice) src.getInputChoice()).getItemStack()), src.getExperience(), src.getCookingTime());
                case final SmokingRecipe src:
                    return new SmokingRecipe(new NamespacedKey(OStrap.space, ks), src.getResult(),
                        new ExactChoice(((CMDMatChoice) src.getInputChoice()).getItemStack()), src.getExperience(), src.getCookingTime());
                case final BlastingRecipe src:
                    return new BlastingRecipe(new NamespacedKey(OStrap.space, ks), src.getResult(),
                        new ExactChoice(((CMDMatChoice) src.getInputChoice()).getItemStack()), src.getExperience(), src.getCookingTime());
                case final CampfireRecipe src:
                    return new CampfireRecipe(new NamespacedKey(OStrap.space, ks), src.getResult(),
                        new ExactChoice(((CMDMatChoice) src.getInputChoice()).getItemStack()), src.getExperience(), src.getCookingTime());
                default:
                    return null;
            }
        }
        return null;
    }

    public static void discRecs(final Player p) {
        final List<NamespacedKey> rls = new ArrayList<>();
        for (final Entry<NamespacedKey, Craft> en : crafts.entrySet()) {
            if (en.getValue().canSee.test(p)) rls.add(en.getKey());
        }
        p.discoverRecipes(rls);
    }

    public record Craft(Recipe rec, Predicate<Player> canSee) {}

    @EventHandler
    public void onCraft(final InventoryClickEvent e) {
        if (e.getClickedInventory() instanceof CraftingInventory) {
            if (e.getSlotType() != InventoryType.SlotType.RESULT) return;
            final ItemStack fin = e.getCurrentItem();
            final SpecialItem si = SpecialItem.get(fin);
            if (si == null || !si.crafted()) return;
            e.setResult(Event.Result.DENY);
            e.setCurrentItem(ItemUtil.air);
            for (final HumanEntity he : e.getViewers()) {
                he.sendMessage(TCUtil.form(Ostrov.PREFIX + "<red>Эта реликвия уже создана!"));
            }
        }
    }

    @EventHandler
    public void onRecipe(final PrepareItemCraftEvent e) {
        final Recipe rc = e.getRecipe();
        if (rc == null) return;
        if (!e.isRepair() && rc instanceof Keyed) {
            if (rc instanceof ShapedRecipe) {
                final ShapedRecipe src = Crafts.getRecipe(((Keyed) rc).getKey(), ShapedRecipe.class);
                final ItemStack[] mtx = e.getInventory().getMatrix();
                if (src == null) {
                    for (final ItemStack it : mtx) {
                        if (ItemUtil.isBlank(it, true) || !it.getItemMeta().hasCustomModelData()) continue;
                        e.getInventory().setResult(ItemUtil.air);
                        return;
                    }
                } else {//1x1-9 2x1-12 1x2-6 3x1-6 1x3-3 2x2-8 2x3-4 3x2-4 3x3-2 магия крч
                    final Collection<RecipeChoice> rcs = src.getChoiceMap().values();
                    rcs.removeIf(c -> c == null);
                    for (final ItemStack it : mtx) {
                        if (!ItemUtil.isBlank(it, false)) {
                            final Iterator<RecipeChoice> rci = rcs.iterator();
                            while (rci.hasNext()) {
                                if (rci.next().test(it)) {
                                    rci.remove();
                                    break;
                                }
                            }
                        }
                    }

                    final CraftingInventory inv = e.getInventory();
                    if (rcs.size() != 0) {
                        inv.setResult(ItemUtil.air);
                        Bukkit.removeRecipe(src.getKey());
                        final HumanEntity pl = e.getViewers().isEmpty() ? null : e.getViewers().getFirst();
                        if (pl == null) return;
                        inv.setResult(Bukkit.craftItem(mtx, pl.getWorld(), (Player) pl));
                        Bukkit.addRecipe(src);
                    }
                }
            } else if (rc instanceof ShapelessRecipe) {
                final ShapelessRecipe src = Crafts.getRecipe(((Keyed) rc).getKey(), ShapelessRecipe.class);
                final ItemStack[] mtx = e.getInventory().getMatrix();
                if (src == null) {
                    for (final ItemStack it : mtx) {
                        if (ItemUtil.isBlank(it, true) || !it.getItemMeta().hasCustomModelData()) continue;
                        e.getInventory().setResult(ItemUtil.air);
                        return;
                    }
                } else {//1x1-9 2x1-12 1x2-6 3x1-6 1x3-3 2x2-4 2x3-4 3x2-4 3x3-2 магия крч
                    final List<RecipeChoice> rcs = src.getChoiceList();
                    for (final ItemStack ti : mtx) {
                        final Iterator<RecipeChoice> ri = rcs.iterator();
                        while (ri.hasNext()) {
                            final RecipeChoice chs = ri.next();
                            if ((chs == null && ItemUtil.isBlank(ti, false)) || chs.test(ti)) {
                                ri.remove();
                                break;
                            }
                        }
                    }

                    final CraftingInventory inv = e.getInventory();
                    if (rcs.size() != 0) {
                        inv.setResult(ItemUtil.air);
                        Bukkit.removeRecipe(src.getKey());
                        final HumanEntity pl = e.getViewers().isEmpty() ? null : e.getViewers().getFirst();
                        if (pl == null) return;
                        inv.setResult(Bukkit.craftItem(mtx, pl.getWorld(), (Player) pl));
                        Bukkit.addRecipe(src);
                    }
                }
            }

            final ItemStack fin = rc.getResult();
            final SpecialItem si = SpecialItem.get(fin);
            if (si != null && si.crafted()) {
                e.getInventory().setResult(ItemUtil.air);
                for (final HumanEntity he : e.getViewers()) {
                    he.sendMessage(TCUtil.form(Ostrov.PREFIX + "<red>Эта реликвия уже создана!"));
                }
            }
        }
    }

    //FurnaceBurnEvent change burn time
    @EventHandler
    public void onCook(final FurnaceSmeltEvent e) {
        final Recipe rc = e.getRecipe();
        if (rc == null) return;
        if (e.getBlock().getState() instanceof Furnace) {
            final CookingRecipe<?> src = Crafts.getRecipe(((Keyed) rc).getKey(), CookingRecipe.class);
            final ItemStack ti = e.getSource();
            if (src != null) {
                if (src.getInputChoice().test(ti)) return;
                Bukkit.removeRecipe(src.getKey());
                final Class<?> cls = rc.getClass();
                final Iterator<Recipe> rci = Bukkit.recipeIterator();
                while (rci.hasNext()) {
                    final Recipe orc = rci.next();
                    if (orc.getClass() == cls && ((CookingRecipe<?>) orc).getInputChoice().test(ti)) {
                        e.setResult(orc.getResult());
                        break;
                    }
                }
                Bukkit.addRecipe(src);
            }
        }
    }

    //FurnaceBurnEvent change burn time
    @EventHandler
    public void onStCook(final FurnaceStartSmeltEvent e) {
        final Recipe rc = e.getRecipe();
        if (e.getBlock().getState() instanceof Furnace) {
            final CookingRecipe<?> src = Crafts.getRecipe(((Keyed) rc).getKey(), CookingRecipe.class);
            final ItemStack ti = e.getSource();
            if (src == null) {
                if (ItemUtil.isBlank(ti, true) || !ti.getItemMeta().hasCustomModelData()) return;
                e.setTotalCookTime(Integer.MAX_VALUE);
            }
        }
    }

    @EventHandler
    public void onCamp(final PrepareSmithingEvent e) {
        final SmithingInventory si = e.getInventory();
        final Recipe rc = si.getRecipe();
        if (rc == null) return;
        if (rc instanceof Keyed) {
            if (rc instanceof SmithingRecipe) {
                final SmithingRecipe src = Crafts.getRecipe(((Keyed) rc).getKey(), SmithingRecipe.class);
                final ItemStack ti = si.getInputMineral();
                if (src == null) {
                    if (ItemUtil.isBlank(ti, true) || !ti.getItemMeta().hasCustomModelData()) return;
                    si.setResult(ItemUtil.air);
                } else {
                    if (src.getAddition().test(ti)) return;
                    si.setResult(ItemUtil.air);
                }
            }
        }
    }

    @EventHandler
    public void onSCut(final PlayerStonecutterRecipeSelectEvent e) {
        final StonecuttingRecipe rc = e.getStonecuttingRecipe();
        final StonecuttingRecipe src = Crafts.getRecipe(((Keyed) rc).getKey(), StonecuttingRecipe.class);
        final StonecutterInventory sci = e.getStonecutterInventory();
        if (src == null) {
            if (ItemUtil.isBlank(sci.getInputItem(), true) ||
                !sci.getInputItem().getItemMeta().hasCustomModelData()) return;
        } else {
            if (src.getInputChoice().test(sci.getInputItem())) return;
        }
        sci.setResult(ItemUtil.air);
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onRecipeBook(final PlayerRecipeBookClickEvent e) {
        final Recipe rc = Crafts.getRecipe(e.getRecipe(), Recipe.class);
        if (rc == null) return;
        e.setCancelled(true);
        final Player p = e.getPlayer();
        final InventoryView iv = p.getOpenInventory();
        switch (iv.getType()) {
            case CRAFTING, WORKBENCH:
                final int start = 1;
                final CraftingInventory cri = (CraftingInventory) iv.getTopInventory();
                int ix = 0;
                for (final ItemStack is : cri) {
                    if ((ix++) < start) continue;
                    if (!ItemUtil.isBlank(is, false)) {
                        giveItemAmt(p, is, is.getAmount());
                        is.setAmount(0);
                    }
                }

                if (rc instanceof ShapedRecipe) {//магия бля

                    final HashMap<CMDMatChoice, String> gridIts = new HashMap<>();
                    for (final Entry<Character, RecipeChoice> en : ((ShapedRecipe) rc).getChoiceMap().entrySet()) {
                        final RecipeChoice ch = en.getValue();
                        if (ch instanceof CMDMatChoice) {
                            final String gs = gridIts.get(ch);
                            gridIts.put((CMDMatChoice) ch, gs == null ?
                                String.valueOf(en.getKey()) : gs + en.getKey());
                        }
                    }

                    final HashMap<CMDMatChoice, Integer> has = new HashMap<>();
                    for (final CMDMatChoice chs : gridIts.keySet()) has.put(chs, 0);
                    for (final ItemStack it : p.getInventory()) {
                        final Iterator<Entry<CMDMatChoice, Integer>> eni = has.entrySet().iterator();
                        while (eni.hasNext()) {
                            final Entry<CMDMatChoice, Integer> en = eni.next();
                            if (en.getKey().test(it)) {
                                en.setValue(en.getValue() + it.getAmount());
                                it.setAmount(0);
                            }
                        }
                    }

                    final String shp = String.join(":", ((ShapedRecipe) rc).getShape());
                    final int rl = shp.indexOf(':') + 1;
                    final Iterator<Entry<CMDMatChoice, String>> eni = gridIts.entrySet().iterator();
                    while (eni.hasNext()) {
                        final Entry<CMDMatChoice, String> en = eni.next();
                        final Integer his = has.get(en.getKey());
                        final String slots = en.getValue();
                        final ItemStack kst = en.getKey().getItemStack();
                        final int split = Math.min(e.isMakeAll() ?
                            kst.getType().getMaxStackSize() : 1, his / slots.length());
                        giveItemAmt(p, kst, his - (split * slots.length()));
                        if (split == 0) continue;
                        for (final char c : slots.toCharArray()) {
                            cri.setItem(getCharIx(shp, rl, c) + start, kst.asQuantity(split));
                        }
                        eni.remove();
                    }

                    if (gridIts.size() != 0) {
                        e.setCancelled(false);
                        int ir = 0;
                        for (final ItemStack is : cri) {
                            if ((ir++) < start) continue;
                            if (!ItemUtil.isBlank(is, false)) {
                                giveItemAmt(p, is, is.getAmount());
                                is.setAmount(0);
                            }
                        }
                        return;
                    }

                } else if (rc instanceof ShapelessRecipe) {//магия бля
                    final HashMap<CMDMatChoice, Integer> gridIts = new HashMap<>();
                    for (final RecipeChoice ch : ((ShapelessRecipe) rc).getChoiceList()) {
                        if (ch instanceof CMDMatChoice) {
                            final Integer gs = gridIts.get(ch);
                            gridIts.put((CMDMatChoice) ch, gs == null ? 1 : gs + 1);
                        }
                    }

                    int mix = start;
                    final HashMap<CMDMatChoice, Integer> has = new HashMap<>();
                    for (final CMDMatChoice chs : gridIts.keySet()) has.put(chs, 0);
                    for (final ItemStack it : p.getInventory()) {
                        final Iterator<Entry<CMDMatChoice, Integer>> eni = has.entrySet().iterator();
                        while (eni.hasNext()) {
                            final Entry<CMDMatChoice, Integer> en = eni.next();
                            if (en.getKey().test(it)) {
                                en.setValue(en.getValue() + it.getAmount());
                                it.setAmount(0);
                            }
                        }
                    }

                    final Iterator<Entry<CMDMatChoice, Integer>> eni = gridIts.entrySet().iterator();
                    while (eni.hasNext()) {
                        final Entry<CMDMatChoice, Integer> en = eni.next();
                        final Integer his = has.get(en.getKey());
                        final int slots = en.getValue();
                        final ItemStack kst = en.getKey().getItemStack();
                        final int split = Math.min(e.isMakeAll() ?
                            kst.getType().getMaxStackSize() : 1, his / slots);
                        giveItemAmt(p, kst, his - (split * slots));
                        if (split == 0) continue;
                        for (int i = slots; i > 0; i--) {
                            cri.setItem(mix, kst.asQuantity(split));
                            mix++;
                        }
                        eni.remove();
                    }

                    if (gridIts.size() != 0) {
                        e.setCancelled(false);
                        int ir = 0;
                        for (final ItemStack is : cri) {
                            if ((ir++) < start) continue;
                            if (!ItemUtil.isBlank(is, false)) {
                                giveItemAmt(p, is, is.getAmount());
                                is.setAmount(0);
                            }
                        }
                        return;
                    }


                }
                break;
            case FURNACE, BLAST_FURNACE, SMOKER:
                final FurnaceInventory fni = (FurnaceInventory) iv.getTopInventory();
                if (rc instanceof CookingRecipe) {
                    final CMDMatChoice chs = (CMDMatChoice) ((CookingRecipe<?>) rc).getInputChoice();
                    final ItemStack in = fni.getSmelting();
                    if (!ItemUtil.isBlank(in, false)) {
                        giveItemAmt(p, in, in.getAmount());
                        fni.setSmelting(ItemUtil.air);
                    }

                    int invCnt = 0;
                    for (final ItemStack it : p.getInventory()) {
                        if (chs.test(it)) {
                            invCnt += it.getAmount();
                            it.setAmount(0);
                        }
                    }

                    if (invCnt == 0) {
                        e.setCancelled(false);
                        return;
                    }

                    final ItemStack cit = chs.getItemStack();
                    final int back = invCnt - cit.getType().getMaxStackSize();
                    if (back > 0) {
                        fni.setSmelting(cit.asQuantity(cit.getType().getMaxStackSize()));
                        giveItemAmt(p, cit, back);
                    } else {
                        fni.setSmelting(cit.asQuantity(invCnt));
                    }
                }
                break;
            default:
                e.setCancelled(false);
        }
    }

    private static int getCharIx(final String shp, final int rl, final char c) {
        final int ci = shp.indexOf(c);
        if (rl < 1) return ci;
        return ci / rl * 3 + ci % rl;
    }

    private static void giveItemAmt(final Player p, final ItemStack it, final int amt) {
        if (amt == 0) return;
        final int sts = it.getType().getMaxStackSize();
        final ItemStack[] its = new ItemStack[amt / sts + 1];
        for (int i = its.length - 1; i > 0; i--) {
            its[i] = it.asQuantity(sts);
        }
        its[0] = it.asQuantity(amt % sts);
        for (final ItemStack i : p.getInventory().addItem(its).values()) {
            p.getWorld().dropItem(p.getLocation(), i);
        }
    }

    @EventHandler
    public void onSmith(final PrepareSmithingEvent e) {
        final SmithingInventory ci = e.getInventory();
        final ItemStack it = e.getResult();
        if (!ItemUtil.isBlank(it, false)) {
            final ItemStack tr = ci.getInputTemplate();
            if (tr == null || ItemType.NETHERITE_UPGRADE_SMITHING_TEMPLATE
                .equals(tr.getType().asItemType())) return;
            final Material mt = it.getType();
            final EquipmentSlot es = mt.getEquipmentSlot();
            final EquipmentSlotGroup esg = es.getGroup();
            final Multimap<Attribute, AttributeModifier> amt = mt.getDefaultAttributeModifiers(es);
            final ItemMeta im = it.getItemMeta();
            im.removeAttributeModifier(es);
            double arm = 0d;
            for (final AttributeModifier am : amt.get(Attribute.ARMOR)) {
                switch (am.getOperation()) {
                    case ADD_NUMBER:
                        arm += am.getAmount();
                        break;
                    case ADD_SCALAR:
                        arm *= am.getAmount();
                        break;
                    case MULTIPLY_SCALAR_1:
                        arm *= (1d + am.getAmount());
                        break;
                }
            }
            double ath = 0d;
            for (final AttributeModifier am : amt.get(Attribute.ARMOR_TOUGHNESS)) {
                switch (am.getOperation()) {
                    case ADD_NUMBER:
                        ath += am.getAmount();
                        break;
                    case ADD_SCALAR:
                        ath *= am.getAmount();
                        break;
                    case MULTIPLY_SCALAR_1:
                        ath *= (1d + am.getAmount());
                        break;
                }
            }
            double akb = 0d;
            for (final AttributeModifier am : amt.get(Attribute.KNOCKBACK_RESISTANCE)) {
                switch (am.getOperation()) {
                    case ADD_NUMBER:
                        akb += am.getAmount();
                        break;
                    case ADD_SCALAR:
                        akb *= am.getAmount();
                        break;
                    case MULTIPLY_SCALAR_1:
                        akb *= (1d + am.getAmount());
                        break;
                }
            }

            final ItemStack add = ci.getInputMineral();
            im.addAttributeModifier(Attribute.ARMOR, new AttributeModifier(NamespacedKey.minecraft("armor_defense"),
                arm * (1d + ItemUtil.getTrimMod(add, Attribute.ARMOR)), Operation.ADD_NUMBER, esg));

            im.addAttributeModifier(Attribute.ARMOR_TOUGHNESS, new AttributeModifier(NamespacedKey.minecraft("armor_toughness"),
                ath * (1d + ItemUtil.getTrimMod(add, Attribute.ARMOR_TOUGHNESS)), Operation.ADD_NUMBER, esg));

            im.addAttributeModifier(Attribute.KNOCKBACK_RESISTANCE, new AttributeModifier(NamespacedKey.minecraft("armor_knockback_resist"),
                akb * (1d + ItemUtil.getTrimMod(add, Attribute.KNOCKBACK_RESISTANCE)), Operation.ADD_NUMBER, esg));

            addAttr(im, Attribute.MAX_HEALTH, add, "armor_max_health", esg);
            addAttr(im, Attribute.SCALE, add, "armor_scale", esg);
            addAttr(im, Attribute.GRAVITY, add, "armor_gravity", esg);
            addAttr(im, Attribute.ATTACK_DAMAGE, add, "armor_attack_damage", esg);
            addAttr(im, Attribute.ATTACK_KNOCKBACK, add, "armor_attack_knockback", esg);
            addAttr(im, Attribute.ATTACK_SPEED, add, "armor_attack_speed", esg);
            addAttr(im, Attribute.MOVEMENT_SPEED, add, "armor_move_speed", esg);
            addAttr(im, Attribute.SNEAKING_SPEED, add, "armor_sneak_speed", esg);
            addAttr(im, Attribute.WATER_MOVEMENT_EFFICIENCY, add, "armor_water_speed", esg);
            addAttr(im, Attribute.JUMP_STRENGTH, add, "armor_jump_strength", esg);
            addAttr(im, Attribute.BLOCK_INTERACTION_RANGE, add, "armor_range_block", esg);
            addAttr(im, Attribute.ENTITY_INTERACTION_RANGE, add, "armor_range_entity", esg);
            addAttr(im, Attribute.BLOCK_BREAK_SPEED, add, "armor_break_speed", esg);

            it.setItemMeta(im);
            e.setResult(it);
        }
    }

    private static void addAttr(final ItemMeta im, final Attribute at, final ItemStack in, final String name, final EquipmentSlotGroup esg) {
        final double mod = ItemUtil.getTrimMod(in, Attribute.BLOCK_BREAK_SPEED);
        if (mod == 0d) return;
        im.addAttributeModifier(at, new AttributeModifier(NamespacedKey.minecraft(name), mod, Operation.MULTIPLY_SCALAR_1, esg));
    }

    @EventHandler
    public void onJoin(final PlayerJoinEvent e) {
        discRecs(e.getPlayer());
    }
}
