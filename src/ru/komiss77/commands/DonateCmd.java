package ru.komiss77.commands;

import java.util.Set;
import com.destroystokyo.paper.ClientOption;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import ru.komiss77.utils.ItemBuilder;


public class DonateCmd implements OCommand {

    private static final ItemStack bookRU;
    private static final ItemStack bookEN;

    static {
        bookRU = new ItemBuilder(Material.WRITTEN_BOOK)
            .name("Книга Желаний")
            .build();

        bookEN = new ItemBuilder(Material.WRITTEN_BOOK)
            .name("Wish Book")
            .build();

        final BookMeta bookMetaRU = (BookMeta) bookRU.getItemMeta();
        bookMetaRU.addPages(Component.text("  §3§lДорогой Друг!\n\n §1Пополнить счёт, и другие возможности, можно в Официальном магазине Острова.\n\n")
            .append(Component.text("§6§nПерейти в Официальный магазин\n").hoverEvent(HoverEvent.showText(Component.text("Клик - перейти")))
                .clickEvent(ClickEvent.openUrl("https://ostrov77.easydonate.ru/"))));
        bookMetaRU.setTitle("Книга Желаний");
        bookMetaRU.setAuthor("Остров77");
        bookRU.setItemMeta(bookMetaRU);

        final BookMeta bookMetaEN = (BookMeta) bookEN.getItemMeta();
        bookMetaEN.addPages(Component.text("  §3§lDear Friend!\n\n §1You can top up your account, and other opportunities, in the Official store.\n\n")
            .append(Component.text("§6§nGo to the Official store\n").hoverEvent(HoverEvent.showText(Component.text("Click - go")))
                .clickEvent(ClickEvent.openUrl("https://ostrov77.easydonate.ru/"))));
        bookMetaEN.setTitle("Wish Book");
        bookMetaEN.setAuthor("Ostrov77");
        bookEN.setItemMeta(bookMetaEN);
    }

    @Override
    public LiteralCommandNode<CommandSourceStack> command() {
        return Commands.literal("donate").executes(cntx -> {
            final CommandSender cs = cntx.getSource().getSender();
            if (!(cs instanceof final Player pl)) {
                cs.sendMessage("§eНе консольная команда!");
                return 0;
            }

            pl.closeInventory();
            final String locale = pl.getClientOption(ClientOption.LOCALE);
            if (locale.equals("ru_ru")) {
                pl.openBook(bookRU);
            } else {
                pl.openBook(bookEN);
            }

            return Command.SINGLE_SUCCESS;
        }).build();
    }

    @Override
    public Set<String> aliases() {
        return Set.of("донат");
    }

    @Override
    public String description() {
        return "Донат меню";
    }
}
    
    
 
