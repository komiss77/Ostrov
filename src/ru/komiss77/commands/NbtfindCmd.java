package ru.komiss77.commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ru.komiss77.Ostrov;
import ru.komiss77.listener.NbtLst;
import ru.komiss77.modules.player.PM;



public class NbtfindCmd implements CommandExecutor {

    //private Ostrov creativeGuard;

    public NbtfindCmd(Ostrov creativeGuard) {
        //this.creativeGuard = creativeGuard;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        
        if ( (sender instanceof Player) && !PM.getOplayer(sender.getName()).hasGroup("moder" ) ) {
            sender.sendMessage("§cДоступно только модераторам!");
            return true;
        } 
        
        
        
        
        
        
            if (args.length != 1) {
                sender.sendMessage("§cnbtfind [материал] (all-любой)");
                return true;
            }
            
            Material material = null;

            if ( !args[0].equals("all")) material = Material.getMaterial(args[0].toUpperCase());

            if (material == null) {
                    sender.sendMessage("§2Сканируем игроков на сервере ... (все предметы)");
            } else {
                    sender.sendMessage("§2Сканируем игроков на сервере ... (материал: "+args[0].toUpperCase()+")");
            }


                    Bukkit.getOnlinePlayers().stream().forEach((p) -> {
                        NbtLst.rebuildInventoryContrnt(p);
                        
                      /*  for (int i=0; i<p.getInventory().getContents().length; i++) {
                            ItemStack item = p.getInventory().getContents()[i];
                            if (item!=null) {
                                if (NbtListener.invalidStackSize(p, item)) item.setAmount(item.getMaxStackSize());
                                if (NbtListener.Invalid_name_lenght(p, item)) item.getItemMeta().setDisplayName(item.getItemMeta().getDisplayName().substring(0,28));
                                if (NbtListener.Invalid_anvill(p, item)) p.getInventory().setItem(i, new ItemStack( item.getType(),  item.getAmount()));
                                if (NbtListener.Invalid_enchant(p, item)) p.getInventory().setItem(i, NbtListener.Repair_enchant(item));
                                if (NbtListener.hasInvalidNbt(p, item)) p.getInventory().setItem(i, new ItemStack( item.getType(),  item.getAmount()));
                            }
                            //if (NbtListener.isInvalidItem(p, item)) {
                              //  p.getInventory().remove(item);
                              //  sender.sendMessage("§eУдалён предмет "+item.getType()+" из инвентаря игрока "+p.getName());
                           // }
                        }
                        
                        //for (ItemStack item:p.getEnderChest().getContents()) {
                        for (int i=0; i<p.getEnderChest().getContents().length; i++) {
                            ItemStack item = p.getEnderChest().getContents()[i];
                            if (item!=null) {
                                if (NbtListener.invalidStackSize(p, item)) item.setAmount(item.getMaxStackSize());
                                if (NbtListener.Invalid_name_lenght(p, item)) item.getItemMeta().setDisplayName(item.getItemMeta().getDisplayName().substring(0,28));
                                if (NbtListener.Invalid_anvill(p, item)) p.getEnderChest().setItem(i, new ItemStack( item.getType(),  item.getAmount()));
                                if (NbtListener.Invalid_enchant(p, item)) p.getEnderChest().setItem(i, NbtListener.Repair_enchant(item));
                                if (NbtListener.hasInvalidNbt(p, item)) p.getEnderChest().setItem(i, new ItemStack( item.getType(),  item.getAmount()));
                            }
                            //if (NbtListener.isInvalidItem(p, item)) {
                            //    p.getInventory().remove(item);
                             //   sender.sendMessage("§eУдалён предмет "+item.getType()+" из эндэр-сундука игрока "+p.getName());
                            //}
                        }*/

                });
                    
                    sender.sendMessage("§2Сканирование закончено!");

                return true;
            }

    
    
        }
