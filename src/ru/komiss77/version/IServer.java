package ru.komiss77.version;

import java.util.List;
import java.util.UUID;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;


public interface IServer {

    //String getlevelName (final World bukkitWorld);
        
    //int getMaxWorldSize (final World bukkitWorld);
    int getitemDespawnRate (final World bukkitWorld);

    int getTps ();

 //   public Player getOfflinePlayer(String name, UUID uuid, Location location);
    
   // @Deprecated
   // public ItemStack getCustomHead (String base64code, final String item_name, final String lore);
    
  //  public BookMeta addPages (BookMeta bookMeta, List <TextComponent> pagesList);
    
    public void sendFakeEquip(final Player player, final int playerInventorySlot, final ItemStack itemStack);
    
    public void sendChunkChange (final Player player, final Chunk chunk); //skyworld

    public void BorderDisplay(final Player player, final Location minPoint, final Location maxPoint, final boolean tpToCenter);
    
    public void openSign(final Player player, final Block b);
    
}
