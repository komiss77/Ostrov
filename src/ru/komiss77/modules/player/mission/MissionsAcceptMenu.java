package ru.komiss77.modules.player.mission;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Timer;
import ru.komiss77.enums.Stat;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.Pagination;
import ru.komiss77.utils.inventory.SlotIterator;
import ru.komiss77.utils.inventory.SlotPos;



public class MissionsAcceptMenu implements InventoryProvider {
    
    
    
    private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Material.GLASS_PANE).name("§8.").build());

    private final Map<Integer,Integer> completed;
    private boolean showCompleted = false;
    
    public MissionsAcceptMenu(final Map<Integer,Integer> completed) {
        this.completed = completed;
    }
    

    
    
    
    @Override
    public void init(final Player p, final InventoryContent content) {
        
        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_AMETHYST_CLUSTER_BREAK, 5, 5);
        content.fillRect(0,0, 4,8, fill);
        
//System.out.println("completed="+completed.toString());        
        
        final Oplayer op = PM.getOplayer(p);
        
        
        
            
       final List <ClickableItem> buttons = new ArrayList<>();

        if (MissionManager.missions.isEmpty()) {
            
            content.set(2,4, ClickableItem.of(new ItemBuilder(Material.GLASS_BOTTLE)
                .name("§7Миссия невыполнима")
                .addLore("")
                .addLore("§5Нет активных миссий" )
                .addLore("")
                .addLore("§7ЛКМ - §fпосмотреть архив")
                .addLore("")
                .build(), e-> {
                    if (e.isLeftClick()) p.performCommand("mission journal");
                }
            ));

        } else {
            
            boolean canTake;
            for (final Mission mi : MissionManager.missions.values()) {
                final List<String>lore = new ArrayList<>();
                canTake = true;
                
                //lore.add("§7ID: §3"+mi.id);
                lore.add("§7Награда: §e"+mi.reward+" рил");
                //lore.add("§7Призовой фонд: §6"+mi.rewardFund*mi.reward+" рил" + (mi.rewardFund<=0?"§сисчерпан!":""));
                if (mi.rewardFund>0) {
                    lore.add("§7Призовой фонд: §6"+mi.rewardFund*mi.reward+" рил");
                } else {
                    lore.add("§7Призовой фонд: §6"+mi.rewardFund*mi.reward+" рил §7(§cисчерпан!§7)");
                    canTake = false;
                }
                
             /*   lore.add("§7Претенденты: §f"+mi.doing);
                lore.add("");
                
                if (Timer.getTime()>mi.validTo) { //просрочена
                    lore.add("§7до §c"+ApiOstrov.dateFromStamp(mi.validTo));
                    canTake = false;
                } else if (mi.validTo-Timer.getTime()<7200) { //2*60*60  желтым, если меньше 2 часов
                    lore.add("§6! §7до §6"+ApiOstrov.dateFromStamp(mi.validTo));
                } else {
                    lore.add("§a✔ §7до §f"+ApiOstrov.dateFromStamp(mi.validTo));
                }*/
                
                
//System.out.println("ID="+mi.id+" ");                
                
                if (completed.containsKey(mi.id)) { //сначала проверить по записям из БД, вдруг в op.missionIds не удалилось!!
                    
                    if (showCompleted) {
                        lore.add("");
                        lore.add("§aВыполнена §f"+ApiOstrov.dateFromStamp(completed.get(mi.id)));

                        buttons.add(ClickableItem.empty(new ItemBuilder(mi.mat)
                            .name(mi.displayName())
                            .setLore(lore)
                            .build())
                        );
                    } else {
                        //пропускаем
                    }
                    
                } else if (op.missionIds.contains(mi.id)) {
                    
                    lore.add("");
                    lore.add("§f**********************");
                    lore.add("§f*     §eПринята      §f*");
                    lore.add("§f**********************");
                    lore.add( "§7Клав. Q - §4отказаться");
                    lore.add("");
                    lore.add("§сПри отказе от миссии");
                    lore.add("§cвесь прогресс будет потерян!");
                    lore.add("");
                    buttons.add(ClickableItem.of(new ItemBuilder(mi.mat)
                        .name(mi.displayName())
                        .setLore(lore)
                        .build(), e-> {
                            if (e.getClick()==ClickType.DROP) {
                                p.performCommand("mission deny "+mi.id);
                                reopen(p, content);
                            } else {
                                PM.soundDeny(p);
                            }
                        }
                    ));
                    
                } else  {
                    
                    lore.add("§7Претенденты: §f"+mi.doing);
                    lore.add("");

                    if (Timer.getTime()>mi.validTo) { //просрочена
                        lore.add("§7до §c"+ApiOstrov.dateFromStamp(mi.validTo));
                        canTake = false;
                    } else if (mi.validTo-Timer.getTime()<7200) { //2*60*60  желтым, если меньше 2 часов
                        lore.add("§6! §7до §6"+ApiOstrov.dateFromStamp(mi.validTo));
                    } else {
                        lore.add("§a✔ §7до §f"+ApiOstrov.dateFromStamp(mi.validTo));
                    }

                    if (op.getStat(Stat.LEVEL)>=mi.level) {
                        lore.add("§a✔ §8Уровень не менее "+mi.level);
                    } else {
                        lore.add("§cУровень не менее §6"+mi.level);
                        canTake = false;
                    }
                    if (op.getStat(Stat.REPUTATION)>=mi.reputation) {
                        lore.add("§a✔ §8Репутация не менее "+mi.reputation);
                    } else {
                        lore.add("§cРепутация не менее §6"+mi.reputation);
                        canTake = false;
                    }

                    final int limit = MissionManager.getLimit(op); //лимит для группы
                    if (op.missionIds.size()>=limit) {
                        lore.add("§cЛимит миссий для вашей группы: §e"+limit);
                        canTake = false;
                    } else {
                        lore.add("§a✔ §8Лимит миссий : доступно "+(limit-op.missionIds.size()));
                    }
                    
                    lore.add("");
                    lore.addAll(Mission.getRequest(mi));
                    lore.add("");
                    
                    if (canTake) {
                        
                        lore.add("§7ЛКМ - §2принять");
                        
                        buttons.add(ClickableItem.of(new ItemBuilder(mi.mat)
                            .name(mi.displayName())
                            .setLore(lore)
                            .build(), e-> {
                                if (e.isLeftClick()) {
                                    p.performCommand("mission accept "+mi.id);
                                }
                            }
                        ));  

                    } else {
                        
                        lore.add("§cНевозможно принять");
                        
                        buttons.add(ClickableItem.of(new ItemBuilder(mi.mat)
                            .name(mi.displayName())
                            .setLore(lore)
                            .build(), e-> {
                                PM.soundDeny(p);
                            }
                        ));     

                    }
                }

            }
            
            
            
            
            

            
        }


        
               
        
        

        
        
        
        
        
        
        final Pagination pagination = content.pagination();

        pagination.setItems(buttons.toArray(new ClickableItem[buttons.size()]));
        pagination.setItemsPerPage(21);    


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

        content.set(4, 4, ClickableItem.of(new ItemBuilder(showCompleted ? Material.LIME_DYE : Material.GRAY_DYE)
            .name(showCompleted ? "§7Скрыть завершенные" : "§7Показать завершенные")
            .build(), e -> {
                showCompleted = !showCompleted;
                reopen(p, content);
            }));

    }


    
    
    
    
    
    
    
    
    
}
