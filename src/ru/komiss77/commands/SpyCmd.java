package ru.komiss77.commands;


import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.Pagination;
import ru.komiss77.utils.inventory.SlotIterator;
import ru.komiss77.utils.inventory.SlotPos;
import ru.komiss77.utils.inventory.SmartInventory;




public class SpyCmd implements CommandExecutor, TabCompleter {
    
    //private static final List<String> subCommands = Arrays.asList( "help");
 //   private static boolean use;
    //private static SpyCmd spy;
    //private static final HashMap <String,BukkitTask> spyes;

    //static {
    //    spyes = new HashMap<>();
   // }
    
   // 
  //  public static boolean isSpy(String name) {
   //     return spyes.containsKey(name);
  //  }

    
    @Override
    public List<String> onTabComplete(CommandSender cs, Command cmnd, String command, String[] args) {
        
        List <String> sugg = new ArrayList<>();

        switch (args.length) {
            
            case 1:
                //0- пустой (то,что уже введено)
                if (cs.hasPermission("ostrov.spy")){
                    for (final Player p : Bukkit.getOnlinePlayers()) {
                        if ( !p.getName().equals(cs.getName()) && !p.hasPermission("ostrov.spy") && p.getGameMode()!=GameMode.SPECTATOR && p.getName().startsWith(args[0])) sugg.add(p.getName());
                    }
                }
                break;


        }
        
       return sugg;
    }
       
    
    

   // public SpyCmd() {
    //    use = Config.getCongig().getBoolean("modules.command.spy");
    //    if (!use) return;
        
    //    try {
    //        if (spy!=null) {
                //HandlerList.unregisterAll(spy);
        //    }
   //         spy = SpyCmd.this;
            //Bukkit.getPluginManager().registerEvents(spy, Ostrov.getInstance());
            
            //Ostrov.log_ok ("§2"+this.getClass().getSimpleName()+" активен!");
            
   //     } catch (Exception ex) { 
   //         Ostrov.log_err("§4Не удалось загрузить настройки "+this.getClass().getSimpleName()+" : "+ex.getMessage());
   //     }
  //  }
    

//        if ( !p.isFlying() && ( spy_gamemodes.get(p.getName()).equals("SURVIVAL") || spy_gamemodes.get(p.getName()).equals("ADVENTURE")) ) ApiOstrov.teleportSave(p, p.getLocation());

    
    
    
    
    
    @Override
    public boolean onCommand(CommandSender cs, Command cmd, String string, String[] arg) {
        
        if ( ! (cs instanceof Player) ) {
            cs.sendMessage("§cНе консольная команда!");
            return true;
        }
        
        final Player p = (Player) cs;
        if (!p.hasPermission("ostrov.spy")) {
            return false;
        }
        final Oplayer op = PM.getOplayer(p);

        switch (arg.length) {

            case 0:
                SmartInventory.builder()
                        .id("SpyMenu"+p.getName())
                        .provider(new SpyMenu())
                        .size(6, 9)
                        .title("§1За кем следим?")
                        .build().open(p);
                //меню
                break;

            case 1:

                if (op.spyTask != null) {
                    cs.sendMessage("§cСначала закончите текущее наблюдение!");
                    return false;
                }

                if (cs.getName().equalsIgnoreCase(arg[0])) {
                    cs.sendMessage("§cЗа собой следить не получится!");
                    return false;
                }
                final Player target = Bukkit.getPlayerExact(arg[0]);
                if (target==null) {
                    cs.sendMessage("§c"+arg[0]+" нет на сервере!");
                    return false;
                }
                if (target.getGameMode()==GameMode.SPECTATOR) {
                    cs.sendMessage("§c"+target.getName()+" в режиме зрителя!");
                    return false;
                }
                if (target.hasPermission("ostrov.spy")) {
                    cs.sendMessage("§c"+target.getName()+" тоже имеет права шпиона!");
                    return false;
                }

                final Location back = p.getLocation();
                final GameMode gm = p.getGameMode();

                p.setGameMode(GameMode.SPECTATOR);
                p.teleport(target);
                p.setSpectatorTarget(target);
                target.hidePlayer(Ostrov.instance, p);

                op.spyTask = new BukkitRunnable() {

                    @Override
                    public void run() {
                        if ( p==null || !p.isOnline() ) {
                            reset();
                            return;
                        }
                        if (p.isDead() ||
                            p.getGameMode()!=GameMode.SPECTATOR || 
                            target==null || 
                            !target.isOnline() || 
                            target.isDead() ||
                            p.getSpectatorTarget()==null ||
                            !p.getSpectatorTarget().getName().equals(target.getName()) ||
                            target.getGameMode()==GameMode.SPECTATOR )
                        {
                            back();
                            return;
                        }
                    }

                    private void back() {
                        if (p.getGameMode()==GameMode.SPECTATOR) p.setSpectatorTarget(null);
                        p.teleport(back);
                        Ostrov.sync( ()->p.setGameMode(gm), 1);
                        p.resetTitle();
                        target.showPlayer(Ostrov.instance, p);
                        reset();
                    }

                    private void reset() {
                        this.cancel();
                        op.spyTask = null;
                    }

                }.runTaskTimer(Ostrov.instance, 1, 11);


                break;
        }

        return true;
    }
    



    
    
    

    
    
    
    
    
    
    
    
    
    

    

    private class SpyMenu implements InventoryProvider {



        private final ItemStack fill = new ItemBuilder(Material.GREEN_STAINED_GLASS_PANE).name("§8.").build();;


        public SpyMenu() {
        }



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
                        .addLore("")
                        .addLore("")
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










    }
    
    
    


}
    
    
 
