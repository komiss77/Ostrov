package ru.komiss77.builder;

import java.util.Arrays;
import java.util.List;
import com.google.common.collect.ImmutableList;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.Perm;
import ru.komiss77.builder.menu.BannerEditor;
import ru.komiss77.builder.menu.EntitySetup;
import ru.komiss77.builder.menu.HeadSetup;
import ru.komiss77.modules.menuItem.MenuItem;
import ru.komiss77.modules.menuItem.MenuItemBuilder;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.ItemBuilder;

public class BuilderCmd implements CommandExecutor, TabCompleter {

    public static List<String> subCommands = Arrays.asList("end");
    public static MenuItem bmi;
    public static final ItemStack fill = new ItemBuilder(Material.GREEN_STAINED_GLASS_PANE).build();

    static {
        final ItemStack buildMenu = new ItemBuilder(Material.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE)
                .name("§aМеню билдера")
                .addLore("§6ПКМ на баннер, голову, энтити -")
                .addLore("§e настроить")
                .addFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ITEM_SPECIFICS)
                .build();
        bmi = new MenuItemBuilder("bmi", buildMenu)
                .slot(0)
                //.rightClickCmd("builder")
                //.leftClickCmd("builder")
                .giveOnJoin(false)
                .giveOnWorld_change(false)
                .giveOnRespavn(false)
                .interact(e -> {
                    e.setCancelled(true);
                    if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        if (e.getClickedBlock().getType() == Material.PLAYER_HEAD || e.getClickedBlock().getType() == Material.PLAYER_WALL_HEAD) {
                            HeadSetup.openSetupMenu(e.getPlayer(), e.getClickedBlock());
                            return;
                        } else if (Tag.BANNERS.isTagged(e.getClickedBlock().getType())) {
                            BannerEditor.edit(e.getPlayer(), e.getClickedBlock());
                            return;
                        }
                    }
                    e.getPlayer().performCommand("builder");
                })
                .interactAtEntity(e -> {
                    e.setCancelled(true);
                    if (e.getRightClicked() instanceof LivingEntity) {
                        EntitySetup.openSetupMenu(e.getPlayer(), e.getRightClicked());
                    }
                })
                .create();
    }

    @Override
    public List<String> onTabComplete(CommandSender cs, Command cmnd, String command, String[] args) {

        //   List <String> sugg = new ArrayList<>();
//System.out.println("l="+strings.length+" 0="+strings[0]);

        if (args.length == 1) {
            return subCommands;
            //for (String s : subCommands) {
            //     if (s.startsWith(args[0])) sugg.add(s);
            // }
        }

        return ImmutableList.of();
    }


    // public BuilderCmd() {
    //      init();
    //  }


    @Override
    public boolean onCommand(CommandSender cs, Command cmd, String string, String[] arg) {

        if (!(cs instanceof final Player p)) {
            cs.sendMessage("§eне консольная команда!");
            return true;
        }

        final Oplayer op = PM.getOplayer(p);
        if (!ApiOstrov.canBeBuilder(cs)) {
            p.sendMessage("§сНужно право §e" + Ostrov.MOT_D + ".builder §cили группа §esupermoder");
            return true;
        }


        switch (arg.length) {

            case 0:
                Ostrov.sync(() -> {
                    if (p.getGameMode() == GameMode.SURVIVAL || p.getGameMode() == GameMode.ADVENTURE) {
                        p.performCommand("gm 1");
                        p.setAllowFlight(true);
                        p.setFlying(true);
                    }
                    if (op.setup == null) {
                        final SetupMode sm = new SetupMode(p);
                        op.setup = sm;
                        Bukkit.getPluginManager().registerEvents(sm, Ostrov.getInstance());
                        bmi.giveForce(p);//ItemUtils.giveItemTo(p, openBuildMenu.clone(), p.getInventory().getHeldItemSlot(), false);
                    }

                    if (op.lastCommand != null) {
                        p.performCommand(op.lastCommand);
                        op.lastCommand = null;
                    } else {
                        op.setup.openSetupMenu(p);
                    }

                    //добавляем права билдера
                    if (!op.user_perms.contains("astools.*")) {
                        //    op.setData(Data.USER_PERMS, op.getDataString(Data.USER_PERMS)+",astools.*");- реализовал в PM.calculatePerms(p, op, false);
                        Perm.calculatePerms(p, op, false);
                    }

                }, 4);
                break;

            case 1:
                if (arg[0].equalsIgnoreCase("end")) {
                    end(p.getName());
                }
                break;
        }

        // help(p);
        return true;
    }
    



    
    
    
    

  /*  public void init() {
        try {
     
            //if (!allow_command) {
             //   Ostrov.log_ok ("§e"+this.getClass().getSimpleName()+" выключен.");
            //    return;
            //}
            
            //Bukkit.getPluginManager().registerEvents(this, Ostrov.getInstance());
            
            Ostrov.log_ok ("§2"+this.getClass().getSimpleName()+" активен!");
            
        } catch (Exception ex) { 
            Ostrov.log_err("§4Не удалось загрузить настройки "+this.getClass().getSimpleName()+" : "+ex.getMessage());
        }
    }

    public void reload () {
        //HandlerList.unregisterAll(this);
        Config.loadConfigs();
        init();
    }
    */


    public static void end(final String name) {
        GameMode before = GameMode.SURVIVAL;
        final SetupMode sm = PM.getOplayer(name).setup;
        if (sm != null) {
            before = sm.before;
            HandlerList.unregisterAll(sm);
            if (sm.displayCube != null && !sm.displayCube.isCancelled()) sm.displayCube.cancel();
            PM.getOplayer(name).setup = null;
        }
        final Player p = Bukkit.getPlayerExact(name);
        if (p != null && p.isOnline()) {
            p.setGameMode(before);
            p.closeInventory();
            bmi.remove(p);//ItemUtils.substractAllItems(p, openBuildMenu.getType());
            Perm.calculatePerms(p, PM.getOplayer(name), false);
        }
        //PlayerLst.signCache.remove(name);
    }


}
    
    
 
