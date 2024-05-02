package ru.komiss77.modules.crafts;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.block.Furnace;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.FurnaceStartSmeltEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.BlastingRecipe;
import org.bukkit.inventory.CampfireRecipe;
import org.bukkit.inventory.CookingRecipe;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.RecipeChoice.ExactChoice;
import org.bukkit.inventory.meta.ItemMeta;
import com.destroystokyo.paper.event.player.PlayerRecipeBookClickEvent;
import com.google.common.collect.Multimap;
import io.papermc.paper.event.player.PlayerStonecutterRecipeSelectEvent;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.SmithingInventory;
import org.bukkit.inventory.SmithingRecipe;
import org.bukkit.inventory.SmithingTransformRecipe;
import org.bukkit.inventory.SmokingRecipe;
import org.bukkit.inventory.StonecutterInventory;
import org.bukkit.inventory.StonecuttingRecipe;
import ru.komiss77.Config;
import ru.komiss77.Initiable;
import ru.komiss77.Ostrov;
import ru.komiss77.utils.ItemUtils;



public final class Crafts implements Initiable, Listener {
	
    public static final Map<NamespacedKey, Craft> crafts = new HashMap<>();
    public static final String space = "ostrov";

    public Crafts() {
    	if (!Config.crafts) {
    		Ostrov.log_ok("§6Крафты выключены!");
    		return;
    	}
    	
        Ostrov.instance.getCommand("craft").setExecutor(new CraftCmd());
        reload();
        //Bukkit.getPluginManager().registerEvents(new CraftListener(), Ostrov.instance);
    }
    
    @Override
    public void postWorld() { //обход модулей после загрузки миров, т.к. не всё можно сделать onEnable
    }
    
