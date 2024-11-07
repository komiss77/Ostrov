package ru.komiss77.modules.crafts;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import com.mojang.brigadier.Command;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import ru.komiss77.ApiOstrov;
import ru.komiss77.OStrap;
import ru.komiss77.Ostrov;
import ru.komiss77.commands.tools.OCmdBuilder;
import ru.komiss77.commands.tools.Resolver;
import ru.komiss77.utils.inventory.SmartInventory;

public class CraftCmd {

    public CraftCmd() {
        final String act = "action", name = "name";
        new OCmdBuilder("craft", "/craft edit|remove [имя]")
            .then(Resolver.string(act).then(Resolver.string(name).executes(cntx -> {
                final CommandSender cs = cntx.getSource().getSender();
                if (!(cs instanceof final Player pl)) {
                    cs.sendMessage("§eНе консольная команда!");
                    return 0;
                }

                final String nm;
                return switch (Resolver.string(cntx, act)) {
                    case "edit" -> {
                        if (!ApiOstrov.isLocalBuilder(cs, true)) {
                            pl.sendMessage("§cДоступно только билдерам!");
                            yield 0;
                        }

                        nm = Resolver.string(cntx, name);
                        SmartInventory
                            .builder()
                            .id("Craft " + pl.getName())
                            .provider(new CraftMenu(nm, false))
                            .size(3, 9).title("§eСоздание Крафта " + nm)
                            .build()
                            .open(pl);
                        yield Command.SINGLE_SUCCESS;
                    }
                    case "remove" -> {
                        if (!ApiOstrov.isLocalBuilder(cs, true)) {
                            pl.sendMessage("§cДоступно только билдерам!");
                            yield 0;
                        }

                        nm = Resolver.string(cntx, name);
                        final YamlConfiguration craftConfig = YamlConfiguration.loadConfiguration(
                            new File(Ostrov.instance.getDataFolder().getAbsolutePath() + "/crafts/craft.yml"));
                        if (!craftConfig.getKeys(false).contains(nm)) {
                            pl.sendMessage("§cНет такого крафта!");
                            yield 0;
                        }
                        craftConfig.set(nm, null);
                        Bukkit.removeRecipe(new NamespacedKey(OStrap.space, nm));
                        Crafts.rmvRecipe(new NamespacedKey(OStrap.space, nm));
                        pl.sendMessage("§7Крафт §e" + nm + " §7убран!");
                        try {
                            craftConfig.save(new File(Ostrov.instance.getDataFolder().getAbsolutePath() + "/crafts/craft.yml"));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        yield Command.SINGLE_SUCCESS;
                    }
                    case "view" -> {
                        nm = Resolver.string(cntx, name);
                        if (Bukkit.getRecipe(new NamespacedKey(OStrap.space, nm)) == null) {
                            pl.sendMessage("§cТакого крафта не существует!");
                            yield 0;
                        }
                        SmartInventory
                            .builder()
                            .id("Craft " + pl.getName())
                            .provider(new CraftMenu(nm, true))
                            .size(3, 9).title("§eПросмотр Крафта " + nm)
                            .build()
                            .open(pl);
                        yield Command.SINGLE_SUCCESS;
                    }
                    default -> {
                        pl.sendMessage("§cНеправельный синтакс комманды!");
                        yield 0;
                    }
                };
            }))).suggest(cntx -> {
                if (!ApiOstrov.isLocalBuilder(cntx.getSource().getSender(), true)) {
                    return Set.of();
                }
                return Set.of("edit", "remove");
            }, false)/*.then().suggest(cntx -> {
                if (!ApiOstrov.isLocalBuilder(cntx.getSource().getSender(), true)) {
                    return Set.of();
                }
                return Crafts.crafts.keySet().stream()
                    .map(NamespacedKey::getKey).collect(Collectors.toSet());
            }, false)*/
            .description("Редактор крафтов")
            .aliases("крафт")
            .register();
    }
}
