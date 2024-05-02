package ru.komiss77.modules.player.mission;

import java.util.ArrayList;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import ru.komiss77.ApiOstrov;
import ru.komiss77.OstrovDB;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.PlayerInput;
import ru.komiss77.utils.TCUtils;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InputButton;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.Pagination;
import ru.komiss77.utils.inventory.SlotIterator;
import ru.komiss77.utils.inventory.SlotPos;
import ru.komiss77.version.AnvilGUI;


public class CustomStatNameEditor implements InventoryProvider {
    
    private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Material.PURPLE_STAINED_GLASS_PANE).name("§8.").build());
    
    
    @Override
    public void init(final Player p, final InventoryContent content) {
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
        
        //линия - разделитель
        content.fillRow(4, fill);
        
        
        final ArrayList<ClickableItem> menuEntry = new ArrayList<>();        
                    
        MissionManager.customStatsDisplayNames.keySet().forEach( (name) -> {
            
            final boolean showAmmount = MissionManager.customStatsShowAmmount.get(name);
//Bukkit.broadcastMessage("key="+name+" val="+MissionManager.customStatsDisplayNames.get(name));
            final Material mat;// = MissionManager.customStatsDisplayNames.containsKey(name) ? MissionManager.customStatMat(name): Material.GUNPOWDER;
            if ( name.equals(TCUtils.stripColor(MissionManager.customStatsDisplayNames.get(name))) ) {
                mat = Material.GUNPOWDER;
            } else {
                mat = MissionManager.customStatMat(name);
            }
            
            menuEntry.add(ClickableItem.of(new ItemBuilder(mat)
                    .name("§7значение String:§f "+name)
                    .setAmount(showAmmount ? 2 : 1)
                    .addLore("§7")
                    .addLore(showAmmount ? "§7Колличество будет показано" : "Колличество скрыто")
                    .addLore("§7")
                    .addLore("§7Отображаемый результат:")
                    .addLore(MissionManager.customStatsDisplayNames.get(name)+ (showAmmount?" §7: §dxx":""))
                    .addLore("§7")
                    .addLore("§7ЛКМ - изменить")
                    .addLore("§7")
                    .addLore(showAmmount ? "§7ПКМ - §fскрыть колличество" :  "§7ПКМ - §fпоказать колличество" )
                    .addLore("§7Скрывать колличество удобно")
                    .addLore("§7когда нужно достичь")
                    .addLore("§7любое значение больше 0,")
                    .addLore("§7например, '§6создать клан§7'")
                    .addLore("§7")
                    .addLore("§7клав.Q - §cудалить")
                    .addLore("§7")
                    .build(), e-> {
                        switch (e.getClick()) {
                            case LEFT:
                                final String sugg = MissionManager.customStatsDisplayNames.get(name).replaceAll("§", "&");
                                PlayerInput.get(InputButton.InputType.ANVILL, p, msg -> {
                                    msg = msg.replaceAll("&", "§");
                                        MissionManager.customStatsDisplayNames.put(name, msg);
                                        OstrovDB.executePstAsync(p, "UPDATE `customStats` SET `displayName`='"+msg+"' WHERE `name`='"+name+"';");
                                        reopen(p, content);
                                }, sugg);
                                    
                               /* new AnvilGUI.Builder()
                                        .title("DisplayName для "+name)
                                        .text(MissionManager.customStatsDisplayNames.get(name).replaceAll("§", "&"))
                                        .onComplete( (p1, msg) -> {
                                            msg = msg.replaceAll("&", "§");
                                            MissionManager.customStatsDisplayNames.put(name, msg);
                                            OstrovDB.executePstAsync(p, "UPDATE `customStats` SET `displayName`='"+msg+"' WHERE `name`='"+name+"';");
                                            reopen(p, content);
                                            return AnvilGUI.Response.text(""); 
                                        })
                                        .open(p);*/
                                /*new AnvilGUI(Ostrov.instance, p, MissionManager.customStatsDisplayNames.get(name).replaceAll("§", "&"), (p1, msg) -> {
                                    msg = msg.replaceAll("&", "§");
                                    MissionManager.customStatsDisplayNames.put(name, msg);
                                    OstrovDB.executePstAsync(p, "UPDATE `customStats` SET `displayName`="+msg+" WHERE `name`='"+name+"';");
                                    reopen(p, content);
                                    return null; 
                                });*/
                                break;
                            case RIGHT:
                                MissionManager.customStatsShowAmmount.put(name, !showAmmount);
                                OstrovDB.executePstAsync(p, "UPDATE `customStats` SET `showAmmount`='"+(showAmmount?0:1)+"' WHERE `name`='"+name+"';");
                                reopen(p, content);
                                break;
                            case DROP:
                                MissionManager.customStatsDisplayNames.remove(name);
                                MissionManager.customStatsShowAmmount.remove(name);
                                OstrovDB.executePstAsync(p, "DELETE FROM `customStats` WHERE `name`='"+name+"';");
                                reopen(p, content);
                                break;
                            default:
                                break;
                        }
                    }
            ));
            
        });


            
        final Pagination pagination = content.pagination();


        pagination.setItems(menuEntry.toArray(new ClickableItem[menuEntry.size()]));
        pagination.setItemsPerPage(36);    


        if (!pagination.isLast()) {
            content.set(4, 8, ClickableItem.of(ItemUtils.nextPage, e 
                    -> {
                content.getHost().open(p, pagination.next().getPage()) ;
            }
            ));
        }

        if (!pagination.isFirst()) {
            content.set(4, 0, ClickableItem.of(ItemUtils.previosPage, e 
                    -> {
                content.getHost().open(p, pagination.previous().getPage()) ;
               })
            );
        }

        pagination.addToIterator(content.newIterator(SlotIterator.Type.HORIZONTAL, SlotPos.of(0, 0)).allowOverride(false));

        content.set(5, 0, ClickableItem.of(new ItemBuilder(Material.PLAYER_HEAD)
            .setCustomHeadTexture(ItemUtils.Texture.previosPage)
            .name("§7назад")
            .build(), e -> {
                MissionManager.openMissionsEditMenu(p);
            }));


        content.set(5, 2, ClickableItem.empty(new ItemBuilder(Material.PAPER)
            .setCustomHeadTexture(ItemUtils.Texture.previosPage)
            .name("§7Как добавить?")
            .addLore("§7Для создания ремаппинга")
            .addLore("§7добавьте требование")
            //.addLore("§7с нужным значением")
            .addLore("§7в редакторе миссии.")
            .build()
        ));
    }                
        

                
                
                









        
        
        
        
        
       

    }


    
    
    
    
