package ru.komiss77.modules.player.profile;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.RemoteDB;
import ru.komiss77.Timer;
import ru.komiss77.enums.*;
import ru.komiss77.listener.SpigotChanellMsg;
import ru.komiss77.modules.games.GM;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.*;
import ru.komiss77.utils.inventory.SmartInventory;


public class Friends {

    public static final String FRIENDS_PREFIX = "§a§lД§d§lр§c§lу§e§lз§9§lь§b§lя §f";
    public static final String PARTY_PREFIX = "§6[§eКоманда§6] §3";


    public static void openPartyMain(final Oplayer op) {
        op.menu.section = Section.КОМАНДА;
        op.menu.friendMode = ProfileManager.FriendMode.Просмотр;
        //op.menu.runLoadAnimations();
        //Ostrov.sync( ()->ApiOstrov.sendMessage(op.getPlayer(), Operation.GET_FRIENDS_INFO, op.nik), 20); //задержка для анимации))
        op.menu.current = SmartInventory
            .builder()
            .id(op.nik + op.menu.section.name())
            .provider(new PartyView())
            .size(3, 9)
            .title(op.eng ? Section.КОМАНДА.item_nameEn : Section.КОМАНДА.item_nameRu)
            .build()
            .open(op.getPlayer());
    }

    public static void openPartySettings(final Oplayer op) {
        op.menu.section = Section.КОМАНДА;
        op.menu.friendMode = ProfileManager.FriendMode.Настройки;
        //op.menu.runLoadAnimations();
        op.menu.current = SmartInventory
            .builder()
            .id(op.nik + op.menu.section.name())
            .provider(new PartySettings())
            .size(3, 9)
            .title(op.eng ? Section.КОМАНДА.item_nameEn + "§8: Settings" :
                Section.КОМАНДА.item_nameRu + "§8: Настройки")
            .build()
            .open(op.getPlayer());
    }

    public static void openPartyFind(final Oplayer op) {
        op.menu.section = Section.КОМАНДА;
        op.menu.friendMode = ProfileManager.FriendMode.Поиск;
        op.menu.current = SmartInventory
            .builder()
            .id(op.nik + op.menu.section.name())
            .provider(new PartyFind())
            .size(3, 9)
            .title(op.eng ? Section.КОМАНДА.item_nameEn + "§8: Invite" :
                Section.КОМАНДА.item_nameRu + "§8: Добавить")
            .build()
            .open(op.getPlayer());
    }

    public static void suggestParty(final Player from, final Player to) {
        PM.getOplayer(to).partyInvite.add(from.getName());
        ApiOstrov.executeBungeeCmd(from, "party " + to.getName());
    }


    //самого себе банжик не пришлёт
    public static void onPartyMemberServerSwitch(final String name, final String memberName, final String memberServer, final String memberArena) {
        final Oplayer op = PM.getOplayer(name);
        op.party_members.put(memberName, memberServer);
        if (op.party_leader.equals(memberName)) {
            final Player pl = op.getPlayer();
            if (pl == null) return;
            if (!op.hasSettings(Settings.LeaderFollowDeny)) {
                if (memberServer.equals(Ostrov.MOT_D)) {
                    pl.sendMessage(PARTY_PREFIX + "§7Лидер §6" + memberName + " §7тоже на сервере §3" + memberServer
                        + (memberArena.isEmpty() ? " §7!" : " §7(§9 " + memberArena + "§7)!"));
                } else {
                    pl.sendMessage(PARTY_PREFIX + "§7Лидером §6" + memberName + " §7перешел на сервер §3" + memberServer
                        + (memberArena.isEmpty() ? " §7!" : " §7(§9" + memberArena + "§7)!"));
                    pl.performCommand("server " + memberServer + " " + memberArena);
                }
            } else if (!op.hasSettings(Settings.LeaderTrackDeny)) {
                if (memberServer.equals(Ostrov.MOT_D)) {
                    pl.sendMessage(PARTY_PREFIX + " §7Лидер §6" + memberName + " §7тоже на этом сервере!");
                } else {
                    pl.sendMessage(PARTY_PREFIX + " §7Лидер §6" + memberName + " §7перешел на сервер §3" + memberServer);
                }
            }
        }
    }

