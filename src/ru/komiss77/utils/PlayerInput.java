package ru.komiss77.utils;

import java.util.WeakHashMap;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.world.XYZ;
import ru.komiss77.objects.InputData;
import ru.komiss77.utils.inventory.InputButton;
import ru.komiss77.utils.inventory.InputButton.InputType;
import ru.komiss77.version.AnvilGUI;
import ru.komiss77.version.VM;




// НЕ ПЕРЕМЕЩАТЬ!! 

public class PlayerInput implements Listener {

    public static final WeakHashMap<Player,InputData> fallBackdata;
//    private static final ItemStack right;
    
    static {
        fallBackdata = new WeakHashMap<>();
//        right = new ItemBuilder(Material.PAPER).name("режим ввода текста").addLore("§7Наберите в строке", "§7значение и нажмите", "§7на результат.").build();
    }

    public static void get(final InputType type, final Player p, final Consumer<String> consumer, String suggest) {
        
        final String sugg = suggest = suggest==null ? "" : suggest.replaceAll("§", "&");
        XYZ xyz = null;
                
        switch (type) {
            case ANVILL -> {
                
                final ItemStack left = new ItemBuilder(Material.PAPER)
                        //.name("§7Сейчас:")
                        //.addLore("§7"+sugg)
                        .addLore("Режим ввода текста")
                        .addLore("§7Наберите в строке")
                        .addLore("§7значение и нажмите")
                        .addLore("§7на результат.")
                        .addLore("")
                        .addLore("§bКлик - оставить как есть")
                        .build();
                
                new AnvilGUI.Builder()
                    .title("Введите значение")
                    .text(sugg)
                    .itemLeft(left)
                    //.itemRight(right)
                    .onLeftInputClick( p1 -> consumer.accept(sugg))
                    .onComplete( (p1, msg) -> {
                            consumer.accept(msg); 
                            return AnvilGUI.Response.text(msg); 
                        }
                    )
                    .open(p);
                return;
            }
            
            case CHAT -> {
                p.closeInventory();
                p.sendMessage(Component.text()
                    .append(Component.text("§fНаберите в чате значение "+(suggest.isEmpty() ? "и нажмите Ввод" : "§b>Клик - подставить текущее<")))
                    .hoverEvent(HoverEvent.showText(Component.text("§7Клик - подставить текст для редактирования")))
                    .clickEvent(ClickEvent.runCommand(suggest))
                    .build());
            	}
            
            case SIGN -> {
                xyz = new XYZ(p.getWorld().getName(), p.getLocation().getBlockX(), p.getLocation().getBlockY()-3, p.getLocation().getBlockZ());
                VM.getNmsServer().signInput(p, suggest, xyz);
            }
                
        }
        
        final Oplayer op = PM.getOplayer(p);
        if (op!=null) {
            op.inputData = new InputData(type, consumer, xyz);
        } else {
            fallBackdata.put(p, new InputData(type, consumer, xyz));
        }

    }
    
  /*  public static void unregister (final InputData data) {
        if (data != null && data.lst != null) {
            HandlerList.unregisterAll(data.lst);
            data.lst = null;
        }
    }*/
    
    
    public static void onInput(final String name, final InputButton.InputType type, final String result) { //вызов только SUNC !!!
        
        final Oplayer op = PM.getOplayer(name);
        final Player p = Bukkit.getPlayerExact(name);
        
        InputData data = null;
        if (op==null) {
            if (p!=null) {
                data = fallBackdata.remove(p);
            }
        } else {
            data = op.inputData;
            op.inputData = null;
        }
        if (data==null || data.type != type) {
            return;
        }

        data.setResult(result);
        
        if (p != null) {
            if (data.type==InputType.CHAT) {
                p.sendMessage("");
                p.sendMessage("§aЗначение получено: ");
                p.sendMessage("§f"+data.getResult());
                p.sendMessage("");
            } else if (data.type==InputType.SIGN) {
                if (data.xyz != null) {
                    final Location loc =  data.xyz.getCenterLoc();
                    p.sendBlockChange(loc, loc.getBlock().getBlockData());
                }
            }
            data.accept();
        }
    }
    /*
    public static void onSignPacket(final String name, final String input) {
        final InputData data = signData.get(name);
        if (data!=null) {
            final Player p = Bukkit.getPlayerExact(name);
            if (p!=null) {
                remove(p);
                if (data.consumer!=null) {
                    data.consumer.accept(input);
                }
            } else {
                signData.remove(name);
                HandlerList.unregisterAll(data.lst);
            }
        }
    }

    
        
    private static void remove (final Player p) {
//Ostrov.log_warn("remove "+p.getName());
        final String name = p.getName();
        InputData data = chatData.remove(name);
        if (data!=null) {
            HandlerList.unregisterAll(data.lst);
        }
        data = signData.remove(name);
        if (data!=null) {
            VM.temp.wipe (p,  "ostrov_sign_"+name);
            if (data.xyz != null) {
                final Location loc =  data.xyz.getCenterLoc();
                if (VM.mcVersion.equals("v1_19_R1")) {
                   loc.getBlock().setType(Material.AIR);
                } else {
                    p.sendBlockChange(loc, loc.getBlock().getBlockData());
                }
            }
        }
    }
   
    
    
    @Deprecated
    private static Block findAnAirBlock(final Location loc) {
        for (int y = loc.getBlockY(); y < loc.getWorld().getMaxHeight(); y++) {
            if ( VM.getNmsServer().getFastMat(loc.getWorld(), loc.getBlockX(), y, loc.blockZ())==Material.AIR) {
                loc.setY(y);
                return loc.getBlock();
            }
        }
        return null; */
        //while(loc.getY() < loc.getWorld().getMaxHeight() && VM.getNmsServer().getFastMat(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.blockZ())!=Material.AIR) {//loc.getBlock().getType() != Material.AIR) {
        //    loc.add(0, 1, 0);
        //}
        //return loc.getY() < 255 && loc.getBlock().getType() == Material.AIR ? loc.getBlock() : null;
    }
    

    
    


    


    

    
  
    
