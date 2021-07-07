package ru.komiss77.commands;

import java.util.HashMap;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.komiss77.ApiOstrov;
import ru.komiss77.listener.NbtListener;
import ru.komiss77.Ostrov;
import ru.komiss77.version.VM;

public class Nbtcheck implements CommandExecutor {


    public Nbtcheck(Ostrov creativeGuard) { }

    
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        

        if ( ! (sender instanceof Player) ) {
            sender.sendMessage("§cНе консольная команда!");
            return true;
        } 
        
        if (  !ApiOstrov.hasGroup(sender.getName(), "moder" ) ) {
            sender.sendMessage("§cДоступно только модераторам!");
            return true;
        } 
        

            final Player p = (Player) sender;

            if (p.getInventory().getItemInMainHand()== null) {
                p.sendMessage("§cВозьмите предмет в правую руку!");
                return true;
            }
            p.sendMessage("§eПроверяем предмет: "+p.getInventory().getItemInMainHand().getType());
            if (!NbtListener.invalidStackSize(p, p.getInventory().getItemInMainHand())) p.sendMessage("§2stack_size в порядке!");
            if (!NbtListener.Invalid_name_lenght(p, p.getInventory().getItemInMainHand())) p.sendMessage("§2name_lenght в порядке!");
            if (!NbtListener.Invalid_anvill(p, p.getInventory().getItemInMainHand())) p.sendMessage("§2anvill в порядке!");
            if (!NbtListener.Invalid_enchant(p, p.getInventory().getItemInMainHand())) p.sendMessage("§2enchant в порядке!");

            
            HashMap <String,String> nbtMap = VM.getNmsNbtUtil().getTagMap(p.getInventory().getItemInMainHand());

            if (!nbtMap.isEmpty()) {
                p.sendMessage("§5NBT тэги:");
                for (String t:nbtMap.keySet()) {
                    p.sendMessage("§3"+t+": §7"+nbtMap.get(t));
                }

            } else {
                p.sendMessage("§2NBT тэгов нет!");
            }

            return true;

        }
    
    
    }
