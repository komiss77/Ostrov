package ru.komiss77.menu;


import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InputButton;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;





public class SignEditSelectLine implements InventoryProvider {
    
    
    //private static final ItemStack fill = new ItemBuilder(Material.LIME_STAINED_GLASS_PANE).name("§8.").build();;
    private final Sign sign;
    
    public SignEditSelectLine(final Sign sign) {
        this.sign = sign;
    }
    
    
    
    @Override
    public void init(final Player p, final InventoryContent contents) {
        //p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
        //contents.fillRect(0,0, 2,3, ClickableItem.empty(fill1));
        p.getWorld().playSound(p.getLocation(), Sound.ITEM_LODESTONE_COMPASS_LOCK, 15, 1);
        

        

        
        
        
        
        

        contents.add( new InputButton(InputButton.InputType.ANVILL, new ItemBuilder(Material.NAME_TAG)
                .name("§fСтрока 1")
                .lore("§7")
                .lore("§7Сейчас: ")
                .lore(sign.getLine(0))
                .lore("§7")
                .lore("§7Можно использовать")
                .lore("§7цветовые коды с §f&")
                .lore("§7ЛКМ - изменить")
                .lore("§7")
                .build(),  ChatColor.translateAlternateColorCodes('§', sign.getLine(0)), msg -> {
                    //msg = msg.replaceAll("&k", "").replaceAll("&", "§");
                    
                    if(msg.length()>32 ) {
                        p.sendMessage("§cЛимит 32 символа!");
                        return;
                    }
                    sign.setLine(0, msg);
                    sign.update();
                    reopen(p, contents);
                   // return;
                }));  
        
        
        contents.add( new InputButton(InputButton.InputType.ANVILL, new ItemBuilder(Material.NAME_TAG)
                .name("§fСтрока 2")
                .lore("§7")
                .lore("§7Сейчас: ")
                .lore(sign.getLine(1))
                .lore("§7")
                .lore("§7Можно использовать")
                .lore("§7цветовые коды с §f&")
                .lore("§7ЛКМ - изменить")
                .lore("§7")
                .build(),  ChatColor.translateAlternateColorCodes('§', sign.getLine(1)), msg -> {
                    //msg = msg.replaceAll("&k", "").replaceAll("&", "§");
                    
                    if(msg.length()>32 ) {
                        p.sendMessage("§cЛимит 32 символа!");
                        return;
                    }
                    sign.setLine(1, msg);
                    sign.update();
                    reopen(p, contents);
                   // return;
                }));  
        
        
        contents.add( new InputButton(InputButton.InputType.ANVILL, new ItemBuilder(Material.NAME_TAG)
                .name("§fСтрока 2")
                .lore("§7")
                .lore("§7Сейчас: ")
                .lore(sign.getLine(2))
                .lore("§7")
                .lore("§7Можно использовать")
                .lore("§7цветовые коды с §f&")
                .lore("§7ЛКМ - изменить")
                .lore("§7")
                .build(),  ChatColor.translateAlternateColorCodes('§', sign.getLine(2)), msg -> {
                    //msg = msg.replaceAll("&k", "").replaceAll("&", "§");
                    
                    if(msg.length()>32 ) {
                        p.sendMessage("§cЛимит 32 символа!");
                        return;
                    }
                    sign.setLine(2, msg);
                    sign.update();
                    reopen(p, contents);
                   // return;
                }));  
        
        
        contents.add( new InputButton(InputButton.InputType.ANVILL, new ItemBuilder(Material.NAME_TAG)
                .name("§fСтрока 3")
                .lore("§7")
                .lore("§7Сейчас: ")
                .lore(sign.getLine(3))
                .lore("§7")
                .lore("§7Можно использовать")
                .lore("§7цветовые коды с §f&")
                .lore("§7ЛКМ - изменить")
                .lore("§7")
                .build(),  ChatColor.translateAlternateColorCodes('§', sign.getLine(3)), msg -> {
                    //msg = msg.replaceAll("&k", "").replaceAll("&", "§");
                    
                    if(msg.length()>32 ) {
                        p.sendMessage("§cЛимит 32 символа!");
                        return;
                    }
                    sign.setLine(3, msg);
                    sign.update();
                    reopen(p, contents);
                   // return;
                }));  
        
        
        
        
        

   

  

        

        


        
        

    }
    
    
    
    
    
    
    
    
    
    
}
