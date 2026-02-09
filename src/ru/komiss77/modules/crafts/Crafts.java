package ru.komiss77.modules.crafts;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import com.destroystokyo.paper.event.player.PlayerRecipeBookClickEvent;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.event.player.PlayerStonecutterRecipeSelectEvent;
import io.papermc.paper.persistence.PersistentDataContainerView;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Furnace;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.RecipeChoice.ExactChoice;
import org.bukkit.persistence.PersistentDataType;
import ru.komiss77.Cfg;
import ru.komiss77.Initiable;
import ru.komiss77.Ostrov;
import ru.komiss77.boot.OStrap;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.items.ItemGroup;
import ru.komiss77.modules.items.ItemManager;
import ru.komiss77.modules.items.SpecialItem;
import ru.komiss77.modules.world.BVec;
import ru.komiss77.utils.ClassUtil;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.TCUtil;


public final class Crafts implements Initiable, Listener {

  public static final NamespacedKey RECIPE_KEY = new NamespacedKey(Ostrov.instance, "craft_recipe");//OStrap.key("mat");
  public static final Map<NamespacedKey, Craft> crafts;

  //private static Map<String, RecipeChoice> ids;
  static {
    crafts = new HashMap<>();
    //ids = new HashMap<>();
  }

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
            if (!cfg.getName().endsWith(".yml")) continue;
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

