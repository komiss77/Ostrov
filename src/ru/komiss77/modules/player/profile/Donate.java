package ru.komiss77.modules.player.profile;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Perm;
import ru.komiss77.enums.Data;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.objects.Group;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InputButton;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;


public class Donate implements InventoryProvider {
    
    private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Section.ГРУППЫ.glassMat).name("§8.").build());
    

     
    @Override
    public void onClose(final Player p, final InventoryContent content) {
        PM.getOplayer(p).menu.current = null;
    }

    
    @Override
    public void init(final Player p, final InventoryContent content) {
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
        //content.fillRect(0,0,  5,8, ClickableItem.empty(fill));
        
        final Oplayer op = PM.getOplayer(p);
        //final ProfileManager pm = op.menu;

        
        
        //линия - разделитель
        content.fillRow(4, fill);
        
        //выставить иконки внизу
        for (Section section:Section.values()) {
            content.set(section.slot, Section.getMenuItem(section, op));
        }

        
        

        
        
                     
                
            for (final Group group : Perm.getGroups()) {
                
                //final Group group  = OstrovDB.groups.get(groupName);
                
                if (group==null || group.isStaff()) continue;
                content.set( group.inv_slot, new InputButton(InputButton.InputType.ANVILL,  new ItemBuilder(Material.matchMaterial(group.mat))
                    .name(group.chat_name)
                    .addFlags(ItemFlag.HIDE_ATTRIBUTES)
                    //.addLore("")
                    .setLore(group.lore)
                    //.addLore("")
                    //.addLore("§f15 дней §7- §b"+group.getPrice(15))
                    //.addLore("§f1 месяц §7- §b"+group.getPrice(31))
                    //.addLore("§f3 месяца §7- §b"+group.getPrice(90))
                    //.addLore("")
                    //.addLore("§7ЛКМ - выбрать длительность")
                    //.addLore("")
                    .build(), "15-180 дней", ammount -> {
                        p.closeInventory();
                        if (!ApiOstrov.isInteger(ammount)) {
                            p.sendMessage("§cДолжно быть число!");
                            return;
                        }
                        final int days = Integer.valueOf(ammount);
                        if (days<15 || days>180) {
                            p.sendMessage("§cот 15 до 180 дней!");
                            return;
                        }
                        final int price = group.getPrice(days);
                        if (op.getDataInt(Data.RIL)<price) {
                            p.sendMessage("§cНедостаточно рил! (группа "+group.chat_name+" на "+days+"д. стоит "+price+" рил)");
                            return;
                        }
                        //p.performCommand("donate");
                        //op.setData(Data.RIL, op.getDataInt(Data.RIL)-price);
                        ApiOstrov.executeBungeeCmd(p, "group buy "+p.getName()+" "+group.chat_name+" "+days);
                }));
     
                
                 
             }   
                
                
                
                
                
                final ItemStack add = new ItemBuilder(Material.GOLD_INGOT)
                        .name("§6Пополнить счёт")
                        .addLore("§7")
                        .addLore("§7Для оплаты привилегии")
                        .addLore("§7нужны §bРил")
                        .addLore("§7Рил можно заработать,")
                        .addLore("§7выполняя миссии, или")
                        .addLore("§7пополнить через магазин.")
                        .addLore("§7")
                        .addLore("§fКлик §6- Открыть офф. магазин")
                        .build();

                content.set(4, ClickableItem.of(add
                        , e-> {
                            p.performCommand("donate");
                            //p.closeInventory();
                            //ApiOstrov.executeBungeeCmd(p, "money add");
                        }
                    )
                );                
                
                
                
                
                
                
                
                
                



        

    }


    
    
    
    
    
    
    
    
    
}