    //при onBungeeDataRecieved и изменении режима видимости в меню
    public static void updateViewMode(final Player p) {

        if (GM.GAME.type != ServerType.LOBBY) return;

        final Oplayer op = PM.getOplayer(p);
        for (Player target : Bukkit.getOnlinePlayers()) {
            //if (target.getName().equals(p.getName())) continue;
            final Oplayer targetOp = PM.getOplayer(p);
            if (targetOp != null && !targetOp.nik.equals(op.nik)) {
                boolean showPl = true, showTgt = true;
                //друзья
                if (!op.friends.contains(targetOp.nik)) {
                    if (op.hasSettings(Settings.HideNonFriends)) {
                        p.hidePlayer(Ostrov.instance, target);
                        showTgt = false;
                    }
                    if (targetOp.hasSettings(Settings.HideNonFriends)) {
                        target.hidePlayer(Ostrov.instance, p);
                        showPl = false;
                    }
                }
                //команда
                if (!op.party_members.containsKey(targetOp.nik) && (showTgt || showPl)) {
                    if (op.hasSettings(Settings.HideNonParty) && showTgt) {
                        p.hidePlayer(Ostrov.instance, target);
                        showTgt = false;
                    }
                    if (targetOp.hasSettings(Settings.HideNonParty) && showPl) {
                        target.hidePlayer(Ostrov.instance, p);
                        showPl = false;
                    }
                }

                if (showTgt) p.showPlayer(Ostrov.instance, target);
                if (showPl) target.showPlayer(Ostrov.instance, p);
            }
        }

    }


    // ************************ Секция меню Друзья ****************************

    public static void openFriendsMain(final Oplayer op) {
        op.menu.section = Section.ДРУЗЬЯ;
        op.menu.friendMode = ProfileManager.FriendMode.Просмотр;
        op.menu.runLoadAnimations();
        Ostrov.sync(() -> SpigotChanellMsg.sendMessage(op.getPlayer(), Operation.GET_FRIENDS_INFO, op.nik), 2); //задержка для анимации))
    }

    @Deprecated //это должно быть в меню друзей, для каждого друга
    public static void openFriendsMail(final Oplayer op) {
        op.menu.section = Section.ДРУЗЬЯ;
        op.menu.friendMode = ProfileManager.FriendMode.Письма;

        op.menu.runLoadAnimations();
        final List<ItemStack> mails = new ArrayList<>();
        Ostrov.async(() -> {
            try (Statement stmt = RemoteDB.getConnection().createStatement();
                 ResultSet rs = stmt.executeQuery("select * from `fr_messages` WHERE `reciever`='" + op.nik + "';")) {
                while (rs.next()) {
                    mails.add(new ItemBuilder(Material.PAPER)
                        .name("§7Письмо от §f" + rs.getString("sender"))
                        .lore("")
                        .lore("§7Отправлено:")
                        .lore("§7" + TimeUtil.dateFromStamp(rs.getInt("time")))
                        .lore("")
                        .lore(ItemUtil.genLore(null, rs.getString("message"), "§6"))
                        .lore("")
                        .build()
                    );
                    //time=rs.getInt("time");
                    //msg=new TextComponent("§6Сообщение от §e"+rs.getString("sender")+" §e: §f"+rs.getString("message")+" §8<клик-следущее");
                    //msg.setHoverEvent(new HoverEvent( HoverEvent.Action.SHOW_TEXT, new Text("§5§oКлик - читать следущее \n§7Отправлено: §f"+time ) ));
                    //msg.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, "/fr mread" ) );
                }
                rs.close();
                stmt.close();

                Ostrov.sync(() -> {
                    if (op.menu.section == Section.ДРУЗЬЯ && op.menu.friendMode == ProfileManager.FriendMode.Письма) {
                        op.menu.stopLoadAnimations();
                        op.menu.current = SmartInventory
                            .builder()
                            .id(op.nik + op.menu.section.name())
                            .provider(new FriendMail(mails))
                            .size(3, 9)
                            .title(op.eng ? Section.ДРУЗЬЯ.item_nameEn + "§8: Messages" : Section.ДРУЗЬЯ.item_nameRu + "§8: Письма")
                            .build()
                            .open(op.getPlayer());
                    }
                }, 0);

            } catch (SQLException ex) {
                Ostrov.log_err("FM openFriendsMail : " + ex.getMessage());
            }
        }, 20); //задержка для анимации))
    }

