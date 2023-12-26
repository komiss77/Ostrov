package ru.komiss77.utils;


import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.Player;

import ru.komiss77.utils.inventory.InputButton;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;





public class SignEditSelectLine implements InventoryProvider {
    
    
    //private static final ItemStack fill = new ItemBuilder(Material.LIME_STAINED_GLASS_PANE).name("§8.").build();;
    private final Sign sign;
    private final SignSide side;
    
    public SignEditSelectLine(final Sign sign) {
        this.sign = sign;
        this.side = sign.getSide(Side.FRONT);//---
    }
    
    
    
    @Override
    public void init(final Player p, final InventoryContent contents) {
        //p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
        //contents.fillRect(0,0, 2,3, ClickableItem.empty(fill1));
        p.getWorld().playSound(p.getLocation(), Sound.ITEM_LODESTONE_COMPASS_LOCK, 15, 1);
        

        

        
        
        
        
        

        contents.add( new InputButton(InputButton.InputType.ANVILL, new ItemBuilder(Material.NAME_TAG)
                .name("§fСтрока 1")
                .addLore("§7")
                .addLore("§7Сейчас: ")
                .addLore(side.line(0))
                .addLore("§7")
                .addLore("§7Можно использовать")
                .addLore("§7цветовые коды с §f&")
                .addLore("§7ЛКМ - изменить")
                .addLore("§7")
                .addLore("§7Тэги для 1 строки:")
                .addLore("[Место] §7- варп; стока 2 - название")
                .addLore("[Команда] §7- варп; стока 2 - команда")
                .addLore("§7")
                .addLore("§7")
                .build(), TCUtils.setColorChar('&', TCUtils.toString(side.line(0))), msg -> {
                    //msg = msg.replaceAll("&k", "").replaceAll("&", "§");
                    
                    if(msg.length()>32 ) {
                        p.sendMessage("§cЛимит 32 символа!");
                        return;
                    }
                    sign.getSide(Side.FRONT).line(0, TCUtils.format(msg));
                    sign.update();
                    reopen(p, contents);
                   // return;
                }));  
        
        
        contents.add( new InputButton(InputButton.InputType.ANVILL, new ItemBuilder(Material.NAME_TAG)
                .name("§fСтрока 2")
                .addLore("§7")
                .addLore("§7Сейчас: ")
                .addLore(sign.getSide(Side.FRONT).line(1))
                .addLore("§7")
                .addLore("§7Можно использовать")
                .addLore("§7цветовые коды с §f&")
                .addLore("§7ЛКМ - изменить")
                .addLore("§7")
                .build(), TCUtils.setColorChar('&', TCUtils.toString(side.line(1))), msg -> {
                    //msg = msg.replaceAll("&k", "").replaceAll("&", "§");
                    
                    if(msg.length()>32 ) {
                        p.sendMessage("§cЛимит 32 символа!");
                        return;
                    }
                    sign.getSide(Side.FRONT).line(1, TCUtils.format(msg));
                    sign.update();
                    reopen(p, contents);
                   // return;
                }));  
        
        
        contents.add( new InputButton(InputButton.InputType.ANVILL, new ItemBuilder(Material.NAME_TAG)
                .name("§fСтрока 2")
                .addLore("§7")
                .addLore("§7Сейчас: ")
                .addLore(sign.getSide(Side.FRONT).line(2))
                .addLore("§7")
                .addLore("§7Можно использовать")
                .addLore("§7цветовые коды с §f&")
                .addLore("§7ЛКМ - изменить")
                .addLore("§7")
                .build(), TCUtils.setColorChar('&', TCUtils.toString(side.line(2))), msg -> {
                    //msg = msg.replaceAll("&k", "").replaceAll("&", "§");
                    
                    if(msg.length()>32 ) {
                        p.sendMessage("§cЛимит 32 символа!");
                        return;
                    }
                    sign.getSide(Side.FRONT).line(2, TCUtils.format(msg));
                    sign.update();
                    reopen(p, contents);
                   // return;
                }));  
        
        
        contents.add( new InputButton(InputButton.InputType.ANVILL, new ItemBuilder(Material.NAME_TAG)
                .name("§fСтрока 3")
                .addLore("§7")
                .addLore("§7Сейчас: ")
                .addLore(sign.getSide(Side.FRONT).line(3))
                .addLore("§7")
                .addLore("§7Можно использовать")
                .addLore("§7цветовые коды с §f&")
                .addLore("§7ЛКМ - изменить")
                .addLore("§7")
                .build(), TCUtils.setColorChar('&', TCUtils.toString(side.line(3))), msg -> {
                    //msg = msg.replaceAll("&k", "").replaceAll("&", "§");
                    
                    if(msg.length()>32 ) {
                        p.sendMessage("§cЛимит 32 символа!");
                        return;
                    }
                    sign.getSide(Side.FRONT).line(3, TCUtils.format(msg));
                    sign.update();
                    reopen(p, contents);
                   // return;
                }));  
        
        
        
        
        

   

  

        

        


        
        

    }
    
    
    
    
    
    
    
    
    
    
}
