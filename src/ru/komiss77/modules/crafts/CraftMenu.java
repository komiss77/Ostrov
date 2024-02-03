package ru.komiss77.modules.crafts;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.BlastingRecipe;
import org.bukkit.inventory.CampfireRecipe;
import org.bukkit.inventory.CookingRecipe;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.SmithingRecipe;
import org.bukkit.inventory.SmithingTransformRecipe;
import org.bukkit.inventory.SmokingRecipe;
import org.bukkit.inventory.StonecuttingRecipe;

import ru.komiss77.Ostrov;
import ru.komiss77.modules.crafts.Crafts.Craft;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.TCUtils;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.ItemClickData;
import ru.komiss77.utils.inventory.SlotPos;



public class CraftMenu implements InventoryProvider {

    private static final ItemStack[] invIts;
	private static final int rad = 3;
	
    private final String key;
    private final boolean view;

    private Material tp;

    static {
        invIts = new ItemStack[27];
        for (int i = 0; i < 27; i++) {
            switch (i) {
                case 13:
                    invIts[13] = new ItemBuilder(Material.IRON_NUGGET).name("§7->").build();
                    break;
                case 9:
                    invIts[9] = new ItemBuilder(Material.CHEST).name("§dФормированый").build();
                    break;
                default:
                    invIts[i] = new ItemBuilder(Material.LIGHT_GRAY_STAINED_GLASS_PANE).name("§0.").build();
                    break;
            }
        }
    }
    
    
    public CraftMenu(final String key, final boolean view) {
    	this.key = key;
        this.view = view;
        final Recipe rc = Crafts.getRecipe(new NamespacedKey(Crafts.space, key), Recipe.class);
        if (rc instanceof ShapelessRecipe) {
        	tp = Material.ENDER_CHEST;
		} else if (rc instanceof FurnaceRecipe) {
        	tp = Material.FURNACE;
		} else if (rc instanceof SmokingRecipe) {
        	tp = Material.SMOKER;
		} else if (rc instanceof BlastingRecipe) {
        	tp = Material.BLAST_FURNACE;
		} else if (rc instanceof CampfireRecipe) {
        	tp = Material.CAMPFIRE;
		} else if (rc instanceof SmithingRecipe) {
        	tp = Material.SMITHING_TABLE;
		} else if (rc instanceof StonecuttingRecipe) {
        	tp = Material.STONECUTTER;
		} else {
        	tp = Material.CHEST;
		}
    }
    
