package ru.komiss77.commands;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.protocols.Protocol77;

public class ProtocolCmd implements CommandExecutor, TabCompleter {
	
	public static final String grp = "xpanitely";
	
	@Override
	public List<String> onTabComplete(final CommandSender se, final Command cmd, final String label, final String[] args) {
		if (se instanceof final Player p) {
			if (PM.getOplayer(p).hasGroup(grp)) {
				if (args.length == 2) {
					return Arrays.asList("77");
				}
			}
		}
		return null;
	}

    @Override
    public boolean onCommand(final CommandSender se, final Command cmd, final String label, final String[] args) {
        if (label.equalsIgnoreCase("protocol") && se instanceof final Player p) {
            if (PM.getOplayer(p).hasGroup(grp) && args.length == 1) {
            	switch (ApiOstrov.getInteger(args[0])) {
				case 77:
					if (Protocol77.active) {
	                    p.sendMessage(Ostrov.PREFIX + "§cПротокол уже активен!");
	                    return false;
					}
					
					new Protocol77(p);
                    return true;
				default:
					break;
				}
            }
        }
        return false;
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
