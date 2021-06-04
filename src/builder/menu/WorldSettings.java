package builder.menu;


import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Commands.WorldManagerCommand;
import ru.komiss77.Ostrov;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.LocationUtil;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InputButton;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;





public class WorldSettings implements InventoryProvider {
    
    
    
    //private static final ItemStack fill = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name("§8.").build();;
    private final World world;

    
    public WorldSettings(final World world) {
        this.world = world;
    }
    
    
    
    @Override
    public void init(final Player p, final InventoryContent contents) {
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
        //contents.fillRect(0,0,  2,8, ClickableItem.empty(fill));
        
        
        //"setwordspawn", "delete", "backup", "restore"
        

            //p.teleport( Bukkit.getWorld(itemname).getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
        
        
                
                for (final GameRule rule : GameRule.values()) {
                    
                    if (rule.getType()==Boolean.class) {
                        
                        final boolean on = (boolean) world.getGameRuleValue(rule);
                        
                        contents.add(ClickableItem.of(new ItemBuilder(getRuleMat(rule,on))
                            .name(rule.getName())
                            .lore("")
                            .lore(on ? "§7сейчас §aвключено" : "§7сейчас §cвыключено")
                            .lore("")
                            .lore(on ? "§7ПКМ - §4выкл." : "§7ЛКМ - §2вкл.")
                            .lore("")
                            .build(), e-> {
                                switch (e.getClick()) {
                                    case LEFT:
                                        if (!on) {
                                            world.setGameRule(rule, true);
                                            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.5f, 1);
                                            reopen(p, contents);
                                        }
                                        break;
                                    case RIGHT:
                                        if (on) {
                                            world.setGameRule(rule, false);
                                            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.5f, 1);
                                            reopen(p, contents);
                                        }
                                        break;

                                }
                            }));
                        
                    } else if (rule.getType()==Integer.class) {
                        
                        final int value = (int) world.getGameRuleValue(rule);
                                
                        contents.set(1, 4, new InputButton(InputButton.InputType.ANVILL, new ItemBuilder(Material.NAME_TAG)
                                .name(rule.getName())
                                .lore("")
                                .lore("§7сейчас: "+value)
                                .lore("")
                                .lore("§7ЛКМ - §eизменить")
                                .lore("")
                                .build(),  String.valueOf(value), msg -> {
                                    
                                    if (!ApiOstrov.isInteger(msg)) {
                                        p.sendMessage("§cДолжно быть число!");
                                        Ostrov.soundDeny(p);
                                        return;
                                    }
                                    final int amount = Integer.valueOf(msg);
                                    if (amount<1 || amount>1_000_000) {
                                        p.sendMessage("§cот 1 до 1000000");
                                        Ostrov.soundDeny(p);
                                        return;
                                    }
                                    
                                    world.setGameRule(rule, amount);
                                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.5f, 1);
                                    reopen(p, contents);
                                    //return;
                                }));    


                    }

                }

                




                
                
        contents.set(5,0, ClickableItem.of(new ItemBuilder(Material.ENDER_EYE )
            .name("Точка спавна мира")
            .lore("")
            .lore("§7сейчас: "+LocationUtil.StringFromLoc(world.getSpawnLocation()))
            .lore("")
            .lore( "§7ЛКМ - §eустановить")
            .lore("")
            .build(), e-> {
                if (e.isLeftClick()) {
                    world.setSpawnLocation(p.getLocation());
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.5f, 1);
                    reopen(p, contents);
                }
            }));            
            
        
        
        
        
        










        

        
        
        contents.set( 5, 4, ClickableItem.of( new ItemBuilder(Material.OAK_DOOR).name("назад").build(), e -> 
            WorldManagerCommand.openWorldMenu(p)
        ));
        


        
        

    }

    private Material getRuleMat(final GameRule rule, final boolean on) {
        if (rule==GameRule.ANNOUNCE_ADVANCEMENTS) {
            return Material.FLOWER_BANNER_PATTERN;
        } else if (rule==GameRule.COMMAND_BLOCK_OUTPUT) {
            return Material.COMMAND_BLOCK;
        } else if (rule==GameRule.DISABLE_ELYTRA_MOVEMENT_CHECK) {
            return Material.ELYTRA;
        } else if (rule==GameRule.DISABLE_RAIDS) {
            return Material.IRON_HORSE_ARMOR;
        } else if (rule==GameRule.DO_DAYLIGHT_CYCLE) {
            return Material.SUNFLOWER;
        } else if (rule==GameRule.DO_ENTITY_DROPS) {
            return Material.HOPPER;
        } else if (rule==GameRule.DO_FIRE_TICK) {
            return Material.BLAZE_POWDER;
        }
        return on ? Material.REDSTONE_TORCH : Material.LEVER;
    }


    
    
    
    
    
    
    
    
    
    
}
