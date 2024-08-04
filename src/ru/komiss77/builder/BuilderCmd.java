package ru.komiss77.builder;

import java.util.Arrays;
import java.util.List;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.player.Perm;
import ru.komiss77.builder.menu.BannerEditor;
import ru.komiss77.builder.menu.EntitySetup;
import ru.komiss77.builder.menu.HeadSetup;
import ru.komiss77.commands.OCommand;
import ru.komiss77.commands.tools.Resolver;
import ru.komiss77.modules.menuItem.MenuItem;
import ru.komiss77.modules.menuItem.MenuItemBuilder;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.ItemBuilder;


public class BuilderCmd implements OCommand {

    public static List<String> subCommands = Arrays.asList("end");
    public static MenuItem bmi;
    public static final ItemStack fill = new ItemBuilder(Material.GREEN_STAINED_GLASS_PANE).build();

    static {
        final ItemStack buildMenu = new ItemBuilder(Material.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE)
            .name("§aМеню билдера")
            .lore("§6ПКМ на баннер, голову, энтити -")
            .lore("§e настроить")
            .flags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP)
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
    public LiteralCommandNode<CommandSourceStack> command() {
        final String oper = "operation";
        return Commands.literal("builder").executes(cntx -> {
            final CommandSender cs = cntx.getSource().getSender();
            if (!(cs instanceof final Player pl)) {
                cs.sendMessage("§eНе консольная команда!");
                return 0;
            }

            final Oplayer op = PM.getOplayer(pl);
            if (!ApiOstrov.canBeBuilder(pl)) {
                pl.sendMessage("§cДоступно только персоналу!");
                return 0;
            }

            if (pl.getGameMode() != GameMode.CREATIVE) {
                pl.setGameMode(GameMode.CREATIVE);
                pl.setAllowFlight(true);
                pl.setFlying(true);
            }
            if (op.setup == null) {
                op.setup = new SetupMode(pl);
//                Bukkit.getPluginManager().registerEvents(sm, Ostrov.getInstance());
                bmi.giveForce(pl);//ItemUtils.giveItemTo(p, openBuildMenu.clone(), p.getInventory().getHeldItemSlot(), false);
            }
            op.setup.openSetupMenu(pl);

            /*if (op.lastCommand != null) {
                pl.performCommand(op.lastCommand);
                op.lastCommand = null;
            } else {
                op.setup.openSetupMenu(pl);
            }*/

            //добавляем права билдера
            if (!op.user_perms.contains("astools.*")) {
                Perm.calculatePerms(pl, op, false);
            }
            return Command.SINGLE_SUCCESS;
        }).then(Resolver.string(oper).suggests((cntx, sb)->{
            final CommandSender cs = cntx.getSource().getSender();
            if (!(cs instanceof Player) || !ApiOstrov.canBeBuilder(cs)) {
                return sb.buildFuture();
            }
            sb.suggest("end");
            return sb.buildFuture();
        }).executes(cntx -> {
            final CommandSender cs = cntx.getSource().getSender();
            if (!(cs instanceof final Player pl)) {
                cs.sendMessage("§eНе консольная команда!");
                return 0;
            }

            if (!ApiOstrov.canBeBuilder(pl)) {
                pl.sendMessage("§cДоступно только персоналу!");
                return 0;
            }

            end(PM.getOplayer(pl));
            return Command.SINGLE_SUCCESS;
        })).build();
    }

    @Override
    public List<String> aliases() {
        return List.of("билдер");
    }

    @Override
    public String description() {
        return "Режим строителя";
    }


    public static void end(final Oplayer opl) {
        GameMode before = GameMode.SURVIVAL;
        final SetupMode sm = opl.setup;
        if (sm != null) {
            before = sm.before;
            if (sm.displayCube != null && !sm.displayCube.isCancelled()) sm.displayCube.cancel();
            opl.setup = null;
        }
        final Player p = Bukkit.getPlayerExact(opl.nik);
        if (p != null && p.isOnline()) {
            p.setGameMode(before);
            p.closeInventory();
            bmi.remove(p);//ItemUtils.substractAllItems(p, openBuildMenu.getType());
            Perm.calculatePerms(p, opl, false);
        }
        //PlayerLst.signCache.remove(name);
    }


}
    
    
 
