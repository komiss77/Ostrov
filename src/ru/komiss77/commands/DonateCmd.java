package ru.komiss77.commands;


import com.destroystokyo.paper.ClientOption;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import ru.komiss77.utils.ItemBuilder;




public class DonateCmd implements CommandExecutor {
    
    private static final ItemStack bookRU;
    private static final ItemStack bookEN;

    public DonateCmd() {
        
    }
    
    static {
        
        
        
        
        
        
        
        
        bookRU = new ItemBuilder(Material.WRITTEN_BOOK)
                .name("Книга Желаний")
                .build();
        
        bookEN = new ItemBuilder(Material.WRITTEN_BOOK)
                .name("Wish Book")
                .build();
        
        
        
        /*ComponentBuilder page=new ComponentBuilder("  §3§lДорогой Друг!\n\n §1Пополнить счёт, и другие возможности, можно в Официальном магазине Острова.\n\n");
        
        
        page.append( new ComponentBuilder("§6§nПерейти в Официальный магазин\n" )
            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Клик-перейти")))
            .event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://ostrov77.easydonate.ru/"))
            .create());
        
        TextComponent text;*/
        BookMeta bookMetaRU = (BookMeta) bookRU.getItemMeta();
        bookMetaRU.addPages(Component.text("  §3§lДорогой Друг!\n\n §1Пополнить счёт, и другие возможности, можно в Официальном магазине Острова.\n\n")
        	.append(Component.text("§6§nПерейти в Официальный магазин\n" ).hoverEvent(HoverEvent.showText(Component.text("Клик - перейти")))
        		.clickEvent(ClickEvent.openUrl("https://ostrov77.easydonate.ru/"))));
        bookMetaRU.setTitle("Книга Желаний");
        bookMetaRU.setAuthor("Остров77");
        bookRU.setItemMeta(bookMetaRU);          
        
        BookMeta bookMetaEN = (BookMeta) bookEN.getItemMeta();
        bookMetaEN.addPages(Component.text("  §3§lDear Friend!\n\n §1You can top up your account, and other opportunities, in the Official store.\n\n")
        	.append(Component.text("§6§nGo to the Official store\n" ).hoverEvent(HoverEvent.showText(Component.text("Click - go")))
        		.clickEvent(ClickEvent.openUrl("https://ostrov77.easydonate.ru/"))));
        bookMetaEN.setTitle("Wish Book");
        bookMetaEN.setAuthor("Ostrov77");
        bookEN.setItemMeta(bookMetaEN);  
    }
    
    
    
    
    
    
    @Override
    public boolean onCommand(CommandSender cs, Command cmd, String string, String[] arg) {
        
        if ( ! (cs instanceof Player) ) {
            cs.sendMessage("§eне консольная команда!");
            return true;
        }

        final Player p=(Player) cs;
        
        p.closeInventory();
        final String locale = p.getClientOption(ClientOption.LOCALE);
//Ostrov.log("sendMessage locale="+locale);
        if (locale.equals("ru_ru")) {
            p.openBook(bookRU);
        } else {
            p.openBook(bookEN);
        }
        return true;
    }
    



    

    
    

    

  
    
    
    
    
    
    
    


}
    
    
 
