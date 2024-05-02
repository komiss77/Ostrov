package ru.komiss77.modules.crafts;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.utils.inventory.SmartInventory;

public class CraftCmd implements CommandExecutor, TabCompleter {
    
    @Override
    public List<String> onTabComplete(final CommandSender send, final Command cmd, final String al, final String[] args) {
        if (send instanceof Player) {
            final List<String> sugg = new ArrayList<>();
            if (args.length == 1) {
                if (ApiOstrov.isLocalBuilder(send, false)) {
                    sugg.add("edit");
                    sugg.add("remove");
                }
            } else if (args.length == 2 && (args[0].equalsIgnoreCase("edit") || 
            	args[0].equalsIgnoreCase("remove")) && ApiOstrov.isLocalBuilder(send, false)) {
            	for (final NamespacedKey rk : Crafts.crafts.keySet()) {
                    sugg.add(rk.getKey());
            	}
            }
            return sugg;
        }
        return Collections.emptyList();
    }
    
    @Override
    public boolean onCommand(final CommandSender send, final Command cmd, final String label, final String[] args) {
        if (label.equalsIgnoreCase("craft") && send instanceof Player) {
            final Player p = (Player) send;
            //админ комманды
            if (args.length == 2) {
            	switch (args[0]) {
				case "edit":
					if (ApiOstrov.isLocalBuilder(send, true)) {
                    SmartInventory
                        .builder()
                        .id("Craft " + p.getName())
                        .provider(new CraftMenu(args[1], false))
                        .size(3, 9).title("§eСоздание Крафта " + args[1])
                        .build()
                        .open(p);
					}
					break;
				case "remove":
					if (ApiOstrov.isLocalBuilder(send, true)) {
		            	final YamlConfiguration craftConfig = YamlConfiguration.loadConfiguration(
		                		new File(Ostrov.instance.getDataFolder().getAbsolutePath() + "/crafts/craft.yml"));
	                	if (craftConfig.getKeys(false).contains(args[1])) {
	                        craftConfig.set(args[1], null);
	                        Bukkit.removeRecipe(new NamespacedKey(Crafts.space, args[1]));
	                        Crafts.rmvRecipe(new NamespacedKey(Crafts.space, args[1]));
	                        p.sendMessage("§7Крафт §e" + args[1] + " §7убран!");
	        				try {
								craftConfig.save(new File(Ostrov.instance.getDataFolder().getAbsolutePath() + "/crafts/craft.yml"));
							} catch (IOException e) {
								e.printStackTrace();
							}
	                	} else {
	                        p.sendMessage("§cНет такого крафта!");
						}
					}
					break;
				case "view":
                	if (Bukkit.getRecipe(new NamespacedKey(Crafts.space, args[1])) == null) {
                        p.sendMessage("§cТакого крафта не существует!");
                	} else {
                    	SmartInventory
	                        .builder()
	                        .id("Craft " + p.getName())
	                        .provider(new CraftMenu(args[1], true))
	                        .size(3, 9).title("§eПросмотр Крафта " + args[1])
	                        .build()
	                        .open(p);
                	}
					break;
				default:
                    p.sendMessage("§cНеправельный синтакс комманды!");
					break;
				}
            } else {
                p.sendMessage("§cНеправельный синтакс комманды!");
            }
        }
        return true;
    }
}
