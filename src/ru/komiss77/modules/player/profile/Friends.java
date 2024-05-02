package ru.komiss77.modules.player.profile;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.OstrovDB;
import ru.komiss77.enums.Operation;
import ru.komiss77.enums.ServerType;
import ru.komiss77.enums.Settings;
import ru.komiss77.listener.SpigotChanellMsg;
import ru.komiss77.modules.games.GM;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.inventory.SmartInventory;


public class Friends {

    public static final String FRIENDS_PREFIX = "§a§lД§d§lр§c§lу§e§lз§9§lь§b§lя";
    public static final String PARTY_PREFIX = "§6[§eКоманда§6] §3";
    



    public static void openPartyMain (final Oplayer op) {
        op.menu.section = Section.КОМАНДА;
        op.menu.friendMode = ProfileManager.FriendMode.Просмотр;
        //op.menu.runLoadAnimations();
        //Ostrov.sync( ()->ApiOstrov.sendMessage(op.getPlayer(), Operation.GET_FRIENDS_INFO, op.nik), 20); //задержка для анимации))
        op.menu.current = SmartInventory
                .builder()
                .id(op.nik+op.menu.section.name())
                .provider(new PartyView())
                .size(6, 9)
                .title(op.eng ? Section.КОМАНДА.item_nameEn : Section.КОМАНДА.item_nameRu)
                .build()
                .open(op.getPlayer());
    }
    
    public static void openPartySettings (final Oplayer op) {
        op.menu.section = Section.КОМАНДА;
        op.menu.friendMode = ProfileManager.FriendMode.Настройки;
        //op.menu.runLoadAnimations();
        op.menu.current = SmartInventory
                .builder()
                .id(op.nik+op.menu.section.name())
                .provider(new PartySettings())
                .size(6, 9)
                .title(op.eng ? Section.КОМАНДА.item_nameEn + "§8: Settings" :
                    Section.КОМАНДА.item_nameRu + "§8: Настройки")
                .build()
                .open(op.getPlayer());
    }
    
    public static void openPartyFind (final Oplayer op) {
        op.menu.section = Section.КОМАНДА;
        op.menu.friendMode = ProfileManager.FriendMode.Поиск;
        op.menu.current = SmartInventory
                .builder()
                .id(op.nik+op.menu.section.name())
                .provider(new PartyFind())
                .size(6, 9)
                .title(op.eng ? Section.КОМАНДА.item_nameEn + "§8: Invite" :
                    Section.КОМАНДА.item_nameRu + "§8: Добавить")
                .build()
                .open(op.getPlayer());
    }