    public static void onFriendsInfoRecieve(final Oplayer op, final String rawData) {
        if (op.menu.section == Section.ДРУЗЬЯ && op.menu.friendMode == ProfileManager.FriendMode.Просмотр) {
            op.menu.stopLoadAnimations();
            op.menu.current = SmartInventory
                .builder()
                .id(op.nik + op.menu.section.name())
                .provider(new FriendView(rawData))
                .size(3, 9)
                .title(op.eng ? Section.ДРУЗЬЯ.item_nameEn : Section.ДРУЗЬЯ.item_nameRu)
                .build()
                .open(op.getPlayer());
        }
    }

    public static void openFriendsSettings(final Oplayer op) {
        op.menu.section = Section.ДРУЗЬЯ;
        op.menu.friendMode = ProfileManager.FriendMode.Настройки;
        op.menu.current = SmartInventory
            .builder()
            .id(op.nik + op.menu.section.name())
            .provider(new FriendSettings())
            .size(3, 9)
            .title(op.eng ? Section.ДРУЗЬЯ.item_nameEn + "§8: Settings" : Section.ДРУЗЬЯ.item_nameRu + "§8: Настройки")
            .build()
            .open(op.getPlayer());
    }

    public static void openFriendsFind(final Oplayer op) {
        op.menu.section = Section.ДРУЗЬЯ;
        op.menu.friendMode = ProfileManager.FriendMode.Поиск;
        op.menu.current = SmartInventory
            .builder()
            .id(op.nik + op.menu.section.name())
            .provider(new FriendFind())
            .size(3, 9)
            .title(op.eng ? Section.ДРУЗЬЯ.item_nameEn + "§8: Invite" : Section.ДРУЗЬЯ.item_nameRu + "§8: Добавить")
            .build()
            .open(op.getPlayer());
    }

    //*****************************************************************


    public static void suggestFriend(final Player from, final Player to) {
        from.sendMessage(TCUtil.form(FRIENDS_PREFIX + "Ты <i>предлагаешь дружить</i> <olive><u>" + to.getName() + "</u> <white>!"));
        final Oplayer toOp = PM.getOplayer(to);
        final String name = from.getName();
        toOp.friendInvite.add(name);
        to.sendMessage(TCUtil.form(FRIENDS_PREFIX + "<olive><u>" + name + "</u> <white>предлагает дружить!"));
        to.sendMessage(TCUtil.form("<green><obf>K</obf> ")
            .append(Component.text().content("Принять").color(CustomTextColor.APPLE)
            .hoverEvent(TCUtil.form(TCUtil.sided("<dark_green>Клик")))
            .clickEvent(ClickEvent.callback(ClickCallback.widen(pl -> add(pl, toOp, name), Player.class))))
            .append(TCUtil.form(" <gold><obf>K</obf> "))
            .append(Component.text().content("Отклонить").color(CustomTextColor.APPLE)
                .hoverEvent(TCUtil.form(TCUtil.sided("<dark_red>Клик")))
                .clickEvent(ClickEvent.callback(ClickCallback.widen(pl -> {
                    pl.sendMessage(Ostrov.PREFIX + "§6" + name + " занесён в игнор!");
                    ApiOstrov.executeBungeeCmd(pl, "ignore " + name);
                    toOp.friendInvite.remove(name);
                }, Player.class))))
            .append(TCUtil.form(" <red><obf>K</obf>")));
    }


