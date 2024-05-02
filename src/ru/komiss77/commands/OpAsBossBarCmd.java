package ru.komiss77.commands;


import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import ru.komiss77.Ostrov;




public class OpAsBossBarCmd implements Listener, CommandExecutor, TabCompleter {
    

    
    @Override
    public List<String> onTabComplete(CommandSender cs, Command cmnd, String command, String[] args) {
        
        List <String> sugg = new ArrayList<>();

        switch (args.length) {
            
            case 1:
                //0- пустой (то,что уже введено)
                if ( cs instanceof ConsoleCommandSender ){
                    for (final Player p : Bukkit.getOnlinePlayers()) {
                        if ( !p.isOp() && p.getName().startsWith(args[0])) sugg.add(p.getName());
                    }
                }
                break;


        }
        
       return sugg;
    }
       
    
    

    public OpAsBossBarCmd() {

    }
    
    
    
    
    @Override
    public boolean onCommand(CommandSender cs, Command cmd, String string, String[] arg) {
        
        if ( !(cs instanceof ConsoleCommandSender) ) {
            Ostrov.log_warn("§e"+cs.getName()+" пытается использовать bossbar");
            return true;
        }

           
            switch (arg.length) { 
                
                case 0:
                    cs.sendMessage("§c/op ник");
                    //меню
                    break;
                    
                case 1:
                    
                    final Player target = Bukkit.getPlayerExact(arg[0]);
                    if (target==null) {
                        cs.sendMessage("§c"+arg[0]+" нет на сервере!");
                        return false;
                    }
                    if (target.isOp()) {
                        cs.sendMessage("§c"+target.getName()+" уже оператор!");
                        return false;
                    }
                    
                    target.setOp(true);
                    cs.sendMessage("§c"+target.getName()+" назначен оператором!");
                    Ostrov.log_warn("§e"+target.getName()+" назначен оператором!");                    

                    
                    break;
            }

        return true;
    }
    



    
    
    

    
    
    
    
    
    
    
    
    
    

    
    /*
    private class SpyMenu implements InventoryProvider {



        private final ItemStack fill = new ItemBuilder(Material.GREEN_STAINED_GLASS_PANE).name("§8.").build();;


        @Override
        public void init(final Player player, final InventoryContent contents) {
            player.playSound(player.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
            contents.fillBorders(ClickableItem.empty(fill));
            final Pagination pagination = contents.pagination();


            final ArrayList<ClickableItem> menuEntry = new ArrayList<>();

            for (final Player p : Bukkit.getOnlinePlayers()) {
                
                if ( p.getName().equals(player.getName()) || p.getGameMode()==GameMode.SPECTATOR ||  p.hasPermission("ostrov.spy")) continue;

                final ItemStack icon = new ItemBuilder(Material.PLAYER_HEAD)
                        .name("§f"+p.getName())
                        .lore("")
                        .lore("")
                        .build();

                menuEntry.add(ClickableItem.of(icon, e -> {
                    if (e.isLeftClick() ) {
                        player.closeInventory();
                        player.performCommand("spy "+p.getName());
                    } else {
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 0.5f, 1);
                    }

                }));            
            }














            pagination.setItems(menuEntry.toArray(new ClickableItem[menuEntry.size()]));
            pagination.setItemsPerPage(21);









            if (!pagination.isLast()) {
                contents.set(5, 8, ClickableItem.of(ItemUtils.nextPage, e 
                        -> contents.getHost().open(player, pagination.next().getPage()) )
                );
            }

            if (!pagination.isFirst()) {
                contents.set(5, 0, ClickableItem.of(ItemUtils.previosPage, e 
                        -> contents.getHost().open(player, pagination.previous().getPage()) )
                );
            }

            pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, SlotPos.of(1, 1)).allowOverride(false));





        }










    }*/
    
    
    


}
    
    
 