    public static void suggestParty(final Player p1, final Oplayer op, final Player p2) {
        //p1.sendMessage(friendsPrefix+" §fВы отправили предложение дружать §f"+p2.getName());
        PM.getOplayer(p2).partyInvite.add(p1.getName());
        ApiOstrov.executeBungeeCmd(p1, "party invite "+p2.getName());
        //принятие пока по /party accept
        //p2.sendMessage(friendsPrefix+" §f"+p1.getName()+" §7предлагает дружить. Принять предложение можно в меню друзей.");
    }


    
    //самого себе банжик не пришлёт
    public static void onPartyMemberServerSwitch(final String name, final String memberName, final String memberServer, final String memberArena) {
        //if (op.party_leader.isEmpty()) return; по идее синхронно с банжи, должно совпадать
//Ostrov.log_warn("onPartyMemberServerSwitch for name="+name+" "+memberName+"->"+memberServer+"->"+memberArena);
        final Oplayer op = PM.getOplayer(name);
        op.party_members.put(memberName, memberServer);
        
        if (op.party_leader.equals(memberName)) {
            
            if (!op.hasSettings(Settings.Party_SlaveDeny)) {
                
                if (memberServer.equals(Ostrov.MOT_D)) {
                    
                    op.getPlayer().sendMessage(PARTY_PREFIX+" §7Лидер команды §6"+memberName+" §7на вашем сервере §3"+memberServer
                            +(memberArena.isEmpty()?" §7!":" §7(§bарена "+memberArena+") !"));
                    
                } else {
                    
                    op.getPlayer().sendMessage(PARTY_PREFIX+" §7Вы последовали за лидером команды §6"+memberName+" §7на сервер §3"+memberServer
                            +(memberArena.isEmpty()?" §7!":" §7("+memberArena+") !"));
                    op.getPlayer().performCommand("server "+memberServer+" "+memberArena);
                    
                }
                
            } else if (!op.hasSettings(Settings.Party_LeaderTrackDeny)) {
                
                if (memberServer.equals(Ostrov.MOT_D)) {
                    
                    op.getPlayer().sendMessage(PARTY_PREFIX+" §7Лидер команды §6"+memberName+" §7теперь на вашем сервере !");
                } else {
                    
                    op.getPlayer().sendMessage(PARTY_PREFIX+" §7Лидер команды §6"+memberName+" §7теперь на серверe §3"+memberServer
                            +(memberArena.isEmpty()?" §7!":" §7("+memberArena+") !"));
                }
            }
            
        }
    }    
    
    
    //при onBungeeDataRecieved и изменении режима видимости в меню
    public static void updateViewMode(final Player p) {
        
        if (GM.GAME.type!=ServerType.LOBBY) return;
        
        final Oplayer op = PM.getOplayer(p);
        
        Oplayer targetOp;
        for (Player target : Bukkit.getOnlinePlayers()) {
            //if (target.getName().equals(p.getName())) continue;
            targetOp = PM.getOplayer(p);
            if (targetOp!=null && !targetOp.nik.equals(op.nik)) {
                //друзья
                if (op.friends.contains(targetOp.nik)) {
                    if (op.hasSettings(Settings.Fr_ShowFriendDeny)) {
                        p.hidePlayer(Ostrov.instance, target);
                    } else {
                        p.showPlayer(Ostrov.instance, target);
                    }
                    if (targetOp.hasSettings(Settings.Fr_ShowFriendDeny)) {
                        target.hidePlayer(Ostrov.instance, p);
                    } else {
                        target.showPlayer(Ostrov.instance, p);
                    }
                }  
                //команда
                else if (op.party_members.containsKey(targetOp.nik)) {
                    if (op.hasSettings(Settings.Fr_ShowPartyDeny)) {
                        p.hidePlayer(Ostrov.instance, target);
                    } else {
                        p.showPlayer(Ostrov.instance, target);
                    }
                    if (targetOp.hasSettings(Settings.Fr_ShowPartyDeny)) {
                        target.hidePlayer(Ostrov.instance, p);
                    } else {
                        target.showPlayer(Ostrov.instance, p);
                    }
                }
                //остальные
                else {
                    if (op.hasSettings(Settings.Fr_ShowOtherDeny)) {
                        p.hidePlayer(Ostrov.instance, target);
                    } else {
                        p.showPlayer(Ostrov.instance, target);
                    }
                    if (targetOp.hasSettings(Settings.Fr_ShowOtherDeny)) {
                        target.hidePlayer(Ostrov.instance, p);
                    } else {
                        target.showPlayer(Ostrov.instance, p);
                    }
                }
            }
        }

    }





    // ************************ Секция меню Друзья ****************************

    public static void openFriendsMain (final Oplayer op) {
        op.menu.section = Section.ДРУЗЬЯ;
        op.menu.friendMode = ProfileManager.FriendMode.Просмотр;
        op.menu.runLoadAnimations();
        Ostrov.sync( ()->SpigotChanellMsg.sendMessage(op.getPlayer(), Operation.GET_FRIENDS_INFO, op.nik), 20); //задержка для анимации))
    }
    
    
    
    
    
