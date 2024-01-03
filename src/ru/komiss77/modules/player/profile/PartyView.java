package ru.komiss77.modules.player.profile;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;





public class PartyView implements InventoryProvider {
    
    
    
    private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Section.ДРУЗЬЯ.glassMat).name("§8.").build());


    

     
    @Override
    public void onClose(final Player p, final InventoryContent content) {
        PM.getOplayer(p).menu.current = null;
    }

    
    @Override
    public void init(final Player p, final InventoryContent content) {
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
        
        final Oplayer op = PM.getOplayer(p);
        
        //линия - разделитель
        content.fillRow(4, fill);
        
        //выставить иконки внизу
        for (Section section:Section.values()) {
            content.set(section.slot, Section.getMenuItem(section, op));
        }

        

        
        if (op.party_members.isEmpty()) {
            
            content.add(ClickableItem.of( new ItemBuilder(Material.ENDER_EYE)
                    .name("§eСоздать свою команду")
                    .addLore( "§7" )
                    .build(), e-> {
                        ApiOstrov.executeBungeeCmd(p, "party create");
                        op.party_leader = op.nik;
                        op.party_members.put(op.nik, Ostrov.MOT_D);
                        reopen(p, content);
                    }
                )
            );
            
            
            //приглашения ??
            
            
            
            
            
            
        } else if (op.nik.equals(op.party_leader)) {  //лидер
            
            
            
            for (final String name : op.party_members.keySet()) {

                final ItemStack  head = new ItemBuilder(Material.PLAYER_HEAD)
                    .name(name)
                    .addLore(op.party_leader.equals(name) ? "§aЛидер" : "§7Участник")
                    .addLore("")
                    .addLore("§7Сервер: §a"+op.party_members.get(name))
                    .addLore("")
                    .addLore(op.party_leader.equals(name)? "§7Клав. Q - §cпокинуть команду" : "§7клав.Q - §cвыгнать")
                    .addLore(op.party_leader.equals(name)? "" : "§7ПКМ - §6передать лидерство")
                    .addLore("")
                    .build();

                content.add(ClickableItem.of(head, e-> {
                        if (e.getClick()==ClickType.DROP) {
                            if (op.party_leader.equals(name)) {
                                ApiOstrov.executeBungeeCmd(p, "party leave");
                                op.party_members.clear();
                                op.party_leader = "";
                            } else {
                                ApiOstrov.executeBungeeCmd(p, "party kick "+name);
                                op.party_members.remove(name);
                            }
                            reopen(p, content);
                        } else if (e.getClick()==ClickType.RIGHT) {
                            ApiOstrov.executeBungeeCmd(p, "party leader "+name);
                            op.party_leader = name;
                            reopen(p, content);
                        }
                    }
                ));

            }
            
            if (op.party_members.size()<8) {
                content.add(ClickableItem.of( new ItemBuilder(Material.ENDER_EYE)
                        .name("§aпригласить")
                        .addLore( "" )
                        .addLore("§7Чтобы отправить приглашение")
                        .addLore("§7в команду, встаньте рядом")
                        .addLore("§7и нажмите на эту иконку." )
                        .addLore( "" )
                        .build(), e-> {
                            //mode = FriendMode.Поиск;
                            Friends.openPartyFind(op);
                        }
                    )
                );
            }
            
        } else {
            
            
            for (final String name : op.party_members.keySet()) {

                final ItemStack  head = new ItemBuilder(Material.PLAYER_HEAD)
                    .name(name)
                    .addLore(op.party_leader.equals(name) ? "§aЛидер" : "§7Участник")
                    .addLore( "" )
                    .addLore("§7Сервер: §a"+op.party_members.get(name))
                    .addLore( "" )
                    .addLore( op.nik.equals(name) ? "§7Клав. Q - §cпокинуть команду" : "" )
                    .addLore( "" )
                    .build();

                if (op.nik.equals(name)) {
                    content.add(ClickableItem.of(head, e-> {
                        if (e.getClick()==ClickType.DROP) {
                            ApiOstrov.executeBungeeCmd(p, "party leave");
                            op.party_members.clear();
                            op.party_leader = "";
                            reopen(p, content);
                        }
                    }));
                } else {
                    content.add(ClickableItem.empty(head));
                }

            }

        }
   

 
                


      //  }




                
                
                
                
                
                
                
                
                
                
                
                
                
                
                

                
                
                
                
                
                
                
                
                
                


        

    }


    
    
    
    
    
    
    
    
    
}
