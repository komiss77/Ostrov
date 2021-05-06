package ru.komiss77.Commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import ru.komiss77.Listener.InvSeeListener;
import ru.komiss77.utils.InvStatus;
import ru.komiss77.version.VM;

public class Invsee implements CommandExecutor {


    public Invsee() {}

    @Override
    public boolean onCommand (final CommandSender se, Command cmd, String label, final String[] arg) {
        
       // (new Thread(new Runnable() {
        //    @Override
        ////    public void run() {
                
                if (se.hasPermission("ostrov.invsee")) {
                    if (arg.length > 0) {
                        
                        
                        
                        
                        
                        /*
                        OfflinePlayer offline = Bukkit.getOfflinePlayer(arg[0]);
                        Player online = Bukkit.getPlayerExact(arg[0]);
                        ItemStack[] armor = new ItemStack[4];
                        boolean isArmor = arg.length > 1 && arg[1].equalsIgnoreCase("Armor");

                        if (!offline.hasPlayedBefore() && online == null) {
                            se.sendMessage("§cИгрок "+arg[0]+" никогда не играл на этом сервере!");
                            return;
                        }

                        Object inventory;
                        Inventory enderChest;

                        
                        if (offline.isOnline()) {
                            
                            //armor = loadInventory(online).getArmorContents();
                           // enderChest = online.getEnderChest();
                           // inventory = loadInventory(online);
                            
                        } else {
                            
                            online = VM.getNmsServer().getOfflinePlayer( arg[0], offline.getUniqueId(), ((Player)se).getLocation() );
                            //if (!Ostrov.powerNBT) {
                            //    se.sendMessage( "Для просмотра оффлайн-игроков нужен плагин PowerNBT !");
                            //    return;
                            //}

                            //armor = loadArmorFromPlayer(offline);
                            //enderChest = loadItemsFromPlayer("EnderItems", offline);
                            //inventory = loadItemsFromPlayer("Inventory", offline);
                            
                        }

                            armor = loadInventory(online).getArmorContents();
                            enderChest = online.getEnderChest();
                            inventory = loadInventory(online);

                            if (arg.length > 1) {
                            if (isArmor) {
                                Inventory player = Bukkit.createInventory(offline.getPlayer(), 9, "§1Аммуниция");

                                player.setContents(armor);
                                inventory = player;
                            }

                            if (arg[1].equalsIgnoreCase("Ender")) {
                                inventory = enderChest;
                            }
                        }

                        Player p = Bukkit.getPlayer(se.getName());
                        InvSeeListener.addPlayer(p, new InvStatus(isArmor, online));
                        //CmdInvSee.this.plugin.addPlayer(player1, new InvStatus(isArmor, online));
                        p.openInventory((Inventory) inventory);
                        se.sendMessage("§aОтрываем инвентарь" + offline.getName() +"...");
                        */
                        
                    } else {
                        se.sendMessage(ChatColor.RED + "Используйте: /invsee <ник> [Armor/Ender]");
                    }
                } else {
                    se.sendMessage("§cУ Вас нет пава ostrov.invsee");
                }

         //   }
        //})).start();
        return true;
    }
    
    
    
    private PlayerInventory loadInventory(Player p) {
        return p != null ? p.getInventory() : null;
    }

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