    //согласие на инвайт
    public static void add(final Player adder, final Oplayer adderOp, final String name) {
      adder.closeInventory();
        final Player to = Bukkit.getPlayerExact(name);
        if (to == null) {
          adder.sendMessage(Ostrov.PREFIX + "§c" + name + " уже не на сервере!");
            return;
        }
      if (!adderOp.friendInvite.remove(name)) return;

        final Oplayer toOp = PM.getOplayer(name);

      RemoteDB.executePstAsync(adder, "INSERT INTO `fr_friends` (`f1`, `f2`) values ('" + adderOp.nik + "', '" + toOp.nik + "') ");

      adderOp.friends.add(name);
      toOp.friends.add(adderOp.nik);
      SpigotChanellMsg.sendMessage(adder, Operation.FRIEND_ADD, adderOp.nik, toOp.nik);
      SpigotChanellMsg.sendMessage(to, Operation.FRIEND_ADD, toOp.nik, adderOp.nik);

      adder.sendMessage(TCUtil.form(FRIENDS_PREFIX + "§2Теперь ты друг §a" + toOp.nik + "§2! §8«Клик - ЛС")
            .hoverEvent(HoverEvent.showText(TCUtil.form("<green>Клик <apple>- написать ЛС!")))
            .clickEvent(ClickEvent.suggestCommand("/friend mail " + toOp.nik + " ")));

      to.sendMessage(TCUtil.form(FRIENDS_PREFIX + "§2Теперь ты друг §a" + adderOp.nik + "§2! §8«Клик - ЛС")
            .hoverEvent(HoverEvent.showText(TCUtil.form("<green>Клик <apple>- написать ЛС!")))
          .clickEvent(ClickEvent.suggestCommand("/friend mail " + adderOp.nik + " ")));

      adder.getWorld().playSound(adder.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1f, 1f);

      //Принятие Друга - делаем запись только для принимающего
      RemoteDB.executePstAsync(null, "INSERT INTO " + Table.HISTORY.table_name +
          " (`action`,`sender`,`target`,`data`) VALUES ('"
          + HistoryType.FRIEND_ADD.name() + "','" + name + "','" + adderOp.nik + "','" + Timer.secTime() + "');");
    }

  public static void delete(final Player deleter, final Oplayer deleterOp, final String name) {

    RemoteDB.executePstAsync(deleter, "DELETE FROM `fr_friends` WHERE (f1 = '"
        + deleterOp.nik + "' AND f2='" + name + "') OR (f1 = '" + name + "' AND f2='" + deleterOp.nik + "') ");

    deleterOp.friends.remove(name);
    deleter.sendMessage(FRIENDS_PREFIX + "§7Убран игрок §4" + name);
    SpigotChanellMsg.sendMessage(deleter, Operation.FRIEND_DELETE, deleterOp.nik, name);
    deleter.playSound(deleter, Sound.BLOCK_CONDUIT_ATTACK_TARGET, 1f, 2f);

        final Player to = Bukkit.getPlayerExact(name);
        if (to != null) {
            final Oplayer toOp = PM.getOplayer(name);
          toOp.friends.remove(deleterOp.nik);
          SpigotChanellMsg.sendMessage(to, Operation.FRIEND_DELETE, toOp.nik, deleterOp.nik);
          to.sendMessage(FRIENDS_PREFIX + "§7Дружба с §4" + deleterOp.nik + " §7разорвана!");
            to.playSound(to, Sound.BLOCK_CONDUIT_ATTACK_TARGET, 1f, 2f);
        }
    //Удаление Друга - делаем запись только для удаляющего
    RemoteDB.executePstAsync(null, "INSERT INTO " + Table.HISTORY.table_name +
        " (`action`,`sender`,`target`,`data`) VALUES ('"
        + HistoryType.FRIEND_DEL.name() + "','" + deleterOp.nik + "','" + name + "','" + Timer.secTime() + "');");
    }
}
