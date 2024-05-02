
package ru.komiss77;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import ru.komiss77.modules.menuItem.MenuItemsManager;

//MySQL Player Data Bridge
//https://www.spigotmc.org/resources/mysql-inventory-bridge.7849/

public class EncodeData {
    
    private static final ItemStack empty;

    static {
        empty = new ItemStack(Material.AIR);
    }
    
    public static String itemStackArrayToBase64(final ItemStack[] items) {
        
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream bukkitOutStream = new BukkitObjectOutputStream(outputStream);
            // Write the size of the inventory
            bukkitOutStream.writeInt(items.length);

            // Save every element in the list
            for (int i = 0; i < items.length; i++) {
                if (MenuItemsManager.isSpecItem(items[i])) {
                    bukkitOutStream.writeObject(empty); 
                } else {
                    bukkitOutStream.writeObject(items[i]); 
                }
            }

            // Serialize that array
            bukkitOutStream.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
            
        } catch (IOException e) {
            Ostrov.log_err("itemStackArrayToBase64 - "+e.getMessage());
            return "error";
        }
    }
	
    
    
    
    public static ItemStack @Nullable [] itemStackArrayFromBase64(final String data) {
    	try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack[] items = new ItemStack[dataInput.readInt()];
    
            // Read the serialized inventory
            for (int i = 0; i < items.length; i++) {
                final ItemStack is = (ItemStack) dataInput.readObject();
            	if (MenuItemsManager.isSpecItem(is)) {
                    items[i] = empty;
                } else {
                    items[i] = is;
                }
            }
            
            dataInput.close();
            return items;
            
        } catch (IllegalArgumentException | ClassNotFoundException | IOException e) {
            Ostrov.log_err("itemStackArrayFromBase64 - "+e.getMessage());
            return null;
        }
    }
    
    
    
    public static String potionEffectsToBase64(Collection<PotionEffect> collection) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BukkitObjectOutputStream boos = new BukkitObjectOutputStream(baos);

            boos.writeInt(collection.toArray().length);

            for (int i = 0; i < collection.toArray().length; ++i) {
                boos.writeObject(collection.toArray()[i]);
            }

            boos.close();
            return Base64Coder.encodeLines(baos.toByteArray());
        } catch (IOException e) {
            Ostrov.log_err("potionEffectsToBase64 - "+e.getMessage());
            return "error";
        }
    }

    public static @Nullable Collection<PotionEffect> potionEffectsFromBase64(String s) {
        
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(Base64Coder.decodeLines(s));
            BukkitObjectInputStream bois = new BukkitObjectInputStream(bais);
            PotionEffect[] apotioneffect = new PotionEffect[bois.readInt()];

            final ArrayList<PotionEffect> arraylist = new ArrayList<>();
            for (int i = 0; i < apotioneffect.length; ++i) {
                arraylist.add((PotionEffect) bois.readObject());
            }

            bois.close();
            return arraylist;
        } catch (ClassNotFoundException | IOException e) {
            Ostrov.log_err("potionEffectsFromBase64 - "+e.getMessage());
            return null;
        }
    }    
    
    
    
}