    @Override
    public void init(final Player p, final InventoryContent its) {
        final Inventory inv = its.getInventory();
        if (inv != null) inv.setContents(invIts);
        final Recipe rc = Crafts.getRecipe(new NamespacedKey(Crafts.space, key), Recipe.class);
//        p.sendMessage("k=" + new NamespacedKey(Crafts.space, key) + ", f=" + rc + ", " + Crafts.crafts.toString());
        its.set(9, rc == null ? ClickableItem.of(makeIcon(tp), e -> {
	        switch (tp) {
	            case CHEST:
	            default:
	            	tp = Material.ENDER_CHEST;
	                break;
	            case ENDER_CHEST:
	            	tp = Material.FURNACE;
	                break;
	            case FURNACE:
	            	tp = Material.SMOKER;
	                break;
	            case SMOKER:
	            	tp = Material.BLAST_FURNACE;
	                break;
	            case BLAST_FURNACE:
	            	tp = Material.CAMPFIRE;
	                break;
	            case CAMPFIRE:
	            	tp = Material.SMITHING_TABLE;
	                break;
	            case SMITHING_TABLE:
	            	tp = Material.STONECUTTER;
	                break;
	            case STONECUTTER:
	            	tp = Material.CHEST;
	                break;
	        }
	        reopen(p, its);
        }) : ClickableItem.empty(makeIcon(tp)));
        its.set(16, view ? ClickableItem.empty(new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE)) : 
        	ClickableItem.from(new ItemBuilder(Material.GREEN_CONCRETE_POWDER).name("§aГотово!").build(), e -> {
            if (e.getEvent() instanceof InventoryClickEvent) {
            	((InventoryClickEvent) e.getEvent()).setCancelled(true);
            }
            final ItemStack rst = inv.getItem(14);
            if (ItemUtils.isBlank(rst, false)) {
                p.sendMessage("§cСначала закончите крафт!");
                return;
            }
            
            //запоминание крафта
        	final YamlConfiguration craftConfig = YamlConfiguration.loadConfiguration(new File(Ostrov.instance.getDataFolder().getAbsolutePath() + "/crafts/craft.yml"));
        	craftConfig.set(key, null);
        	craftConfig.set(key + ".result", ItemUtils.toString(rst, "="));
        	//craftConfig.set(key + ".world", Ostrov.subServer.toString());
        	craftConfig.set(key + ".type", getRecType(tp));
            final ConfigurationSection cs = craftConfig.getConfigurationSection(key);
            final NamespacedKey nKey = new NamespacedKey(Crafts.space, key);
        	Bukkit.getConsoleSender().sendMessage(cs.getName());
            final Recipe nrc;
            final ItemStack it;
            final String[] shp;
            switch (inv.getItem(9).getType()) {
            case SMOKER:
                it = inv.getItem(11);
                if (it == null || it.getType() == Material.AIR) {
                    p.sendMessage("§cСначала закончите крафт!");
                    return;
                }
                cs.set("recipe.a", ItemUtils.toString(it, "="));
                nrc = new SmokingRecipe(nKey, rst, CMDMatChoice.of(it), 0.5f, 100);
                Bukkit.removeRecipe(nKey);
                Bukkit.addRecipe(nrc);
                break;
            case BLAST_FURNACE:
                it = inv.getItem(11);
                if (it == null || it.getType() == Material.AIR) {
                    p.sendMessage("§cСначала закончите крафт!");
                    return;
                }
                cs.set("recipe.a", ItemUtils.toString(it, "="));
                nrc = new BlastingRecipe(nKey, rst, CMDMatChoice.of(it), 0.5f, 100);
                Bukkit.removeRecipe(nKey);
                Bukkit.addRecipe(nrc);
                break;
            case CAMPFIRE:
                it = inv.getItem(11);
                if (it == null || it.getType() == Material.AIR) {
                    p.sendMessage("§cСначала закончите крафт!");
                    return;
                }
                cs.set("recipe.a", ItemUtils.toString(it, "="));
                nrc = new CampfireRecipe(nKey, rst, CMDMatChoice.of(it), 0.5f, 500);
                Bukkit.removeRecipe(nKey);
                Bukkit.addRecipe(nrc);
                break;
            case FURNACE:
                it = inv.getItem(11);
                if (it == null || it.getType() == Material.AIR) {
                    p.sendMessage("§cСначала закончите крафт!");
                    return;
                }
                cs.set("recipe.a", ItemUtils.toString(it, "="));
                nrc = new FurnaceRecipe(nKey, rst, CMDMatChoice.of(it), 0.5f, 200);
                Bukkit.removeRecipe(nKey);
                Bukkit.addRecipe(nrc);
                break;
            case SMITHING_TABLE:
                it = inv.getItem(10);
                final ItemStack scd = inv.getItem(12);
                final ItemStack tpl = inv.getItem(2);
                if (ItemUtils.isBlank(it, false) || ItemUtils.isBlank(scd, false)) {
                    p.sendMessage("§cСначала закончите крафт!");
                    return;
                }
                cs.set("recipe.a", ItemUtils.toString(it, "="));
                cs.set("recipe.b", ItemUtils.toString(scd, "="));
                cs.set("recipe.c", ItemUtils.toString(tpl, "="));
                nrc = new SmithingTransformRecipe(nKey, rst, CMDMatChoice.of(tpl), CMDMatChoice.of(it), CMDMatChoice.of(scd), false);
                Bukkit.removeRecipe(nKey);
                Bukkit.addRecipe(nrc);
                break;
            case STONECUTTER:
                it = inv.getItem(11);
                if (it == null || it.getType() == Material.AIR) {
                    p.sendMessage("§cСначала закончите крафт!");
                    return;
                }
                cs.set("recipe.a", ItemUtils.toString(it, "="));
                nrc = new StonecuttingRecipe(nKey, rst, CMDMatChoice.of(it));
                Bukkit.removeRecipe(nKey);
                Bukkit.addRecipe(nrc);
                break;
            case ENDER_CHEST:
            	final ShapelessRecipe lrs = new ShapelessRecipe(nKey, rst);
                shp = new String[]{"abc", "def", "ghi"};
                for (byte cy = 0; cy < 3; cy++) {
                    for (byte cx = 1; cx < 4; cx++) {
                        final ItemStack ti = inv.getItem(cy * 9 + cx);
                        if (!ItemUtils.isBlank(ti, false)) {
                            lrs.addIngredient(CMDMatChoice.of(ti));
                            cs.set("recipe." + shp[cy].charAt(cx - 1), ItemUtils.toString(ti, "="));
                        }
                    }
                }
                nrc = lrs;
                Bukkit.removeRecipe(nKey);
                Bukkit.addRecipe(nrc);
                
//                p.sendMessage("f=" + lrs.getResult() + "" + lrs.getChoiceList().size());
                break;
            case CHEST:
            default://тоже магия
                final ShapedRecipe srs = new ShapedRecipe(nKey, rst);
                final ItemStack[] rcs = new ItemStack[rad*rad];
                int xMin = -1, xMax = -1, yMin = -1, yMax = -1;
                for (int cx = 0; cx < rad; cx++) {
                	for (int cy = 0; cy < rad; cy++) {
                        final ItemStack ti = inv.getItem(cy * 9 + cx + 1);
                        if (!ItemUtils.isBlank(ti, false)) {
                        	if (xMin == -1 || xMin > cx) xMin = cx;
                        	if (yMin == -1 || yMin > cy) yMin = cy;
                        	if (xMax < cx) xMax = cx;
                        	if (yMax < cy) yMax = cy;
                        }
                        rcs[cy*rad + cx] = ti;
                    }
                }
                
                if (xMin == -1 || yMin == -1) {
                    p.sendMessage("§cСначала закончите крафт!");
                    return;
                }
                
                shp = makeShape(xMax + 1 - xMin, yMax + 1 - yMin);
                final StringBuilder sb = new StringBuilder(shp.length * (xMax + 1 - xMin));
                for (final String s : shp) {
                	sb.append(":").append(s);
                }
                cs.set("shape", sb.substring(1));
                srs.shape(shp);
                
                for (int cx = xMax; cx >= xMin; cx--) {
                	for (int cy = yMax; cy >= yMin; cy--) {
                		final ItemStack ti = rcs[cy*rad + cx];
                        if (!ItemUtils.isBlank(ti, false)) {
                        	srs.setIngredient(shp[cy-yMin].charAt(cx-xMin), CMDMatChoice.of(ti));
                            cs.set("recipe." + shp[cy - yMin].charAt(cx - xMin), ItemUtils.toString(ti, "="));
                        }
                	}
                }
                nrc = srs;
                Bukkit.removeRecipe(nKey);
                Bukkit.addRecipe(srs);
                break;

            }

            Crafts.crafts.put(nKey, new Craft(nrc, pl -> true));

			try {
				craftConfig.save(new File(Ostrov.instance.getDataFolder().getAbsolutePath() + "/crafts/craft.yml"));
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			
			p.sendMessage(TCUtils.format(Ostrov.PREFIX + "§7Крафт §к" + key + " §7завершен!"));
            p.closeInventory();
        }));
        //final ClickableItem cl = ClickableItem.from(ItemUtils.air, e -> e.setCurrentItem(e.getCursor().asOne()));
        final Consumer<ItemClickData> canEdit = e -> {
			if (e.getEvent() instanceof InventoryClickEvent)
				((InventoryClickEvent) e.getEvent()).setCancelled(view);
        };
        switch (tp) {
		    case SMOKER:
		    case BLAST_FURNACE:
		    case CAMPFIRE:
		    case FURNACE:
		        if (rc == null) {
		        	setEditSlot(SlotPos.of(1, 2), null, its, canEdit);
		        	
		        	setEditSlot(SlotPos.of(1, 5), null, its, canEdit);
		        } else {
		        	setEditSlot(SlotPos.of(1, 2), ((CMDMatChoice) ((CookingRecipe<?>) rc).getInputChoice()).getItemStack(), its, canEdit);
		        	
		        	setEditSlot(SlotPos.of(1, 5), rc.getResult(), its, canEdit);
		        }
		        break;
		    case SMITHING_TABLE:
		        if (rc == null) {
		        	setEditSlot(SlotPos.of(0, 2), null, its, canEdit);
		        	setEditSlot(SlotPos.of(1, 1), null, its, canEdit);
		        	setEditSlot(SlotPos.of(1, 3), null, its, canEdit);
		
		        	setEditSlot(SlotPos.of(1, 5), null, its, canEdit);
		        } else {
		        	setEditSlot(SlotPos.of(0, 2), ((CMDMatChoice) ((SmithingTransformRecipe) rc).getTemplate()).getItemStack(), its, canEdit);
		        	setEditSlot(SlotPos.of(1, 1), ((CMDMatChoice) ((SmithingTransformRecipe) rc).getBase()).getItemStack(), its, canEdit);
		        	setEditSlot(SlotPos.of(1, 3), ((CMDMatChoice) ((SmithingTransformRecipe) rc).getAddition()).getItemStack(), its, canEdit);
		
		        	setEditSlot(SlotPos.of(1, 5), rc.getResult(), its, canEdit);
		        }
		        break;
		    case STONECUTTER:
		        if (rc == null) {
		        	setEditSlot(SlotPos.of(1, 2), null, its, canEdit);
		        	
		        	setEditSlot(SlotPos.of(1, 5), null, its, canEdit);
		        } else {
		        	setEditSlot(SlotPos.of(1, 2), ((CMDMatChoice) ((StonecuttingRecipe) rc).getInputChoice()).getItemStack(), its, canEdit);
		        	
		        	setEditSlot(SlotPos.of(1, 5), rc.getResult(), its, canEdit);
		        }
		        break;
		    case ENDER_CHEST:
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
		        	setEditSlot(SlotPos.of(0, 1), rci.hasNext() ? ((CMDMatChoice) rci.next()).getItemStack() : null, its, canEdit);
		        	setEditSlot(SlotPos.of(0, 2), rci.hasNext() ? ((CMDMatChoice) rci.next()).getItemStack() : null, its, canEdit);
		        	setEditSlot(SlotPos.of(0, 3), rci.hasNext() ? ((CMDMatChoice) rci.next()).getItemStack() : null, its, canEdit);
		        	setEditSlot(SlotPos.of(1, 1), rci.hasNext() ? ((CMDMatChoice) rci.next()).getItemStack() : null, its, canEdit);
		        	setEditSlot(SlotPos.of(1, 2), rci.hasNext() ? ((CMDMatChoice) rci.next()).getItemStack() : null, its, canEdit);
		        	setEditSlot(SlotPos.of(1, 3), rci.hasNext() ? ((CMDMatChoice) rci.next()).getItemStack() : null, its, canEdit);
		        	setEditSlot(SlotPos.of(2, 1), rci.hasNext() ? ((CMDMatChoice) rci.next()).getItemStack() : null, its, canEdit);
		        	setEditSlot(SlotPos.of(2, 2), rci.hasNext() ? ((CMDMatChoice) rci.next()).getItemStack() : null, its, canEdit);
		        	setEditSlot(SlotPos.of(2, 3), rci.hasNext() ? ((CMDMatChoice) rci.next()).getItemStack() : null, its, canEdit);
		        	
		        	setEditSlot(SlotPos.of(1, 5), rc.getResult(), its, canEdit);
		        }
		        break;
		    case CHEST:
		    default:
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
				        	setEditSlot(SlotPos.of(r, c + 1), chs == null ? ItemUtils.air : ((CMDMatChoice) chs).getItemStack(), its, canEdit);
			        	}
		        	}
		        	
		        	setEditSlot(SlotPos.of(1, 5), rc.getResult(), its, canEdit);
		        }
		        break;
		}
    }
    
    private static final String dsp = "abcdefghi";
    private static String[] makeShape(final int dX, final int dY) {
    	final String[] sp = new String[dY];
    	for (int i = 0; i < dY; i++) {
    		sp[i] = dsp.substring(i*dX, i*dX + dX);
    	}
		return sp;
	}



	private void setEditSlot(final SlotPos slot, final ItemStack it, final InventoryContent its, Consumer<ItemClickData> canEdit) {
        its.set(slot, ClickableItem.from(ItemUtils.isBlank(it, false) ? ItemUtils.air : it, canEdit));
        its.setEditable(slot, !view);
	}
    
    private ItemStack makeIcon(final Material mt) {
        return switch (mt) {
            default -> new ItemBuilder(Material.CHEST).name("§dФормированый").build();
            case ENDER_CHEST -> new ItemBuilder(Material.ENDER_CHEST).name("§5Безформенный").build();
            case FURNACE -> new ItemBuilder(Material.FURNACE).name("§6Печевой").build();
            case SMOKER -> new ItemBuilder(Material.SMOKER).name("§cЗапекающий").build();
            case BLAST_FURNACE -> new ItemBuilder(Material.BLAST_FURNACE).name("§7Плавильный").build();
            case CAMPFIRE -> new ItemBuilder(Material.CAMPFIRE).name("§eКостерный").build();
            case SMITHING_TABLE -> new ItemBuilder(Material.SMITHING_TABLE).name("§fКующий").build();
            case STONECUTTER -> new ItemBuilder(Material.STONECUTTER).name("§7Режущий").build();
        };
    }
    
	private String getRecType(final Material m) {
        return switch (m) {
            case SMOKER -> "smoker";
            case BLAST_FURNACE -> "blaster";
            case CAMPFIRE -> "campfire";
            case FURNACE -> "furnace";
            case SMITHING_TABLE -> "smith";
            case STONECUTTER -> "cutter";
            case ENDER_CHEST -> "noshape";
            default -> "shaped";
        };
    }


}
