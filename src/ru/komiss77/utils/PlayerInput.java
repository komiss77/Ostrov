package ru.komiss77.utils;

import java.util.function.Consumer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.komiss77.objects.CaseInsensitiveMap;
import ru.komiss77.Ostrov;
import ru.komiss77.utils.inventory.InputButton.InputType;
import ru.komiss77.version.VM;







// НЕ ПЕРЕМЕЩАТЬ!! 

public class PlayerInput implements Listener {
    
    private static CaseInsensitiveMap <Consumer> chatData;
    private static CaseInsensitiveMap <Consumer> signData;
    private static CaseInsensitiveMap <Location> signTemp;

    
    
    public PlayerInput() {
       chatData = new CaseInsensitiveMap<>();
       signData = new CaseInsensitiveMap<>();
       signTemp = new CaseInsensitiveMap<>();
       Bukkit.getPluginManager().registerEvents(PlayerInput.this, Ostrov.instance);
    }

    
    @Deprecated
    public static void get(final Player player, final Consumer<String> result) {
       chatData.put(player.getName(), result);
    }

    public static void get(final InputType type, final Player p, final Consumer<String> consumer, String suggest) {
        
        suggest = suggest.replaceAll("§", "&");
        
        switch (type) {
            
            case CHAT:
                p.closeInventory();
                final TextComponent msg = new TextComponent("§fНаберите в чате значение §b>Клик - подставить текущее<" );
                msg.setHoverEvent(new HoverEvent( HoverEvent.Action.SHOW_TEXT, new Text("§7Клик - подставить текст для редактирования")));
                msg.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, suggest));
                p.spigot().sendMessage(msg);
                //p.sendMessage("§fНаберите в чате значение и нажмите Enter");
                chatData.put(p.getName(), consumer);
                break;
                
            case SIGN:
                final Block b = findAnAirBlock(p.getLocation());
                if(b == null) {
                    p.sendMessage("§cПолучение данных невозможно, попробуйте в другом месте!");
                    consumer.accept("");
                } else {
                    b.setType(Material.ACACIA_SIGN);
                    
                    final Sign sign = (Sign) b.getState();
                    ItemUtils.fillSign(sign, suggest);
                    
                    signData.put(p.getName(), consumer);
                    signTemp.put(p.getName(), b.getLocation());
                    
                    Ostrov.sync( () -> {
                        try {
                            VM.getNmsServer().openSign(p, b);
                        } catch (NullPointerException ex) {
                            Ostrov.log_err("PlayerInput Sign : "+ex.getMessage());
                        }
                    }, 2);
                }
                break;
                
        }
        
    }

    
    
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onSignChange(final SignChangeEvent e) {
        if (signData.containsKey(e.getPlayer().getName()) && signTemp.containsKey(e.getPlayer().getName()) ) {
            final Block b = signTemp.get(e.getPlayer().getName()).getBlock();
            
            if (!b.getLocation().equals(e.getBlock().getLocation())) return;
            
            final Consumer consumer = (Consumer)signData.get(e.getPlayer().getName());
            
            StringBuilder sb = new StringBuilder();
            for (String line : e.getLines()) {
                if (line != null && line.length() > 0) {
                    sb.append(line);
                }
            }
            final String input = sb.toString();
            
            e.setCancelled(true);
            b.setType(Material.AIR);
            signTemp.remove(e.getPlayer().getName());
            
            Ostrov.sync( () -> {
                consumer.accept(input.replaceAll("&k", "").replaceAll("&", "§"));
            }, 0);
            signData.remove(e.getPlayer().getName());
        }
            
    }
    
    private static Block findAnAirBlock(final Location loc) {
        while(loc.getY() < 255 && loc.getBlock().getType() != Material.AIR) {
            loc.add(0, 1, 0);
        }
        return loc.getY() < 255 && loc.getBlock().getType() == Material.AIR ? loc.getBlock() : null;
    }
    
    /*
    private void openSign(final Player p, final Block b) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    Object world = b.getWorld().getClass().getMethod("getHandle").invoke(b.getWorld());
                    Object blockPos = getNMSClass("BlockPosition").getConstructor(int.class, int.class, int.class).newInstance(b.getX(), b.getY(), b.getZ());
                    Object sign = world.getClass().getMethod("getTileEntity", getNMSClass("BlockPosition")).invoke(world, blockPos);
                    Object player = p.getClass().getMethod("getHandle").invoke(p);
                    player.getClass().getMethod("openSign", getNMSClass("TileEntitySign")).invoke(player, sign);
                } catch (Exception e) {
                    Ostrov.instance.getLogger().log(Level.WARNING, "can not open Sign : "+e.getMessage());
                }
            }
        }.runTaskLater(Ostrov.instance, 2L);
    }*/
    
    
    
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(final AsyncPlayerChatEvent e) {
        if (chatData.containsKey(e.getPlayer().getName())) {
            final Consumer consumer = (Consumer)chatData.get(e.getPlayer().getName());
            chatData.remove(e.getPlayer().getName());
            Ostrov.sync( () -> {
                consumer.accept(e.getMessage().replaceAll("&k", "").replaceAll("&", "§"));
            }, 0);
            e.getPlayer().sendMessage("");
            e.getPlayer().sendMessage("§aЗначение получено: ");
            e.getPlayer().sendMessage("§f"+e.getMessage());
            e.getPlayer().sendMessage("");
            e.getRecipients().clear();
    }
        
        
        
        

   }



















    
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(final PlayerQuitEvent e) {
        if (chatData.containsKey(e.getPlayer().getName())) chatData.remove(e.getPlayer().getName());
        if (signData.containsKey(e.getPlayer().getName())) signData.remove(e.getPlayer().getName());
        if (signTemp.containsKey(e.getPlayer().getName())) {
            signTemp.get(e.getPlayer().getName()).getBlock().setType(Material.AIR);
            signTemp.remove(e.getPlayer().getName());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onKick(final PlayerKickEvent e) {
        if (chatData.containsKey(e.getPlayer().getName())) chatData.remove(e.getPlayer().getName());
        if (signData.containsKey(e.getPlayer().getName())) signData.remove(e.getPlayer().getName());
        if (signTemp.containsKey(e.getPlayer().getName())) {
            signTemp.get(e.getPlayer().getName()).getBlock().setType(Material.AIR);
            signTemp.remove(e.getPlayer().getName());
        }
    }
    
}
