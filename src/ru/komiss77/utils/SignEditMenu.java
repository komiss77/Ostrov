package ru.komiss77.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.Player;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InputButton;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;


public class SignEditMenu implements InventoryProvider {
    
    private final Sign sign;
    private final SignSide frontSide;
    private final SignSide backSide;
    
    public SignEditMenu(final Sign sign) {
        this.sign = sign;
        frontSide = sign.getSide(Side.FRONT);//---
        backSide = sign.getSide(Side.BACK);//---
    }
    
    
    
    @Override
    public void init(final Player p, final InventoryContent content) {
        //p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
        //contents.fillRect(0,0, 2,3, ClickableItem.empty(fill1));
        p.getWorld().playSound(p.getLocation(), Sound.ITEM_LODESTONE_COMPASS_LOCK, 15, 1);
        

        

        int line=0;
        for (final Component c : frontSide.lines()) {
            final int line_ = line;
            content.set(0, line, new InputButton(InputButton.InputType.ANVILL, new ItemBuilder(Material.NAME_TAG)
                .name("§fПеред, Строка "+(line+1))
                .addLore("§7Сейчас: ")
                .addLore(c)
                .build(), TCUtils.toString(frontSide.line(line)).replace('§', '&'), msg -> {
                    if(msg.length()>32 ) {
                        p.sendMessage("§cЛимит 32 символа!");
                        return;
                    }
                    frontSide.line(line_, TCUtils.format(msg.replace('&', '§')));//sign.getSide(Side.FRONT).line(line, TCUtils.format(msg));
                    sign.update();
                    reopen(p, content);
                }));
            line++;
        }
        
        content.set(0, line, ClickableItem.of(new ItemBuilder(frontSide.isGlowingText() ? Material.GLOWSTONE : Material.MAGMA_BLOCK)
                    .name("§7Свечение текста спереди")
                    .build(), e -> {
                        frontSide.setGlowingText(!frontSide.isGlowingText());
                        sign.update();
                        reopen(p, content);
                    }
                )
        );
        
        
        line=0;
        for (final Component c : backSide.lines()) {
            final int line_ = line;
            content.set(1, line, new InputButton(InputButton.InputType.ANVILL, new ItemBuilder(Material.NAME_TAG)
                .name("§fЗад, Строка "+(line+1))
                .addLore("§7Сейчас: ")
                .addLore(c)
                .build(), TCUtils.toString(backSide.line(line)).replace('§', '&'), msg -> {
                    if(msg.length()>32 ) {
                        p.sendMessage("§cЛимит 32 символа!");
                        return;
                    }
                    backSide.line(line_, TCUtils.format(msg.replace('&', '§')));//sign.getSide(Side.FRONT).line(line, TCUtils.format(msg));
                    sign.update();
                    reopen(p, content);
                }));
            line++;
        }
        
        
        content.set(1, line, ClickableItem.of(new ItemBuilder(backSide.isGlowingText() ? Material.GLOWSTONE : Material.MAGMA_BLOCK)
                    .name("§7Свечение текста сзади")
                    .build(), e -> {
                        backSide.setGlowingText(!backSide.isGlowingText());
                        sign.update();
                        reopen(p, content);
                    }
                )
        );




        content.set(2, 4, ClickableItem.empty(new ItemBuilder(Material.PAPER)
                    .name("§eПодсказки")
                    .addLore("§7Можно использовать")
                    .addLore("§7цветовые коды с §f&")
                    .addLore("§7ЛКМ - изменить")
                    .addLore("§7")
                    .addLore("§7Тэги для 1 строки:")
                    .addLore("[Место] §7- варп; стока 2 - название")
                    .addLore("[Команда] §7- варп; стока 2 - команда")
                    .build()
                )
        );
        


    }
    
    
    
    
    
    
    
    
    
    
}
