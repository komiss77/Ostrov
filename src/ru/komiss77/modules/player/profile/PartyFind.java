package ru.komiss77.modules.player.profile;


import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.ApiOstrov;
import ru.komiss77.enums.Settings;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.LocationUtil;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.Pagination;
import ru.komiss77.utils.inventory.SlotIterator;
import ru.komiss77.utils.inventory.SlotPos;





public class PartyFind implements InventoryProvider {
    
    
    
    private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Section.ДРУЗЬЯ.glassMat).name("§8.").build());
    

     
    @Override
    public void onClose(final Player p, final InventoryContent content) {
        PM.getOplayer(p).menu.current = null;
    }

    
    @Override
    public void init(final Player p, final InventoryContent content) {
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
        //content.fillRect(0,0,  5,8, ClickableItem.empty(fill));
        
        final Oplayer op = PM.getOplayer(p);
        //final ProfileManager pm = op.menu;

        
        
        //линия - разделитель
        content.fillRow(4, fill);
        
        //выставить иконки внизу
        for (Section section:Section.values()) {
            content.set(section.slot, Section.getMenuItem(section, op));
        }

        
        
        
        
        
        

        final Pagination pagination = content.pagination();
        final ArrayList<ClickableItem> menuEntry = new ArrayList<>();        

        Oplayer findOp;
        boolean found = false;
        
        for (final Player find : Bukkit.getOnlinePlayers()) {

            if (find.getName().equals(p.getName())) continue;
            if (LocationUtil.getDistance(p.getLocation(), find.getLocation()) > 30) continue;

            findOp = PM.getOplayer(find);
            if (findOp==null) continue;
            found = true;
            
            if (op.party_members.containsKey(find.getName()) ) {

                final ItemStack friend_item = new ItemBuilder(Material.EMERALD)
                        .name(find.getName())
                        .addLore("")
                        .addLore("§2Уже в вашей команде")
                        .addLore("")
                        .build();

                menuEntry.add(ClickableItem.empty(friend_item));

            } else if (!findOp.party_leader.isEmpty()) {

                final ItemStack friend_item = new ItemBuilder(Material.SKELETON_SKULL)
                        .name(find.getName())
                        .addLore("")
                        .addLore("§2Уже в команде "+findOp.party_leader)
                        .addLore("")
                        .build();

                menuEntry.add(ClickableItem.empty(friend_item));

            } else if (findOp.hasSettings(Settings.Party_InviteOtherDeny) && !ApiOstrov.isFriend(op.nik, find.getName())) {

                final ItemStack friend_item = new ItemBuilder(Material.SKELETON_SKULL)
                        .name(find.getName())
                        .addLore("")
                        .addLore("§cПриглашения в команду")
                        .addLore("§cот посторонних")
                        .addLore("§cотключены в настройках.")
                        .addLore("")
                        .build();

                menuEntry.add(ClickableItem.empty(friend_item));

            } else if (findOp.hasSettings(Settings.Party_InviteFriendsDeny) && ApiOstrov.isFriend(op.nik, find.getName())) {

                final ItemStack friend_item = new ItemBuilder(Material.SKELETON_SKULL)
                        .name(find.getName())
                        .addLore("")
                        .addLore("§cПриглашения в команду")
                        .addLore("§cот друзей")
                        .addLore("§cотключены в настройках.")
                        .addLore("")
                        .build();

                menuEntry.add(ClickableItem.empty(friend_item));

            } else if (op.isBlackListed(p.getName())) {

                final ItemStack friend_item = new ItemBuilder(Material.WITHER_SKELETON_SKULL)
                        .name(find.getName())
                        .addLore("")
                        .addLore("§cВ игноре")
                        .addLore("")
                        .build();

                menuEntry.add(ClickableItem.empty(friend_item));

            }  else if (findOp.isBlackListed(p.getName())) {

                final ItemStack friend_item = new ItemBuilder(Material.WITHER_SKELETON_SKULL)
                        .name(find.getName())
                        .addLore("")
                        .addLore("§cВы занесены в игнор")
                        .addLore("")
                        .build();

                menuEntry.add(ClickableItem.empty(friend_item));

            }  else if (findOp.partyInvite.contains(p.getName())) {

                final ItemStack friend_item = new ItemBuilder(Material.CREEPER_HEAD)
                        .name(find.getName())
                        .addLore("")
                        .addLore("§6Приглашение уже")
                        .addLore("§6отправлено.")
                        .addLore("")
                        .build();

                menuEntry.add(ClickableItem.empty(friend_item));

            } else { 

               final ItemStack friend_item = new ItemBuilder(Material.PLAYER_HEAD)
                        .name(find.getName())
                        .addLore("")
                        .addLore("§aПригласить в команду")
                        .addLore("")
                        .build();

                menuEntry.add(ClickableItem.of(friend_item
                        , e-> {
                            if (find.isOnline()) {
                                Friends.suggestParty(p, op, find);
                                reopen(p, content);
                            }
                        }
                    )
                );

            }                       



        }

        if (!found) {

            final ItemStack notFound = new ItemBuilder(Material.GLASS_BOTTLE)
                    .name("§7Никого не смогли найти..")
                    .addLore("")
                    .addLore("§7Поиск ведется в радиусе")
                    .addLore("§75 блоков.")
                    .addLore("")
                    .addLore("§7ЛКМ - обновить")
                    .addLore("")
                    .build();

            content.set(13, ClickableItem.of(notFound, e-> {
                reopen(p, content);
            }));

        }

        
        
        
        
        
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


        

    }


    
    
    
    
    
    
    
    
    
}
