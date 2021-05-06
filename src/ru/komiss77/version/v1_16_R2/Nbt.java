package ru.komiss77.version.v1_16_R2;

import java.util.HashMap;
import net.minecraft.server.v1_16_R2.NBTTagCompound;
import org.bukkit.craftbukkit.v1_16_R2.inventory.CraftItemStack;
import ru.komiss77.ApiOstrov;
import ru.komiss77.version.INbt;



public class Nbt implements INbt {

    

    @Override
    public boolean hasString(final org.bukkit.inventory.ItemStack item, final String name) {
        NBTTagCompound tag = getTag(item);
        return tag.hasKey(name);
    }
    
    @Override
    public org.bukkit.inventory.ItemStack addString(org.bukkit.inventory.ItemStack item, final String name, final String value) {
        NBTTagCompound tag = getTag(item);
        if (value.equals("true") || value.equals("false") ) {
            tag.setBoolean(name, Boolean.valueOf(value));
        } else if (ApiOstrov.isInteger(value)) {
            tag.setInt(name, Integer.valueOf(value));
        } else {
            tag.setString(name, value);
        }
        return setTag(item, tag);
    }
    
    @Override
    public String getString(org.bukkit.inventory.ItemStack item, String name) {
        NBTTagCompound tag = getTag(item);
        return tag.getString(name);
    }          

    @Override
   public org.bukkit.inventory.ItemStack setDamage(org.bukkit.inventory.ItemStack item, int damage) {
        net.minecraft.server.v1_16_R2.ItemStack itemNms = CraftItemStack.asNMSCopy(item);
        itemNms.setDamage(2);
        return CraftItemStack.asBukkitCopy(itemNms);
    }          

    @Override
   public int getDamage(org.bukkit.inventory.ItemStack item) {
        net.minecraft.server.v1_16_R2.ItemStack itemNms = CraftItemStack.asNMSCopy(item);
        return itemNms.getDamage();
    }          

    @Override
    public HashMap<String,String> getTagMap(org.bukkit.inventory.ItemStack item) {
        HashMap <String, String> map = new HashMap<>();
        NBTTagCompound tag = getTag(item);
        for ( String t : tag.getKeys()) {
            map.put (t, tag.get(t).toString());
        }
        return map;
    }
    
    private NBTTagCompound getTag(final org.bukkit.inventory.ItemStack item) {
        net.minecraft.server.v1_16_R2.ItemStack itemNms = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag;
        if (itemNms.hasTag()) tag = itemNms.getTag();
        else tag = new NBTTagCompound();
        return tag;
    }
    
    private org.bukkit.inventory.ItemStack setTag(org.bukkit.inventory.ItemStack item, NBTTagCompound tag) {
        net.minecraft.server.v1_16_R2.ItemStack itemNms = CraftItemStack.asNMSCopy(item);
        itemNms.setTag(tag);
        return CraftItemStack.asBukkitCopy(itemNms);
    }

    

    
}