    public static void openFriendsMail (final Oplayer op) {
        op.menu.section = Section.ДРУЗЬЯ;
        op.menu.friendMode = ProfileManager.FriendMode.Письма;
        
        op.menu.runLoadAnimations();
        final List<ItemStack> mails = new ArrayList<>();
        Ostrov.async( ()->{
            try (Statement stmt = OstrovDB.getConnection().createStatement(); 
                 ResultSet rs = stmt.executeQuery("select * from `fr_messages` WHERE `reciever`='"+op.nik+"';")) {
                    while(rs.next()) {
                        mails.add(new ItemBuilder(Material.PAPER)
                            .name("§7Письмо от §f"+rs.getString("sender"))
                            .addLore("")
                            .addLore("§7Отправлено:")
                            .addLore("§7"+ApiOstrov.dateFromStamp(rs.getInt("time")))
                            .addLore("")
                            .addLore(ItemUtils.genLore(null, rs.getString("message"), "§6"))
                            .addLore("")
                            .build()
                        );
                        //time=rs.getInt("time");
                        //msg=new TextComponent("§6Сообщение от §e"+rs.getString("sender")+" §e: §f"+rs.getString("message")+" §8<клик-следущее");
                        //msg.setHoverEvent(new HoverEvent( HoverEvent.Action.SHOW_TEXT, new Text("§5§oКлик - читать следущее \n§7Отправлено: §f"+time ) ));
                        //msg.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, "/fr mread" ) );
                    }
                    rs.close();
                    stmt.close();
                    
                    Ostrov.sync(()-> {
                        if (op.menu.section==Section.ДРУЗЬЯ && op.menu.friendMode==ProfileManager.FriendMode.Письма) {
                            op.menu.stopLoadAnimations();
                            op.menu.current = SmartInventory
                                    .builder()
                                    .id(op.nik+op.menu.section.name())
                                    .provider(new FriendMail(mails))
                                    .size(6, 9)
                                    .title(op.eng ? Section.ДРУЗЬЯ.item_nameEn + "§8: Messages" : Section.ДРУЗЬЯ.item_nameRu + "§8: Письма")
                                    .build()
                                    .open(op.getPlayer());
                        }
                    }, 0);
                    
            } catch (SQLException ex) { 
                Ostrov.log_err("FM openFriendsMail : "+ex.getMessage());
            }
        }, 20); //задержка для анимации))

    }
    
    public static void onFriendsInfoRecieve (final Oplayer op, final String rawData) {
        if (op.menu.section==Section.ДРУЗЬЯ && op.menu.friendMode==ProfileManager.FriendMode.Просмотр) {
            op.menu.stopLoadAnimations();
            op.menu.current = SmartInventory
                    .builder()
                    .id(op.nik+op.menu.section.name())
                    .provider(new FriendView(rawData))
                    .size(6, 9)
                    .title(op.eng ? Section.ДРУЗЬЯ.item_nameEn : Section.ДРУЗЬЯ.item_nameRu)
                    .build()
                    .open(op.getPlayer());
        }// else p.sendMessage("уже другое меню"); }
    }
    
    public static void openFriendsSettings (final Oplayer op) {
        op.menu.section = Section.ДРУЗЬЯ;
        op.menu.friendMode = ProfileManager.FriendMode.Настройки;
        op.menu.current = SmartInventory
                .builder()
                .id(op.nik+op.menu.section.name())
                .provider(new FriendSettings())
                .size(6, 9)
                .title(op.eng ? Section.ДРУЗЬЯ.item_nameEn + "§8: Settings" : Section.ДРУЗЬЯ.item_nameRu + "§8: Настройки")
                .build()
                .open(op.getPlayer());
    }
    
    public static void openFriendsFind (final Oplayer op) {
        op.menu.section = Section.ДРУЗЬЯ;
        op.menu.friendMode = ProfileManager.FriendMode.Поиск;
        op.menu.current = SmartInventory
                .builder()
                .id(op.nik+op.menu.section.name())
                .provider(new FriendFind())
                .size(6, 9)
                .title(op.eng ? Section.ДРУЗЬЯ.item_nameEn + "§8: Invite" : Section.ДРУЗЬЯ.item_nameRu + "§8: Добавить")
                .build()
                .open(op.getPlayer());
    }
    
 //*****************************************************************   























    

    public static void suggestFriend(final Player p1, final Oplayer op, final Player p2) {
        p1.sendMessage(FRIENDS_PREFIX+" §fВы отправили предложение дружить §f"+p2.getName());
        PM.getOplayer(p2).friendInvite.add(p1.getName());
        p2.sendMessage(FRIENDS_PREFIX+" §f"+p1.getName()+" §7предлагает дружить. Принять предложение можно в меню друзей.");
    }



