package ru.komiss77.modules.player.mission;

import java.util.ArrayList;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import ru.komiss77.OstrovDB;
import ru.komiss77.enums.Stat;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InputButton;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.Pagination;
import ru.komiss77.utils.inventory.SlotIterator;
import ru.komiss77.utils.inventory.SlotPos;


public class RequestSelect implements InventoryProvider {
    
    private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Material.PURPLE_STAINED_GLASS_PANE).name("§8.").build());
    private final Mission mi;

    
    public RequestSelect(final Mission mission) {
        this.mi = mission;
    }
     

    
    @Override
    public void init(final Player p, final InventoryContent content) {
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
        

        
        //линия - разделитель
        content.fillRow(5, fill);
        
        
        
        
        final ArrayList<ClickableItem> menuEntry = new ArrayList<>();        
        

        menuEntry.add( new InputButton( InputButton.InputType.ANVILL, new ItemBuilder(Material.ACACIA_SIGN)
            .name("Создать локальное требование")
            .addLore("§7")
            .addLore("§7до 16 символов")
            .addLore("§7Счётчик добавляется через метод")
            .addLore("§bApiOstrov.addCustomStat(Player, String, int)")
            .addLore("§7")
            .addLore("§7ЛКМ - добавить")
            .addLore("§7Так же будет создан")
            .addLore("§7ремаппинг для отображаемого")
            .addLore("§7название, редактировать")
            .addLore("§7в главном меню миссиий.")
            .build(), "чтототам", name -> {
                if (name.length()>16) {
                    p.sendMessage("§cДо 16 символов!");
                    reopen(p, content);
                    return;
                }
                mi.request.put(name, 1);
                if (!MissionManager.customStatsDisplayNames.containsKey(name)) {
                    MissionManager.customStatsDisplayNames.put(name, "§e"+name);
                    MissionManager.customStatsShowAmmount.put(name, true);
                    OstrovDB.executePstAsync(p, "INSERT INTO `customStats`(name,displayName) VALUES ('"+name+"','§e"+name+"')");
                }
                MissionManager.editMission(p, mi);
        }));
                    
        for (String name : MissionManager.customStatsDisplayNames.keySet()) {
            menuEntry.add(ClickableItem.of(new ItemBuilder(MissionManager.customStatMat(name))
                .name("§7локальное требование")
                .addLore("§f"+name)
                .addLore("")
                .addLore("§7Отображаемое название:")
                .addLore(MissionManager.customStatsDisplayNames.get(name))
                .addLore("")
                .addLore("§7ЛКМ - добавить к требованиям")
                .addLore("")
                .addLore("§7Изменять и удалять ремаппинг")
                .addLore("§7для custoStat можно в")
                .addLore("§7главном меню редактора.")
                .addLore("")
                .build(), e-> {
                    mi.request.put(name, 1);
                    MissionManager.editMission(p, mi);
                }
            ));
        }

                    
        for (Stat stat : Stat.values()) {
            switch (stat) {
                case EXP, FLAGS, KARMA, LEVEL, PLAY_TIME, REPUTATION -> {
                    continue;
                }
                default -> {}
            }
            menuEntry.add(ClickableItem.of(new ItemBuilder(Material.matchMaterial(stat.game.mat))
                .name("§f"+stat.name())
                .addLore(stat.game.displayName+" "+stat.desc)
                .addLore("")
                .addLore("§7ЛКМ - добавить к требованиям")
                .addLore("")
                .build(), e-> {
                    mi.request.put(stat.name(), 1);
                    MissionManager.editMission(p, mi);
                }
            ));
        }

        
        
        
        
       content.set(5, 4, ClickableItem.of(new ItemBuilder(Material.OAK_DOOR)
                .setCustomHeadTexture(ItemUtils.Texture.previosPage)
                .name("§7вернуться")
                .build(), e -> {
                    MissionManager.editMission(p, mi);
                }));


            
        final Pagination pagination = content.pagination();
        pagination.setItems(menuEntry.toArray(ClickableItem[]::new));
        pagination.setItemsPerPage(45);    


        if (!pagination.isLast()) {
            content.set(5, 8, ClickableItem.of(ItemUtils.nextPage, e 
                    -> {
                content.getHost().open(p, pagination.next().getPage()) ;
            }
            ));
        }

        if (!pagination.isFirst()) {
            content.set(5, 0, ClickableItem.of(ItemUtils.previosPage, e 
                    -> {
                content.getHost().open(p, pagination.previous().getPage()) ;
               })
            );
        }

        pagination.addToIterator(content.newIterator(SlotIterator.Type.HORIZONTAL, SlotPos.of(0, 0)).allowOverride(false));


    }                
        

                
                
                









        
        
        
        
        
       

    }


    
    
    
    
