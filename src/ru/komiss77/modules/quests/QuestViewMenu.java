package ru.komiss77.modules.quests;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.ApiOstrov;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.quests.progs.IProgress;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.Pagination;
import ru.komiss77.utils.inventory.SlotIterator;
import ru.komiss77.utils.inventory.SlotPos;





public class QuestViewMenu implements InventoryProvider {
    
    private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Material.LIME_STAINED_GLASS_PANE).name("§8.").build());
    
    @Override
    public void init(final Player p, final InventoryContent content) {
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
        content.fillRect(0,0, 4,8, fill);
        
        final Oplayer op = PM.getOplayer(p);
        
        final Pagination pagination = content.pagination();
        
        final List<ClickableItem> buttons = new ArrayList<>();
        
        final boolean builder = ApiOstrov.isLocalBuilder(p, false);
        
        for (final Quest q : Quest.codeMap.values()) {
            
        	final IProgress pr = op.quests.get(q);
        	
        	if (pr != null) {
        		if (pr.isDone()) {
                    buttons.add(ClickableItem.empty(new ItemBuilder(Material.SCUTE)
                    	.name(q.displayName).addLore("§8Завершено").build()));
        		} else {
                    final ItemStack is = new ItemBuilder(Material.RAW_GOLD)
                        .name(q.displayName)
                        .setLore(Quest.loreMap.get(q))
                        .addLore("§aАктивно" + (q.amount>0 ? "§7, прогресс: §f"+pr.getProg()+" §7из §f"+q.amount : "") )
                        .addLore(builder ? "§b*Отладка: §eЛКМ-завершить" : "")
                        .addLore(builder && q.amount>0 ? "§b*Отладка: §eПКМ-добавить прогресс" : "")
                        .build();
    		        if (builder) {
    		            buttons.add(ClickableItem.of(is, e-> {
    		                if (e.isLeftClick()) {
    		                	QuestManager.complete(p, op, q);
    		                    reopen(p, content);
    		                } else if (e.isRightClick() && q.amount>0) {
    		                	QuestManager.addProgress(p, op, q);
    		                    reopen(p, content);
    		                }
    		            }));
    		        } else {
    		            buttons.add(ClickableItem.empty(is));
    		        }
				}
        	} else if (builder) {
                buttons.add(ClickableItem.of(new ItemBuilder(Material.FIREWORK_STAR)
                .name(q.displayName).setLore(Quest.loreMap.get(q))
                .addLore("§6Предстоит").addLore("§7Откроется после выполнения §e" + q.parent.displayName)
                .addLore("§b*Отладка: §eЛКМ-завершить")
                .addLore("§b*Отладка: §eПКМ-добавить прогресс")
                .build(), e -> {
	                if (e.isLeftClick()) {
	                	QuestManager.complete(p, op, q);
	                    reopen(p, content);
	                } else if (e.isRightClick() && q.amount>0) {
	                	QuestManager.addProgress(p, op, q);
	                    reopen(p, content);
	                }
	            }));
			}
	    }
        
        pagination.setItems(buttons.toArray(new ClickableItem[0]));
        pagination.setItemsPerPage(21);
        pagination.addToIterator(content.newIterator(SlotIterator.Type.HORIZONTAL, SlotPos.of(1, 1)).allowOverride(false));
        
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
    }
    
}