    @Override
    public void reload() {
    	HandlerList.unregisterAll(this);
    	if (!Config.crafts) {
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
		Ostrov.log_ok("§2Крафты запущены!");
    }

    @Override
    public void onDisable() {
    	if (!Config.crafts) {
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
        //    Logger.getLogger(Crafts.class.getName()).log(Level.SEVERE, null, ex);
        //}
    }
    
    public static void readCraft(final ConfigurationSection cs) {
        //ConfigurationSection cs = craftConfig.getConfigurationSection("crafts");
        final ItemStack resultItem = ItemUtils.parseItem(cs.getString("result"), "=");
        final NamespacedKey nsk = new NamespacedKey(space, cs.getName());
        //cs = craftConfig.getConfigurationSection("crafts." + c + ".recipe");
        final Recipe recipe;
        final ItemStack it;
        switch (cs.getString("type")) {//(craftConfig.getString("crafts." + c + ".type")) {
            case "smoker":
                if (ItemUtils.isBlank((it = ItemUtils.parseItem(cs.getString("recipe.a"), "=")), false)) return;
                recipe = new SmokingRecipe(nsk, resultItem, CMDMatChoice.of(it), 0.5f, 100);
                break;
            case "blaster":
                if (ItemUtils.isBlank((it = ItemUtils.parseItem(cs.getString("recipe.a"), "=")), false)) return;
                recipe = new BlastingRecipe(nsk, resultItem, CMDMatChoice.of(it), 0.5f, 100);
                break;
            case "campfire":
                if (ItemUtils.isBlank((it = ItemUtils.parseItem(cs.getString("recipe.a"), "=")), false)) return;
                recipe = new CampfireRecipe(nsk, resultItem, CMDMatChoice.of(it), 0.5f, 500);
                break;
            case "furnace":
                if (ItemUtils.isBlank((it = ItemUtils.parseItem(cs.getString("recipe.a"), "=")), false)) return;
                recipe = new FurnaceRecipe(nsk, resultItem, CMDMatChoice.of(it), 0.5f, 200);
                break;
            case "cutter":
                if (ItemUtils.isBlank((it = ItemUtils.parseItem(cs.getString("recipe.a"), "=")), false)) return;
                recipe = new StonecuttingRecipe(nsk, resultItem, CMDMatChoice.of(it));
                break;
            case "smith":
            	it = ItemUtils.parseItem(cs.getString("recipe.a"), "=");
                final ItemStack scd = ItemUtils.parseItem(cs.getString("recipe.b"), "=");
                if (ItemUtils.isBlank(it, false) || ItemUtils.isBlank(scd, false)) return;
                recipe = new SmithingTransformRecipe(nsk, resultItem, CMDMatChoice.of(
                	ItemUtils.parseItem(cs.getString("recipe.c"), "=")), CMDMatChoice.of(it), CMDMatChoice.of(scd), false);
                break;
            case "noshape":
                recipe = new ShapelessRecipe(nsk, resultItem);
                for (final String s : cs.getConfigurationSection("recipe").getKeys(false)) {
                	final ItemStack ii = ItemUtils.parseItem(cs.getString("recipe." + s), "=");
                	if (!ii.getType().isAir()) {
                        ((ShapelessRecipe) recipe).addIngredient(CMDMatChoice.of(ItemUtils.parseItem(cs.getString("recipe." + s), "=")));
                	}
                }
                break;
            case "shaped":
            default:
                recipe = new ShapedRecipe(nsk, resultItem);
                final String shp = cs.getString("shape");
                ((ShapedRecipe) recipe).shape(shp == null ? new String[]{"abc", "def", "ghi"} : shp.split(":"));
                for (final String s : cs.getConfigurationSection("recipe").getKeys(false)) {
                    ((ShapedRecipe) recipe).setIngredient(s.charAt(0), CMDMatChoice.of(ItemUtils.parseItem(cs.getString("recipe." + s), "=")));
                }
                break;
        }
        Bukkit.addRecipe(recipe);
        //final SubServer sv = SubServer.parseSubServer(cs.getString("world"));
        crafts.put(nsk, new Craft(recipe, p -> true));
    
	}

	@SuppressWarnings("unchecked")
	public static <G extends Recipe> G getRecipe(final NamespacedKey key, final Class<G> cls) {
		if (!key.getNamespace().equals(Crafts.space)) return null;
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
	        if (rc instanceof ShapedRecipe) {
	        	final ShapedRecipe src = new ShapedRecipe(new NamespacedKey(space, ks), rc.getResult());
	        	src.shape(((ShapedRecipe) rc).getShape());
	        	for (final Entry<Character, RecipeChoice> en : ((ShapedRecipe) rc).getChoiceMap().entrySet()) {
	        		if (en.getValue() == null) continue;
	        		src.setIngredient(en.getKey(), new ExactChoice(((CMDMatChoice) en.getValue()).getItemStack()));
	        	}
				return src;
			} else if (rc instanceof ShapelessRecipe) {
	        	final ShapelessRecipe src = new ShapelessRecipe(new NamespacedKey(space, ks), rc.getResult());
	        	for (final RecipeChoice ch : ((ShapelessRecipe) rc).getChoiceList()) {
	        		if (ch == null) continue;
	        		src.addIngredient(new ExactChoice(((CMDMatChoice) ch).getItemStack()));
	        	}
				return src;
			} else if (rc instanceof final FurnaceRecipe src) {
                return new FurnaceRecipe(new NamespacedKey(space, ks), src.getResult(),
					new ExactChoice(((CMDMatChoice) src.getInputChoice()).getItemStack()), src.getExperience(), src.getCookingTime());
			} else if (rc instanceof final SmokingRecipe src) {
                return new SmokingRecipe(new NamespacedKey(space, ks), src.getResult(),
					new ExactChoice(((CMDMatChoice) src.getInputChoice()).getItemStack()), src.getExperience(), src.getCookingTime());
			} else if (rc instanceof final BlastingRecipe src) {
                return new BlastingRecipe(new NamespacedKey(space, ks), src.getResult(),
					new ExactChoice(((CMDMatChoice) src.getInputChoice()).getItemStack()), src.getExperience(), src.getCookingTime());
			} else if (rc instanceof final CampfireRecipe src) {
                return new CampfireRecipe(new NamespacedKey(space, ks), src.getResult(),
					new ExactChoice(((CMDMatChoice) src.getInputChoice()).getItemStack()), src.getExperience(), src.getCookingTime());
			} else {
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
	public void onCraft(final PrepareItemCraftEvent e) {
		final Recipe rc = e.getRecipe();
		if (rc == null) return;
		if (!e.isRepair() && rc instanceof Keyed) {
			if (rc instanceof ShapedRecipe) {
				final ShapedRecipe src = Crafts.getRecipe(((Keyed) rc).getKey(), ShapedRecipe.class);
				final ItemStack[] mtx = e.getInventory().getMatrix();
				if (src == null) {
					for (final ItemStack it : mtx) {
						if (ItemUtils.isBlank(it, true) || !it.getItemMeta().hasCustomModelData()) continue;
						e.getInventory().setResult(ItemUtils.air);
						return;
					}
				} else {//1x1-9 2x1-12 1x2-6 3x1-6 1x3-3 2x2-8 2x3-4 3x2-4 3x3-2 магия крч
					final Collection<RecipeChoice> rcs = src.getChoiceMap().values();
					rcs.removeIf(c -> c == null);
					for (final ItemStack it : mtx) {
						if (!ItemUtils.isBlank(it, false)) {
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
						inv.setResult(ItemUtils.air);
						Bukkit.removeRecipe(src.getKey());
						final HumanEntity pl = e.getViewers().isEmpty() ? null : e.getViewers().get(0);
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
						if (ItemUtils.isBlank(it, true) || !it.getItemMeta().hasCustomModelData()) continue;
						e.getInventory().setResult(ItemUtils.air);
						return;
					}
				} else {//1x1-9 2x1-12 1x2-6 3x1-6 1x3-3 2x2-8 2x3-4 3x2-4 3x3-2 магия крч
					final List<RecipeChoice> rcs = src.getChoiceList();
					for (final ItemStack ti : mtx) {
						final Iterator<RecipeChoice> ri = rcs.iterator();
						while (ri.hasNext()) {
							final RecipeChoice chs = ri.next();
							if ((chs == null && ItemUtils.isBlank(ti, false)) || chs.test(ti)) {
								ri.remove();
								break;
							}
						}
					}
					
					final CraftingInventory inv = e.getInventory();
					if (rcs.size() != 0) {
						inv.setResult(ItemUtils.air);
						Bukkit.removeRecipe(src.getKey());
						final HumanEntity pl = e.getViewers().isEmpty() ? null : e.getViewers().get(0);
						if (pl == null) return;
						inv.setResult(Bukkit.craftItem(mtx, pl.getWorld(), (Player) pl));
						Bukkit.addRecipe(src);
                    }
				}
			}
		}
	}
	
	//FurnaceBurnEvent change burn time
	@EventHandler
	public void onCook(final FurnaceSmeltEvent e) {
		final Recipe rc = e.getRecipe();
		if (rc == null) return;
		if (rc instanceof CookingRecipe && e.getBlock().getState() instanceof Furnace) {
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
		if (rc == null) return;
		if (rc instanceof CookingRecipe && e.getBlock().getState() instanceof Furnace) {
			final CookingRecipe<?> src = Crafts.getRecipe(((Keyed) rc).getKey(), CookingRecipe.class);
			final ItemStack ti = e.getSource();
			if (src == null) {
				if (ItemUtils.isBlank(ti, true) || !ti.getItemMeta().hasCustomModelData()) return;
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
					if (ItemUtils.isBlank(ti, true) || !ti.getItemMeta().hasCustomModelData()) return;
					si.setResult(ItemUtils.air);
				} else {
					if (src.getAddition().test(ti)) return;
					si.setResult(ItemUtils.air);
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
            if (ItemUtils.isBlank(sci.getInputItem(), true) ||
                !sci.getInputItem().getItemMeta().hasCustomModelData()) return;
        } else {
            if (src.getInputChoice().test(sci.getInputItem())) return;
        }
        sci.setResult(ItemUtils.air);
        e.setCancelled(true);
    }
	
	@EventHandler (priority = EventPriority.NORMAL, ignoreCancelled = true)
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
				if (!ItemUtils.isBlank(is, false)) {
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
						if (!ItemUtils.isBlank(is, false)) {
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
						if (!ItemUtils.isBlank(is, false)) {
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
				if (!ItemUtils.isBlank(in, false)) {
					giveItemAmt(p, in, in.getAmount());
					fni.setSmelting(ItemUtils.air);
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
		final ItemStack[] its = new ItemStack[amt/sts + 1];
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
		if (!ItemUtils.isBlank(it, false)) {
			final ItemStack tr = ci.getInputTemplate();
			if (tr != null && Tag.ITEMS_TRIM_TEMPLATES.isTagged(tr.getType())) {
				final Material mt = it.getType();
				final EquipmentSlot es = mt.getEquipmentSlot();
				final Multimap<Attribute, AttributeModifier> amt = mt.getDefaultAttributeModifiers(es);
				final ItemMeta im = it.getItemMeta();
				im.removeAttributeModifier(es);
				double arm = 0d;
				for (final AttributeModifier am : amt.get(Attribute.GENERIC_ARMOR)) {
					switch (am.getOperation()) {
					case ADD_NUMBER:
						arm += am.getAmount();
						break;
					case ADD_SCALAR, MULTIPLY_SCALAR_1:
						arm *= am.getAmount();
						break;
					}
				}
				double ath = 0d;
				for (final AttributeModifier am : amt.get(Attribute.GENERIC_ARMOR_TOUGHNESS)) {
					switch (am.getOperation()) {
					case ADD_NUMBER:
						ath += am.getAmount();
						break;
					case ADD_SCALAR, MULTIPLY_SCALAR_1:
						ath *= am.getAmount();
						break;
					}
				}
				double akb = 0d;
				for (final AttributeModifier am : amt.get(Attribute.GENERIC_KNOCKBACK_RESISTANCE)) {
					switch (am.getOperation()) {
					case ADD_NUMBER:
						akb += am.getAmount();
						break;
					case ADD_SCALAR, MULTIPLY_SCALAR_1:
						akb *= am.getAmount();
						break;
					}
				}
				
				final ItemStack add = ci.getInputMineral();
				im.addAttributeModifier(Attribute.GENERIC_ARMOR, new AttributeModifier(UUID.randomUUID(), 
					"generic.armor", arm * (1d + ItemUtils.getTrimMod(add, Attribute.GENERIC_ARMOR)), Operation.ADD_NUMBER, es));
				
				im.addAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS, new AttributeModifier(UUID.randomUUID(), 
					"generic.armor_toughness", ath * (1d + ItemUtils.getTrimMod(add, Attribute.GENERIC_ARMOR_TOUGHNESS)), Operation.ADD_NUMBER, es));
				
				im.addAttributeModifier(Attribute.GENERIC_KNOCKBACK_RESISTANCE, new AttributeModifier(UUID.randomUUID(), 
					"generic.armor_anticnockback", akb * (1d + ItemUtils.getTrimMod(add, Attribute.GENERIC_KNOCKBACK_RESISTANCE)), Operation.ADD_NUMBER, es));
				
				im.addAttributeModifier(Attribute.GENERIC_MAX_HEALTH, new AttributeModifier(UUID.randomUUID(), 
					"generic.armor_max_health", ItemUtils.getTrimMod(add, Attribute.GENERIC_MAX_HEALTH), Operation.ADD_NUMBER, es));
				
				im.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, new AttributeModifier(UUID.randomUUID(), 
					"generic.armor_attack_damage", ItemUtils.getTrimMod(add, Attribute.GENERIC_ATTACK_DAMAGE), Operation.MULTIPLY_SCALAR_1, es));
				
				im.addAttributeModifier(Attribute.GENERIC_ATTACK_KNOCKBACK, new AttributeModifier(UUID.randomUUID(), 
					"generic.armor_attack_knockback", ItemUtils.getTrimMod(add, Attribute.GENERIC_ATTACK_KNOCKBACK), Operation.MULTIPLY_SCALAR_1, es));
				
				im.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, new AttributeModifier(UUID.randomUUID(), 
					"generic.armor_attack_speed", ItemUtils.getTrimMod(add, Attribute.GENERIC_ATTACK_SPEED), Operation.MULTIPLY_SCALAR_1, es));
				
				im.addAttributeModifier(Attribute.GENERIC_MOVEMENT_SPEED, new AttributeModifier(UUID.randomUUID(), 
					"generic.armor_move_speed", ItemUtils.getTrimMod(add, Attribute.GENERIC_MOVEMENT_SPEED), Operation.MULTIPLY_SCALAR_1, es));
				
				it.setItemMeta(im);
				e.setResult(it);
			}
		}
	}
	
	@EventHandler
	public void onJoin(final PlayerJoinEvent e) {
		discRecs(e.getPlayer());
	}
}
