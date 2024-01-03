package ru.komiss77.commands;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import ru.komiss77.Ostrov;

public class InvseeCmd implements CommandExecutor, TabCompleter {
	
	@Override
	public List<String> onTabComplete(final CommandSender se, final Command cmd, final String label, final String[] args) {
		if (se instanceof Player) {
			final Player p = (Player) se;
			if (p.hasPermission("ostrov.invsee")) {
				if (args.length == 2) {
					return Arrays.asList("main", "ender", "extra");
				}
			}
		}
		return null;
	}

    @Override
    public boolean onCommand (final CommandSender se, final Command cmd, final String label, final String[] args) {
        if (se instanceof final Player pl) {
            if (se.hasPermission("ostrov.invsee")) {
            	final Player opl;
            	switch (args.length) {
				case 2:
					opl = Bukkit.getPlayerExact(args[0]);
					if (opl == null) {
                        se.sendMessage(Ostrov.PREFIX + "§cИгрок " + args[0] + " не онлайн!");
                        return false;
					}
					
					final Inventory inv;
					switch (args[1]) {
					case "main":
						inv = opl.getInventory();
						break;
					case "ender":
						inv = opl.getEnderChest();
						break;
					case "extra":
					default:
                        se.sendMessage(Ostrov.PREFIX + "§cНеправильный синтакс комманды!");
                        return false;
					}
					
					pl.openInventory(inv);
					opl.sendMessage(Ostrov.PREFIX + pl.getName() + " §aпросматривает твой инвентарь!");
					break;
				case 1:
					opl = Bukkit.getPlayerExact(args[0]);
					if (opl == null) {
                        se.sendMessage(Ostrov.PREFIX + "§cИгрок " + args[0] + " не онлайн!");
                        return false;
					}
					
					pl.openInventory(opl.getInventory());
					opl.sendMessage(Ostrov.PREFIX + pl.getName() + " §aпросматривает твой инвентарь!");
					break;
				default:
					break;
				}
            } else {
                se.sendMessage("§cУ Вас нет права ostrov.invsee");
            }
        }
        return true;
    }
    
    
    
    /*private PlayerInventory loadInventory(Player p) {
        return p != null ? p.getInventory() : null;
    }*/

    /*private Inventory loadItemsFromPlayer(String name, OfflinePlayer player) {
        String type = "§1Инвентарь ";

        if (name.equals("EnderItems")) {
            type = "§1Эндэр-сундук ";
        }

        NBTList list = PowerNBT.getApi().readOfflinePlayer(player).getList(name);
        Inventory inventory = Bukkit.createInventory((InventoryHolder) null, 36, type + player.getName() + " §4[Только просмотр]");

        if (player != null) {
            for (int inc = 0; inc < list.size(); ++inc) {
                NBTCompound nbt = (NBTCompound) list.get(inc);
                byte slot = nbt.getByte("Slot");

                if (slot < 100) {
                    inventory.setItem(slot, this.getItemFromNBT(nbt));
                }
            }
        }

        return inventory;
    }

    private ItemStack[] loadArmorFromPlayer(OfflinePlayer player) {
        NBTList list = PowerNBT.getApi().readOfflinePlayer(player).getList("Inventory");
        ItemStack[] armor = new ItemStack[4];

        if (player != null) {
            for (int inc = 0; inc < list.size(); ++inc) {
                NBTCompound nbt = (NBTCompound) list.get(inc);
                byte slot = nbt.getByte("Slot");

                if (slot >= 100) {
                    armor[slot - 100] = this.getItemFromNBT(nbt);
                }
            }
        }

        return armor;
    }*/

   /* private ItemStack getItemFromNBT(NBTCompound nbt) {
        try {
            //net.minecraft.server.v1_14_R1.Item nmsitem = Item.//Item.getById( Integer.valueOf(nbt.getString("id")) );
            CraftItemStack nmsitem = CraftItemStack.asNewCraftStack( Item.getById(Integer.valueOf(nbt.getString("id"))) , nbt.getInt("Count"));
            ItemStack is = CraftItemStack.asNewCraftStack(null);
            Class e = NMSHandler.getCB("inventory.CraftItemStack");
            Class nmsItem = NMSHandler.getNMS("Item");
            //ItemStack item = (ItemStack) e.getDeclaredMethod("asNewCraftStack", new Class[] { nmsItem, Integer.TYPE}).invoke((Object) null, new Object[] { nmsItem.getDeclaredMethod("d", new Class[] { String.class}).invoke((Object) null, new Object[] { nbt.getString("id")}), nbt.getInt("Count")});
            ItemStack item = (ItemStack) e.getDeclaredMethod("asNewCraftStack", new Class[] { nmsItem, Integer.TYPE}).invoke((Object) null, new Object[] { nmsItem.getDeclaredMethod("d", new Class[] { String.class}).invoke((Object) null, new Object[] { nbt.getString("id")}), nbt.getInt("Count")});

            item.setDurability(nbt.getShort("Damage"));
            if (nbt.containsKey("tag")) {
                NBTCompound comp = nbt.getCompound("tag");

                if (comp.containsKey("ench")) {
                    Object[] lore;
                    int l = (lore = comp.getList("ench").toArray()).length;

                    for (int disp = 0; disp < l; ++disp) {
                        Object m = lore[disp];

                        if (m instanceof NBTCompound) {
                            NBTCompound i = (NBTCompound) m;

                            item.addUnsafeEnchantment(Enchantment.getById(i.getShort("id")), i.getShort("lvl"));
                        }
                    }
                }

                if (comp.containsKey("display")) {
                    ItemMeta itemmeta = item.getItemMeta();
                    NBTCompound nbtcompound = comp.getCompound("display");

                    if (nbtcompound.containsKey("Lore")) {
                        NBTList nbtlist = nbtcompound.getList("Lore");
                        ArrayList arraylist = new ArrayList();

                        for (int i = 0; i < nbtlist.size(); ++i) {
                            arraylist.add((String) nbtlist.get(i));
                        }

                        itemmeta.setLore(arraylist);
                    }

                    if (nbtcompound.containsKey("Name")) {
                        itemmeta.setDisplayName(nbtcompound.getString("Name"));
                    }

                    item.setItemMeta(itemmeta);
                }
            }

            return item;
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            //exception.printStackTrace();
            Ostrov.log_err("INVSEE: "+e.getMessage());
            return new ItemStack(Material.DIRT, -1);
        }
    }*/

}
