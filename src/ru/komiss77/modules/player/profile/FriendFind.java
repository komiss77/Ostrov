package ru.komiss77.modules.player.profile;


import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
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





public class FriendFind implements InventoryProvider {
    
    
    
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
            
            if (op.friends.contains(find.getName()) ) {

                final ItemStack friend_item = new ItemBuilder(Material.EMERALD)
                        .name(find.getName())
                        .addLore("")
                        .addLore("§2Уже друзья")
                        .addLore("")
                        .build();

                menuEntry.add(ClickableItem.empty(friend_item));

            } else if (findOp.hasSettings(Settings.Fr_InviteDeny)) {

                final ItemStack friend_item = new ItemBuilder(Material.SKELETON_SKULL)
                        .name(find.getName())
                        .addLore("")
                        .addLore("§cПредложения дружить")
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

            }  else if (findOp.friendInvite.contains(p.getName())) {

                final ItemStack friend_item = new ItemBuilder(Material.CREEPER_HEAD)
                        .name(find.getName())
                        .addLore("")
                        .addLore("§6Предложение дружить.")
                        .addLore("§6отправлено.")
                        .addLore("")
                        .build();

                menuEntry.add(ClickableItem.empty(friend_item));

            } else { 

               final ItemStack friend_item = new ItemBuilder(Material.PLAYER_HEAD)
                        .name(find.getName())
                        .addLore("")
                        .addLore("§aПредложить дружбу")
                        .addLore("")
                        .build();

                menuEntry.add(ClickableItem.of(friend_item
                        , e-> {
                            if (find.isOnline()) {
                                Friends.suggestFriend(p, op, find);
                                //mode = FriendMode.Просмотр;
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

        
              /* if (op.friends.isEmpty()) {

                    content.set( SlotPos.of(1, 4), ClickableItem.of(new ItemBuilder(Material.GLASS_BOTTLE)
                                .setName("§7Ой!")
                                .addLore("У Вас пока нет друзей!")
                                .addLore("")
                                .addLore("Чтобы добавить друга/подругу,")
                                .addLore("§7встаньте рядом и")
                                .addLore("§7клик на бутылочку.")
                                .addLore("")
                                .build(), e-> {

                                }
                        ) 
                    );

                } else {*/

        



        

        
        /*
        content.set( 5, 8, ClickableItem.of( new ItemBuilder(Material.OAK_DOOR).name("Закрыть").build(), e -> 
        {
            p.closeInventory();
        }
        ));
        */


        

    }


    
    
    
    
    
    
    
    
    
}