    //согласие на инвайт
    public static void add(final Player p1, final Oplayer op1, final String name) {
        p1.closeInventory();
        
        final Player p2 = Bukkit.getPlayerExact(name);
        if (p2==null) {
            p1.sendMessage("§cЧтобы подружиться, вы должны стоять рядом.");
            return;
        } 
        if (!op1.friendInvite.remove(name)) return;
        
        final Oplayer op2 = PM.getOplayer(name);
        
        OstrovDB.executePstAsync(p1, "INSERT INTO `fr_friends` (`f1`, `f2`) values ('"+op1.nik+"', '"+op2.nik+"') ");
        
        op1.friends.add(name);
        op2.friends.add(op1.nik);
        SpigotChanellMsg.sendMessage(p1, Operation.FRIEND_ADD, op1.nik, op2.nik);
        SpigotChanellMsg.sendMessage(p2, Operation.FRIEND_ADD, op2.nik, op1.nik);
        
        p1.sendMessage(Component.text(FRIENDS_PREFIX+" §2Вы подружились с §a"+op2.nik+" §2!  §8<<Клик-написать сообщение")
        	.hoverEvent(HoverEvent.showText(Component.text("§5§oКлик-написать сообщение!")))
        	.clickEvent(ClickEvent.suggestCommand("/friend mail "+op2.nik+" прив")));
        
        p2.sendMessage(Component.text(FRIENDS_PREFIX+" §2Вы подружились с §a"+op1.nik+" §2!  §8<<Клик-написать сообщение")
        	.hoverEvent(HoverEvent.showText(Component.text("§5§oКлик-написать сообщение!")))
        	.clickEvent(ClickEvent.suggestCommand("/friend mail "+op1.nik+" прив")));
        
        /*final TextComponent done=new TextComponent(friendsPrefix+" §2Вы подружились с §a"+op2.nik+" §2!  §8<<Клик-написать сообщение");
        done.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/friend mail "+op2.nik+" привет "));
        done.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new Text("§5§oКлик-написать сообщение!") ) );
        p1.spigot().sendMessage(done);
        
        final TextComponent done2=new TextComponent(friendsPrefix+" §2Вы подружились с §a"+op1.nik+" §2!  §8<<Клик-написать сообщение");
        //done2.setText(friendsPrefix+" §2Вы подружились с §a"+op1.nik+" §2!  §8<<Клик-написать сообщение");
        done2.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/friend mail "+op1.nik+" привет "));
        p2.spigot().sendMessage(done2);*/
        
        p1.getWorld().playSound(p1.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1f, 1f);
        
        //салютик

    }



    public static void delete(final Player p1, final Oplayer op1, final String name) {
        
        OstrovDB.executePstAsync(p1, "DELETE FROM `fr_friends` WHERE (f1 = '"+op1.nik+"' AND f2='"+name+"') OR (f1 = '"+name+"' AND f2='"+op1.nik+"') ");
        
        op1.friends.remove(name);
        SpigotChanellMsg.sendMessage(p1, Operation.FRIEND_DELETE, op1.nik, name);
        
        p1.sendMessage(FRIENDS_PREFIX+" §fВы больше не дружите с §4"+name);
        
        final Player p2 = Bukkit.getPlayerExact(name);
        if (p2!=null) {
            final Oplayer op2 = PM.getOplayer(name);
            op2.friends.remove(op1.nik);
            SpigotChanellMsg.sendMessage(p2, Operation.FRIEND_DELETE, op2.nik, op1.nik);
            p2.sendMessage(FRIENDS_PREFIX+" §fВы больше не дружите с §4"+op1.nik);
//p2.sendMessage("DEL "+op2.nik);
        } 

//p1.sendMessage("DEL "+name);
    }

















    /*
    public static TextComponent logoDeny (final String nik) {
        final TextComponent block=new TextComponent(" §4✕");
        block.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ignore add "+nik));
        block.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new Text("§4Заблокировать "+nik+" до перезахода.") ) );
        return block;
    }

    public static TextComponent logoMail (final String nik) {
        final TextComponent mail=new TextComponent(" §6✉");
        mail.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/friend mail "+nik+" "));
        mail.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new Text("§eНаписать "+nik+" сообщение.") ) );
        return mail;
    }
    
    public static TextComponent logoTp (final String nik) {
        final TextComponent tp=new TextComponent(" §3✈");
        tp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend jump "+nik));
        tp.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new Text("§5Отправить "+nik+" запрос на ТП.") ) );
        return tp;
    }    
*/


    
    
    
    
}