  public static void readCraft(final ConfigurationSection cfg) {
    //ConfigurationSection cfg = craftConfig.getConfigurationSection("crafts");
    final String name = cfg.getName();
    final ItemStack resultItem = ItemUtil.parse(cfg.getString("result"));
//resultItem.getPersistentDataContainer().getKeys().stream().forEach(k -> {
//    Ostrov.log_warn("readCraft "+k.getKey()+" "+k.value());
//});
    final NamespacedKey key = new NamespacedKey(OStrap.space, name);
    //cfg = craftConfig.getConfigurationSection("crafts." + c + ".recipe");
        final Recipe recipe;
    final ItemStack recipe_a = ItemUtil.parse(cfg.getString("recipe.a"));

    switch (cfg.getString("type")) {//(craftConfig.getString("crafts." + c + ".type")) {
            case "smoker":
              if (ItemUtil.isBlank(recipe_a, false)) return;
              recipe = new SmokingRecipe(key, resultItem, of(recipe_a, name), 0.5f, 100);
              //recipe = new SmokingRecipe(key, resultItem, new RecipeChoice.MaterialChoice(recipe_a.getType()), 0.5f, 100);
                break;
            case "blaster":
              if (ItemUtil.isBlank(recipe_a, false)) return;
              recipe = new BlastingRecipe(key, resultItem, of(recipe_a, name), 0.5f, 100);
              //recipe = new BlastingRecipe(key, resultItem, new RecipeChoice.MaterialChoice(recipe_a.getType()), 0.5f, 100);
                break;
            case "campfire":
              if (ItemUtil.isBlank(recipe_a, false)) return;
              recipe = new CampfireRecipe(key, resultItem, of(recipe_a, name), 0.5f, 500);
              //recipe = new CampfireRecipe(key, resultItem, new RecipeChoice.MaterialChoice(recipe_a.getType()), 0.5f, 100);
                break;
            case "furnace":
              if (ItemUtil.isBlank(recipe_a, false)) return;
              recipe = new FurnaceRecipe(key, resultItem, of(recipe_a, name), 0.5f, 200);
              //recipe = new FurnaceRecipe(key, resultItem, new RecipeChoice.MaterialChoice(recipe_a.getType()), 0.5f, 100);
                break;
            case "cutter":
              if (ItemUtil.isBlank(recipe_a, false)) return;
              recipe = new StonecuttingRecipe(key, resultItem, of(recipe_a, name));
              //recipe = new StonecuttingRecipe(key, resultItem, new RecipeChoice.MaterialChoice(recipe_a.getType()));
                break;
            case "smith":
              //recipe_a = ItemUtil.parse(cfg.getString("recipe.a"));
              final ItemStack recipe_b = ItemUtil.parse(cfg.getString("recipe.b"));
              if (ItemUtil.isBlank(recipe_a, false) || ItemUtil.isBlank(recipe_b, false)) return;
              final ItemStack recipe_c = ItemUtil.parse(cfg.getString("recipe.c"));
              recipe = new SmithingTransformRecipe(key, resultItem, of(recipe_c, name),
                  of(recipe_a, name), of(recipe_b, name), !recipe_a.hasData(DataComponentTypes.DAMAGE));
              //recipe = new SmithingTransformRecipe(key, resultItem, new RecipeChoice.MaterialChoice(recipe_c.getType()),
              //    new RecipeChoice.MaterialChoice(recipe_a.getType()), new RecipeChoice.MaterialChoice(recipe_b.getType()), !recipe_a.hasData(DataComponentTypes.DAMAGE));
                break;
            case "noshape":
              recipe = new ShapelessRecipe(key, resultItem);
              for (final String s : cfg.getConfigurationSection("recipe").getKeys(false)) {
                final ItemStack recipe_ = ItemUtil.parse(cfg.getString("recipe." + s));
                if (!recipe_.getType().isAir()) {
                  //((ShapelessRecipe) recipe).addIngredient(IdChoice.of(ItemUtil.parse(cfg.getString("recipe." + s))));
                  ((ShapelessRecipe) recipe).addIngredient(new RecipeChoice.MaterialChoice(recipe_.getType()));
                    }
                }
                break;
            case "shaped":
            default:
              recipe = new ShapedRecipe(key, resultItem);
              final String shapeStr = cfg.getString("shapeStr");
              ShapedRecipe shapedRecipe = (ShapedRecipe) recipe;
              if (shapeStr == null) {
                shapedRecipe.shape(new String[]{"abc", "def", "ghi"});
              } else {
                shapedRecipe.shape(shapeStr.split(":"));
              }
              //((ShapedRecipe) recipe).shape(shapeStr == null ? new String[]{"abc", "def", "ghi"} : shapeStr.split(":"));
              for (final String s : cfg.getConfigurationSection("recipe").getKeys(false)) {
                final ItemStack recipe_ = ItemUtil.parse(cfg.getString("recipe." + s));
                if (ItemUtil.isBlank(recipe_, false)) {
                  Ostrov.log_warn("Craft recipe_ isBlank : " + s);
                    continue;
                  }
                  //Caused by: java.lang.IllegalArgumentException: empty RecipeChoice isn't allowed here
                RecipeChoice recipeChoice = new RecipeChoice.ExactChoice(recipe_);//IdChoice.of(recipe_);
                  shapedRecipe.setIngredient(s.charAt(0), recipeChoice);
                }
                break;
        }
        Bukkit.addRecipe(recipe);
    //final SubServer sv = SubServer.parseSubServer(cfg.getString("world"));
      crafts.put(key, new Craft(recipe, p -> true));

    }

  public static ExactChoice of(final @Nullable ItemStack it, final String name) {
    if (ItemUtil.isBlank(it, false)) return (ExactChoice) RecipeChoice.empty();
    //final String id = it.getPersistentDataContainer().get(ItemGroup.KEY, PersistentDataType.STRING);
    it.getItemMeta().getPersistentDataContainer().set(RECIPE_KEY, PersistentDataType.STRING, name);
    final ExactChoice rc = new RecipeChoice.ExactChoice(it);
    //ids.put(id, rc);
    return rc;
  }


