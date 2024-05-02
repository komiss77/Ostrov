package ru.komiss77.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.komiss77.Ostrov;
import ru.komiss77.listener.NbtLst;
import ru.komiss77.modules.player.PM;

public class NbtcheckCmd implements CommandExecutor {


    public NbtcheckCmd(Ostrov creativeGuard) { }

    
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        

        if ( ! (sender instanceof Player) ) {
            sender.sendMessage("§cНе консольная команда!");
            return true;
        } 
        
        if (  !PM.getOplayer(sender.getName()).hasGroup("moder" ) ) {
            sender.sendMessage("§cДоступно только модераторам!");
            return true;
        } 
        

            final Player p = (Player) sender;

            if (p.getInventory().getItemInMainHand()== null) {
                p.sendMessage("§cВозьмите предмет в правую руку!");
                return true;
            }
            p.sendMessage("§eПроверяем предмет: "+p.getInventory().getItemInMainHand().getType());
            if (!NbtLst.invalidStackSize(p, p.getInventory().getItemInMainHand())) p.sendMessage("§2stack_size в порядке!");
            if (!NbtLst.Invalid_name_lenght(p, p.getInventory().getItemInMainHand())) p.sendMessage("§2name_lenght в порядке!");
            if (!NbtLst.Invalid_anvill(p, p.getInventory().getItemInMainHand())) p.sendMessage("§2anvill в порядке!");
            if (!NbtLst.Invalid_enchant(p, p.getInventory().getItemInMainHand())) p.sendMessage("§2enchant в порядке!");

            
         /*   HashMap <String,String> nbtMap = VM.getNmsNbtUtil().getTagMap(p.getInventory().getItemInMainHand());

            if (!nbtMap.isEmpty()) {
                p.sendMessage("§5NBT тэги:");
                for (String t:nbtMap.keySet()) {
                    p.sendMessage("§3"+t+": §7"+nbtMap.get(t));
                }

            } else {
                p.sendMessage("§2NBT тэгов нет!");
            }*/

            return true;

        }
    
    
    }
