package ru.komiss77.commands;

import java.util.ArrayList;
import java.util.List;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.scheduler.BukkitRunnable;
import ru.komiss77.Ostrov;
import ru.komiss77.commands.tools.Resolver;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.inventory.*;


public class SpyCmd implements OCommand {

    //запрос банжи, если есть - разкодировать raw
    //если пустой - выкачать из снапшота БД

    @Override
    public LiteralCommandNode<CommandSourceStack> command() {
        final String player = "player";
        return Commands.literal("spy")
            .executes(cntx -> {
                final CommandSender cs = cntx.getSource().getSender();
                if (!(cs instanceof final Player p)) {
                    cs.sendMessage("§eНе консольная команда!");
                    return 0;
                }

                final Oplayer op = PM.getOplayer(p);
                if (!op.isStaff) {
                    p.sendMessage("§cДоступно только персоналу!");
                    return 0;
                }

                SmartInventory.builder()
                    .id("SpyMenu" + p.getName())
                    .provider(new SpyMenu())
                    .size(6, 9)
                    .title("§5За кем следим?")
                    .build().open(p);
                return Command.SINGLE_SUCCESS;
            })
            .then(Resolver.player(player).executes(cntx -> {
                final CommandSender cs = cntx.getSource().getSender();
                if (!(cs instanceof final Player p)) {
                    cs.sendMessage("§eНе консольная команда!");
                    return 0;
                }

                final Oplayer op = PM.getOplayer(p);
                if (!op.isStaff) {
                    p.sendMessage("§cДоступно только персоналу!");
                    return 0;
                }

                final Player tgt = Resolver.player(cntx, player);
                if (tgt == null) {
                    p.sendMessage(Ostrov.PREFIX + "§cТакой игрок не онлайн");
                    return 0;
                }

                if (op.spyOrigin != null) {
                    cs.sendMessage("§cСначала закончите текущее наблюдение!");
                    return 0;
                }

                if (p.getEntityId() == tgt.getEntityId()) {
                    cs.sendMessage("§cЗа собой следить не получится!");
                    return 0;
                }

                if (tgt.getGameMode() == GameMode.SPECTATOR) {
                    cs.sendMessage("§c" + tgt.getName() + " в режиме зрителя!");
                    return 0;
                }

                op.spyOrigin = p.getLocation();
                final GameMode gm = p.getGameMode();

                p.setGameMode(GameMode.SPECTATOR);
                p.teleport(tgt);
                p.setSpectatorTarget(tgt);
                tgt.hidePlayer(Ostrov.instance, p);

                new BukkitRunnable() {

                    @Override
                    public void run() {
                        if (!p.isOnline()) {
                            this.cancel();
                            return;
                        }
                        if (p.isDead() ||
                            p.getGameMode() != GameMode.SPECTATOR ||
                            !tgt.isOnline() ||
                            tgt.isDead() ||
                            p.getSpectatorTarget() == null ||
                            !p.getSpectatorTarget().getName().equals(tgt.getName()) ||
                            tgt.getGameMode() == GameMode.SPECTATOR) {
                            back();
                        }
                    }

                    private void back() {
                        if (p.getGameMode() == GameMode.SPECTATOR) {
                            p.setSpectatorTarget(null);
                        }
                        p.teleport(op.spyOrigin == null ? p.getLocation() : op.spyOrigin);
                        Ostrov.sync(() -> p.setGameMode(gm), 1);
                        tgt.showPlayer(Ostrov.instance, p);
                        p.resetTitle();
                        this.cancel();
                    }

                }.runTaskTimer(Ostrov.instance, 1, 11);
                return Command.SINGLE_SUCCESS;
            }))
            .build();
    }

    @Override
    public List<String> aliases() {
        return List.of("шпион");
    }

    @Override
    public String description() {
        return "Слежка за игроками";
    }


    static class SpyMenu implements InventoryProvider {


        private final ItemStack fill = new ItemBuilder(ItemType.GREEN_STAINED_GLASS_PANE).name("§8.").build();
        ;


        public SpyMenu() {
        }

        @Override
        public void init(final Player player, final InventoryContent contents) {
            player.playSound(player.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
            contents.fillBorders(ClickableItem.empty(fill));
            final Pagination pagination = contents.pagination();


            final ArrayList<ClickableItem> menuEntry = new ArrayList<>();

            for (final Player p : Bukkit.getOnlinePlayers()) {

                if (p.getName().equals(player.getName()) || p.getGameMode() == GameMode.SPECTATOR || p.hasPermission("ostrov.spy"))
                    continue;

                final ItemStack icon = new ItemBuilder(ItemType.PLAYER_HEAD)
                    .name("§f" + p.getName())
                    .lore("")
                    .lore("")
                    .build();

                menuEntry.add(ClickableItem.of(icon, e -> {
                    if (e.isLeftClick()) {
                        player.closeInventory();
                        player.performCommand("spy " + p.getName());
                    } else {
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 0.5f, 1);
                    }

                }));
            }

            pagination.setItems(menuEntry.toArray(new ClickableItem[0]));
            pagination.setItemsPerPage(21);

            if (!pagination.isLast()) {
                contents.set(5, 8, ClickableItem.of(ItemUtil.nextPage, e
                    -> contents.getHost().open(player, pagination.next().getPage()))
                );
            }

            if (!pagination.isFirst()) {
                contents.set(5, 0, ClickableItem.of(ItemUtil.previosPage, e
                    -> contents.getHost().open(player, pagination.previous().getPage()))
                );
            }

            pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, SlotPos.of(1, 1)).allowOverride(false));
        }
    }
}
    
    
 