  public static boolean isOs(final RecipeChoice choice) {
    if (choice == null || choice instanceof ExactChoice) return false;
    final List<ItemStack> choices = ((ExactChoice) choice).getChoices();
    for (ItemStack ci : choices) {
      if (ci.hasItemMeta() || ci.getItemMeta().getPersistentDataContainer().has(RECIPE_KEY)) {
        return true;
      }
    }
    return false;
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

   /* public static Recipe fakeRec(final Recipe rc) {
        if (rc instanceof Keyed) {
            final String ks = ((Keyed) rc).getKey().getKey();
            switch (rc) {
                case ShapedRecipe src:
                    final ShapedRecipe drc = new ShapedRecipe(new NamespacedKey(OStrap.space, ks), rc.getResult());
                    drc.shape(src.getShape());
                    for (final Entry<Character, RecipeChoice> en : src.getChoiceMap().entrySet()) {
                        if (en.getValue() == null) continue;
                        drc.setIngredient(en.getKey(), new ExactChoice(((IdChoice) en.getValue()).getItemStack()));
                    }
                    return drc;
                case ShapelessRecipe src:
                    final ShapelessRecipe lrc = new ShapelessRecipe(new NamespacedKey(OStrap.space, ks), rc.getResult());
                    for (final RecipeChoice ch : src.getChoiceList()) {
                        if (ch == null) continue;
                        lrc.addIngredient(new ExactChoice(((IdChoice) ch).getItemStack()));
                    }
                    return lrc;
                case final FurnaceRecipe src:
                    return new FurnaceRecipe(new NamespacedKey(OStrap.space, ks), src.getResult(),
                        new ExactChoice(((IdChoice) src.getInputChoice()).getItemStack()), src.getExperience(), src.getCookingTime());
                case final SmokingRecipe src:
                    return new SmokingRecipe(new NamespacedKey(OStrap.space, ks), src.getResult(),
                        new ExactChoice(((IdChoice) src.getInputChoice()).getItemStack()), src.getExperience(), src.getCookingTime());
                case final BlastingRecipe src:
                    return new BlastingRecipe(new NamespacedKey(OStrap.space, ks), src.getResult(),
                        new ExactChoice(((IdChoice) src.getInputChoice()).getItemStack()), src.getExperience(), src.getCookingTime());
                case final CampfireRecipe src:
                    return new CampfireRecipe(new NamespacedKey(OStrap.space, ks), src.getResult(),
                        new ExactChoice(((IdChoice) src.getInputChoice()).getItemStack()), src.getExperience(), src.getCookingTime());
                default:
                    return null;
            }
        }
        return null;
    }*/



    public record Craft(Recipe rec, Predicate<Player> canSee) {}

    @EventHandler
    public void onPrepare(final PrepareItemCraftEvent e) {
      final Recipe recipe = e.getRecipe();
      if (recipe == null) return;
      if (e.isRepair() || !(recipe instanceof Keyed)) {
        return;
      }
        /*switch (recipe) {
            case ComplexRecipe cxr -> {
                for (final ItemStack it : e.getInventory().getMatrix()) {
                    if (!ItemManager.isCustom(it)) continue;
                    e.getInventory().setResult(ItemUtil.air);
                    return;
                }
            }
            case ShapedRecipe shr -> {
                final ShapedRecipe src = Crafts.getRecipe(shr.getKey(), ShapedRecipe.class);
                final ItemStack[] mtx = e.getInventory().getMatrix();
                if (src == null) {
                    for (final ItemStack it : mtx) {
                        if (!ItemManager.isCustom(it)) continue;
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
                        inv.setResult(Bukkit.craftItem(ClassUtil.scale(mtx, 3, 3), pl.getWorld(), (Player) pl));
                        Bukkit.addRecipe(src);
                    }
                }
            }
            case ShapelessRecipe slr -> {
                final ShapelessRecipe src = Crafts.getRecipe(slr.getKey(), ShapelessRecipe.class);
                final ItemStack[] mtx = e.getInventory().getMatrix();
                if (src == null) {
                    for (final ItemStack it : mtx) {
                        if (!ItemManager.isCustom(it)) continue;
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
                        inv.setResult(Bukkit.craftItem(ClassUtil.scale(mtx, 3, 3), pl.getWorld(), (Player) pl));
                        Bukkit.addRecipe(src);
                    }
                }
            }
            default -> {
            }
        }*/

      final ItemStack result = recipe.getResult();
      final PersistentDataContainerView pdc = result.getPersistentDataContainer();
      if (!pdc.isEmpty() && pdc.has(SpecialItem.DATA)) {
        final SpecialItem si = SpecialItem.get(result);
        Ostrov.log_warn("Crafts PrepareItemCraftEvent SpecialItem=" + (si == null ? "null" : si.name() + " crafted?" + si.crafted()));
            if (si != null && si.crafted()) {
              Entity owner = si.own();
              ItemBuilder ib = new ItemBuilder(ItemType.BARRIER).name("<red>Эта реликвия уже создана!");
              if (owner == null) {
                ib.lore("§3Владелец и место не известны");
              } else if (owner instanceof Item item) {
                BVec loc = si.loc();
                if (loc == null) {
                  ib.lore("§6Валяется неизвестно где");
                } else {
                  ib.lore("§6Валяется в мире " + loc.wname() + " на " + loc.x + "," + loc.y + "," + loc.z);
                }
              } else if (owner instanceof Player player) {
                ib.lore("§3Владелец " + owner.getName());
              }
              e.getInventory().setResult(ib.build());
              //for (final HumanEntity he : e.getViewers()) {
              //    he.sendMessage(TCUtil.form(Ostrov.PREFIX + "<red>Эта реликвия уже создана!"));
              //}
            }
      }

    }

  @EventHandler
  public void onCraft(final CraftItemEvent e) {
    final Recipe recipe = e.getRecipe();
    final ItemStack current = e.getCurrentItem();
    if (current != null && current.getType() == Material.BARRIER) {
      if (current.hasItemMeta() && current.getItemMeta().hasDisplayName() && !current.getItemMeta().lore().isEmpty()) {
        Ostrov.log_warn("Crafts CraftItemEvent BARRIER deny!");
        e.setCancelled(true);
        return;
      }
    }
    if (recipe == null) return;
    if (!(recipe instanceof Keyed)) {
      return;
    }
    final ItemStack result = recipe.getResult();
    final PersistentDataContainerView pdc = result.getPersistentDataContainer();
    if (!pdc.isEmpty() && pdc.has(SpecialItem.DATA)) {
      final SpecialItem si = SpecialItem.get(result);
      Ostrov.log_warn("Crafts CraftItemEvent SpecialItem=" + (si == null ? "null" : si.name() + " crafted?" + si.crafted()));
      if (si != null) {
        if (si.crafted()) {
          e.getInventory().setResult(ItemUtil.air);
          for (final HumanEntity he : e.getViewers()) {
            he.sendMessage(TCUtil.form(Ostrov.PREFIX + "<red>Эта реликвия уже создана!"));
          }
        } else {
          si.obtain(e.getWhoClicked(), result);
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
                if (!ItemManager.isCustom(ti)) return;
                e.setTotalCookTime(Integer.MAX_VALUE);
            }
        }
    }

    @EventHandler
    public void onCamp(final PrepareSmithingEvent e) {
        final SmithingInventory si = e.getInventory();
        final Recipe rc = si.getRecipe();
        if (!(rc instanceof SmithingRecipe)) return;
        final SmithingRecipe src = Crafts.getRecipe(((Keyed) rc).getKey(), SmithingRecipe.class);
        final ItemStack ti = si.getInputMineral();
        if (src == null) {
            if (!ItemManager.isCustom(ti)) return;
        } else if (ti != null) {
            if (src.getAddition().test(ti)) return;
        }
        si.setResult(ItemUtil.air);
    }

    @EventHandler
    public void onSCut(final PlayerStonecutterRecipeSelectEvent e) {
        final StonecuttingRecipe rc = e.getStonecuttingRecipe();
        final StonecuttingRecipe src = Crafts.getRecipe(rc.getKey(), StonecuttingRecipe.class);
        final StonecutterInventory sci = e.getStonecutterInventory();
        final ItemStack ti = sci.getInputItem();
        if (src == null) {
            if (!ItemManager.isCustom(ti)) return;
        } else if (ti != null) {
            if (src.getInputChoice().test(ti)) return;
        }
        sci.setResult(ItemUtil.air);
        e.setCancelled(true);
    }

  //@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
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

                  //final HashMap<IdChoice, String> gridIts = new HashMap<>();
                  final HashMap<ExactChoice, String> gridIts = new HashMap<>(); //рецепт, слот(слоты)
                    for (final Entry<Character, RecipeChoice> en : ((ShapedRecipe) rc).getChoiceMap().entrySet()) {
                        final RecipeChoice ch = en.getValue();
                      //if (ch instanceof IdChoice) {
                      //    final String gs = gridIts.get(ch);
                      //    gridIts.put((IdChoice) ch, gs == null ?
                      //        String.valueOf(en.getKey()) : gs + en.getKey());
                      //}
                      if (ch instanceof ExactChoice) {
                            final String gs = gridIts.get(ch);
                        gridIts.put((ExactChoice) ch, gs == null ? String.valueOf(en.getKey()) : gs + en.getKey());
                        }
                    }

                  //final HashMap<IdChoice, Integer> has = new HashMap<>();
                  //for (final IdChoice chs : gridIts.keySet()) has.put(chs, 0);
                  final HashMap<ExactChoice, Integer> has = new HashMap<>();
                  for (final ExactChoice chs : gridIts.keySet()) has.put(chs, 0);
                    for (final ItemStack it : p.getInventory()) {
                      //final Iterator<Entry<IdChoice, Integer>> eni = has.entrySet().iterator();
                      final Iterator<Entry<ExactChoice, Integer>> eni = has.entrySet().iterator();
                        while (eni.hasNext()) {
                          //final Entry<IdChoice, Integer> en = eni.next();
                          final Entry<ExactChoice, Integer> en = eni.next();
                          //if (en.getKey().test(it)) {
                          if (test(en.getKey(), it)) {
                                en.setValue(en.getValue() + it.getAmount());
                                it.setAmount(0);
                            }
                        }
                    }

                    final String shp = String.join(":", ((ShapedRecipe) rc).getShape());
                    final int rl = shp.indexOf(':') + 1;
                  //final Iterator<Entry<IdChoice, String>> eni = gridIts.entrySet().iterator();
                  final Iterator<Entry<ExactChoice, String>> eni = gridIts.entrySet().iterator();
                    while (eni.hasNext()) {
                      //final Entry<IdChoice, String> en = eni.next();
                      final Entry<ExactChoice, String> en = eni.next();
                        final Integer his = has.get(en.getKey());
                        final String slots = en.getValue();
                      //final ItemStack kst = en.getKey().getItemStack();
                      final ItemStack kst = getItemStack(en.getKey());
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
                  //final HashMap<IdChoice, Integer> gridIts = new HashMap<>();
                  final HashMap<ExactChoice, Integer> gridIts = new HashMap<>();
                    for (final RecipeChoice ch : ((ShapelessRecipe) rc).getChoiceList()) {
                      //if (ch instanceof IdChoice) {
                      if (isOs(ch)) {
                            final Integer gs = gridIts.get(ch);
                        //gridIts.put((IdChoice) ch, gs == null ? 1 : gs + 1);
                        gridIts.put((ExactChoice) ch, gs == null ? 1 : gs + 1);
                        }
                    }

                    int mix = start;
                  //final HashMap<IdChoice, Integer> has = new HashMap<>();
                  final HashMap<ExactChoice, Integer> has = new HashMap<>();
                  //for (final IdChoice chs : gridIts.keySet()) has.put(chs, 0);
                  for (final ExactChoice chs : gridIts.keySet()) has.put(chs, 0);
                    for (final ItemStack it : p.getInventory()) {
                      //final Iterator<Entry<IdChoice, Integer>> eni = has.entrySet().iterator();
                      final Iterator<Entry<ExactChoice, Integer>> eni = has.entrySet().iterator();
                        while (eni.hasNext()) {
                          //final Entry<IdChoice, Integer> en = eni.next();
                          final Entry<ExactChoice, Integer> en = eni.next();
                          //if (en.getKey().test(it)) {
                          if (test(en.getKey(), it)) {
                                en.setValue(en.getValue() + it.getAmount());
                                it.setAmount(0);
                            }
                        }
                    }

                  //final Iterator<Entry<IdChoice, Integer>> eni = gridIts.entrySet().iterator();
                  final Iterator<Entry<ExactChoice, Integer>> eni = gridIts.entrySet().iterator();
                    while (eni.hasNext()) {
                      //final Entry<IdChoice, Integer> en = eni.next();
                      final Entry<ExactChoice, Integer> en = eni.next();
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

                  if (!gridIts.isEmpty()) {
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
                  //final IdChoice chs = (IdChoice) ((CookingRecipe<?>) rc).getInputChoice();
                  final ExactChoice chs = (ExactChoice) ((CookingRecipe<?>) rc).getInputChoice();
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

  private static boolean test(final ExactChoice exactChoice, final ItemStack it) {
    final List<ItemStack> choices = exactChoice.getChoices();
    if (it == null || it.getType() == Material.AIR) {
      //return getChoices().contains(Material.AIR);
      for (ItemStack ci : choices) {
        if (ci.getType() == Material.AIR) return true;
      }
      return false;
    }
    //if (!choices.contains(it.getType())) {
    //    return false;
    //}
    if (!it.hasItemMeta() || !it.getItemMeta().getPersistentDataContainer().has(RECIPE_KEY)) {
      for (ItemStack ci : choices) {
        if (ci.getType() == it.getType()) return true;
      }
      return false;
    }
    final String id = it.getItemMeta().getPersistentDataContainer().get(RECIPE_KEY, PersistentDataType.STRING);
    for (ItemStack ci : choices) {
      if (ci.hasItemMeta() || ci.getItemMeta().getPersistentDataContainer().has(RECIPE_KEY)) {
        return id.equals(ci.getItemMeta().getPersistentDataContainer().get(RECIPE_KEY, PersistentDataType.STRING));
      }
    }
    //return Objects.equals(id, it.getPersistentDataContainer().get(ItemGroup.KEY, PersistentDataType.STRING));
    return false;
  }

  private static ItemStack getItemStack(final ExactChoice exactChoice) {
    final ItemStack is = exactChoice.getChoices().getFirst();
    if (!is.hasItemMeta() || !is.getItemMeta().getPersistentDataContainer().has(ItemGroup.KEY)) return ItemUtil.air;
    final Material mt = is.getType();
    final String id = is.getItemMeta().getPersistentDataContainer().get(ItemGroup.KEY, PersistentDataType.STRING);
    final ItemGroup cmts = ItemGroup.get(id);
    final ItemStack ci = cmts == null ? null : cmts.item(mt.asItemType());
    if (ci != null) return ci.asOne();
    final ItemStack it = new ItemStack(mt);
    return ItemUtil.isBlank(it, false) ? ItemUtil.air : it;
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

  @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(final PlayerJoinEvent e) {
        discRecs(e.getPlayer());
    }

  public static void discRecs(final Player p) {
    final List<NamespacedKey> rls = new ArrayList<>();
    for (final Entry<NamespacedKey, Craft> en : crafts.entrySet()) {
      if (en.getValue().canSee.test(p)) rls.add(en.getKey());
    }
    p.discoverRecipes(rls);
  }
}
